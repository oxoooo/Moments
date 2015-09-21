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

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.instagram.strings.StringBridge;
import com.squareup.okhttp.OkHttpClient;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Date;
import java.util.HashMap;

import ooo.oxo.moments.net.LoggingInterceptor;
import ooo.oxo.moments.net.SignedBody;
import ooo.oxo.moments.net.TimestampTypeAdapter;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;

public class InstaApplication extends Application {

    private final HashMap<Class, Object> apis = new HashMap<>();

    private OkHttpClient httpClient;

    private Gson gson;

    private Retrofit retrofit;

    public static InstaApplication from(Context context) {
        return (InstaApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        InstaSharedState.createInstance(this);

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);

        httpClient = new OkHttpClient();

        httpClient.setCookieHandler(cookieManager);

        httpClient.networkInterceptors().add(chain -> chain.proceed(chain.request()
                .newBuilder().header("User-Agent", "Instagram 7.6.0 Android").build()));

        httpClient.networkInterceptors().add(new LoggingInterceptor());

        InstaSharedState.getInstance().applyProxy();

        gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Date.class, new TimestampTypeAdapter())
                .create();

        retrofit = new Retrofit.Builder()
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl("https://i.instagram.com/api/")
                .build();
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    void applyHttpProxy(String host, int port) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                httpClient.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port)));
                return null;
            }
        }.execute();
    }

    void removeHttpProxy() {
        httpClient.setProxy(Proxy.NO_PROXY);
    }

    public <T> T createApi(Class<T> service) {
        if (!apis.containsKey(service)) {
            T instance = retrofit.create(service);
            apis.put(service, instance);
        }

        //noinspection unchecked
        return (T) apis.get(service);
    }

    public <T extends SignedBody> HashMap<String, String> sign(T object) {
        String json = gson.toJson(object, object.getClass());
        String signature = StringBridge.getSignatureString(json.getBytes());
        HashMap<String, String> body = new HashMap<>();
        body.put("ig_sig_key_version", "4");
        body.put("signed_body", signature + "." + json);
        return body;
    }

}
