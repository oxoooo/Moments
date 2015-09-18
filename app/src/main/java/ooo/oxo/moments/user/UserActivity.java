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
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;

import java.util.List;

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
import ooo.oxo.moments.model.Media;
import ooo.oxo.moments.model.User;
import ooo.oxo.moments.util.RxEndlessRecyclerView;
import ooo.oxo.moments.util.StatusBarTintDelegate;
import ooo.oxo.moments.util.StatusBarUtils;
import pocketknife.BindExtra;
import pocketknife.NotRequired;
import pocketknife.PocketKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class UserActivity extends AppCompatActivity implements
        UserGridAdapter.GridListener,
        RequestListener<String, GlideDrawable> {

    private static final String TAG = "UserActivity";

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

    private CompositeSubscription subscriptions = new CompositeSubscription();

    private MenuItem viewAsGrid;
    private MenuItem viewAsStream;

    private FeedApi feedApi;
    private UserApi userApi;

    private LinearLayoutManager streamLayoutManager;
    private FeedAdapter streamAdapter;

    private GridLayoutManager gridLayoutManager;
    private UserGridAdapter gridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.user_activity);

        ButterKnife.bind(this);
        PocketKnife.bindExtras(this);

        setTitle(null);
        setSupportActionBar(binding.toolbar);

        statusBarHeight = StatusBarUtils.getStatusBarHeight(this);

        binding.collapsingToolbar.setPadding(0, statusBarHeight, 0, 0);

        binding.appbar.addOnOffsetChangedListener(
                (AppBarLayout appbar, int i) -> binding.refresher.setEnabled(i == 0));

        binding.toolbar.setNavigationOnClickListener(v -> supportFinishAfterTransition());
        binding.toolbar.post(() -> binding.appbar.addOnOffsetChangedListener(new StatusBarTintDelegate(
                this, binding.toolbar.getHeight(), statusBarHeight)));

        binding.refresher.setProgressViewOffset(true, refresherStart, refresherStart + refresherLength);
        binding.refresher.setColorSchemeColors(colorPrimary);

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
        ViewCompat.setTransitionName(binding.avatar, fromPostId + "_avatar");

        streamLayoutManager = new LinearLayoutManager(this);
        streamAdapter = new FeedAdapter(this, this);

        gridLayoutManager = new GridLayoutManager(this, 3);
        gridAdapter = new UserGridAdapter(this, this);

        viewAsStream();

        setupEndlessLoading();
        setupRefresh();

        load();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscriptions.unsubscribe();
    }

    private String getId(int position) {
        if (binding.content.getLayoutManager() == streamLayoutManager) {
            return streamAdapter.get(position).id;
        } else if (binding.content.getLayoutManager() == gridLayoutManager) {
            return gridAdapter.get(position).id;
        } else {
            return null;
        }
    }

    private void addAll(List<Media> items) {
        if (binding.content.getLayoutManager() == streamLayoutManager) {
            streamAdapter.addAll(items);
        } else if (binding.content.getLayoutManager() == gridLayoutManager) {
            gridAdapter.addAll(items);
        }
    }

    private void replaceWith(List<Media> items) {
        streamAdapter.addAll(items);
        gridAdapter.addAll(items);
    }

    private void subscribeAppending(Observable<FeedApi.FeedEnvelope> observable) {
        observable = observable.cache();

        subscriptions.add(observable
                .subscribe(envelope -> binding.refresher.setRefreshing(false)));

        subscriptions.add(observable
                .filter(envelope -> envelope.items != null)
                .subscribe(envelope -> addAll(envelope.items)));
    }

    private void subscribeRefreshing(Observable<FeedApi.FeedEnvelope> observable) {
        observable = observable.cache();

        subscriptions.add(observable
                .subscribe(envelope -> binding.refresher.setRefreshing(false)));

        subscriptions.add(observable
                .filter(envelope -> envelope.items != null)
                .subscribe(envelope -> replaceWith(envelope.items)));
    }

    private void setupEndlessLoading() {
        subscribeAppending(RxEndlessRecyclerView.reachesEnd(binding.content)
                .flatMap(position -> {
                    binding.refresher.setRefreshing(true);
                    return loadFeed(getId(position));
                }));
    }

    private void setupRefresh() {
        // FIXME: 找不到优雅的方式同时刷新用户资料
        subscribeRefreshing(RxSwipeRefreshLayout.refreshes(binding.refresher)
                .flatMap(avoid -> loadFeed(null)));
    }

    private void load() {
        binding.refresher.post(() -> binding.refresher.setRefreshing(true));
        loadUser().subscribe(envelope -> populateProfile(envelope.user));
        subscribeRefreshing(loadFeed(null));
    }

    private Observable<FeedApi.FeedEnvelope> loadFeed(String maxId) {
        return feedApi.ofUser(id, maxId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<UserApi.UserEnvelope> loadUser() {
        return userApi.infoOf(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void populateProfile(User profile) {
        binding.setUser(profile);
        Glide.with(this)
                .load(profile.profilePicUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .bitmapTransform(new CropCircleTransformation(this))
                .listener(this)
                .into(binding.avatar);
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
        binding.content.setPadding(marginStream, marginStream, marginStream, marginStream);
        binding.content.setLayoutManager(streamLayoutManager);
        binding.content.setAdapter(streamAdapter);
    }

    private void viewAsGrid() {
        binding.content.setPadding(marginGrid, marginGrid, marginGrid, marginGrid);
        binding.content.setLayoutManager(gridLayoutManager);
        binding.content.setAdapter(gridAdapter);
    }

}
