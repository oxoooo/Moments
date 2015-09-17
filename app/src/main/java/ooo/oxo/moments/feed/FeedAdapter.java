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
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import ooo.oxo.moments.R;
import ooo.oxo.moments.model.Comment;
import ooo.oxo.moments.model.Media;
import ooo.oxo.moments.text.CommentTextUtils;
import ooo.oxo.moments.widget.RatioImageView;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

    private final Context context;
    private final LayoutInflater inflater;
    private final FeedListener listener;

    private List<Media> feed;

    public FeedAdapter(Context context, FeedListener listener) {
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
        return new ViewHolder(inflater.inflate(R.layout.feed_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Media item = feed.get(position);

        Glide.with(context)
                .load(item.user.profilePicUrl)
                .bitmapTransform(new CropCircleTransformation(context))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.avatar);

        ViewCompat.setTransitionName(holder.avatar, item.id + "_avatar");

        holder.user.setText(item.user.username);
        holder.time.setText(DATE_FORMAT.format(item.takenAt));

        holder.image.setOriginalSize(item.originalWidth, item.originalHeight);

        Glide.with(context).load(item.imageVersions)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.image);

        if (item.likeCount > 0) {
            holder.likes.setText(context.getString(R.string.n_likes, item.likeCount));
            holder.likes.setVisibility(View.VISIBLE);
        } else {
            holder.likes.setVisibility(View.GONE);
        }

        holder.comments.removeAllViews();
        holder.comments.setVisibility(
                item.caption != null || item.commentCount > 0 ? View.VISIBLE : View.GONE);

        if (item.caption != null) {
            TextView caption = (TextView) inflater.inflate(
                    R.layout.feed_comment_item, holder.comments, false);

            CharSequence text = CommentTextUtils.format(
                    item.user.username, item.caption.text, item.tags,
                    () -> listener.onUserClick(item.user.pk), null);

            caption.setText(text, TextView.BufferType.SPANNABLE);
            caption.setMovementMethod(LinkMovementMethod.getInstance());

            holder.comments.addView(caption);
        }

        for (Comment comment : item.comments) {
            TextView child = (TextView) inflater.inflate(
                    R.layout.feed_comment_item, holder.comments, false);

            CharSequence text = CommentTextUtils.format(
                    comment.user.username, comment.text,
                    () -> listener.onUserClick(comment.user.pk));

            child.setText(text, TextView.BufferType.SPANNABLE);
            child.setMovementMethod(LinkMovementMethod.getInstance());

            holder.comments.addView(child);
        }
    }

    @Override
    public int getItemCount() {
        return feed == null ? 0 : feed.size();
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

        @Bind(R.id.avatar)
        ImageView avatar;

        @Bind(R.id.user)
        TextView user;

        @Bind(R.id.time)
        TextView time;

        @Bind(R.id.image)
        RatioImageView image;

        @Bind(R.id.likes)
        TextView likes;

        @Bind(R.id.comments)
        ViewGroup comments;

        public ViewHolder(View itemView) {
            super(itemView);
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
