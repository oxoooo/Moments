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

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

public interface FriendshipApi {

    @GET("v1/friendships/autocomplete_user_list/")
    Call autocompleteUserList();

    @GET("v1/friendships/suggested/")
    Call suggested();

    @GET("v1/friendships/show/{id}/")
    Call show(@Path("id") String id);

    @POST("v1/friendships/create/{id}/")
    Call create(@Path("id") String id);

    @POST("v1/friendships/destroy/{id}/")
    Call destroy(@Path("id") String id);

    @POST("v1/friendships/block/{id}/")
    Call block(@Path("id") String id);

}
