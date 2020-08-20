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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aletheiaware.common.android.utils.CommonAndroidUtils;
import com.aletheiaware.common.utils.CommonUtils;
import com.aletheiaware.joy.android.scene.GLScene;
import com.aletheiaware.perspective.Perspective;
import com.aletheiaware.perspective.PerspectiveProto.Puzzle;
import com.aletheiaware.perspective.PerspectiveProto.Solution;
import com.aletheiaware.perspective.PerspectiveProto.World;
import com.aletheiaware.perspective.android.R;
import com.aletheiaware.perspective.android.billing.BillingManager;
import com.aletheiaware.perspective.android.utils.PerspectiveAndroidUtils;
import com.aletheiaware.perspective.utils.PerspectiveUtils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class GameActivity extends AppCompatActivity implements Perspective.Callback, BillingManager.Callback {

    private static final long STAR_VIBRATION_GAP = 60;
    private static final long[][] STAR_VIBRATIONS = {
            {0, 70},
            {0, 70, STAR_VIBRATION_GAP, 90},
            {0, 70, STAR_VIBRATION_GAP, 90, STAR_VIBRATION_GAP, 115},
            {0, 70, STAR_VIBRATION_GAP, 90, STAR_VIBRATION_GAP, 115, STAR_VIBRATION_GAP, 145},
            {0, 70, STAR_VIBRATION_GAP, 90, STAR_VIBRATION_GAP, 115, STAR_VIBRATION_GAP, 145, STAR_VIBRATION_GAP, 180},
    };
    private static final long[] TRAVEL_VIBRATION = {0, 10};

    public AlertDialog gameOverDialog;
    private String worldName;
    private int puzzleIndex;
    private boolean outlineEnabled;
    private World world;
    private GameView gameView;
    private GLScene glScene;
    private Perspective perspective;
    private SharedPreferences preferences;
    private Vibrator vibrator;
    private BillingManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_loading);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        manager = new BillingManager(this, this);

        if (savedInstanceState != null) {
            load(savedInstanceState);
        } else {
            final Intent intent = getIntent();
            if (intent != null) {
                Bundle data = intent.getExtras();
                if (data != null) {
                    load(data);
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(PerspectiveAndroidUtils.WORLD_EXTRA, worldName);
        outState.putInt(PerspectiveAndroidUtils.PUZZLE_EXTRA, puzzleIndex);
        outState.putBoolean(PerspectiveAndroidUtils.OUTLINE_EXTRA, outlineEnabled);
    }

    private void load(Bundle data) {
        worldName = data.getString(PerspectiveAndroidUtils.WORLD_EXTRA);
        puzzleIndex = data.getInt(PerspectiveAndroidUtils.PUZZLE_EXTRA);
        if (data.containsKey(PerspectiveAndroidUtils.ORIENTATION_EXTRA)) {
            setRequestedOrientation(data.getInt(PerspectiveAndroidUtils.ORIENTATION_EXTRA));
        }
        if (puzzleIndex < 1) {
            puzzleIndex = 1;
        }
        outlineEnabled = PerspectiveUtils.isTutorial(worldName)
                || data.getBoolean(PerspectiveAndroidUtils.OUTLINE_EXTRA)
                || preferences.getBoolean(getString(R.string.preference_puzzle_outline_key), true);

        // Load World, Scene, and Perspective in Worker Thread
        new Thread() {
            @Override
            public void run() {
                try {
                    Log.d(PerspectiveUtils.TAG, "Loading World");
                    world = PerspectiveAndroidUtils.getWorld(getAssets(), worldName);
                    Log.d(PerspectiveUtils.TAG, "Creating Scene");
                    glScene = new GLScene();
                    perspective = new Perspective(GameActivity.this, glScene, world.getSize());
                    perspective.scenegraphs.put("basic", PerspectiveAndroidUtils.createBasicSceneGraph(glScene, world));
                    perspective.outlineEnabled = outlineEnabled;
                    float[] background = PerspectiveUtils.BLACK;
                    String colour = world.getBackgroundColour();
                    if (colour != null && !colour.isEmpty()) {
                        background = glScene.getFloatArray(colour);
                    }
                    glScene.putFloatArray(GLScene.BACKGROUND, background);
                    glScene.putIntArray("camera-viewport", glScene.getViewport());

                    // Create Game View in UI Thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(PerspectiveUtils.TAG, "Creating View");
                            setContentView(R.layout.activity_game);
                            gameView = findViewById(R.id.game_view);
                            gameView.setScene(glScene);
                            gameView.setPerspective(perspective);

                            loadPuzzle();
                        }
                    });
                } catch (IOException e) {
                    CommonAndroidUtils.showErrorDialog(GameActivity.this, R.style.ErrorDialogTheme, R.string.error_game_init, e);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    public GLScene getGlScene() {
        return glScene;
    }

    public Perspective getPerspective() {
        return perspective;
    }

    @Override
    protected void onDestroy() {
        if (gameView != null) {
            gameView.quit();
            gameView = null;
        }
        if (manager != null) {
            manager.destroy();
            manager = null;
        }
        super.onDestroy();
    }

    private void vibrate(long[] pattern) {
        if (vibrator == null || !vibrator.hasVibrator()) {
            Log.d(PerspectiveUtils.TAG, "No vibrator");
        } else if (preferences.getBoolean(getString(R.string.preference_puzzle_vibration_key), true)) {
            vibrator.vibrate(pattern, -1);
        }
    }

    @UiThread
    private void loadPuzzle() {
        Log.d(PerspectiveUtils.TAG, "Loading Puzzle: " + puzzleIndex);
        new Thread() {
            @Override
            public void run() {
                final Puzzle puzzle = PerspectiveUtils.getPuzzle(world, puzzleIndex);
                if (puzzle != null) {
                    perspective.importPuzzle(puzzle);
                    final String name = CommonUtils.capitalize(world.getName()) + " - " + puzzleIndex;
                    final int foreground = colourStringToInt(world.getForegroundColour());
                    final int background = colourStringToInt(world.getBackgroundColour());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            View root = findViewById(R.id.game_root);
                            root.setBackgroundColor(background);
                            TextView titleText = findViewById(R.id.game_puzzle_title);
                            titleText.setText(name);
                            titleText.setTextColor(foreground);
                            TextView descriptionText = findViewById(R.id.game_puzzle_description);
                            descriptionText.setText(puzzle.getDescription());
                            descriptionText.setTextColor(foreground);
                            descriptionText.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }.start();
    }

    private int colourStringToInt(String colour) {
        float[] array = PerspectiveUtils.WHITE;
        if (colour != null && !colour.isEmpty()) {
            array = glScene.getFloatArray(colour);
        }
        int red = (int) (array[0] * 255f);
        int green = (int) (array[1] * 255f);
        int blue = (int) (array[2] * 255f);
        return 0xff << 24 | (red & 0xff) << 16 | (green & 0xff) << 8 | (blue & 0xff);
    }

    @Override
    public void addSceneGraphNode(String shader, String name, String type, String mesh, String colour, String texture, String material) {
        try {
            PerspectiveAndroidUtils.addSceneGraphNode(glScene, perspective, getAssets(), shader, name, type, mesh, colour, texture, material);
        } catch (IOException e) {
            CommonAndroidUtils.showErrorDialog(this, R.style.ErrorDialogTheme, R.string.error_get_scene_graph_node, e);
            e.printStackTrace();
        }
    }

    @Override
    public void onDropComplete() {
        Log.d(PerspectiveUtils.TAG, "Drop Complete");
        vibrate(TRAVEL_VIBRATION);
        // TODO
    }

    @Override
    public void onRotateComplete() {
        // TODO
    }

    @Override
    public void onTurnComplete() {
        Log.d(PerspectiveUtils.TAG, "Turn Complete");
        // TODO
    }

    @Override
    public void onGameLost() {
        Log.d(PerspectiveUtils.TAG, "Game Lost");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this, R.style.GameLostDialogTheme);
                builder.setView(getLayoutInflater().inflate(R.layout.dialog_game_lost, null));
                builder.setPositiveButton(R.string.puzzle_retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        perspective.clearAllLocations();
                        loadPuzzle();
                    }
                });
                builder.setNeutralButton(R.string.main_menu, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });
                builder.setCancelable(false);
                gameOverDialog = builder.create();
                gameOverDialog.show();
            }
        });
    }

    @Override
    public void onGameWon() {
        Log.d(PerspectiveUtils.TAG, "Game Won");
        final Solution solution = perspective.getSolution();
        int target = perspective.puzzle.getTarget();
        int score = solution.getScore();
        Log.d(PerspectiveUtils.TAG, "Score: " + score + " (" + target + ")");
        final int stars = PerspectiveUtils.scoreToStars(score, target);
        Log.d(PerspectiveUtils.TAG, "Stars: " + stars);
        new Thread() {
            @Override
            public void run() {
                try {
                    String hash = PerspectiveUtils.getHash(perspective.puzzle.toByteArray());
                    PerspectiveAndroidUtils.saveSolution(GameActivity.this, worldName, hash, solution);
                } catch (IOException | NoSuchAlgorithmException e) {
                    CommonAndroidUtils.showErrorDialog(GameActivity.this, R.style.ErrorDialogTheme, R.string.error_save_solution, e);
                    e.printStackTrace();
                }
            }
        }.start();

        if (PerspectiveUtils.isTutorial(worldName)) {
            CommonAndroidUtils.setPreference(GameActivity.this, getString(R.string.preference_tutorial_completed), "true");
        }

        // Vibrate once for each star earned
        if (stars > 0) {
            vibrate(STAR_VIBRATIONS[stars - 1]);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this, R.style.GameWonDialogTheme);
                View view = getLayoutInflater().inflate(R.layout.dialog_game_won, null);
                builder.setView(view);
                view.findViewById(R.id.game_won_star1).setVisibility((stars > 0) ? View.VISIBLE : View.GONE);
                view.findViewById(R.id.game_won_star2).setVisibility((stars > 1) ? View.VISIBLE : View.GONE);
                view.findViewById(R.id.game_won_star3).setVisibility((stars > 2) ? View.VISIBLE : View.GONE);
                view.findViewById(R.id.game_won_star4).setVisibility((stars > 3) ? View.VISIBLE : View.GONE);
                view.findViewById(R.id.game_won_star5).setVisibility((stars > 4) ? View.VISIBLE : View.GONE);
                if (puzzleIndex < world.getPuzzleCount()) {
                    builder.setPositiveButton(R.string.puzzle_next, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            perspective.clearAllLocations();
                            puzzleIndex++;
                            loadPuzzle();
                        }
                    });
                } else {
                    final String nextWorld = getNextWorld();
                    if (!nextWorld.equals(PerspectiveUtils.WORLD_TUTORIAL)) {
                        builder.setPositiveButton(R.string.puzzle_next, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                worldName = nextWorld;
                                puzzleIndex = 1;
                                setResult(RESULT_OK);
                                finish();
                                Intent intent = new Intent(GameActivity.this, GameActivity.class);
                                intent.putExtra(PerspectiveAndroidUtils.WORLD_EXTRA, worldName);
                                intent.putExtra(PerspectiveAndroidUtils.PUZZLE_EXTRA, puzzleIndex);
                                startActivity(intent);
                            }
                        });
                    }
                }
                if (stars < 5) {
                    builder.setNegativeButton(R.string.puzzle_retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            perspective.clearAllLocations();
                            loadPuzzle();
                        }
                    });
                }
                builder.setNeutralButton(R.string.main_menu, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        setResult(RESULT_OK);
                        finish();
                    }
                });
                builder.setCancelable(false);
                gameOverDialog = builder.create();
                gameOverDialog.show();
            }
        });
    }

    @NonNull
    private String getNextWorld() {
        switch (worldName) {
            case PerspectiveUtils.WORLD_TUTORIAL:
                return PerspectiveUtils.WORLD_ONE;
            case PerspectiveUtils.WORLD_ONE:
                return PerspectiveUtils.WORLD_TWO;
            case PerspectiveUtils.WORLD_TWO:
                return PerspectiveUtils.WORLD_THREE;
            case PerspectiveUtils.WORLD_THREE:
                return PerspectiveUtils.WORLD_FOUR;
            case PerspectiveUtils.WORLD_FOUR:
                return PerspectiveUtils.WORLD_FIVE;
            case PerspectiveUtils.WORLD_FIVE:
                return PerspectiveUtils.WORLD_SIX;
            case PerspectiveUtils.WORLD_SIX:
                if (manager.hasPurchased(PerspectiveUtils.WORLD_SEVEN)) {
                    return PerspectiveUtils.WORLD_SEVEN;
                } // else fallthrough
            case PerspectiveUtils.WORLD_SEVEN:
                if (manager.hasPurchased(PerspectiveUtils.WORLD_EIGHT)) {
                    return PerspectiveUtils.WORLD_EIGHT;
                } // else fallthrough
            case PerspectiveUtils.WORLD_EIGHT:
                if (manager.hasPurchased(PerspectiveUtils.WORLD_NINE)) {
                    return PerspectiveUtils.WORLD_NINE;
                } // else fallthrough
            case PerspectiveUtils.WORLD_NINE:
                if (manager.hasPurchased(PerspectiveUtils.WORLD_TEN)) {
                    return PerspectiveUtils.WORLD_TEN;
                } // else fallthrough
            case PerspectiveUtils.WORLD_TEN:
                if (manager.hasPurchased(PerspectiveUtils.WORLD_ELEVEN)) {
                    return PerspectiveUtils.WORLD_ELEVEN;
                } // else fallthrough
            case PerspectiveUtils.WORLD_ELEVEN:
                if (manager.hasPurchased(PerspectiveUtils.WORLD_TWELVE)) {
                    return PerspectiveUtils.WORLD_TWELVE;
                } // else fallthrough
            case PerspectiveUtils.WORLD_TWELVE:
                if (manager.hasPurchased(PerspectiveUtils.WORLD_THIRTEEN)) {
                    return PerspectiveUtils.WORLD_THIRTEEN;
                } // else fallthrough
            case PerspectiveUtils.WORLD_THIRTEEN:
                if (manager.hasPurchased(PerspectiveUtils.WORLD_FOURTEEN)) {
                    return PerspectiveUtils.WORLD_FOURTEEN;
                } // else fallthrough
            case PerspectiveUtils.WORLD_FOURTEEN:
                if (manager.hasPurchased(PerspectiveUtils.WORLD_FIFTEEN)) {
                    return PerspectiveUtils.WORLD_FIFTEEN;
                } // else fallthrough
            default:
                return PerspectiveUtils.WORLD_TUTORIAL;
        }
    }

    @Override
    public void onBillingClientSetup() {
        Log.d(PerspectiveUtils.TAG, "Billing Client Setup");
        // TODO
    }

    @Override
    public void onPurchasesUpdated() {
        Log.d(PerspectiveUtils.TAG, "Purchases Updated");
        // TODO
    }
}
