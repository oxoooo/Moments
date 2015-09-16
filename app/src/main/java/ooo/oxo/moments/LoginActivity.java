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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ooo.oxo.moments.feed.FeedActivity;
import ooo.oxo.moments.model.AccessToken;
import ooo.oxo.moments.net.OauthApi;
import ooo.oxo.moments.net.OauthUtils;
import retrofit.Callback;
import retrofit.Response;

public class LoginActivity extends AppCompatActivity implements Callback<AccessToken> {

    private static final String TAG = "LoginActivity";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.login)
    View login;

    @Bind(R.id.progress)
    View progress;

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_activity);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        setTitle(null);

        OauthApi oauthApi = InstaApplication.from(this).createApi(OauthApi.class);

        Uri data = getIntent().getData();
        if (data != null && "insta-login".equals(data.getScheme())) {
            String code = data.getQueryParameter("code");
            if (!TextUtils.isEmpty(code)) {
                login.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                OauthUtils.accessToken(oauthApi, code).enqueue(this);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (InstaSharedState.getInstance().hasAccessToken()) {
            login.setVisibility(View.GONE);
            startMainActivity();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.proxy:
                startActivity(new Intent(this, ProxyActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.login)
    void login(View v) {
        Uri uri = Uri.parse("https://api.instagram.com/oauth/authorize/").buildUpon()
                .appendQueryParameter("client_id", BuildConfig.CLIENT_ID)
                .appendQueryParameter("redirect_uri", OauthApi.REDIRECT_URI)
                .appendQueryParameter("response_type", OauthApi.RESPONSE_TYPE_CODE)
                .appendQueryParameter("scope", "comments relationships likes")
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivity(intent);
    }

    @Override
    public void onResponse(Response<AccessToken> response) {
        progress.setVisibility(View.GONE);
        login.setVisibility(View.VISIBLE);

        AccessToken token = response.body();
        if (token != null) {
            InstaSharedState.getInstance().setAccessToken(token.accessToken);
            startMainActivity();
        }
    }

    @Override
    public void onFailure(Throwable t) {
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, FeedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        handler.postDelayed(() -> {
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 1000);
    }

}
