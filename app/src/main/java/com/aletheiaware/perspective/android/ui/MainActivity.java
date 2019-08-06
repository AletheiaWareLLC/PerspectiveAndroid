/*
 * Copyright 2019 Aletheia Ware LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aletheiaware.perspective.android.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;

import com.aletheiaware.common.android.utils.CommonAndroidUtils;
import com.aletheiaware.perspective.android.R;
import com.aletheiaware.perspective.android.utils.PerspectiveAndroidUtils;

public class MainActivity extends AppCompatActivity {

    private Button playButton;
    private Button settingsButton;
    private ImageButton logoButton;
    public AlertDialog legaleseDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playButton = findViewById(R.id.main_play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playButton.setEnabled(false);
                String preference = CommonAndroidUtils.getPreference(MainActivity.this, getString(R.string.preference_tutorial_completed), "false");
                if (Boolean.parseBoolean(preference)) {
                    Intent intent = new Intent(MainActivity.this, WorldSelectActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, GameActivity.class);
                    intent.putExtra(PerspectiveAndroidUtils.WORLD_EXTRA, PerspectiveAndroidUtils.WORLD_TUTORIAL);
                    intent.putExtra(PerspectiveAndroidUtils.PUZZLE_EXTRA, 1);
                    startActivity(intent);
                }
            }
        });

        settingsButton = findViewById(R.id.main_settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsButton.setEnabled(false);
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        logoButton = findViewById(R.id.aletheia_ware_llc_logo);
        logoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoButton.setEnabled(false);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://aletheiaware.com"));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        playButton.setEnabled(true);
        settingsButton.setEnabled(true);
        logoButton.setEnabled(true);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            legalese();
        }
    }

    public void legalese() {
        String preference = CommonAndroidUtils.getPreference(MainActivity.this, getString(R.string.preference_legalese_accepted), "false");
        if (!Boolean.parseBoolean(preference)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.LegaleseDialogTheme);
            View view = getLayoutInflater().inflate(R.layout.dialog_legalese, null);
            final CheckBox termsCheck = view.findViewById(R.id.legalese_terms_of_service_check);
            final CheckBox policyCheck = view.findViewById(R.id.legalese_privacy_policy_check);
            final CheckBox betaCheck = view.findViewById(R.id.legalese_beta_test_agreement_check);
            builder.setView(view);
            builder.setPositiveButton(R.string.legalese_action_accept, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    if (!termsCheck.isChecked()) {
                        CommonAndroidUtils.showErrorDialog(MainActivity.this, R.style.ErrorDialogTheme, getString(R.string.error_terms_of_service_required));
                        return;
                    }
                    if (!policyCheck.isChecked()) {
                        CommonAndroidUtils.showErrorDialog(MainActivity.this, R.style.ErrorDialogTheme, getString(R.string.error_privacy_policy_required));
                        return;
                    }
                    if (!betaCheck.isChecked()) {
                        CommonAndroidUtils.showErrorDialog(MainActivity.this, R.style.ErrorDialogTheme, getString(R.string.error_beta_test_agreement_required));
                        return;
                    }
                    CommonAndroidUtils.setPreference(MainActivity.this, getString(R.string.preference_legalese_accepted), "true");
                    dialog.cancel();
                }
            });
            builder.setNegativeButton(R.string.legalese_action_reject, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
            builder.setCancelable(false);
            legaleseDialog = builder.create();
            legaleseDialog.show();
        }
    }
}
