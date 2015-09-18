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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ooo.oxo.moments.api.AccountApi;
import ooo.oxo.moments.feed.FeedActivity;
import ooo.oxo.moments.model.LoginForm;
import ooo.oxo.moments.model.User;
import ooo.oxo.moments.net.SignedBody;
import retrofit.Callback;
import retrofit.Response;

public class LoginActivity extends AppCompatActivity implements Callback<AccountApi.LoginEnvelope> {

    private static final String TAG = "LoginActivity";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.username)
    TextView username;

    @Bind(R.id.password)
    TextView password;

    @Bind(R.id.login)
    View login;

    @Bind(R.id.progress)
    View progress;

    private Handler handler = new Handler(Looper.getMainLooper());

    private InstaApplication application;
    private InstaSharedState sharedState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        application = InstaApplication.from(this);
        sharedState = InstaSharedState.getInstance();

        setContentView(R.layout.login_activity);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        setTitle(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sharedState.hasAccount()) {
            performLogin();
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
        InstaSharedState.getInstance().setAccount(
                username.getText().toString(),
                password.getText().toString());
        performLogin();
    }

    private void performLogin() {
        username.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        login.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        LoginForm form = new LoginForm();
        form.username = sharedState.getUsername();
        form.password = sharedState.getPassword();
        form.deviceId = sharedState.getDeviceId();
        form.guid = sharedState.getUuid();

        HashMap<String, String> body = SignedBody.build(
                application.getGson(), form, LoginForm.class);

        application.createApi(AccountApi.class).login(body).enqueue(this);
    }

    @Override
    public void onResponse(Response<AccountApi.LoginEnvelope> response) {
        progress.setVisibility(View.GONE);

        if (response.body() == null) {
            Toast.makeText(this, "登录失败", Toast.LENGTH_SHORT).show();
            username.setVisibility(View.VISIBLE);
            password.setVisibility(View.VISIBLE);
            login.setVisibility(View.VISIBLE);
            sharedState.setAccount(null, null);
        } else {
            startMainActivity(response.body().loggedInUser);
        }
    }

    @Override
    public void onFailure(Throwable t) {
        Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show();
    }

    private void startMainActivity(User user) {
        Intent intent = new Intent(this, FeedActivity.class);
        intent.putExtra("user", user);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        handler.postDelayed(() -> {
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 1000);
    }

}
