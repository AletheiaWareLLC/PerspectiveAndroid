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
import android.support.annotation.WorkerThread;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.aletheiaware.common.android.utils.CommonAndroidUtils;
import com.aletheiaware.perspective.PerspectiveProto.Solution;
import com.aletheiaware.perspective.PerspectiveProto.World;
import com.aletheiaware.perspective.android.PuzzleAdapter;
import com.aletheiaware.perspective.android.R;
import com.aletheiaware.perspective.android.WorldAdapter;
import com.aletheiaware.perspective.android.billing.BillingManager;
import com.aletheiaware.perspective.android.utils.PerspectiveAndroidUtils;
import com.aletheiaware.perspective.utils.PerspectiveUtils;
import com.android.billingclient.api.BillingClient.BillingResponseCode;
import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldSelectActivity extends AppCompatActivity implements WorldAdapter.Callback, BillingManager.Callback {

    private final Map<String, int[]> puzzleStars = new HashMap<>();
    private final Map<String, SkuDetails> skuDetails = new HashMap<>();
    private WorldAdapter adapter;
    private BillingManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world_select);

        // RecyclerView
        RecyclerView recyclerView = findViewById(R.id.world_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        // Adapter
        adapter = new WorldAdapter(this, this);
        recyclerView.setAdapter(adapter);

        manager = new BillingManager(this, this);

        new Thread() {
            @Override
            public void run() {
                addWorld(PerspectiveAndroidUtils.WORLD_TUTORIAL);
                addWorld(PerspectiveAndroidUtils.WORLD_ONE);
                addWorld(PerspectiveAndroidUtils.WORLD_TWO);
                addWorld(PerspectiveAndroidUtils.WORLD_THREE);
                addWorld(PerspectiveAndroidUtils.WORLD_FOUR);
                addWorld(PerspectiveAndroidUtils.WORLD_FIVE);
                addWorld(PerspectiveAndroidUtils.WORLD_SIX);
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (manager != null) {
            manager.destroy();
        }
    }

    @WorkerThread
    private void addWorld(final String world) {
        try {
            final World w = PerspectiveAndroidUtils.getWorld(getAssets(), world);
            final int puzzles = w.getPuzzleCount();
            final int[] stars = new int[puzzles];
            int totalStars = 0;
            for (int i = 0; i < puzzles; i++) {
                int target = w.getPuzzle(i).getTarget();
                stars[i] = -1;
                Solution s = PerspectiveAndroidUtils.loadSolution(WorldSelectActivity.this, world, i + 1);
                if (s != null) {
                    stars[i] = PerspectiveAndroidUtils.scoreToStars(s.getScore(), target);
                    totalStars += stars[i];
                }
            }
            puzzleStars.put(world, stars);
            int worldStars = 0;
            if (puzzles > 0) {
                worldStars = totalStars/puzzles;
            }
            adapter.addWorld(w, worldStars);
        } catch (IOException e) {
            CommonAndroidUtils.showErrorDialog(this, R.style.ErrorDialogTheme, R.string.error_add_world, e);
            e.printStackTrace();
        }
    }

    @Override
    public void onSelect(World world) {
        if (world != null) {
            String name = world.getName();
            RecyclerView view = (RecyclerView) getLayoutInflater().inflate(R.layout.dialog_puzzle_select, null);
            view.setLayoutManager(new GridLayoutManager(view.getContext(), 3, GridLayoutManager.VERTICAL, false));
            final AlertDialog dialog = new AlertDialog.Builder(WorldSelectActivity.this, R.style.WorldSelectDialogTheme)
                    .setView(view)
                    .setTitle(PerspectiveAndroidUtils.capitalize(name))
                    .create();
            PuzzleAdapter puzzleAdapter = new PuzzleAdapter(this, world, puzzleStars.get(name), new PuzzleAdapter.Callback() {
                @Override
                public void onSelect(String world, int puzzle) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    setResult(RESULT_OK);
                    finish();
                    Intent intent = new Intent(WorldSelectActivity.this, GameActivity.class);
                    intent.putExtra(PerspectiveAndroidUtils.WORLD_EXTRA, world);
                    intent.putExtra(PerspectiveAndroidUtils.PUZZLE_EXTRA, puzzle);
                    startActivity(intent);
                }
            });
            view.setAdapter(puzzleAdapter);
            dialog.show();
        }
    }

    @Override
    public void onBuy(String world) {
        Log.d(PerspectiveUtils.TAG, "Buying: " + world);
        SkuDetails details = skuDetails.get(world);
        Log.d(PerspectiveUtils.TAG, "SKU: " + details);
        manager.initiatePurchaseFlow(details, null);
    }

    @Override
    public void onBillingClientSetup() {
        Log.d(PerspectiveUtils.TAG, "Billing Client Setup");
        new Thread() {
            @Override
            public void run() {
                List<String> skus = new ArrayList<>();
                skus.add(PerspectiveAndroidUtils.WORLD_SEVEN);
                skus.add(PerspectiveAndroidUtils.WORLD_EIGHT);
                skus.add(PerspectiveAndroidUtils.WORLD_NINE);
                skus.add(PerspectiveAndroidUtils.WORLD_TEN);
                skus.add(PerspectiveAndroidUtils.WORLD_ELEVEN);
                skus.add(PerspectiveAndroidUtils.WORLD_TWELVE);
                querySkuDetails(skus);
            }
        }.start();
    }

    @Override
    public void onPurchasesUpdated() {
        Log.d(PerspectiveUtils.TAG, "Purchases Updated");
        new Thread() {
            @Override
            public void run() {
                if (manager.hasPurchased(PerspectiveAndroidUtils.WORLD_SEVEN)) {
                    addWorld(PerspectiveAndroidUtils.WORLD_SEVEN);
                }
                if (manager.hasPurchased(PerspectiveAndroidUtils.WORLD_EIGHT)) {
                    addWorld(PerspectiveAndroidUtils.WORLD_EIGHT);
                }
                if (manager.hasPurchased(PerspectiveAndroidUtils.WORLD_NINE)) {
                    addWorld(PerspectiveAndroidUtils.WORLD_NINE);
                }
                if (manager.hasPurchased(PerspectiveAndroidUtils.WORLD_TEN)) {
                    addWorld(PerspectiveAndroidUtils.WORLD_TEN);
                }
                if (manager.hasPurchased(PerspectiveAndroidUtils.WORLD_ELEVEN)) {
                    addWorld(PerspectiveAndroidUtils.WORLD_ELEVEN);
                }
                if (manager.hasPurchased(PerspectiveAndroidUtils.WORLD_TWELVE)) {
                    addWorld(PerspectiveAndroidUtils.WORLD_TWELVE);
                }
            }
        }.start();
    }

    @Override
    public void onTokenConsumed(String purchaseToken) {
        Log.d(PerspectiveUtils.TAG, "Token Consumed: " + purchaseToken);
        // TODO
    }

    public void querySkuDetails(List<String> skus) {
        Log.d(PerspectiveUtils.TAG, "Querying SKUs: " + skus);
        manager.querySkuDetailsAsync(SkuType.INAPP, skus, new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                int code = billingResult.getResponseCode();
                Log.d(PerspectiveUtils.TAG, "SKU query finished. Response code: " + code);
                if (code == BillingResponseCode.OK) {
                    for (SkuDetails details : skuDetailsList) {
                        Log.d(PerspectiveUtils.TAG, "SKU: " + details);
                        skuDetails.put(details.getSku(), details);
                        adapter.addWorld(details.getSku(), details.getPrice());
                    }
                }
            }
        });
    }
}
