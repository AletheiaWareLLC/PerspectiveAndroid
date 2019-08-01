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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.aletheiaware.perspective.android.PuzzleAdapter;
import com.aletheiaware.perspective.android.R;
import com.aletheiaware.perspective.android.WorldAdapter;
import com.aletheiaware.perspective.android.utils.PerspectiveAndroidUtils;

import java.io.IOException;

public class LevelSelectActivity extends AppCompatActivity implements PuzzleAdapter.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);

        // RecyclerView
        RecyclerView recyclerView = findViewById(R.id.world_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        // Adapter
        final WorldAdapter adapter = new WorldAdapter(this, this);
        recyclerView.setAdapter(adapter);

        new Thread() {
            @Override
            public void run() {
                try {
                    adapter.addWorld(PerspectiveAndroidUtils.getWorld(getAssets(), PerspectiveAndroidUtils.TUTORIAL_WORLD));
                    adapter.addWorld(PerspectiveAndroidUtils.getWorld(getAssets(), PerspectiveAndroidUtils.GROUND_ZERO_WORLD));
                    adapter.addWorld(PerspectiveAndroidUtils.getWorld(getAssets(), PerspectiveAndroidUtils.ALPHA_ONE_WORLD));
                    adapter.addWorld(PerspectiveAndroidUtils.getWorld(getAssets(), PerspectiveAndroidUtils.PORTAL_TWO_WORLD));
                    adapter.addWorld(PerspectiveAndroidUtils.getWorld(getAssets(), PerspectiveAndroidUtils.SEA_THREE_WORLD));
                    adapter.addWorld(PerspectiveAndroidUtils.getWorld(getAssets(), PerspectiveAndroidUtils.HIGH_FIVE_WORLD));
                    adapter.addWorld(PerspectiveAndroidUtils.getWorld(getAssets(), PerspectiveAndroidUtils.MAGIC_EIGHT_WORLD));
                    adapter.addWorld(PerspectiveAndroidUtils.getWorld(getAssets(), PerspectiveAndroidUtils.CLOUD_NINE_WORLD));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onSelection(String world, int puzzle) {
        // TODO allow new worlds to be purchased
        setResult(RESULT_OK);
        finish();
        Intent intent = new Intent(LevelSelectActivity.this, GameActivity.class);
        intent.putExtra(PerspectiveAndroidUtils.WORLD_EXTRA, world);
        intent.putExtra(PerspectiveAndroidUtils.PUZZLE_EXTRA, puzzle);
        startActivity(intent);
    }

}
