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

import android.view.View;
import android.view.ViewGroup;

public class ViewGroupUtils {

    public static int[] calculateBounds(View view, ViewGroup container) {
        int[] result = new int[]{0, 0, 0, 0};

        View parent = view;
        do {
            if (!(parent.getParent() instanceof ViewGroup)) {
                break;
            }
            result[0] += parent.getLeft();
            result[1] += parent.getTop();
            result[2] += parent.getRight();
            result[3] += parent.getBottom();

            parent = (View) parent.getParent();
        } while (parent != container);

        return result;
    }

}
