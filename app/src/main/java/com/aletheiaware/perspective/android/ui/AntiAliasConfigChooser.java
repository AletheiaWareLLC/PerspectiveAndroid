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

import android.opengl.GLSurfaceView;
import android.util.Log;

import com.aletheiaware.joy.android.scene.GLUtils;
import com.aletheiaware.perspective.utils.PerspectiveUtils;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

class AntiAliasConfigChooser implements GLSurfaceView.EGLConfigChooser {
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
                GLUtils.checkError("AntiAliasConfigChooser.chooseConfig");
            } else {
                for (int i = 0; i < configCounts[0]; i++) {
                    int[] result = new int[1];
                    if (egl.eglGetConfigAttrib(display, configs[i], EGL10.EGL_SAMPLE_BUFFERS, result)) {
                        Log.d(PerspectiveUtils.TAG, "EGL_SAMPLE_BUFFERS:" + result[0]);
                    } else {
                        GLUtils.checkError("AntiAliasConfigChooser.chooseConfig");
                    }
                    if (egl.eglGetConfigAttrib(display, configs[i], EGL10.EGL_SAMPLES, result)) {
                        Log.d(PerspectiveUtils.TAG, "EGL_SAMPLES:" + result[0]);
                    } else {
                        GLUtils.checkError("AntiAliasConfigChooser.chooseConfig");
                    }
                }
                return configs[0];
            }
        }
        return null;
    }
}
