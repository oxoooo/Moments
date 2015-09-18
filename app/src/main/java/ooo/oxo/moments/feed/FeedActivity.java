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

package ooo.oxo.moments.feed;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import ooo.oxo.moments.InstaApplication;
import ooo.oxo.moments.ProxyActivity;
import ooo.oxo.moments.R;
import ooo.oxo.moments.api.FeedApi;
import ooo.oxo.moments.databinding.FeedActivityBinding;
import ooo.oxo.moments.model.Media;
import ooo.oxo.moments.model.User;
import ooo.oxo.moments.user.UserActivity;
import ooo.oxo.moments.util.ImageViewBindingUtil;
import ooo.oxo.moments.util.RxEndlessRecyclerView;
import ooo.oxo.moments.util.StatusBarTintDelegate;
import ooo.oxo.moments.util.StatusBarUtils;
import pocketknife.BindExtra;
import pocketknife.PocketKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class FeedActivity extends AppCompatActivity implements
        FeedAdapter.FeedListener {

    private static final String TAG = "FeedActivity";

    FeedActivityBinding binding;

    @Bind(R.id.avatar)
    ImageView avatar;

    @Bind(R.id.user_name)
    TextView userName;

    @Bind(R.id.full_name)
    TextView fullName;

    @BindColor(R.color.primary)
    int colorPrimary;

    @BindExtra("user")
    User user;

    private FeedApi feedApi;

    private CompositeSubscription subscriptions = new CompositeSubscription();

    private FeedAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.feed_activity);

        ButterKnife.bind(this);
        PocketKnife.bindExtras(this);

        setSupportActionBar(binding.toolbar);

        // TODO: remove these 2
        binding.appbar.addOnOffsetChangedListener(new StatusBarTintDelegate(this));
        ((ViewGroup.MarginLayoutParams) binding.toolbar.getLayoutParams()).topMargin
                = StatusBarUtils.getStatusBarHeight(this);

        binding.toolbar.setNavigationOnClickListener(v -> binding.drawer.openDrawer(GravityCompat.START));

        binding.refresher.setColorSchemeColors(colorPrimary);

        ImageViewBindingUtil.loadRoundImage(avatar, user.profilePicUrl);
        userName.setText(user.username);
        fullName.setText(user.fullName);

        adapter = new FeedAdapter(this, this);

        binding.content.setLayoutManager(new LinearLayoutManager(this));
        binding.content.setAdapter(adapter);

        feedApi = InstaApplication.from(this).createApi(FeedApi.class);

        setupEndlessLoading();
        setupRefresh();

        load();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscriptions.unsubscribe();
    }

    private void subscribeAppending(Observable<FeedApi.FeedEnvelope> observable) {
        observable = observable.cache();

        subscriptions.add(observable
                .subscribe(envelope -> binding.refresher.setRefreshing(false)));

        subscriptions.add(observable
                .filter(envelope -> envelope.items != null)
                .subscribe(envelope -> adapter.addAll(envelope.items)));
    }

    private void subscribeRefreshing(Observable<FeedApi.FeedEnvelope> observable) {
        observable = observable.cache();

        subscriptions.add(observable
                .subscribe(envelope -> binding.refresher.setRefreshing(false)));

        subscriptions.add(observable
                .filter(envelope -> envelope.items != null)
                .subscribe(envelope -> adapter.replaceWith(envelope.items)));
    }

    private void setupEndlessLoading() {
        subscribeAppending(RxEndlessRecyclerView.reachesEnd(binding.content)
                .flatMap(position -> {
                    binding.refresher.setRefreshing(true);
                    return load(adapter.get(position).id);
                }));
    }

    private void setupRefresh() {
        subscribeRefreshing(RxSwipeRefreshLayout.refreshes(binding.refresher)
                .flatMap(avoid -> load(null)));
    }

    private void load() {
        binding.refresher.post(() -> binding.refresher.setRefreshing(true));
        subscribeRefreshing(load(null));
    }

    private Observable<FeedApi.FeedEnvelope> load(String maxId) {
        return feedApi.timeline(maxId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .cache();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.proxy:
                startActivity(new Intent(this, ProxyActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onUserClick(FeedAdapter.ViewHolder holder) {
        Media item = adapter.get(holder.getAdapterPosition());

        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("user", item.user);
        intent.putExtra("from_post", item.id);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, holder.binding.avatar, item.id + "_avatar");

        startActivity(intent, options.toBundle());
    }

    @Override
    public void onUserClick(long id) {
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("id", id);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onImageClick(FeedAdapter.ViewHolder holder) {
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

}
