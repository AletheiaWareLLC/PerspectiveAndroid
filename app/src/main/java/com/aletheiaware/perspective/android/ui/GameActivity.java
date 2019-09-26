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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aletheiaware.common.android.utils.CommonAndroidUtils;
import com.aletheiaware.joy.android.scene.GLScene;
import com.aletheiaware.joy.scene.AttributeNode;
import com.aletheiaware.joy.scene.SceneGraphNode;
import com.aletheiaware.perspective.Perspective;
import com.aletheiaware.perspective.PerspectiveProto.Puzzle;
import com.aletheiaware.perspective.PerspectiveProto.Solution;
import com.aletheiaware.perspective.PerspectiveProto.World;
import com.aletheiaware.perspective.android.R;
import com.aletheiaware.perspective.android.billing.BillingManager;
import com.aletheiaware.perspective.android.utils.PerspectiveAndroidUtils;
import com.aletheiaware.perspective.utils.PerspectiveUtils;

import java.io.IOException;

public class GameActivity extends AppCompatActivity implements Perspective.Callback, BillingManager.Callback {

    private static final long STAR_VIBRATION_GAP = 60;
    private static final long[][] STAR_VIBRATIONS = {
            {0, 70},
            {0, 70, STAR_VIBRATION_GAP, 90},
            {0, 70, STAR_VIBRATION_GAP, 90, STAR_VIBRATION_GAP, 115},
            {0, 70, STAR_VIBRATION_GAP, 90, STAR_VIBRATION_GAP, 115, STAR_VIBRATION_GAP, 145},
            {0, 70, STAR_VIBRATION_GAP, 90, STAR_VIBRATION_GAP, 115, STAR_VIBRATION_GAP, 145, STAR_VIBRATION_GAP, 180},
    };
    private static final long[] DROP_VIBRATION = {0, 10};

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
        setContentView(R.layout.activity_game);

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
    protected void onSaveInstanceState(Bundle outState) {
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
        outlineEnabled = PerspectiveAndroidUtils.isTutorial(worldName)
                || data.getBoolean(PerspectiveAndroidUtils.OUTLINE_EXTRA)
                || preferences.getBoolean(getString(R.string.preference_puzzle_outline_key), true);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView == null) {
            init();
        }
    }

    public GLScene getGlScene() {
        return glScene;
    }

    public Perspective getPerspective() {
        return perspective;
    }

    private void init() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Log.d(PerspectiveUtils.TAG, "Loading World");
                    world = PerspectiveAndroidUtils.getWorld(getAssets(), worldName);
                    Log.d(PerspectiveUtils.TAG, "Creating Scene");
                    glScene = new GLScene();
                    perspective = new Perspective(GameActivity.this, glScene, world.getSize());
                    perspective.rotationNode = PerspectiveAndroidUtils.createBasicSceneGraph(glScene, world);
                    perspective.outlineEnabled = outlineEnabled;
                    float[] background = PerspectiveUtils.BLACK;
                    String colour = world.getBackgroundColour();
                    if (colour != null && !colour.isEmpty()) {
                        background = glScene.getFloatArray(colour);
                    }
                    glScene.putFloatArray(GLScene.BACKGROUND, background);
                    Log.d(PerspectiveUtils.TAG, "Loading Puzzle");
                    loadPuzzle();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (gameView != null) {
                                gameView.quit();
                            }
                            Log.d(PerspectiveUtils.TAG, "Creating View");
                            gameView = new GameView(GameActivity.this, glScene, perspective);
                            setContentView(gameView);
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
    protected void onDestroy() {
        super.onDestroy();
        if (gameView != null) {
            gameView.quit();
        }
        if (manager != null) {
            manager.destroy();
        }
    }

    private void vibrate(long[] pattern) {
        if (vibrator == null || !vibrator.hasVibrator()) {
            Log.d(PerspectiveUtils.TAG, "No vibrator");
        } else if (preferences.getBoolean(getString(R.string.preference_puzzle_vibration_key), true)) {
            vibrator.vibrate(pattern, -1);
        }
    }

    private void loadPuzzle() {
        final Puzzle puzzle = PerspectiveAndroidUtils.getPuzzle(world, puzzleIndex);
        if (puzzle != null) {
            perspective.importPuzzle(puzzle);
            final String name = PerspectiveAndroidUtils.capitalize(world.getName()) + " - " + puzzleIndex;
            float[] array = PerspectiveUtils.WHITE;
            String colour = world.getForegroundColour();
            if (colour != null && !colour.isEmpty()) {
                array = glScene.getFloatArray(colour);
            }
            int red = (int) (array[0] * 255f);
            int green = (int) (array[1] * 255f);
            int blue = (int) (array[2] * 255f);
            final int foreground = 0xff << 24 | (red & 0xff) << 16 | (green & 0xff) << 8 | (blue & 0xff);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LinearLayout view = (LinearLayout) getLayoutInflater().inflate(R.layout.puzzle_toast, null);
                    TextView nameText = view.findViewById(R.id.puzzle_toast_name);
                    nameText.setText(name);
                    nameText.setTextColor(foreground);
                    final String description = puzzle.getDescription();
                    if (description != null && !description.isEmpty()) {
                        TextView descriptionText = view.findViewById(R.id.puzzle_toast_description);
                        descriptionText.setText(description);
                        descriptionText.setTextColor(foreground);
                        descriptionText.setVisibility(View.VISIBLE);
                    }
                    // TODO maybe Toast isn't the right tool for the job
                    Toast toast = new Toast(GameActivity.this);
                    // TODO increase duration
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(view);
                    toast.show();
                    // TODO if toast is still showing when game is over, call toast.cancel();
                }
            });
        }
    }

    @Override
    public SceneGraphNode getSceneGraphNode(String program, String name, String type, String mesh) {
        try {
            return PerspectiveAndroidUtils.getSceneGraphNode(glScene, getAssets(), program, name, type, mesh);
        } catch (IOException e) {
            CommonAndroidUtils.showErrorDialog(this, R.style.ErrorDialogTheme, R.string.error_get_scene_graph_node, e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AttributeNode getAttributeNode(String program, String name, String type, String colour) {
        return PerspectiveAndroidUtils.getAttributeNode(program, type, colour);
    }

    @Override
    public void onDropComplete() {
        Log.d(PerspectiveUtils.TAG, "Drop Complete");
        vibrate(DROP_VIBRATION);
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
                        perspective.clearAllLocations();
                        loadPuzzle();
                        dialog.cancel();
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
        final int stars = PerspectiveAndroidUtils.scoreToStars(score, target);
        Log.d(PerspectiveUtils.TAG, "Stars: " + stars);
        new Thread() {
            @Override
            public void run() {
                try {
                    PerspectiveAndroidUtils.saveSolution(GameActivity.this, worldName, puzzleIndex, solution);
                } catch (IOException e) {
                    CommonAndroidUtils.showErrorDialog(GameActivity.this, R.style.ErrorDialogTheme, R.string.error_save_solution, e);
                    e.printStackTrace();
                }
            }
        }.start();

        if (PerspectiveAndroidUtils.isTutorial(worldName)) {
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
                            perspective.mainRotation.makeIdentity();
                            puzzleIndex++;
                            loadPuzzle();
                        }
                    });
                } else {
                    final String nextWorld = getNextWorld();
                    if (nextWorld != null) {
                        builder.setPositiveButton(R.string.puzzle_next, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                worldName = nextWorld;
                                puzzleIndex = 1;
                                init();
                            }
                        });
                    }
                }
                if (stars < 5) {
                    builder.setNegativeButton(R.string.puzzle_retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            perspective.clearAllLocations();
                            loadPuzzle();
                            dialog.cancel();
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

    private String getNextWorld() {
        switch (worldName) {
            case PerspectiveAndroidUtils.WORLD_TUTORIAL:
                return PerspectiveAndroidUtils.WORLD_ONE;
            case PerspectiveAndroidUtils.WORLD_ONE:
                return PerspectiveAndroidUtils.WORLD_TWO;
            case PerspectiveAndroidUtils.WORLD_TWO:
                return PerspectiveAndroidUtils.WORLD_THREE;
            case PerspectiveAndroidUtils.WORLD_THREE:
                return PerspectiveAndroidUtils.WORLD_FOUR;
            case PerspectiveAndroidUtils.WORLD_FOUR:
                return PerspectiveAndroidUtils.WORLD_FIVE;
            case PerspectiveAndroidUtils.WORLD_FIVE:
                return PerspectiveAndroidUtils.WORLD_SIX;
            case PerspectiveAndroidUtils.WORLD_SIX:
                if (manager.hasPurchased(PerspectiveAndroidUtils.WORLD_SEVEN)) {
                    return PerspectiveAndroidUtils.WORLD_SEVEN;
                } // else fallthrough
            case PerspectiveAndroidUtils.WORLD_SEVEN:
                if (manager.hasPurchased(PerspectiveAndroidUtils.WORLD_EIGHT)) {
                    return PerspectiveAndroidUtils.WORLD_EIGHT;
                } // else fallthrough
            case PerspectiveAndroidUtils.WORLD_EIGHT:
                if (manager.hasPurchased(PerspectiveAndroidUtils.WORLD_NINE)) {
                    return PerspectiveAndroidUtils.WORLD_NINE;
                } // else fallthrough
            case PerspectiveAndroidUtils.WORLD_NINE:
                if (manager.hasPurchased(PerspectiveAndroidUtils.WORLD_TEN)) {
                    return PerspectiveAndroidUtils.WORLD_TEN;
                } // else fallthrough
            case PerspectiveAndroidUtils.WORLD_TEN:
                if (manager.hasPurchased(PerspectiveAndroidUtils.WORLD_ELEVEN)) {
                    return PerspectiveAndroidUtils.WORLD_ELEVEN;
                } // else fallthrough
            case PerspectiveAndroidUtils.WORLD_ELEVEN:
                if (manager.hasPurchased(PerspectiveAndroidUtils.WORLD_TWELVE)) {
                    return PerspectiveAndroidUtils.WORLD_TWELVE;
                } // else fallthrough
            default:
                return null;
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

    @Override
    public void onTokenConsumed(String purchaseToken) {
        Log.d(PerspectiveUtils.TAG, "Token Consumed: " + purchaseToken);
        // TODO
    }
}
