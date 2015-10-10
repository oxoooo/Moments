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

package ooo.oxo.moments.inbox;

import android.content.Context;
import android.databinding.ObservableList;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ooo.oxo.moments.R;
import ooo.oxo.moments.databinding.InboxNewsItemBinding;
import ooo.oxo.moments.model.Story;
import ooo.oxo.moments.widget.BindingRecyclerView;

public class NewsInboxAdapter extends BindingRecyclerView.ListAdapter<Story, NewsInboxAdapter.ViewHolder> {

    public NewsInboxAdapter(Context context, ObservableList<Story> data) {
        super(context, data);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater, R.layout.inbox_news_item, parent);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.binding.setStory(data.get(position));
    }

    public class ViewHolder extends BindingRecyclerView.ViewHolder<InboxNewsItemBinding> {

        public ViewHolder(LayoutInflater inflater, @LayoutRes int layoutId, ViewGroup parent) {
            super(inflater, layoutId, parent);
        }

    }

}
