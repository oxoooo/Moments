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
import android.databinding.ObservableArrayList;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.trello.rxlifecycle.components.support.RxFragment;

import java.util.List;

import ooo.oxo.moments.InstaApplication;
import ooo.oxo.moments.R;
import ooo.oxo.moments.ViewerActivity;
import ooo.oxo.moments.api.FeedApi;
import ooo.oxo.moments.databinding.UserGridFragmentBinding;
import ooo.oxo.moments.model.Media;
import ooo.oxo.moments.rx.RxEndlessRecyclerView;
import ooo.oxo.moments.rx.RxList;
import ooo.oxo.moments.util.ImageCandidatesUtil;
import pocketknife.BindArgument;
import pocketknife.PocketKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class UserGridFragment extends RxFragment implements
        RefreshEnabler,
        UserGridAdapter.GridListener {

    private static final String TAG = "UserGridFragment";

    @BindArgument("id")
    long id;

    private UserGridFragmentBinding binding;

    private FeedApi feedApi;

    private ObservableArrayList<Media> feed;

    public static UserGridFragment newFragment(long id) {
        Bundle arguments = new Bundle();
        arguments.putLong("id", id);

        UserGridFragment fragment = new UserGridFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        feedApi = InstaApplication.from(getContext()).createApi(FeedApi.class);
        feed = ((UserActivity) getActivity()).feed;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = UserGridFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        PocketKnife.bindArguments(this);

        binding.refresher.setColorSchemeResources(R.color.primary);

        binding.content.setLayoutManager(new GridLayoutManager(getContext(), 3));
        binding.content.setAdapter(new UserGridAdapter(getContext(), feed, this));

        binding.setFeed(feed);

        RxEndlessRecyclerView.reachesEnd(binding.content)
                .compose(bindToLifecycle())
                .map(feed::get)
                .flatMap(last -> load(last.id))
                .subscribe(RxList.appendTo(feed), this::showError);

        RxSwipeRefreshLayout.refreshes(binding.refresher)
                .compose(bindToLifecycle())
                .flatMap(avoid -> load(null))
                .subscribe(RxList.replace(feed), this::showError);

        load(null)
                .compose(bindToLifecycle())
                .subscribe(RxList.replace(feed), this::showError);

        binding.refresher.post(() -> binding.refresher.setRefreshing(true));
    }

    private Observable<List<Media>> load(String maxId) {
        return feedApi.ofUser(id, maxId)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> binding.refresher.setRefreshing(true))
                .doOnCompleted(() -> binding.refresher.setRefreshing(false))
                .filter(envelope -> envelope.items != null)
                .map(envelope -> envelope.items);
    }

    private void showError(Throwable error) {
        ((UserActivity) getActivity()).showError(error);
    }

    @Override
    public void setCanRefresh(boolean canRefresh) {
        binding.refresher.setEnabled(canRefresh);
    }

    @Override
    public void onImageClick(UserGridAdapter.ViewHolder holder) {
        Media item = feed.get(holder.getAdapterPosition());
        Media.Resource best = ImageCandidatesUtil.pickBest(item.imageVersions.candidates);

        if (best == null) {
            return;
        }

        Intent intent = new Intent(getContext(), ViewerActivity.class);
        intent.setData(Uri.parse(best.url));

        if (item.imageVersions.picked != null) {
            intent.putExtra("thumbnail", item.imageVersions.picked.url);
        }

        if (item.caption != null) {
            intent.putExtra("caption", item.caption.text);
        }

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(), holder.binding.image, best.url);

        getActivity().startActivity(intent, options.toBundle());
    }

}
