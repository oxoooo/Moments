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

import ooo.oxo.moments.BuildConfig;
import ooo.oxo.moments.model.AccessToken;
import retrofit.Call;

public class OauthUtils {

    public static Call<AccessToken> accessToken(OauthApi api, String code) {
        return api.accessToken(BuildConfig.CLIENT_ID, BuildConfig.CLIENT_SECRET,
                OauthApi.GRANT_TYPE_AUTHORIZATION_CODE, OauthApi.REDIRECT_URI, code);
    }

}
