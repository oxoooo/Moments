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

package ooo.oxo.moments.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;

public class IconifiedTabLayout extends TabLayout {

    public IconifiedTabLayout(Context context) {
        super(context);
    }

    public IconifiedTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IconifiedTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setTabsFromPagerAdapter(@NonNull PagerAdapter adapter) {
        if (adapter instanceof IconifiedPagerAdapter) {
            this.removeAllTabs();
            int i = 0;

            for (int count = adapter.getCount(); i < count; ++i) {
                this.addTab(this.newTab().setIcon(((IconifiedPagerAdapter) adapter).getPageIcon(i)));
            }
        } else {
            super.setTabsFromPagerAdapter(adapter);
        }
    }

}
