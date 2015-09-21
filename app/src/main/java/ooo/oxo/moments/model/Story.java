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

public class Story {

    public int type;

    public Args args;

    public class Args {

        public String text;

        public long profileId;

        public String profileImage;

        public List<Link> links;

        public List<Media> media;

        public Date timestamp;

        public class Link {

            public int start;

            public int end;

            public String id;

            public String type;

        }

        public class Media {

            public String id;

            public String image;

        }

    }

}
