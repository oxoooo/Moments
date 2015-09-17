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
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import ooo.oxo.moments.InstaApplication;
import ooo.oxo.moments.ProxyActivity;
import ooo.oxo.moments.R;
import ooo.oxo.moments.api.FeedApi;
import ooo.oxo.moments.model.Media;
import ooo.oxo.moments.user.UserActivity;
import ooo.oxo.moments.util.StatusBarTintDelegate;
import retrofit.Callback;
import retrofit.Response;

public class FeedActivity extends AppCompatActivity implements
        Callback<FeedApi.FeedEnvelope>,
        SwipeRefreshLayout.OnRefreshListener,
        FeedAdapter.FeedListener {

    private static final String TAG = "FeedActivity";

    @Bind(R.id.appbar)
    AppBarLayout appbar;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.refresher)
    SwipeRefreshLayout refresher;

    @Bind(R.id.content)
    RecyclerView content;

    @BindColor(R.color.primary)
    int colorPrimary;

    private FeedAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.feed_activity);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        appbar.addOnOffsetChangedListener(new StatusBarTintDelegate(this, colorPrimary));

        refresher.setColorSchemeColors(colorPrimary);
        refresher.setOnRefreshListener(this);

        adapter = new FeedAdapter(this, this);

        content.setLayoutManager(new LinearLayoutManager(this));
        content.setAdapter(adapter);

        load();
    }

    @Override
    public void onRefresh() {
        load();
    }

    private void load() {
        refresher.post(() -> refresher.setRefreshing(true));
        InstaApplication.from(this).createApi(FeedApi.class).timeline(null).enqueue(this);
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
    public void onResponse(Response<FeedApi.FeedEnvelope> response) {
        refresher.setRefreshing(false);

        if (response.errorBody() != null) {
            try {
                Log.e(TAG, response.errorBody().string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FeedApi.FeedEnvelope envelope = response.body();
        if (envelope == null) {
            return;
        }

        adapter.setFeed(envelope.items);
    }

    @Override
    public void onFailure(Throwable t) {
        Log.e(TAG, "network failure", t);
    }

    @Override
    public void onUserClick(FeedAdapter.ViewHolder holder) {
        Media item = adapter.getFeed().get(holder.getAdapterPosition());

        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("user", item.user);
        intent.putExtra("from_post", item.id);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, holder.avatar, item.id + "_avatar");

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
