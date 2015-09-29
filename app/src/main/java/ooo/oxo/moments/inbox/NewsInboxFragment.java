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

import android.databinding.ObservableArrayList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.trello.rxlifecycle.components.support.RxFragment;

import java.util.List;

import butterknife.ButterKnife;
import ooo.oxo.moments.InstaApplication;
import ooo.oxo.moments.R;
import ooo.oxo.moments.api.NewsApi;
import ooo.oxo.moments.databinding.InboxNewsFragmentBinding;
import ooo.oxo.moments.model.Story;
import ooo.oxo.moments.rx.RxList;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class NewsInboxFragment extends RxFragment {

    private final ObservableArrayList<Story> inbox = new ObservableArrayList<>();

    private InboxNewsFragmentBinding binding;

    private NewsApi newsApi;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newsApi = InstaApplication.from(getContext()).createApi(NewsApi.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = InboxNewsFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        binding.content.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.content.setAdapter(new NewsInboxAdapter(getContext(), inbox));

        binding.setInbox(inbox);

        RxSwipeRefreshLayout.refreshes(binding.refresher)
                .compose(bindToLifecycle())
                .flatMap(avoid -> load())
                .subscribe(RxList.replace(inbox), this::showError);

        load().compose(bindToLifecycle())
                .subscribe(RxList.replace(inbox), this::showError);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        inbox.clear();
    }

    private Observable<List<Story>> load() {
        return newsApi.news()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> binding.refresher.setRefreshing(true))
                .doOnCompleted(() -> binding.refresher.setRefreshing(false))
                .filter(envelope -> envelope.stories != null)
                .map(envelope -> envelope.stories);
    }

    private void showError(Throwable error) {
        Toast.makeText(getContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
    }

}
