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

package ooo.oxo.moments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding.support.design.widget.RxNavigationView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ooo.oxo.moments.app.RxActivity;
import ooo.oxo.moments.explore.ExploreFragment;
import ooo.oxo.moments.feed.FeedFragment;
import ooo.oxo.moments.inbox.InboxFragment;
import ooo.oxo.moments.model.User;
import ooo.oxo.moments.user.UserActivity;
import ooo.oxo.moments.util.FuckingFragmentManager;
import ooo.oxo.moments.util.ImageViewBindingUtil;
import pocketknife.BindExtra;
import pocketknife.PocketKnife;

public class MainActivity extends RxActivity {

    private static final String TAG = "MainActivity";

    @Bind(R.id.drawer)
    DrawerLayout drawer;

    @Bind(R.id.navigation)
    NavigationView navigation;

    @Bind(R.id.avatar)
    ImageView avatar;

    @Bind(R.id.user_name)
    TextView userName;

    @Bind(R.id.full_name)
    TextView fullName;

    @BindExtra("user")
    User user;

    private FuckingFragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        ButterKnife.bind(this);
        PocketKnife.bindExtras(this);

        ImageViewBindingUtil.loadRoundImage(avatar, user.profilePicUrl);
        userName.setText(user.username);
        fullName.setText(user.fullName);

        fragmentManager = new FuckingFragmentManager(this, R.id.container);

        switchFragment(R.id.home);
        navigation.setCheckedItem(R.id.home);

        subscribe(
                RxNavigationView.itemSelections(navigation)
                        .map(MenuItem::getItemId)
                        .map(this::switchFragment)
                        .filter(result -> result),
                result -> drawer.closeDrawers()
        );
    }

    public void openNavigation() {
        drawer.openDrawer(GravityCompat.START);
    }

    private boolean switchFragment(@IdRes int id) {
        switch (id) {
            case R.id.home:
                fragmentManager.switchTo(FeedFragment.class);
                return true;
            case R.id.explore:
                fragmentManager.switchTo(ExploreFragment.class);
                return true;
            case R.id.inbox:
                fragmentManager.switchTo(InboxFragment.class);
                return true;
            case R.id.proxy:
                startActivity(new Intent(this, ProxyActivity.class));
                return true;
            default:
                return false;
        }
    }

    @OnClick(R.id.avatar)
    void onClickSelf(View v) {
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("from_post", "navigation");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        @SuppressWarnings("unchecked")
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                new Pair<>(avatar, "navigation_avatar"),
                new Pair<>(userName, "navigation_user_name"),
                new Pair<>(fullName, "navigation_full_name"));

        startActivity(intent, options.toBundle());
    }

}
