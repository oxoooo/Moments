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
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.OnClick;
import ooo.oxo.moments.R;
import ooo.oxo.moments.databinding.FeedItemBinding;
import ooo.oxo.moments.model.Comment;
import ooo.oxo.moments.model.Media;
import ooo.oxo.moments.text.CommentTextUtils;
import ooo.oxo.moments.widget.ArrayRecyclerAdapter;

public class FeedAdapter extends ArrayRecyclerAdapter<Media, FeedAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private final FeedListener listener;

    public FeedAdapter(Context context, FeedListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(FeedItemBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Media item = get(position);

        holder.binding.setItem(item);

        holder.binding.image.setOriginalSize(item.originalWidth, item.originalHeight);

        holder.binding.comments.removeAllViews();
        holder.binding.comments.setVisibility(
                item.caption != null || item.commentCount > 0 ? View.VISIBLE : View.GONE);

        if (item.caption != null) {
            TextView caption = (TextView) inflater.inflate(
                    R.layout.feed_comment_item, holder.binding.comments, false);

            CharSequence text = CommentTextUtils.format(
                    item.user.username, item.caption.text, item.tags,
                    () -> listener.onUserClick(item.user.pk), null);

            caption.setText(text, TextView.BufferType.SPANNABLE);
            caption.setMovementMethod(LinkMovementMethod.getInstance());

            holder.binding.comments.addView(caption);
        }

        for (Comment comment : item.comments) {
            TextView child = (TextView) inflater.inflate(
                    R.layout.feed_comment_item, holder.binding.comments, false);

            CharSequence text = CommentTextUtils.format(
                    comment.user.username, comment.text,
                    () -> listener.onUserClick(comment.user.pk));

            child.setText(text, TextView.BufferType.SPANNABLE);
            child.setMovementMethod(LinkMovementMethod.getInstance());

            holder.binding.comments.addView(child);
        }
    }

    public interface FeedListener {

        void onUserClick(ViewHolder holder);

        void onUserClick(long id);

        void onImageClick(ViewHolder holder);

        void onLikesClick(ViewHolder holder);

        void onLike(ViewHolder holder);

        void onComment(ViewHolder holder);

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        FeedItemBinding binding;

        public ViewHolder(FeedItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.user_container)
        void clickUser(View v) {
            listener.onUserClick(this);
        }

        @OnClick(R.id.image_container)
        void clickImage(View v) {
            listener.onImageClick(this);
        }

        @OnClick(R.id.likes)
        void clickLikes(View v) {
            listener.onLikesClick(this);
        }

        @OnClick(R.id.like)
        void like(View v) {
            listener.onLike(this);
        }

        @OnClick(R.id.comment)
        void comment(View v) {
            listener.onComment(this);
        }

    }

}
