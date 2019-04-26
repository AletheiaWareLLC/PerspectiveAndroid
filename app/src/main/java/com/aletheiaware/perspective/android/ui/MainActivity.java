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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.aletheiaware.bc.BC.Channel;
import com.aletheiaware.bc.BC.Channel.EntryCallback;
import com.aletheiaware.bc.BCProto.Block;
import com.aletheiaware.bc.BCProto.BlockEntry;
import com.aletheiaware.bc.android.ui.AccountActivity;
import com.aletheiaware.bc.android.utils.BCAndroidUtils;
import com.aletheiaware.perspective.PerspectiveProto.World;
import com.aletheiaware.perspective.android.R;
import com.aletheiaware.perspective.android.WorldAdapter;
import com.aletheiaware.perspective.android.utils.PerspectiveAndroidUtils;
import com.aletheiaware.perspective.utils.PerspectiveUtils;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private WorldAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // RecyclerView
        RecyclerView recyclerView = findViewById(R.id.main_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        // Adapter
        adapter = new WorldAdapter(this) {
            @Override
            public void onSelection(ByteString hash, World world) {
                // TODO play tutorial first (world 0)
                // TODO show world/puzzle selector
                // TODO allow new worlds to be purchased
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                intent.putExtra(PerspectiveAndroidUtils.WORLD_EXTRA, hash.toByteArray());
                intent.putExtra(PerspectiveAndroidUtils.PUZZLE_EXTRA, 0);
                startActivityForResult(intent, PerspectiveAndroidUtils.GAME_ACTIVITY);
            }
        };
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (BCAndroidUtils.isInitialized()) {
            String alias = BCAndroidUtils.getAlias();
        }
        if (adapter.isEmpty()) {
            refresh();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case PerspectiveAndroidUtils.ACCESS_ACTIVITY:
                switch (resultCode) {
                    case RESULT_OK:
                        refresh();
                        break;
                    case RESULT_CANCELED:
                        setResult(RESULT_CANCELED);
                        finish();
                        break;
                    default:
                        break;
                }
                break;
            case PerspectiveAndroidUtils.ACCOUNT_ACTIVITY:
                refresh();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, intent);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refresh();
                return true;
            case R.id.menu_account:
                account();
                return true;
            case R.id.menu_settings:
                settings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refresh() {
        // TODO start refresh menu animate
        new Thread() {
            @Override
            public void run() {
                Channel worlds = PerspectiveUtils.getWorldsChannel(getCacheDir(), PerspectiveAndroidUtils.getPerspectiveHost());
                try {
                    worlds.iterate(new EntryCallback() {
                        @Override
                        public boolean onEntry(ByteString hash, Block block, BlockEntry entry) {
                            Log.d(PerspectiveUtils.TAG, "Entry: " + entry);
                            try {
                                World world = World.newBuilder().mergeFrom(entry.getRecord().getPayload()).build();
                                Log.d(PerspectiveUtils.TAG, "World: " + world);
                                adapter.addWorld(entry.getRecordHash(), entry.getRecord().getTimestamp(), world);
                            } catch (InvalidProtocolBufferException e) {
                                /* Ignored */
                                e.printStackTrace();
                            }
                            return true;
                        }
                    });
                } catch (IOException e) {
                    BCAndroidUtils.showErrorDialog(MainActivity.this, R.string.error_read_worlds_failed, e);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO stop refresh menu animate
                    }
                });
            }
        }.start();
    }

    private void account() {
        Intent i = new Intent(this, AccountActivity.class);
        startActivityForResult(i, PerspectiveAndroidUtils.ACCOUNT_ACTIVITY);
    }

    private void settings() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivityForResult(i, PerspectiveAndroidUtils.SETTINGS_ACTIVITY);
    }
}
