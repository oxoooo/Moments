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

import java.util.Date;
import java.util.List;

public class Media {

    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_VIDEO = "video";

    public String id;

    public String type;

    public Date createdTime;

    public Resources images;

    public Resources videos;

    public Caption caption;

    public Comments comments;

    public List<String> tags;

    public String link;

    public User user;

    public Likes likes;

    public Location location;

    public class Caption {

        public String text;

    }

    public class Comments {

        public List<Comment> data;

        public int count;

    }

    public class Resources {

        public Resource lowResolution;

        public Resource thumbnail;

        public Resource standardResolution;

        public class Resource {

            public String url;

            public int width;

            public int height;

        }

    }

    public class Likes {

        public int count;

    }

    public class Location {

        public String id;

        public String name;

        public double latitude;

        public double longitude;

    }

}
