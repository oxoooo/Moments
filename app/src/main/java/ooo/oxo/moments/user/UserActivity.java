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

import android.content.Intent;
import android.databinding.DataBindingUtil;
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
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import ooo.oxo.moments.InstaApplication;
import ooo.oxo.moments.R;
import ooo.oxo.moments.api.FeedApi;
import ooo.oxo.moments.api.UserApi;
import ooo.oxo.moments.databinding.UserActivityBinding;
import ooo.oxo.moments.feed.FeedAdapter;
import ooo.oxo.moments.model.User;
import ooo.oxo.moments.util.StatusBarTintDelegate;
import ooo.oxo.moments.util.StatusBarUtils;
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
    long id;

    @BindExtra("user")
    @NotRequired
    User user;

    @BindExtra("from_post")
    @NotRequired
    String fromPostId;

    private UserActivityBinding binding;

    private MenuItem viewAsGrid;
    private MenuItem viewAsStream;

    private FeedApi feedApi;
    private UserApi userApi;

    private FeedAdapter streamAdapter;
    private UserGridAdapter gridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.user_activity);

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

        InstaApplication application = InstaApplication.from(this);
        feedApi = application.createApi(FeedApi.class);
        userApi = application.createApi(UserApi.class);

        if (user != null) {
            populateProfile(user);
        }

        if (id == 0 && user != null) {
            id = user.pk;
        }

        if (id == 0) {
            throw new IllegalStateException("Must specify which user to load");
        }

        supportPostponeEnterTransition();
        ViewCompat.setTransitionName(avatar, fromPostId + "_avatar");

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
                        userApi.infoOf(id),
                        feedApi.ofUser(id, null),
                        (Func2<UserApi.UserEnvelope, FeedApi.FeedEnvelope, Pair<UserApi.UserEnvelope, FeedApi.FeedEnvelope>>) Pair::new
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> populate(result.first, result.second),
                        throwable -> Log.d(TAG, "failed to load user", throwable)
                );
    }

    private void populate(UserApi.UserEnvelope profile, FeedApi.FeedEnvelope timeline) {
        refresher.setRefreshing(false);

        if (profile == null || timeline == null) {
            return;
        }

        streamAdapter.setFeed(timeline.items);
        gridAdapter.setFeed(timeline.items);

        populateProfile(profile.user);
    }

    private void populateProfile(User profile) {
        Glide.with(this)
                .load(profile.profilePicUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .bitmapTransform(new CropCircleTransformation(this))
                .listener(this)
                .into(avatar);

        binding.setUser(profile);
    }

    @Override
    public boolean onException(Exception e, String model, Target<GlideDrawable> target,
                               boolean isFirstResource) {
        supportStartPostponedEnterTransition();
        return false;
    }

    @Override
    public boolean onResourceReady(GlideDrawable resource, String model,
                                   Target<GlideDrawable> target, boolean isFromMemoryCache,
                                   boolean isFirstResource) {
        supportStartPostponedEnterTransition();
        return false;
    }

    @Override
    public void onUserClick(FeedAdapter.ViewHolder holder) {
    }

    @Override
    public void onUserClick(long id) {
        if (this.id == id) {
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
