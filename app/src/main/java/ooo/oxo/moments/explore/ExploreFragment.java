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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.trello.rxlifecycle.components.support.RxFragment;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ooo.oxo.moments.InstaApplication;
import ooo.oxo.moments.MainActivity;
import ooo.oxo.moments.R;
import ooo.oxo.moments.ViewerActivity;
import ooo.oxo.moments.api.FeedApi;
import ooo.oxo.moments.model.Media;
import ooo.oxo.moments.rx.RxArrayRecyclerAdapter;
import ooo.oxo.moments.rx.RxEndlessRecyclerView;
import ooo.oxo.moments.user.UserGridAdapter;
import ooo.oxo.moments.util.ImageCandidatesUtil;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class ExploreFragment extends RxFragment implements
        UserGridAdapter.GridListener {

    private static final String TAG = "ExploreFragment";

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

        appbar.addOnOffsetChangedListener((v, i) -> statusBar.setAlpha(Math.min(
                1, (float) -i / (float) (appbar.getHeight() - statusBar.getHeight()))));

        toolbar.setNavigationOnClickListener(v -> ((MainActivity) getActivity()).openNavigation());

        refresher.setColorSchemeResources(R.color.primary);

        content.setLayoutManager(new GridLayoutManager(getContext(), 3));
        content.setAdapter(adapter);

        RxEndlessRecyclerView.reachesEnd(content)
                .compose(bindToLifecycle())
                .map(adapter::get)
                .flatMap(last -> load(last.id))
                .subscribe(RxArrayRecyclerAdapter.appendTo(adapter), this::showError);

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
        return feedApi.popular(maxId)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> refresher.setRefreshing(true))
                .doOnCompleted(() -> refresher.setRefreshing(false))
                .filter(envelope -> envelope.items != null)
                .map(envelope -> envelope.items);
    }

    private void showError(Throwable error) {
        Toast.makeText(getContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onImageClick(UserGridAdapter.ViewHolder holder) {
        Media item = adapter.get(holder.getAdapterPosition());
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
