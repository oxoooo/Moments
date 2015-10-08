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

package ooo.oxo.moments.feed;

import android.content.Context;
import android.databinding.ObservableList;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import ooo.oxo.moments.databinding.FeedItemBinding;
import ooo.oxo.moments.model.Comment;
import ooo.oxo.moments.model.Media;
import ooo.oxo.moments.text.CommentTextUtils;
import ooo.oxo.moments.widget.BindingRecyclerView;

public class FeedAdapter extends BindingRecyclerView.ListAdapter<Media, FeedAdapter.ViewHolder> {

    private final FeedListener listener;

    public FeedAdapter(Context context, ObservableList<Media> data, FeedListener listener) {
        super(context, data);
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(FeedItemBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Media item = data.get(position);

        // TODO: consider caching for smoother scrolling
        SpannableStringBuilder comments = new SpannableStringBuilder();

        if (item.caption != null) {
            comments.append(CommentTextUtils.format(item.user.username, item.caption.text, item.tags,
                    () -> listener.onUserClick(item.user.pk), null));
        }

        for (Comment comment : item.comments) {
            CharSequence text = new SpannableStringBuilder(
                    CommentTextUtils.format(comment.user.username, comment.text,
                            () -> listener.onUserClick(comment.user.pk)));

            if (comments.length() > 0) {
                comments.append("\n");
            }

            comments.append(text);
        }

        holder.binding.setHolder(holder);
        holder.binding.setItem(item);
        holder.binding.setComments(comments);

        holder.binding.image.setOriginalSize(item.originalWidth, item.originalHeight);
    }

    public interface FeedListener {

        void onUserClick(ViewHolder holder);

        void onUserClick(long id);

        void onImageClick(ViewHolder holder);

        void onLikesClick(ViewHolder holder);

        void onLike(ViewHolder holder);

        void onComment(ViewHolder holder);

    }

    public class ViewHolder extends BindingRecyclerView.ViewHolder<FeedItemBinding> {

        public ViewHolder(FeedItemBinding binding) {
            super(binding);
            ButterKnife.bind(this, itemView);
        }

        public void clickUser(View v) {
            listener.onUserClick(this);
        }

        public void clickImage(View v) {
            listener.onImageClick(this);
        }

        public void clickLikes(View v) {
            listener.onLikesClick(this);
        }

        public void like(View v) {
            listener.onLike(this);
        }

        public void comment(View v) {
            listener.onComment(this);
        }

    }

}
