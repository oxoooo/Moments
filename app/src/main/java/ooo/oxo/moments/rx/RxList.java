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

package ooo.oxo.moments.rx;

import java.util.List;

import rx.functions.Action1;

public class RxList {

    public static <E> Action1<List<E>> appendTo(List<E> list) {
        return list::addAll;
    }

    public static <E> Action1<List<E>> prependToOrReplace(List<E> list) {
        return items -> {
            if (items.isEmpty()) {
                return;
            }

            if (list.isEmpty()) {
                list.addAll(items);
                return;
            }

            if (list.size() >= items.size() && list.subList(0, items.size()).equals(items)) {
                for (int i = items.size() - 1; i >= 0; i--) {
                    list.set(i, items.get(i));
                }
                return;
            }

            list.clear();
            list.addAll(items);
        };
    }

}
