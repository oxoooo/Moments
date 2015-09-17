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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

public class User implements Parcelable {

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public long pk;

    public String username;

    public String fullName;

    public String profilePicUrl;

    @Nullable
    public String biography;

    @Nullable
    public String externalUrl;

    public int mediaCount = -1;
    public int followerCount = -1;
    public int followingCount = -1;
    public int usertagsCount = -1;

    protected User(Parcel in) {
        pk = in.readLong();
        username = in.readString();
        fullName = in.readString();
        profilePicUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(pk);
        dest.writeString(username);
        dest.writeString(fullName);
        dest.writeString(profilePicUrl);
    }

}