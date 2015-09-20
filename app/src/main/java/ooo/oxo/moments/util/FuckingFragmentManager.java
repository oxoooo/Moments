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

package ooo.oxo.moments.util;

import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import java.util.HashMap;

public class FuckingFragmentManager {

    private final HashMap<String, Fragment> fragments = new HashMap<>();

    private final FragmentActivity activity;
    private final FragmentManager fragmentManager;

    private final int container;

    private String current;

    public FuckingFragmentManager(FragmentActivity activity, @IdRes int container) {
        this.activity = activity;
        this.fragmentManager = activity.getSupportFragmentManager();
        this.container = container;
    }

    public void switchTo(Class<? extends Fragment> fragment) {
        String name = fragment.getName();

        if (current != null) {
            fragmentManager.beginTransaction().hide(fragments.get(current)).commit();
        }

        if (fragmentManager.findFragmentByTag(name) == null) {
            Fragment instance = Fragment.instantiate(activity, name);
            fragments.put(name, instance);
            fragmentManager.beginTransaction().add(container, instance, name).commit();
        } else {
            fragmentManager.beginTransaction().show(fragments.get(name)).commit();
        }

        current = name;
    }

    @Nullable
    public Fragment getFragment(Class<? extends Fragment> fragment) {
        return fragments.get(fragment.getName());
    }

}
