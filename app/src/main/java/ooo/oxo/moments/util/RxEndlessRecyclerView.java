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

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;

import rx.Observable;

public class RxEndlessRecyclerView {

    public static Observable<Integer> reachesEnd(RecyclerView view) {
        return RxRecyclerView.scrollEvents(view)
                .filter(i -> view.getLayoutManager() != null)
                .map(event -> {
                    RecyclerView.LayoutManager layoutManager = view.getLayoutManager();
                    if (layoutManager instanceof LinearLayoutManager) { // also GridLayoutManager
                        return ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                    } else {
                        return 0; // TODO: StaggeredGridLayoutManager
                    }
                })
                .filter(i -> i >= view.getLayoutManager().getItemCount() - 1)
                .distinctUntilChanged();
    }

}
