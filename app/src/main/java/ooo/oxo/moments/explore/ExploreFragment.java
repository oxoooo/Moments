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

package ooo.oxo.moments.explore;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import ooo.oxo.moments.InstaApplication;
import ooo.oxo.moments.R;
import ooo.oxo.moments.api.FeedApi;
import ooo.oxo.moments.feed.FeedAdapter;
import ooo.oxo.moments.user.UserGridAdapter;
import ooo.oxo.moments.util.RxEndlessRecyclerView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ExploreFragment extends Fragment implements
        UserGridAdapter.GridListener {

    private static final String TAG = "ExploreFragment";

    @Bind(R.id.refresher)
    SwipeRefreshLayout refresher;

    @Bind(R.id.content)
    RecyclerView content;

    private FeedApi feedApi;

    private CompositeSubscription subscriptions = new CompositeSubscription();

    private UserGridAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        feedApi = InstaApplication.from(getContext()).createApi(FeedApi.class);
        adapter = new UserGridAdapter(getContext(), this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.explore_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        refresher.setColorSchemeResources(R.color.primary);

        content.setLayoutManager(new GridLayoutManager(getContext(), 3));
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
        return feedApi.popular(maxId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    @Override
    public void onUserClick(FeedAdapter.ViewHolder holder) {
    }

    @Override
    public void onUserClick(long id) {
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

}
