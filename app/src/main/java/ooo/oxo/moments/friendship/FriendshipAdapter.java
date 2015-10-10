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

package ooo.oxo.moments.friendship;

import android.content.Context;
import android.databinding.ObservableList;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ooo.oxo.library.databinding.support.widget.BindingRecyclerView;
import ooo.oxo.moments.R;
import ooo.oxo.moments.databinding.FriendshipItemBinding;
import ooo.oxo.moments.model.User;

public class FriendshipAdapter extends BindingRecyclerView.ListAdapter<User, FriendshipAdapter.ViewHolder> {

    private final FriendshipListener listener;

    public FriendshipAdapter(Context context, ObservableList<User> data, FriendshipListener listener) {
        super(context, data);
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater, R.layout.friendship_item, parent);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.binding.setUser(data.get(position));
    }

    public interface FriendshipListener {

        void openUser(ViewHolder holder);

    }

    public class ViewHolder extends BindingRecyclerView.ViewHolder<FriendshipItemBinding> {

        public ViewHolder(LayoutInflater inflater, @LayoutRes int layoutId, ViewGroup parent) {
            super(inflater, layoutId, parent);
            itemView.setOnClickListener(v -> listener.openUser(this));
        }

    }

}
