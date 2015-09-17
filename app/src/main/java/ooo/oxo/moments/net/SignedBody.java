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

import com.google.gson.Gson;
import com.instagram.strings.StringBridge;

import java.util.HashMap;

public class SignedBody {

    public String _csrftoken;
    public String _uid;
    public String _uuid;

    public static <T extends SignedBody> HashMap<String, String> build(Gson gson, T object, Class<T> type) {
        String json = gson.toJson(object, type);
        String signature = StringBridge.getSignatureString(json.getBytes());
        HashMap<String, String> body = new HashMap<>();
        body.put("ig_sig_key_version", "4");
        body.put("signed_body", signature + "." + json);
        return body;
    }

}
