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

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aletheiaware.common.android.utils.CommonAndroidUtils;
import com.aletheiaware.joy.android.scene.GLScene;
import com.aletheiaware.joy.android.scene.GLUtils;
import com.aletheiaware.joy.scene.RotationGesture;
import com.aletheiaware.perspective.Perspective;
import com.aletheiaware.perspective.PerspectiveProto.Puzzle;
import com.aletheiaware.perspective.PerspectiveProto.Solution;
import com.aletheiaware.perspective.PerspectiveProto.World;
import com.aletheiaware.perspective.android.PerspectiveGame;
import com.aletheiaware.perspective.android.R;
import com.aletheiaware.perspective.android.utils.PerspectiveAndroidUtils;
import com.aletheiaware.perspective.utils.PerspectiveUtils;

import java.io.IOException;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

public class GameActivity extends AppCompatActivity {

    private String worldName;
    private int puzzleIndex;
    private World world;
    private RotationGesture gesture;
    private GLSurfaceView gameView;
    private GLScene scene;
    private PerspectiveGame perspective;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        final Intent intent = getIntent();
        if (intent != null) {
            Bundle data = intent.getExtras();
            if (data != null) {
                worldName = data.getString(PerspectiveAndroidUtils.WORLD_EXTRA);
                puzzleIndex = data.getInt(PerspectiveAndroidUtils.PUZZLE_EXTRA);
            }
        }

        final DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final int width = metrics.widthPixels;
        final int height = metrics.heightPixels;
        gesture = new RotationGesture(Math.min(width, height)) {
            @Override
            public void rotate(float radX, float radY) {
                if (perspective != null) {
                    perspective.rotate(radX, radY);
                }
            }
        };

