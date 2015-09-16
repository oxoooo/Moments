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

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v4.graphics.ColorUtils;

public class ColorMixer {

    public static int mix(@ColorInt int background, @ColorInt int foreground, float ratio) {
        int alpha = Color.alpha(foreground);
        alpha = (int) Math.floor((float) alpha * Math.max(0f, Math.min(1f, ratio)));
        foreground = ColorUtils.setAlphaComponent(foreground, alpha);
        return ColorUtils.compositeColors(foreground, background);
    }

}
