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
import java.util.Objects;

public class Story {

    public int type;

    public Args args;

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Story)) {
            return false;
        }

        Story that = (Story) o;

        return this.type == that.type && Objects.equals(this.args, that.args);
    }

    public class Args {

        public String text;

        public long profileId;

        public String profileImage;

        public List<Link> links;

        public List<Media> media;

        public Date timestamp;

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }

            if (!(o instanceof Args)) {
                return false;
            }

            Args that = (Args) o;

            return Objects.equals(this.text, that.text)
                    && this.profileId == that.profileId
                    && Objects.equals(this.links, that.links)
                    && Objects.equals(this.media, that.media)
                    && Objects.equals(this.profileImage, that.profileImage)
                    && Objects.equals(this.timestamp, that.timestamp);
        }

        public class Link {

            public int start;

            public int end;

            public String id;

            public String type;

            @Override
            public boolean equals(Object o) {
                if (o == this) {
                    return true;
                }

                if (!(o instanceof Link)) {
                    return false;
                }

                Link that = (Link) o;

                return this.start == that.start
                        && this.end == that.end
                        && Objects.equals(this.id, that.id)
                        && Objects.equals(this.type, that.type);
            }

        }

        public class Media {

            public String id;

            public String image;

            @Override
            public boolean equals(Object o) {
                if (o == this) {
                    return true;
                }

                if (!(o instanceof Media)) {
                    return false;
                }

                Media that = (Media) o;

                return Objects.equals(this.id, that.id) && Objects.equals(this.image, that.image);
            }

        }

    }

}
