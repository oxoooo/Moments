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

package ooo.oxo.moments.text;

import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;

import java.util.List;

public class CommentTextUtils {

    public static CharSequence format(String username, String text, @Nullable List<String> tags,
                                      OnUserClickListener onUserClickListener,
                                      @Nullable OnTagClickListener onTagClickListener) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append(username);
        builder.append(" ");
        builder.append(text);

        builder.setSpan(new UsernameSpan() {
            @Override
            public void onClick(View widget) {
                onUserClickListener.onUserClick();
            }
        }, 0, username.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        if (tags != null && !tags.isEmpty() && onTagClickListener != null) {
            // TODO
        }

        return builder;
    }

    public static CharSequence format(String username, String text, OnUserClickListener listener) {
        return format(username, text, null, listener, null);
    }

    public interface OnUserClickListener {

        void onUserClick();

    }

    public interface OnTagClickListener {

        void onTagClick(String tag);

    }

}
