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

package ooo.oxo.moments.user;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ooo.oxo.moments.databinding.UserGridItemBinding;
import ooo.oxo.moments.model.Media;
import ooo.oxo.moments.widget.ArrayRecyclerAdapter;

public class UserGridAdapter extends ArrayRecyclerAdapter<Media, UserGridAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private final GridListener listener;

    public UserGridAdapter(Context context, GridListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(UserGridItemBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.binding.setItem(get(position));
    }

    public interface GridListener {

        void onImageClick(ViewHolder holder);

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        UserGridItemBinding binding;

        public ViewHolder(UserGridItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            itemView.setOnClickListener(v -> listener.onImageClick(this));
        }

    }

}
