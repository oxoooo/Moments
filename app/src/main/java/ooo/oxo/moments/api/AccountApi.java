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

package ooo.oxo.moments.api;

import java.util.HashMap;

import ooo.oxo.moments.model.User;
import retrofit.Call;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public interface AccountApi {

    @FormUrlEncoded
    @POST("v1/accounts/login/")
    Call<LoginEnvelope> login(@FieldMap HashMap<String, String> signedBody);

    @POST("v1/accounts/logout/")
    Call logout(@FieldMap HashMap<String, String> signedBody);

    @POST("v1/accounts/change_profile_picture/")
    Call changeProfilePicture();

    class LoginEnvelope {

        public User loggedInUser;

    }

}
