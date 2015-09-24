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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.trello.rxlifecycle.components.support.RxFragment;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ooo.oxo.moments.InstaApplication;
import ooo.oxo.moments.R;
import ooo.oxo.moments.api.FeedApi;
import ooo.oxo.moments.model.Media;
import ooo.oxo.moments.rx.RxArrayRecyclerAdapter;
import pocketknife.BindArgument;
import pocketknife.PocketKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class UserTaggedFragment extends RxFragment implements
        RefreshEnabler,
        UserGridAdapter.GridListener {

    private static final String TAG = "UserGridFragment";

    @Bind(R.id.refresher)
    SwipeRefreshLayout refresher;

    @Bind(R.id.content)
    RecyclerView content;

    @BindArgument("id")
    long id;

    private FeedApi feedApi;

    private UserGridAdapter adapter;

    public static UserTaggedFragment newFragment(long id) {
        Bundle arguments = new Bundle();
        arguments.putLong("id", id);

        UserTaggedFragment fragment = new UserTaggedFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        feedApi = InstaApplication.from(getContext()).createApi(FeedApi.class);
        adapter = new UserGridAdapter(getContext(), this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_grid_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        PocketKnife.bindArguments(this);

        refresher.setColorSchemeResources(R.color.primary);

        content.setLayoutManager(new GridLayoutManager(getContext(), 3));
        content.setAdapter(adapter);

        RxSwipeRefreshLayout.refreshes(refresher)
                .compose(bindToLifecycle())
                .flatMap(avoid -> load(null))
                .subscribe(RxArrayRecyclerAdapter.replace(adapter), this::showError);

        load(null)
                .compose(bindToLifecycle())
                .subscribe(RxArrayRecyclerAdapter.replace(adapter), this::showError);

        refresher.post(() -> refresher.setRefreshing(true));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter.clear();
    }

    private Observable<List<Media>> load(String maxId) {
        return feedApi.tagged(id, maxId)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> refresher.setRefreshing(true))
                .doOnCompleted(() -> refresher.setRefreshing(false))
                .filter(envelope -> envelope.items != null)
                .map(envelope -> envelope.items);
    }

    private void showError(Throwable error) {
        ((UserActivity) getActivity()).showError(error);
    }

    @Override
    public void setCanRefresh(boolean canRefresh) {
        refresher.setEnabled(canRefresh);
    }

    @Override
    public void onImageClick(UserGridAdapter.ViewHolder holder) {
    }

}