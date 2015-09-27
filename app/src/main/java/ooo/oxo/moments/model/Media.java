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

package ooo.oxo.moments.model;

import android.support.annotation.IntDef;

import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.List;

public class Media {

    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;

    public long pk;

    public String id;

    @MediaType
    public int mediaType;

    public int originalWidth;
    public int originalHeight;

    public boolean hasLiked;

    public String code;

    public int likeCount;
    public List<User> likers;

    public int commentCount;
    public List<Comment> comments;
    public boolean hasMoreComments;

    public Date takenAt;

    public boolean photoOfYou;

    public Caption caption;

    public List<String> tags;

    public User user;

    @SerializedName("image_versions2")
    public ImageCandidates imageVersions;

    public List<Resource> videoVersions;

    @Override
    public boolean equals(Object o) {
        return this == o || ((o instanceof Media) && ((Media) o).id.equals(id));
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_IMAGE, TYPE_VIDEO})
    @interface MediaType {
    }

    public class ImageCandidates {

        public List<Resource> candidates;

        public Resource picked;

    }

    public class Resource {

        public String url;

        public int width;

        public int height;

    }

    public class Caption {

        public String text;

        public Date createdAt;

    }

}
