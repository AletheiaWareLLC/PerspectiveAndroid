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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.aletheiaware.common.android.utils.CommonAndroidUtils;
import com.aletheiaware.perspective.android.R;
import com.aletheiaware.perspective.android.utils.PerspectiveAndroidUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO on first use show legalese
        Button playButton = findViewById(R.id.main_play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String preference = CommonAndroidUtils.getPreference(MainActivity.this, getString(R.string.preference_tutorial_completed), "false");
                if (Boolean.parseBoolean(preference)) {
                    Intent intent = new Intent(MainActivity.this, LevelSelectActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, GameActivity.class);
                    intent.putExtra(PerspectiveAndroidUtils.WORLD_EXTRA, PerspectiveAndroidUtils.TUTORIAL_WORLD);
                    intent.putExtra(PerspectiveAndroidUtils.PUZZLE_EXTRA, 0);
                    startActivity(intent);
                }
            }
        });

        Button settingsButton = findViewById(R.id.main_settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        ImageButton logoButton = findViewById(R.id.aletheia_ware_llc_logo);
        logoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://aletheiaware.com"));
                startActivity(intent);
            }
        });
    }
}
