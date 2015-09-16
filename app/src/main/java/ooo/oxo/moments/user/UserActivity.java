/*
 * Moments - To the best Instagram client
 * Copyright (C) 2015  XiNGRZ <xxx@oxo.ooo>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program;  if not, see <http://www.gnu.org/licenses/>.
 */

package ooo.oxo.moments.user;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.transition.Transition;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import ooo.oxo.moments.InstaApplication;
import ooo.oxo.moments.InstaSharedState;
import ooo.oxo.moments.R;
import ooo.oxo.moments.feed.FeedAdapter;
import ooo.oxo.moments.model.Media;
import ooo.oxo.moments.model.User;
import ooo.oxo.moments.net.Envelope;
import ooo.oxo.moments.net.UserApi;
import ooo.oxo.moments.util.StatusBarTintDelegate;
import ooo.oxo.moments.util.StatusBarUtils;
import ooo.oxo.moments.util.ViewGroupUtils;
import pocketknife.BindExtra;
import pocketknife.NotRequired;
import pocketknife.PocketKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;

public class UserActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener,
        UserGridAdapter.GridListener,
        RequestListener<String, GlideDrawable> {

    private static final String TAG = "UserActivity";

    @Bind(R.id.appbar)
    AppBarLayout appbar;

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.refresher)
    SwipeRefreshLayout refresher;

    @Bind(R.id.content)
    RecyclerView content;

    @Bind(R.id.avatar)
    ImageView avatar;

    @Bind(R.id.full_name)
    TextView fullName;

    @Bind(R.id.user_name)
    TextView userName;

    @Bind(R.id.bio)
    TextView bio;

    @Bind(R.id.counts)
    View counts;

    @Bind(R.id.posts)
    TextView posts;

    @Bind(R.id.followers)
    TextView followers;

    @Bind(R.id.following)
    TextView following;

    @BindColor(R.color.primary)
    int colorPrimary;

    @BindDimen(R.dimen.srl_start)
    int refresherStart;

    @BindDimen(R.dimen.srl_length)
    int refresherLength;

    @BindDimen(R.dimen.item_margin_grid)
    int marginGrid;

    @BindDimen(R.dimen.item_margin_stream)
    int marginStream;

    int statusBarHeight = 0;

    @BindExtra("id")
    @NotRequired
    String id;

    @BindExtra("user")
    @NotRequired
    User user;

    @BindExtra("from_post")
    @NotRequired
    String fromPostId;

    private MenuItem viewAsGrid;
    private MenuItem viewAsStream;

    private String accessToken;

    private UserApi userApi;

    private FeedAdapter streamAdapter;
    private UserGridAdapter gridAdapter;

    private Animator avatarReveal;

    private boolean isEntered = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.user_activity);

        ButterKnife.bind(this);
        PocketKnife.bindExtras(this);

        setTitle(null);
        setSupportActionBar(toolbar);

        statusBarHeight = StatusBarUtils.getStatusBarHeight(this);

        collapsingToolbar.setPadding(0, statusBarHeight, 0, 0);

        appbar.addOnOffsetChangedListener((AppBarLayout appbar, int i) -> refresher.setEnabled(i == 0));

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(v -> supportFinishAfterTransition());
        toolbar.post(() -> appbar.addOnOffsetChangedListener(new StatusBarTintDelegate(
                this, toolbar.getHeight(), statusBarHeight)));

        refresher.setProgressViewOffset(true, refresherStart, refresherStart + refresherLength);
        refresher.setColorSchemeColors(colorPrimary);
        refresher.setOnRefreshListener(this);

        accessToken = InstaSharedState.getInstance().getAccessToken();

        userApi = InstaApplication.from(this).createApi(UserApi.class);

        if (user != null) {
            populateProfile(user);
        }

        if (id == null && user != null) {
            id = user.id;
        }

        if (id == null) {
            throw new IllegalStateException("Must specify which user to load");
        }

        if (Build.VERSION.SDK_INT >= 21 && fromPostId != null) {
            isEntered = false;
            supportPostponeEnterTransition();
            ViewCompat.setTransitionName(avatar, id + "_" + fromPostId + "_avatar");

            getWindow().getSharedElementEnterTransition().addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    //revealAvatarView();
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                }

                @Override
                public void onTransitionPause(Transition transition) {
                }

                @Override
                public void onTransitionResume(Transition transition) {
                }
            });
        }

        streamAdapter = new FeedAdapter(this, this);
        gridAdapter = new UserGridAdapter(this, this);

        viewAsStream();

        load();
    }

    @Override
    public void onRefresh() {
        load();
    }

    private void load() {
        refresher.post(() -> refresher.setRefreshing(true));

        Observable
                .combineLatest(
                        userApi.profile(id, accessToken),
                        userApi.timeline(id, 200, accessToken),
                        (Func2<Envelope<User>, Envelope<List<Media>>, Pair<Envelope<User>, Envelope<List<Media>>>>) Pair::new
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> populate(result.first, result.second),
                        throwable -> Log.d(TAG, "failed to load user", throwable)
                );
    }

    @TargetApi(21)
    private void setupAvatarReveal() {  // FIXME: it flashes, unexpectedly
        int[] bounds = ViewGroupUtils.calculateBounds(avatar, appbar);
        avatarReveal = ViewAnimationUtils.createCircularReveal(appbar,
                (int) (bounds[0] + (float) avatar.getWidth() / 2f),
                (int) (bounds[1] + (float) avatar.getHeight() / 2f),
                (float) avatar.getWidth() / 2f,
                (float) bounds[2]);
    }

    @TargetApi(21)
    private void revealAvatarView() {
        if (avatarReveal != null) {
            avatarReveal.start();
            avatarReveal = null;
        }
    }

    private void populate(Envelope<User> profile, Envelope<List<Media>> timeline) {
        refresher.setRefreshing(false);

        if (profile == null || timeline == null) {
            return;
        }

        streamAdapter.setFeed(timeline.data);
        gridAdapter.setFeed(timeline.data);

        populateProfile(profile.data);
    }

    private void populateProfile(User profile) {
        Glide.with(this)
                .load(profile.profilePicture)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .bitmapTransform(new CropCircleTransformation(this))
                .listener(this)
                .into(avatar);

        userName.setText(profile.username);

        if (TextUtils.isEmpty(profile.fullName)) {
            fullName.setVisibility(View.GONE);
        } else {
            fullName.setText(profile.fullName);
            fullName.setVisibility(View.VISIBLE);
        }

        if (TextUtils.isEmpty(profile.bio)) {
            bio.setVisibility(View.GONE);
        } else {
            bio.setText(profile.bio);
            bio.setVisibility(View.VISIBLE);
        }

        if (profile.counts != null) {
            posts.setText(String.valueOf(profile.counts.media));
            followers.setText(String.valueOf(profile.counts.followedBy));
            following.setText(String.valueOf(profile.counts.follows));
            counts.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onException(Exception e, String model, Target<GlideDrawable> target,
                               boolean isFirstResource) {
        supportStartPostponedEnterTransition();
        isEntered = true;
        return false;
    }

    @Override
    public boolean onResourceReady(GlideDrawable resource, String model,
                                   Target<GlideDrawable> target, boolean isFromMemoryCache,
                                   boolean isFirstResource) {
        if (!isEntered) {
            //setupAvatarReveal();
            supportStartPostponedEnterTransition();
            isEntered = true;
        }

        return false;
    }

    @Override
    public void onUserClick(FeedAdapter.ViewHolder holder) {
    }

    @Override
    public void onUserClick(String id) {
        if (this.id.equals(id)) {
            return;
        }

        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("id", id);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onImageClick(FeedAdapter.ViewHolder holder) {
    }

    @Override
    public void onImageClick(UserGridAdapter.ViewHolder holder) {
    }

    @Override
    public void onLikesClick(FeedAdapter.ViewHolder holder) {
    }

    @Override
    public void onLike(FeedAdapter.ViewHolder holder) {
    }

    @Override
    public void onComment(FeedAdapter.ViewHolder holder) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        viewAsGrid = menu.findItem(R.id.view_as_grid);
        viewAsStream = menu.findItem(R.id.view_as_stream);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view_as_grid:
                viewAsGrid.setVisible(false);
                viewAsStream.setVisible(true);
                viewAsGrid();
                return true;
            case R.id.view_as_stream:
                viewAsStream.setVisible(false);
                viewAsGrid.setVisible(true);
                viewAsStream();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void viewAsStream() {
        content.setPadding(marginStream, marginStream, marginStream, marginStream);
        content.setLayoutManager(new LinearLayoutManager(this));
        content.setAdapter(streamAdapter);
    }

    private void viewAsGrid() {
        content.setPadding(marginGrid, marginGrid, marginGrid, marginGrid);
        content.setLayoutManager(new GridLayoutManager(this, 3));
        content.setAdapter(gridAdapter);
    }

}
