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

import java.util.List;

import ooo.oxo.moments.InstaApplication;
import ooo.oxo.moments.R;
import ooo.oxo.moments.api.NewsApi;
import ooo.oxo.moments.databinding.InboxMineFragmentBinding;
import ooo.oxo.moments.model.Story;
import ooo.oxo.moments.rx.RxList;
import ooo.oxo.moments.widget.RxBindingFragment;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class MyInboxFragment extends RxBindingFragment<InboxMineFragmentBinding> {

    private final ObservableArrayList<Story> inbox = new ObservableArrayList<>();

    private NewsApi newsApi;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newsApi = InstaApplication.from(getContext()).createApi(NewsApi.class);
    }

    @Nullable
    @Override
    public InboxMineFragmentBinding onCreateBinding(LayoutInflater inflater,
                                                    @Nullable ViewGroup container,
                                                    @Nullable Bundle savedInstanceState) {
        return InboxMineFragmentBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        binding.content.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.content.setAdapter(new MyInboxAdapter(getContext(), inbox));

        RxSwipeRefreshLayout.refreshes(binding.refresher)
                .compose(bindToLifecycle())
                .flatMap(avoid -> load())
                .subscribe(RxList.prependToOrReplace(inbox), this::showError);

        load().compose(bindToLifecycle())
                .subscribe(RxList.appendTo(inbox), this::showError);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        inbox.clear();
    }

    private Observable<List<Story>> load() {
        return newsApi.inbox()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> binding.refresher.setRefreshing(true))
                .doOnCompleted(() -> binding.refresher.setRefreshing(false))
                .filter(envelope -> envelope.oldStories != null)
                .map(envelope -> envelope.oldStories);
    }

    private void showError(Throwable error) {
        Toast.makeText(getContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
    }

}
