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

package ooo.oxo.moments.net;

import java.util.List;

import ooo.oxo.moments.model.Media;
import ooo.oxo.moments.model.User;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface UserApi {

    @GET("v1/users/self/feed")
    Call<Envelope<List<Media>>> feed(@Query("count") int count,
                                     @Query("access_token") String accessToken);

    @GET("v1/users/{id}")
    Observable<Envelope<User>> profile(@Path("id") String id,
                                       @Query("access_token") String accessToken);

    @GET("v1/users/{id}/media/recent")
    Observable<Envelope<List<Media>>> timeline(@Path("id") String id,
                                               @Query("count") int count,
                                               @Query("access_token") String accessToken);

}
