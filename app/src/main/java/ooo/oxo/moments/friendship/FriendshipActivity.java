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

package ooo.oxo.moments.friendship;

import android.content.Intent;
import android.databinding.ObservableArrayList;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import ooo.oxo.moments.InstaApplication;
import ooo.oxo.moments.R;
import ooo.oxo.moments.api.FriendshipApi;
import ooo.oxo.moments.databinding.FriendshipActivityBinding;
import ooo.oxo.moments.model.User;
import ooo.oxo.moments.rx.RxEndlessRecyclerView;
import ooo.oxo.moments.rx.RxList;
import ooo.oxo.moments.user.UserActivity;
import ooo.oxo.moments.widget.RxBindingAppCompatActivity;
import pocketknife.BindExtra;
import pocketknife.NotRequired;
import pocketknife.PocketKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class FriendshipActivity extends RxBindingAppCompatActivity<FriendshipActivityBinding>
        implements FriendshipAdapter.FriendshipListener {

    private static final String TAG = "FriendshipActivity";

    private final ObservableArrayList<User> friends = new ObservableArrayList<>();

    @BindExtra("id")
    long id;

    @BindExtra("what")
    String what;

    @NotRequired
    @BindExtra("user")
    User user;

    private FriendshipApi friendshipApi;

    private String nextMaxId;

    @Override
    protected int getContentView(Bundle savedInstanceState) {
        return R.layout.friendship_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        friendshipApi = InstaApplication.from(this).createApi(FriendshipApi.class);

        nextMaxId = null;

        PocketKnife.bindExtras(this);

        setSupportActionBar(binding.toolbar);

        if (user == null) {
            if ("followers".equals(what)) {
                setTitle(R.string.profile_followers);
            } else if ("following".equals(what)) {
                setTitle(R.string.profile_following);
            }
        } else {
            if ("followers".equals(what)) {
                setTitle(getString(R.string.friendship_his_followers, user.username));
            } else if ("following".equals(what)) {
                setTitle(getString(R.string.friendship_his_following, user.username));
            }
        }

        binding.toolbar.setNavigationOnClickListener(v -> supportFinishAfterTransition());

        binding.content.setAdapter(new FriendshipAdapter(this, friends, this));

        RxEndlessRecyclerView.reachesEnd(binding.content)
                .compose(bindToLifecycle())
                .filter(avoid -> nextMaxId != null)
                .flatMap(avoid -> load(nextMaxId))
                .subscribe(RxList.appendTo(friends), this::showError);

        load(null).compose(bindToLifecycle())
                .subscribe(RxList.prependToOrReplace(friends), this::showError);
    }

    private Observable<FriendshipApi.UsersEnvelope> createObservable(String maxId) {
        if ("followers".equals(what)) {
            return friendshipApi.followers(id, maxId);
        }

        if ("following".equals(what)) {
            return friendshipApi.following(id, maxId);
        }

        throw new IllegalArgumentException();
    }

    private Observable<List<User>> load(String maxId) {
        return createObservable(maxId)
                .observeOn(AndroidSchedulers.mainThread())
                .filter(envelope -> envelope.users != null)
                .doOnNext(envelope -> nextMaxId = envelope.nextMaxId)
                .map(envelope -> envelope.users);
    }

    private void showError(Throwable error) {
        Log.e(TAG, "error", error);
        Toast.makeText(this, R.string.error_network, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void openUser(FriendshipAdapter.ViewHolder holder) {
        User item = friends.get(holder.getAdapterPosition());

        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("user", item);
        intent.putExtra("from_post", "friendship");

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, holder.binding.avatar, "friendship_avatar");

        startActivity(intent, options.toBundle());
    }

}
