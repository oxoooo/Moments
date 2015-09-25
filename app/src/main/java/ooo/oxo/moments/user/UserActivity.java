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
import android.databinding.DataBindingUtil;
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
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import ooo.oxo.moments.InstaApplication;
import ooo.oxo.moments.R;
import ooo.oxo.moments.api.UserApi;
import ooo.oxo.moments.databinding.UserActivityBinding;
import ooo.oxo.moments.friendship.FriendshipActivity;
import ooo.oxo.moments.model.User;
import ooo.oxo.moments.util.PostponedTransitionTrigger;
import ooo.oxo.moments.widget.IconifiedPagerAdapter;
import pocketknife.BindExtra;
import pocketknife.NotRequired;
import pocketknife.PocketKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class UserActivity extends RxAppCompatActivity {

    private static final String TAG = "UserActivity";

    @BindExtra("id")
    @NotRequired
    long id;

    @BindExtra("user")
    @NotRequired
    User user;

    @BindExtra("from_post")
    @NotRequired
    String fromPostId;

    private UserActivityBinding binding;

    private UserApi userApi;

    private PostponedTransitionTrigger transitionTrigger;

    private boolean isAvatarLoaded = false;

    private int offset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.user_activity);

        ButterKnife.bind(this);
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

        if (user != null) {
            supportPostponeEnterTransition();
            ViewCompat.setTransitionName(binding.avatar, fromPostId + "_avatar");
            ViewCompat.setTransitionName(binding.userName, fromPostId + "_user_name");
            ViewCompat.setTransitionName(binding.fullName, fromPostId + "_full_name");
            transitionTrigger = new PostponedTransitionTrigger(this);
            populateProfile(user);
        }

        if (id == 0 && user != null) {
            id = user.pk;
        }

        if (id == 0) {
            throw new IllegalStateException("Must specify which user to load");
        }

        loadUser()
                .compose(bindToLifecycle())
                .subscribe(this::populateProfile);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        transitionTrigger.cancel();
    }

    private void syncRefresherState(int tab) {
        Object fragment = binding.pager.getAdapter().instantiateItem(binding.pager, tab);

        if (fragment instanceof RefreshEnabler) {
            ((RefreshEnabler) fragment).setCanRefresh(offset == 0);
        }
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
                    return UserGridFragment.newFragment(id);
                case 1:
                    return UserStreamFragment.newFragment(id);
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
                    return R.drawable.ic_view_module_white_24dp;
                case 1:
                    return R.drawable.ic_view_stream_white_24dp;
                case 2:
                    return R.drawable.ic_map_white_24dp;
                case 3:
                    return R.drawable.ic_person_pin_white_24dp;
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
