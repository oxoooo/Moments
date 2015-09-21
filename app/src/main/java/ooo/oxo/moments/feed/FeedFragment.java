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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import ooo.oxo.moments.InstaApplication;
import ooo.oxo.moments.MainActivity;
import ooo.oxo.moments.R;
import ooo.oxo.moments.api.FeedApi;
import ooo.oxo.moments.model.Media;
import ooo.oxo.moments.user.UserActivity;
import ooo.oxo.moments.util.RxEndlessRecyclerView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class FeedFragment extends Fragment implements
        FeedAdapter.FeedListener {

    private static final String TAG = "FeedFragment";

    @Bind(R.id.appbar)
    AppBarLayout appbar;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.status_bar)
    View statusBar;

    @Bind(R.id.refresher)
    SwipeRefreshLayout refresher;

    @Bind(R.id.content)
    RecyclerView content;

    private FeedApi feedApi;

    private CompositeSubscription subscriptions = new CompositeSubscription();

    private FeedAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        feedApi = InstaApplication.from(getContext()).createApi(FeedApi.class);
        adapter = new FeedAdapter(getContext(), this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.feed_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        appbar.addOnOffsetChangedListener((v, i) -> statusBar.setAlpha(Math.min(
                1, (float) -i / (float) (appbar.getHeight() - statusBar.getHeight()))));

        toolbar.setNavigationOnClickListener(v -> ((MainActivity) getActivity()).openNavigation());

        refresher.setColorSchemeResources(R.color.primary);

        content.setLayoutManager(new LinearLayoutManager(getContext()));
        content.setAdapter(adapter);

        setupEndlessLoading();
        setupRefresh();

        load();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        subscriptions.unsubscribe();
        adapter.clear();
    }

    private void subscribeAppending(Observable<FeedApi.FeedEnvelope> observable) {
        observable = observable.cache();

        subscriptions.add(observable
                .subscribe(envelope -> refresher.setRefreshing(false)));

        subscriptions.add(observable
                .filter(envelope -> envelope.items != null)
                .subscribe(envelope -> adapter.addAll(envelope.items)));
    }

    private void subscribeRefreshing(Observable<FeedApi.FeedEnvelope> observable) {
        observable = observable.cache();

        subscriptions.add(observable
                .subscribe(envelope -> refresher.setRefreshing(false)));

        subscriptions.add(observable
                .filter(envelope -> envelope.items != null)
                .subscribe(envelope -> adapter.replaceWith(envelope.items)));
    }

    private void setupEndlessLoading() {
        subscribeAppending(RxEndlessRecyclerView.reachesEnd(content)
                .flatMap(position -> {
                    refresher.setRefreshing(true);
                    return load(adapter.get(position).id);
                }));
    }

    private void setupRefresh() {
        subscribeRefreshing(RxSwipeRefreshLayout.refreshes(refresher)
                .flatMap(avoid -> load(null)));
    }

    private void load() {
        refresher.post(() -> refresher.setRefreshing(true));
        subscribeRefreshing(load(null));
    }

    private Observable<FeedApi.FeedEnvelope> load(String maxId) {
        return feedApi.timeline(maxId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    @Override
    public void onUserClick(FeedAdapter.ViewHolder holder) {
        Media item = adapter.get(holder.getAdapterPosition());

        Intent intent = new Intent(getContext(), UserActivity.class);
        intent.putExtra("user", item.user);
        intent.putExtra("from_post", item.id);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(), holder.binding.avatar, item.id + "_avatar");

        getActivity().startActivity(intent, options.toBundle());
    }

    @Override
    public void onUserClick(long id) {
        Intent intent = new Intent(getContext(), UserActivity.class);
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
