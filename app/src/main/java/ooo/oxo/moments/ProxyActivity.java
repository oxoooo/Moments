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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class ProxyActivity extends AppCompatActivity {

    @Bind(R.id.host)
    EditText host;

    @Bind(R.id.port)
    EditText port;

    @Bind(R.id.enabled)
    CheckBox enabled;

    private InstaSharedState sharedState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.proxy_activity);
        ButterKnife.bind(this);

        sharedState = InstaSharedState.getInstance();

        host.setText(sharedState.getProxyHost());
        port.setText(String.valueOf(sharedState.getProxyPort()));
        enabled.setChecked(sharedState.isProxyEnabled());
    }

    @OnCheckedChanged(R.id.enabled)
    void enable(CompoundButton buttonView, boolean isChecked) {
        sharedState.setProxyEnabled(isChecked);
    }

    @OnClick(R.id.save)
    void save(View v) {
        sharedState.setProxy(
                host.getText().toString(),
                Integer.parseInt(port.getText().toString()));

        finish();
    }

}
