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

package com.aletheiaware.perspective.android.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.util.Log;

import com.aletheiaware.common.android.utils.CommonAndroidUtils;
import com.aletheiaware.common.utils.CommonUtils;
import com.aletheiaware.joy.JoyProto.Mesh;
import com.aletheiaware.joy.JoyProto.Shader;
import com.aletheiaware.joy.android.scene.GLCameraNode;
import com.aletheiaware.joy.android.scene.GLColourAttribute;
import com.aletheiaware.joy.android.scene.GLLightNode;
import com.aletheiaware.joy.android.scene.GLProgram;
import com.aletheiaware.joy.android.scene.GLProgramNode;
import com.aletheiaware.joy.android.scene.GLScene;
import com.aletheiaware.joy.android.scene.GLVertexNormalMesh;
import com.aletheiaware.joy.android.scene.GLVertexNormalMeshNode;
import com.aletheiaware.joy.scene.AttributeNode;
import com.aletheiaware.joy.scene.MatrixTransformationNode;
import com.aletheiaware.joy.scene.MeshLoader;
import com.aletheiaware.joy.scene.Scene;
import com.aletheiaware.joy.scene.SceneGraphNode;
import com.aletheiaware.perspective.PerspectiveProto.Puzzle;
import com.aletheiaware.perspective.PerspectiveProto.Solution;
import com.aletheiaware.perspective.PerspectiveProto.World;
import com.aletheiaware.perspective.utils.PerspectiveUtils;

import java.io.IOException;

import androidx.annotation.WorkerThread;

public class PerspectiveAndroidUtils {

    public static final String ORIENTATION_EXTRA = "orientation";
    public static final String OUTLINE_EXTRA = "outline";
    public static final String PUZZLE_EXTRA = "puzzle";
    public static final String WORLD_EXTRA = "world";

    private PerspectiveAndroidUtils() {
    }

    public static World getWorld(AssetManager assets, String world) throws IOException {
        return PerspectiveUtils.readWorld(assets.open("world/" + world + ".pb"));
    }

    public static MatrixTransformationNode createBasicSceneGraph(GLScene scene, World world) {
        if (!world.containsShader("basic")) {
            Log.d(PerspectiveUtils.TAG, "Missing basic shader");
        }
        Shader shader = world.getShaderOrThrow("basic");
        GLProgram basicProgram = new GLProgram(shader);

        GLProgramNode basicNode = new GLProgramNode(basicProgram);
        scene.putProgramNode("basic", basicNode);

        GLLightNode light = new GLLightNode("basic", "light");
        basicNode.addChild(light);

        GLCameraNode camera = new GLCameraNode();
        light.addChild(camera);

        MatrixTransformationNode rotation = new MatrixTransformationNode("main-rotation");
        camera.addChild(rotation);

        return rotation;
    }

    public static SceneGraphNode getSceneGraphNode(final GLScene scene, AssetManager assets, String program, String name, String type, String mesh) throws IOException {
        switch (type) {
            case "outline":
            case "block":
            case "goal":
            case "portal":
            case "sphere":
                if (scene.getVertexNormalMesh(mesh) == null) {
                    new MeshLoader(assets.open("mesh/" + mesh + ".pb")) {
                        @Override
                        public void onMesh(Mesh mesh) throws IOException {
                            System.out.println("Name: " + mesh.getName());
                            scene.putVertexNormalMesh(mesh.getName(), new GLVertexNormalMesh(mesh));
                        }
                    }.start();
                }
                return new GLVertexNormalMeshNode(program, mesh);
            default:
                System.err.println("Unrecognized: " + program + " " + name + " " + type + " " + mesh);
        }
        return null;
    }

    public static AttributeNode getAttributeNode(String program, String type, String colour) {
        if (type.equals("outline") && colour.equals("multi-colour")) {
            return new AttributeNode(new GLColourAttribute(program, colour) {
                private final float[] hsv = {0.0f, 0.9f, 0.5f};
                private final float[] rgba = {1.0f, 1.0f, 1.0f, 1.0f};

                @Override
                public float[] getColour(Scene scene) {
                    hsv[0] = (hsv[0] + 0.1f) % 360;
                    //System.out.println(System.currentTimeMillis() + " : " + hsv[0]);
                    int colour = Color.HSVToColor(hsv);
                    rgba[0] = Color.red(colour);
                    rgba[1] = Color.green(colour);
                    rgba[2] = Color.blue(colour);
                    rgba[3] = 1.0f;
                    //System.out.println(java.util.Arrays.toString(rgba));
                    return rgba;
                }
            });
        }
        return new AttributeNode(new GLColourAttribute(program, colour));
    }

    @WorkerThread
    public static void saveSolution(Context context, String world, String puzzle, Solution solution) throws IOException {
        PerspectiveUtils.saveSolution(context.getFilesDir(), world, puzzle, solution);
    }

    @WorkerThread
    public static Solution loadSolution(Context context, String world, String puzzle) throws IOException {
        return PerspectiveUtils.loadSolution(context.getFilesDir(), world, puzzle);
    }

    @WorkerThread
    public static void clearSolutions(Context context) throws IOException {
        PerspectiveUtils.clearSolutions(context.getFilesDir());
    }
}
