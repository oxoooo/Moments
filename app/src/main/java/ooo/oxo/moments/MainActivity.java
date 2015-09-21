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
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ooo.oxo.moments.explore.ExploreFragment;
import ooo.oxo.moments.feed.FeedFragment;
import ooo.oxo.moments.model.User;
import ooo.oxo.moments.user.UserActivity;
import ooo.oxo.moments.util.FuckingFragmentManager;
import ooo.oxo.moments.util.ImageViewBindingUtil;
import pocketknife.BindExtra;
import pocketknife.PocketKnife;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Bind(R.id.drawer)
    DrawerLayout drawer;

    @Bind(R.id.appbar)
    AppBarLayout appbar;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.status_bar)
    View statusBar;

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

    private CompositeSubscription subscriptions = new CompositeSubscription();

    private FuckingFragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        ButterKnife.bind(this);
        PocketKnife.bindExtras(this);

        setSupportActionBar(toolbar);

        appbar.addOnOffsetChangedListener((v, i) -> statusBar.setAlpha(Math.min(
                1, (float) -i / (float) (appbar.getHeight() - statusBar.getHeight()))));

        toolbar.setNavigationOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

        ImageViewBindingUtil.loadRoundImage(avatar, user.profilePicUrl);
        userName.setText(user.username);
        fullName.setText(user.fullName);

        fragmentManager = new FuckingFragmentManager(this, R.id.container);

        switchFragment(R.id.home);

        navigation.setCheckedItem(R.id.home);
        navigation.setNavigationItemSelectedListener(menuItem -> {
            if (switchFragment(menuItem.getItemId())) {
                drawer.closeDrawers();
                return true;
            } else {
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscriptions.unsubscribe();
    }

    private boolean switchFragment(@IdRes int id) {
        switch (id) {
            case R.id.home:
                fragmentManager.switchTo(FeedFragment.class);
                return true;
            case R.id.explore:
                fragmentManager.switchTo(ExploreFragment.class);
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
