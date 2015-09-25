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

package ooo.oxo.moments.binding;

import android.databinding.BindingAdapter;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import java.util.Date;

@SuppressWarnings("unused")
public class TextViewBindingUtil {

    @BindingAdapter("bind:relativeDateTime")
    public static void setRelativeDateTime(TextView view, Date date) {
        view.setText(DateUtils.getRelativeTimeSpanString(view.getContext(), date.getTime()));
    }

    @BindingAdapter("bind:spannable")
    public static void setSpannable(TextView view, CharSequence text) {
        view.setText(text, TextView.BufferType.SPANNABLE);
        view.setMovementMethod(LinkMovementMethod.getInstance());
    }

}
