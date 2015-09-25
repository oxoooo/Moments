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

import android.support.annotation.Nullable;

import java.util.List;

import ooo.oxo.moments.model.Media;

public class ImageCandidatesUtil {

    @Nullable
    public static Media.Resource pickBest(List<Media.Resource> candidates) {
        Media.Resource best = null;

        for (Media.Resource candidate : candidates) {
            if (best == null || (candidate.width >= best.width && candidate.height >= best.height)) {
                best = candidate;
            }
        }

        return best;
    }

}
