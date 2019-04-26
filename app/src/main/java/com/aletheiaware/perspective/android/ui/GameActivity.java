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
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.aletheiaware.bc.android.ui.AccessActivity;
import com.aletheiaware.bc.android.utils.BCAndroidUtils;
import com.aletheiaware.joy.android.scene.GLScene;
import com.aletheiaware.joy.android.scene.GLUtils;
import com.aletheiaware.joy.scene.MatrixTransformationNode;
import com.aletheiaware.joy.scene.RotationGesture;
import com.aletheiaware.perspective.PerspectiveProto.Puzzle;
import com.aletheiaware.perspective.PerspectiveProto.World;
import com.aletheiaware.perspective.android.PerspectiveAndroid;
import com.aletheiaware.perspective.android.R;
import com.aletheiaware.perspective.android.utils.PerspectiveAndroidUtils;
import com.aletheiaware.perspective.utils.PerspectiveUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyPair;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

public class GameActivity extends AppCompatActivity {

    private byte[] worldId;
    private int puzzleIndex;
    private RotationGesture gesture;
    private GLSurfaceView gameView;
    private GLScene scene;
    private PerspectiveAndroid perspective;
    private World world;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        final Intent intent = getIntent();
        if (intent != null) {
            Bundle data = intent.getExtras();
            if (data != null) {
                worldId = data.getByteArray(PerspectiveAndroidUtils.WORLD_EXTRA);
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

        gameView = new GLSurfaceView(this) {
            @Override
            public boolean performClick() {
                return super.performClick();
            }
        };
        gameView.setEGLContextClientVersion(2);
        gameView.setEGLConfigChooser(new GLSurfaceView.EGLConfigChooser() {
            @Override
            public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
                int[] attributes = {
                        EGL10.EGL_LEVEL, 0,
                        EGL10.EGL_RENDERABLE_TYPE, 4,  // EGL_OPENGL_ES2_BIT
                        EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
                        EGL10.EGL_RED_SIZE, 8,
                        EGL10.EGL_GREEN_SIZE, 8,
                        EGL10.EGL_BLUE_SIZE, 8,
                        EGL10.EGL_DEPTH_SIZE, 16,
                        // TODO EGL10.EGL_SAMPLE_BUFFERS, 0,
                        // TODO EGL10.EGL_SAMPLES, 4,  // This is for 4x MSAA.
                        EGL10.EGL_NONE
                };
                EGLConfig[] configs = new EGLConfig[1];
                int[] configCounts = new int[1];
                egl.eglChooseConfig(display, attributes, configs, 1, configCounts);

                if (configCounts[0] == 0) {
                    Log.e(PerspectiveUtils.TAG, "Unable to choose EGL config");
                    GLUtils.checkError("gameView.setEGLConfigChooser");
                    return null;
                } else {
                    for (int i = 0; i < configCounts[0]; i++) {
                        int[] result = new int[1];
                        if (egl.eglGetConfigAttrib(display, configs[i], EGL10.EGL_SAMPLE_BUFFERS, result)) {
                            Log.d(PerspectiveUtils.TAG, "EGL_SAMPLE_BUFFERS:" + result[0]);
                        } else {
                            GLUtils.checkError("gameView.setEGLConfigChooser");
                        }
                    }
                    return configs[0];
                }
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
        if (BCAndroidUtils.isInitialized()) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        String alias = BCAndroidUtils.getAlias();
                        KeyPair keys = BCAndroidUtils.getKeyPair();
                        File cache = getCacheDir();
                        InetAddress host = PerspectiveAndroidUtils.getPerspectiveHost();
                        String wId = Base64.encodeToString(worldId, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
                        Log.d(PerspectiveUtils.TAG, "Loading World");
                        World w = PerspectiveAndroidUtils.getWorld(alias, keys, cache, host, worldId);
                        Puzzle p = PerspectiveAndroidUtils.getPuzzle(alias, keys, cache, host, wId, world, puzzleIndex);
                        Log.d(PerspectiveUtils.TAG, "Creating Scene");
                        GLScene s = PerspectiveAndroidUtils.createScene(alias, keys, cache, host, wId);
                        PerspectiveAndroid pa = new PerspectiveAndroid(s, w.getSize()) {
                            @Override
                            public void onDropComplete() {
                                // TODO
                            }

                            @Override
                            public void onTurnComplete() {
                                // TODO
                            }

                            @Override
                            public void onGameLost() {
                                // TODO
                            }

                            @Override
                            public void onGameWon() {
                                // TODO
                            }
                        };
                        pa.basicRotation = PerspectiveAndroidUtils.createBasicSceneGraph(alias, keys, cache, host, scene, wId, w);
                        pa.lineRotation = PerspectiveAndroidUtils.createLineSceneGraph(alias, keys, cache, host, scene, wId, w);
                        if (p != null) {
                            pa.importPuzzle(p);
                        }
                        init(w, s, pa);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } else {
            Intent intent = new Intent(this, AccessActivity.class);
            startActivityForResult(intent, PerspectiveAndroidUtils.ACCESS_ACTIVITY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case PerspectiveAndroidUtils.ACCESS_ACTIVITY:
                switch (resultCode) {
                    case RESULT_OK:
                        // Do nothing
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
                // Do nothing
                break;
            default:
                super.onActivityResult(requestCode, resultCode, intent);
                break;
        }
    }

    private void init(World world, final GLScene scene, final PerspectiveAndroid perspective) {
        this.world = world;
        this.scene = scene;
        this.perspective = perspective;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gameView.setRenderer(scene);
                setContentView(gameView);
            }
        });
    }
}
