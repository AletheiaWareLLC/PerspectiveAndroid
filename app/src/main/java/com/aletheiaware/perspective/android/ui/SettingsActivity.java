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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.aletheiaware.common.android.utils.CommonAndroidUtils;
import com.aletheiaware.perspective.android.BuildConfig;
import com.aletheiaware.perspective.android.R;
import com.aletheiaware.perspective.android.utils.PerspectiveAndroidUtils;

import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup UI
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            Fragment preferenceFragment = new SettingsPreferenceFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.preference_frame, preferenceFragment);
            ft.commit();
        }
    }

    public static class SettingsPreferenceFragment extends PreferenceFragmentCompat {
        private Preference clearProgressPreference;
        private Preference appVersionPreference;
        private Preference supportPreference;

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.fragment_preference);

            clearProgressPreference = findPreference(getString(R.string.preference_clear_progress_key));
            clearProgressPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final FragmentActivity activity = getActivity();
                    if (activity == null) {
                        return false;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogTheme);
                    builder.setIcon(R.drawable.warning);
                    builder.setTitle(R.string.preference_clear_progress_confirmation_title);
                    builder.setMessage(R.string.preference_clear_progress_confirmation_message);
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CommonAndroidUtils.setPreference(activity, getString(R.string.preference_tutorial_completed), "false");
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        PerspectiveAndroidUtils.clearSolutions(activity);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                    return true;
                }
            });

            appVersionPreference = findPreference(getString(R.string.preference_app_version_key));
            appVersionPreference.setShouldDisableView(true);
            appVersionPreference.setEnabled(false);
            appVersionPreference.setSelectable(false);

            supportPreference = findPreference(getString(R.string.preference_support_key));
            supportPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final FragmentActivity activity = getActivity();
                    if (activity == null) {
                        return false;
                    }
                    CommonAndroidUtils.support(activity, new StringBuilder());
                    return true;
                }
            });

            update();
        }

        public void update() {
            final FragmentActivity activity = getActivity();
            if (activity == null) {
                return;
            }

            appVersionPreference.setSummary(BuildConfig.BUILD_TYPE + "-" + BuildConfig.VERSION_NAME);
        }
    }
}
