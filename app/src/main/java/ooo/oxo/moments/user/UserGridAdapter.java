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
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ooo.oxo.moments.R;
import ooo.oxo.moments.feed.FeedAdapter;
import ooo.oxo.moments.model.Media;
import ooo.oxo.moments.widget.RatioImageView;

public class UserGridAdapter extends RecyclerView.Adapter<UserGridAdapter.ViewHolder> {

    private final Context context;
    private final LayoutInflater inflater;
    private final GridListener listener;

    private List<Media> feed;

    public UserGridAdapter(Context context, GridListener listener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    public List<Media> getFeed() {
        return feed;
    }

    public void setFeed(List<Media> feed) {
        this.feed = feed;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.user_grid_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Media item = feed.get(position);

        Media.Resource image = null;

        if (item.imageVersions != null) {
            for (Media.Resource version : item.imageVersions.candidates) {
                if (image == null || version.width > image.width) {
                    image = version;
                }
            }
        }

        if (image != null) {
            Glide.with(context)
                    .load(image.url)
                    .into(holder.image);
        }
    }

    @Override
    public int getItemCount() {
        return feed == null ? 0 : feed.size();
    }

    public interface GridListener extends FeedAdapter.FeedListener {

        void onImageClick(ViewHolder holder);

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.image)
        RatioImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            image.setOriginalSize(1, 1);
            itemView.setOnClickListener(v -> listener.onImageClick(this));
        }

    }

}
