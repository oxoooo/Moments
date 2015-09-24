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

import java.util.List;

import ooo.oxo.moments.model.Media;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface FeedApi {

    @GET("v1/feed/timeline/")
    Observable<FeedEnvelope> timeline(@Query("max_id") String maxId);

    @GET("v1/feed/popular/")
    Observable<FeedEnvelope> popular(@Query("max_id") String maxId);

    @GET("v1/feed/user/{id}/")
    Observable<FeedEnvelope> ofUser(@Path("id") long id, @Query("max_id") String maxId);

    @GET("v1/usertags/{id}/feed/")
    Observable<FeedEnvelope> tagged(@Path("id") long id, @Query("max_id") String maxId);

    @GET("v1/feed/liked/")
    Call liked(@Query("max_id") String maxId);

    class FeedEnvelope {

        public List<Media> items;

    }

}
