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
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding.support.design.widget.RxNavigationView;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import ooo.oxo.moments.app.FuckingFragmentManager;
import ooo.oxo.moments.explore.ExploreFragment;
import ooo.oxo.moments.feed.FeedFragment;
import ooo.oxo.moments.inbox.InboxFragment;
import ooo.oxo.moments.model.User;
import ooo.oxo.moments.user.UserActivity;
import pocketknife.BindExtra;
import pocketknife.PocketKnife;

public class MainActivity extends RxAppCompatActivity {

    private static final String TAG = "MainActivity";

    @Bind(R.id.drawer)
    DrawerLayout drawer;

    @Bind(R.id.navigation)
    NavigationView navigation;

    ImageView avatar;

    TextView userName;

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

//FIXME
//        avatar = (ImageView) navigation.findViewById(R.id.avatar);
//        avatar.setOnClickListener(this::onClickSelf);
//        ImageViewBindingUtil.loadRoundImage(avatar, user.profilePicUrl);
//
//        userName = (TextView) navigation.findViewById(R.id.user_name);
//        userName.setText(user.username);
//
//        fullName = (TextView) navigation.findViewById(R.id.full_name);
//        fullName.setText(user.fullName);

        fragmentManager = new FuckingFragmentManager(this, R.id.container);

        switchFragment(R.id.home);
        navigation.setCheckedItem(R.id.home);

        RxNavigationView.itemSelections(navigation)
                .compose(bindToLifecycle())
                .map(MenuItem::getItemId)
                .filter(this::switchFragment)
                .subscribe(result -> drawer.closeDrawers());
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

    void onClickSelf(View v) {
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("from_post", "navigation");

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, avatar, "navigation_avatar");

        startActivity(intent, options.toBundle());
    }

}
