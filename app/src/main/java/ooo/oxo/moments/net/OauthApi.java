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

import ooo.oxo.moments.model.AccessToken;
import retrofit.Call;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public interface OauthApi {

    String REDIRECT_URI = "insta-login://redirect";

    String RESPONSE_TYPE_CODE = "code";

    String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";

    @FormUrlEncoded
    @POST("oauth/access_token")
    Call<AccessToken> accessToken(@Field("client_id") String clientId,
                                  @Field("client_secret") String clientSecret,
                                  @Field("grant_type") String grantType,
                                  @Field("redirect_uri") String redirectUri,
                                  @Field("code") String code);

}
