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

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.design.widget.AppBarLayout;

import com.readystatesoftware.systembartint.SystemBarTintManager;

public class StatusBarTintDelegate implements AppBarLayout.OnOffsetChangedListener {

    private final SystemBarTintManager tintManager;
    private final int color;
    private final int size;
    private final int inset;

    public StatusBarTintDelegate(Activity activity) {
        this(activity, Color.TRANSPARENT);
    }

    public StatusBarTintDelegate(Activity activity, int size, int inset) {
        this(activity, Color.TRANSPARENT, size, inset);
    }

    public StatusBarTintDelegate(Activity activity, @ColorInt int color) {
        this(activity, color, 0, 0);
    }

    public StatusBarTintDelegate(Activity activity, @ColorInt int color, int size, int inset) {
        this.color = color;
        this.size = size;
        this.inset = inset;
        tintManager = new SystemBarTintManager(activity);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintColor(color);
        tintManager.setStatusBarAlpha(1.0f);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appbar, int offset) {
        float progress;

        if (size == 0) {
            progress = (float) -offset / (float) appbar.getHeight();
        } else {
            progress = Math.max(0, ((float) -offset - (appbar.getHeight() - inset - size)) / size);
        }

        tintManager.setStatusBarTintColor(ColorMixer.mix(color, Color.BLACK, progress));
    }

}
