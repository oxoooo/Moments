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

package ooo.oxo.moments;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.UUID;

public class InstaSharedState {

    private static InstaSharedState instance;

    private final Context context;
    private final SharedPreferences preferences;

    private InstaSharedState(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences("insta", Context.MODE_PRIVATE);
    }

    public static InstaSharedState getInstance() {
        return instance;
    }

    static void createInstance(Context context) {
        instance = new InstaSharedState(context);
    }

    public boolean isProxyEnabled() {
        return preferences.getBoolean("proxy_enabled", false)
                && preferences.contains("proxy_host")
                && preferences.contains("proxy_port");
    }

    public boolean setProxyEnabled(boolean enabled) {
        if (preferences.edit().putBoolean("proxy_enabled", enabled).commit()) {
            applyProxy();
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    public String getProxyHost() {
        return preferences.getString("proxy_host", null);
    }

    public int getProxyPort() {
        return preferences.getInt("proxy_port", 0);
    }

    public boolean setProxy(String host, int port) {
        if (TextUtils.isEmpty(host) || port <= 0) {
            if (!preferences.edit().remove("proxy_host").remove("proxy_port").putBoolean("proxy_enabled", false).commit()) {
                return false;
            }
        } else {
            if (!preferences.edit().putString("proxy_host", host).putInt("proxy_port", port).commit()) {
                return false;
            }
        }

        applyProxy();
        return true;
    }

    void applyProxy() {
        if (isProxyEnabled()) {
            InstaApplication.from(context).applyHttpProxy(getProxyHost(), getProxyPort());
        } else {
            InstaApplication.from(context).removeHttpProxy();
        }
    }

    public String getUsername() {
        return preferences.getString("username", "");
    }

    public String getPassword() {
        return preferences.getString("password", "");
    }

    public boolean setAccount(String username, String password) {
        return preferences.edit()
                .putString("username", username)
                .putString("password", password)
                .commit();
    }

    public boolean hasAccount() {
        return !TextUtils.isEmpty(getUsername()) && !TextUtils.isEmpty(getPassword());
    }

    public String getUuid() {
        String uuid = preferences.getString("uuid", "");

        if (TextUtils.isEmpty(uuid)) {
            uuid = UUID.randomUUID().toString();
            preferences.edit().putString("uuid", uuid).apply();
        }

        return uuid;
    }

    public String getDeviceId() {
        String deviceId = preferences.getString("device_id", "");

        if (TextUtils.isEmpty(deviceId)) {
            deviceId = UUID.randomUUID().toString().replace("-", "").toLowerCase();
            deviceId = deviceId.substring(deviceId.length() - 16);
            deviceId = "android-" + deviceId;
            preferences.edit().putString("device_id", deviceId).apply();
        }

        return deviceId;
    }

    public String getCsrfToken() {
        return preferences.getString("csrf_token", "");
    }

    public boolean setCsrfToken(String token) {
        return preferences.edit().putString("csrf_token", token).commit();
    }

}
