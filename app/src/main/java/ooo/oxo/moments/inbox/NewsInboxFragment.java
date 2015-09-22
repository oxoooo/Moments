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

package ooo.oxo.moments.inbox;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ooo.oxo.moments.InstaApplication;
import ooo.oxo.moments.R;
import ooo.oxo.moments.api.NewsApi;
import ooo.oxo.moments.model.Story;
import ooo.oxo.moments.rx.RxArrayRecyclerAdapter;
import ooo.oxo.moments.rx.RxFragment;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class NewsInboxFragment extends RxFragment {

    @Bind(R.id.refresher)
    SwipeRefreshLayout refresher;

    @Bind(R.id.content)
    RecyclerView content;

    private NewsApi newsApi;

    private NewsInboxAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newsApi = InstaApplication.from(getContext()).createApi(NewsApi.class);
        adapter = new NewsInboxAdapter(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.inbox_news_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        content.setLayoutManager(new LinearLayoutManager(getContext()));
        content.setAdapter(adapter);

        subscribe(RxSwipeRefreshLayout.refreshes(refresher)
                .flatMap(avoid -> load())
                .subscribe(RxArrayRecyclerAdapter.replace(adapter)));

        subscribe(load().subscribe(RxArrayRecyclerAdapter.replace(adapter)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter.clear();
    }

    private Observable<List<Story>> load() {
        return newsApi.news()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> refresher.setRefreshing(true))
                .doOnCompleted(() -> refresher.setRefreshing(false))
                .doOnError(this::showError)
                .filter(envelope -> envelope.stories != null)
                .map(envelope -> envelope.stories);
    }

    private void showError(Throwable error) {
        Toast.makeText(getContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
    }

}
