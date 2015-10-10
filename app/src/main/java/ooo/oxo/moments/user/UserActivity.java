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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import butterknife.OnClick;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import ooo.oxo.moments.InstaApplication;
import ooo.oxo.moments.R;
import ooo.oxo.moments.api.FeedApi;
import ooo.oxo.moments.api.UserApi;
import ooo.oxo.moments.databinding.UserActivityBinding;
import ooo.oxo.moments.friendship.FriendshipActivity;
import ooo.oxo.moments.model.Media;
import ooo.oxo.moments.model.User;
import ooo.oxo.moments.rx.RxList;
import ooo.oxo.moments.util.PostponedTransitionTrigger;
import ooo.oxo.moments.widget.IconifiedPagerAdapter;
import ooo.oxo.moments.widget.RxBindingAppCompatActivity;
import pocketknife.BindExtra;
import pocketknife.NotRequired;
import pocketknife.PocketKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class UserActivity extends RxBindingAppCompatActivity<UserActivityBinding> {

    private static final String TAG = "UserActivity";

    final ObservableArrayList<Media> feed = new ObservableArrayList<>();

    @BindExtra("id")
    @NotRequired
    long id;

    @BindExtra("user")
    @NotRequired
    User user;

    @BindExtra("from_post")
    @NotRequired
    String fromPostId;

    Observable<List<Media>> firstLoad;

    private UserApi userApi;

    private FeedApi feedApi;

    private PostponedTransitionTrigger transitionTrigger;

    private boolean isAvatarLoaded = false;

    private int offset;

    @Override
    protected int getContentView(Bundle savedInstanceState) {
        return R.layout.user_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PocketKnife.bindExtras(this);

        setTitle(null);
        setSupportActionBar(binding.toolbar);

        binding.toolbar.setNavigationOnClickListener(v -> supportFinishAfterTransition());

        binding.pager.setAdapter(new Adapter());
        binding.pager.setOffscreenPageLimit(binding.pager.getAdapter().getCount());
        binding.pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                syncRefresherState(position);
            }
        });

        binding.tabs.setupWithViewPager(binding.pager);

        binding.appbar.addOnOffsetChangedListener((appbar, i) -> {
            offset = i;
            Log.d(TAG, "offset: " + i);
            syncRefresherState(binding.pager.getCurrentItem());
        });

        InstaApplication application = InstaApplication.from(this);

        userApi = application.createApi(UserApi.class);
        feedApi = application.createApi(FeedApi.class);

        if (user != null) {
            supportPostponeEnterTransition();
            ViewCompat.setTransitionName(binding.avatar, fromPostId + "_avatar");
            transitionTrigger = new PostponedTransitionTrigger(this);
            populateProfile(user);
        }

        if (id == 0 && user != null) {
            id = user.pk;
        }

        if (id == 0) {
            throw new IllegalStateException("Must specify which user to load");
        }

        loadUser().compose(bindToLifecycle()).subscribe(this::populateProfile);

        firstLoad = loadFeed(null).compose(bindToLifecycle()).cache();
        firstLoad.subscribe(RxList.appendTo(feed), this::showError);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        transitionTrigger.cancel();
        feed.clear();
    }

    private void syncRefresherState(int tab) {
        Object fragment = binding.pager.getAdapter().instantiateItem(binding.pager, tab);

        if (fragment instanceof RefreshEnabler) {
            ((RefreshEnabler) fragment).setCanRefresh(offset == 0);
        }
    }

    Observable<List<Media>> loadFeed(String maxId) {
        return feedApi.ofUser(id, maxId)
                .observeOn(AndroidSchedulers.mainThread())
                .filter(envelope -> envelope.items != null)
                .map(envelope -> envelope.items);
    }

    private Observable<User> loadUser() {
        return userApi.infoOf(id)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(this::showError)
                .map(envelope -> envelope.user);
    }

    void showError(Throwable error) {
        Toast.makeText(this, R.string.error_network, Toast.LENGTH_SHORT).show();
    }

    private void populateProfile(User profile) {
        binding.setUser(profile);

        if (!isAvatarLoaded) {
            isAvatarLoaded = true;
            Glide.with(this).load(profile.profilePicUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .bitmapTransform(new CropCircleTransformation(this))
                    .listener(transitionTrigger)
                    .into(binding.avatar);
        }

        user = profile;
    }

    @OnClick(R.id.followers_container)
    void openFollowers(View v) {
        Intent intent = new Intent(this, FriendshipActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("user", user);
        intent.putExtra("what", "followers");
        startActivity(intent);
    }

    @OnClick(R.id.following_container)
    void openFollowing(View v) {
        Intent intent = new Intent(this, FriendshipActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("user", user);
        intent.putExtra("what", "following");
        startActivity(intent);
    }

    private class Adapter extends FragmentStatePagerAdapter implements IconifiedPagerAdapter {

        public Adapter() {
            super(getSupportFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new UserGridFragment();
                case 1:
                    return new UserStreamFragment();
                case 2:
                    return new Fragment();
                case 3:
                    return UserTaggedFragment.newFragment(id);
                default:
                    return null;
            }
        }

        @Override
        public int getPageIcon(int position) {
            switch (position) {
                case 0:
                    return R.drawable.ic_view_module_24dp;
                case 1:
                    return R.drawable.ic_view_stream_24dp;
                case 2:
                    return R.drawable.ic_user_places_24dp;
                case 3:
                    return R.drawable.ic_user_tagged_24dp;
                default:
                    return 0;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

    }

}