        gameView = new GLSurfaceView(this);
        gameView.setEGLContextClientVersion(2);
        gameView.setEGLConfigChooser(new GLSurfaceView.EGLConfigChooser() {
            @Override
            public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
                int[][] attributes = {
                        // 4xMSAA
                        {
                                EGL10.EGL_LEVEL, 0,
                                EGL10.EGL_RENDERABLE_TYPE, 4,
                                EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
                                EGL10.EGL_RED_SIZE, 8,
                                EGL10.EGL_GREEN_SIZE, 8,
                                EGL10.EGL_BLUE_SIZE, 8,
                                EGL10.EGL_DEPTH_SIZE, 16,
                                EGL10.EGL_SAMPLE_BUFFERS, 1,
                                EGL10.EGL_SAMPLES, 4,
                                EGL10.EGL_NONE
                        },
                        // 2xMSAA
                        {
                                EGL10.EGL_LEVEL, 0,
                                EGL10.EGL_RENDERABLE_TYPE, 4,
                                EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
                                EGL10.EGL_RED_SIZE, 8,
                                EGL10.EGL_GREEN_SIZE, 8,
                                EGL10.EGL_BLUE_SIZE, 8,
                                EGL10.EGL_DEPTH_SIZE, 16,
                                EGL10.EGL_SAMPLE_BUFFERS, 1,
                                EGL10.EGL_SAMPLES, 2,
                                EGL10.EGL_NONE
                        },
                        // No anti-aliasing
                        {
                                EGL10.EGL_LEVEL, 0,
                                EGL10.EGL_RENDERABLE_TYPE, 4,
                                EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
                                EGL10.EGL_RED_SIZE, 8,
                                EGL10.EGL_GREEN_SIZE, 8,
                                EGL10.EGL_BLUE_SIZE, 8,
                                EGL10.EGL_DEPTH_SIZE, 16,
                                EGL10.EGL_NONE
                        }
                };
                EGLConfig[] configs = new EGLConfig[1];
                int[] configCounts = new int[1];
                for (int[] attribute : attributes) {
                    egl.eglChooseConfig(display, attribute, configs, 1, configCounts);
                    if (configCounts[0] == 0) {
                        Log.e(PerspectiveUtils.TAG, "Unable to choose EGL config");
                        GLUtils.checkError("gameView.setEGLConfigChooser");
                    } else {
                        for (int i = 0; i < configCounts[0]; i++) {
                            int[] result = new int[1];
                            if (egl.eglGetConfigAttrib(display, configs[i], EGL10.EGL_SAMPLE_BUFFERS, result)) {
                                Log.d(PerspectiveUtils.TAG, "EGL_SAMPLE_BUFFERS:" + result[0]);
                            } else {
                                GLUtils.checkError("gameView.setEGLConfigChooser");
                            }
                            if (egl.eglGetConfigAttrib(display, configs[i], EGL10.EGL_SAMPLES, result)) {
                                Log.d(PerspectiveUtils.TAG, "EGL_SAMPLES:" + result[0]);
                            } else {
                                GLUtils.checkError("gameView.setEGLConfigChooser");
                            }
                        }
                        return configs[0];
                    }
                }
                return null;
            }
        });
        gameView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        gesture.start((int) e.getX(), (int) e.getY());
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        gesture.move((int) e.getX(), (int) e.getY());
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (perspective != null) {
                            if (gesture.hasRotated()) {
                                perspective.rotateToAxis();
                            } else {
                                perspective.drop();
                            }
                        }
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Log.d(PerspectiveUtils.TAG, "Loading World");
                    world = PerspectiveAndroidUtils.getWorld(getAssets(), worldName);
                    Log.d(PerspectiveUtils.TAG, "Creating Scene");
                    scene = PerspectiveAndroidUtils.createScene(getAssets(), worldName);
                    perspective = new PerspectiveGame(scene, world.getSize()) {
                        @Override
                        public void onDropComplete() {
                            Log.d(PerspectiveUtils.TAG, "Drop Complete");
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
                                    AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this, R.style.AlertDialogTheme);
                                    builder.setView(getLayoutInflater().inflate(R.layout.dialog_game_lost, null));
                                    builder.setPositiveButton(R.string.puzzle_retry, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            if (perspective != null) {
                                                perspective.reset();
                                            }
                                            dialog.cancel();
                                        }
                                    });
                                    builder.setNegativeButton(R.string.main_menu, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            setResult(RESULT_CANCELED);
                                            finish();
                                        }
                                    });
                                    builder.setCancelable(false);
                                    builder.create().show();
                                }
                            });
                        }

                        @Override
                        public void onGameWon() {
                            Log.d(PerspectiveUtils.TAG, "Game Won");
                            final Solution solution = perspective.getSolution();
                            final int target = puzzle.getTarget();
                            final int score = solution.getScore();
                            Log.d(PerspectiveUtils.TAG, "Score: " + score + " (" + target + ")");
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        PerspectiveAndroidUtils.saveSolution(GameActivity.this, worldName, puzzleIndex, solution);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();

                            if (PerspectiveAndroidUtils.TUTORIAL_WORLD.equals(worldName)) {
                                CommonAndroidUtils.setPreference(GameActivity.this, getString(R.string.preference_tutorial_completed), "true");
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this, R.style.AlertDialogTheme);
                                    View view = getLayoutInflater().inflate(R.layout.dialog_game_won, null);
                                    builder.setView(view);
                                    view.findViewById(R.id.game_won_star1).setVisibility((score <= target + 2) ? View.VISIBLE : View.GONE);
                                    view.findViewById(R.id.game_won_star2).setVisibility((score <= target + 1) ? View.VISIBLE : View.GONE);
                                    view.findViewById(R.id.game_won_star3).setVisibility((score <= target) ? View.VISIBLE : View.GONE);
                                    if (puzzleIndex + 1 < world.getPuzzleCount()) {
                                        builder.setPositiveButton(R.string.puzzle_next, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                                if (perspective != null) {
                                                    perspective.clearAllLocations();
                                                    perspective.mainRotation.makeIdentity();
                                                    puzzleIndex++;
                                                    loadPuzzle();
                                                }
                                            }
                                        });
                                    } else {
                                        String nextWorld = null;
                                        switch (worldName) {
                                            case PerspectiveAndroidUtils.TUTORIAL_WORLD:
                                                nextWorld = PerspectiveAndroidUtils.GROUND_ZERO_WORLD;
                                                break;
                                            case PerspectiveAndroidUtils.GROUND_ZERO_WORLD:
                                                nextWorld = PerspectiveAndroidUtils.ALPHA_ONE_WORLD;
                                                break;
                                            case PerspectiveAndroidUtils.ALPHA_ONE_WORLD:
                                                nextWorld = PerspectiveAndroidUtils.PORTAL_TWO_WORLD;
                                                break;
                                            case PerspectiveAndroidUtils.PORTAL_TWO_WORLD:
                                                nextWorld = PerspectiveAndroidUtils.SEA_THREE_WORLD;
                                                break;
                                            case PerspectiveAndroidUtils.SEA_THREE_WORLD:
                                                nextWorld = PerspectiveAndroidUtils.HIGH_FIVE_WORLD;
                                                break;
                                            case PerspectiveAndroidUtils.HIGH_FIVE_WORLD:
                                                nextWorld = PerspectiveAndroidUtils.MAGIC_EIGHT_WORLD;
                                                break;
                                            case PerspectiveAndroidUtils.MAGIC_EIGHT_WORLD:
                                                nextWorld = PerspectiveAndroidUtils.CLOUD_NINE_WORLD;
                                                break;

                                        }
                                        if (nextWorld != null) {
                                            final Intent intent = new Intent(GameActivity.this, GameActivity.class);
                                            intent.putExtra(PerspectiveAndroidUtils.WORLD_EXTRA, nextWorld);
                                            intent.putExtra(PerspectiveAndroidUtils.PUZZLE_EXTRA, 0);
                                            builder.setPositiveButton(R.string.puzzle_next, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                    finish();
                                                    startActivity(intent);
                                                }
                                            });
                                        }
                                    }
                                    builder.setNegativeButton(R.string.main_menu, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            setResult(RESULT_OK);
                                            finish();
                                        }
                                    });
                                    builder.setCancelable(false);
                                    builder.show();
                                }
                            });
                        }
                    };
                    perspective.rotationNode = PerspectiveAndroidUtils.createBasicSceneGraph(scene, world);
                    perspective.outlineEnabled = PreferenceManager.getDefaultSharedPreferences(GameActivity.this).getBoolean(getString(R.string.preference_puzzle_outline_key), true);
                    float[] background = PerspectiveUtils.BLACK;
                    String colour = world.getColour();
                    if (colour != null && !colour.isEmpty()) {
                        background = scene.getFloatArray(colour);
                    }
                    scene.putFloatArray(GLScene.BACKGROUND, background);
                    Log.d(PerspectiveUtils.TAG, "Loading Puzzle");
                    loadPuzzle();
                    Log.d(PerspectiveUtils.TAG, "Starting Renderer");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gameView.setRenderer(scene);
                            setContentView(gameView);
                        }
                    });
                } catch (IOException e) {
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

    private void loadPuzzle() {
        final Puzzle puzzle = PerspectiveAndroidUtils.getPuzzle(world, puzzleIndex);
        if (puzzle != null) {
            perspective.importPuzzle(puzzle);
            final String description = puzzle.getDescription();
            if (description != null && !description.isEmpty()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView view = (TextView) getLayoutInflater().inflate(R.layout.puzzle_description_toast, null);
                        view.setText(description);
                        Toast toast = new Toast(GameActivity.this);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(view);
                        toast.show();
                    }
                });
            }
        }
    }

    public GLScene getScene() {
        return scene;
    }

    public Perspective getPerspective() {
        return perspective;
    }
}
