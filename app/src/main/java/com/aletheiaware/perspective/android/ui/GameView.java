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
import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.aletheiaware.joy.android.scene.GLScene;
import com.aletheiaware.joy.scene.RotationGesture;
import com.aletheiaware.perspective.Perspective;
import com.aletheiaware.perspective.utils.PerspectiveUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

@SuppressLint("ViewConstructor")
public class GameView extends GLSurfaceView implements OnTouchListener, GLSurfaceView.Renderer {

    private final GLScene scene;
    private final Perspective perspective;
    private final HandlerThread handlerThread;
    private final Handler handler;
    private RotationGesture gesture;

    public GameView(Activity activity, final GLScene scene, Perspective perspective) {
        super(activity);
        this.scene = scene;
        this.perspective = perspective;
        setEGLContextClientVersion(2);
        setEGLConfigChooser(new AntiAliasConfigChooser());
        setOnTouchListener(this);
        setRenderer(this);

        handlerThread = new HandlerThread("MotionEventHandlerThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        scene.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gesture = new RotationGesture(Math.min(width, height)) {
            @Override
            public void rotate(float radX, float radY) {
                perspective.rotate(radX, radY);
            }
        };
        scene.onSurfaceChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        scene.onDrawFrame(gl);
    }

    @Override
    public boolean onTouch(View v, final MotionEvent e) {
        if (perspective.isGameOver()) {
            Log.d(PerspectiveUtils.TAG, "Game Over, ignoring: " + e);
        } else {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            gesture.start((int) e.getX(), (int) e.getY());
                        }
                    });
                    return true;
                case MotionEvent.ACTION_MOVE:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            gesture.move((int) e.getX(), (int) e.getY());
                        }
                    });
                    return true;
                case MotionEvent.ACTION_UP:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (gesture.hasRotated()) {
                                perspective.rotateToAxis();
                            } else {
                                perspective.drop();
                            }
                        }
                    });
                    return true;
            }
        }
        return false;
    }

    public void quit() {
        handlerThread.quit();
    }
}
