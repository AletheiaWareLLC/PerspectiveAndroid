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
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.aletheiaware.common.android.utils.CommonAndroidUtils;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PerspectiveAndroidUtils {

    public static final String PUZZLE_EXTRA = "puzzle";
    public static final String WORLD_EXTRA = "world";
    public static final String TUTORIAL_WORLD = "tutorial";
    public static final String GROUND_ZERO_WORLD = "ground-0";
    public static final String ALPHA_ONE_WORLD = "alpha-1";
    public static final String PORTAL_TWO_WORLD = "portal-2";
    public static final String SEA_THREE_WORLD = "sea-3";
    public static final String HIGH_FIVE_WORLD = "high-5";
    public static final String MAGIC_EIGHT_WORLD = "magic-8";
    public static final String CLOUD_NINE_WORLD = "cloud-9";

    private PerspectiveAndroidUtils() {
    }

    public static World getWorld(AssetManager assets, String world) throws IOException {
        return PerspectiveUtils.readWorld(assets.open("world/" + world + ".pb"));
    }

    public static GLScene createScene(AssetManager assets, String world) throws IOException {
        final GLScene scene = new GLScene();
        List<String> meshes = new ArrayList<>();
        meshes.add("goal");
        meshes.add("outline");
        meshes.add("sphere");
        switch (world) {
            case TUTORIAL_WORLD:
                meshes.add("cube");
                break;
            case PORTAL_TWO_WORLD:
                meshes.add("portal");
                // Fall Through
            case GROUND_ZERO_WORLD:
            case ALPHA_ONE_WORLD:
            case SEA_THREE_WORLD:
            case HIGH_FIVE_WORLD:
            case MAGIC_EIGHT_WORLD:
                meshes.add("block");
                break;
            case CLOUD_NINE_WORLD:
                meshes.add("cloud");
                break;
        }
        for (String name : meshes) {
            new MeshLoader(assets.open("mesh/" + name + ".pb")) {
                @Override
                public void onMesh(Mesh mesh) throws IOException {
                    System.out.println("Name: " + mesh.getName());
                    scene.putVertexNormalMesh(mesh.getName(), new GLVertexNormalMesh(mesh));
                }
            }.start();
        }

        return scene;
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

    public static Puzzle getPuzzle(World world, int puzzleIndex) {
        if (puzzleIndex < world.getPuzzleCount()) {
            return world.getPuzzle(puzzleIndex);
        }
        return null;
    }

    public static SceneGraphNode getSceneGraphNode(String program, String name, String type, String mesh) {
        switch (type) {
            case "outline":
            case "block":
            case "goal":
            case "portal":
            case "sphere":
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
    public static void saveSolution(Context context, String world, int puzzle, Solution solution) throws IOException {
        File directory = new File(new File(context.getFilesDir(), "solutions"), world);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Could not create directory: " + directory.getAbsolutePath());
            }
        }
        File file = new File(directory, puzzle + ".pb");
        try (FileOutputStream out = new FileOutputStream(file)) {
            solution.writeDelimitedTo(out);
        }
    }

    @WorkerThread
    public static Solution loadSolution(Context context, String world, int puzzle) throws IOException {
        File directory = new File(new File(context.getFilesDir(), "solutions"), world);
        if (!directory.exists()) {
            return null;
        }
        File file = new File(directory, puzzle + ".pb");
        if (!file.exists()) {
            return null;
        }
        try (FileInputStream in = new FileInputStream(file)) {
            return Solution.parseDelimitedFrom(in);
        }
    }

    @WorkerThread
    public static void clearSolutions(Context context) throws IOException {
        File directory = new File(context.getFilesDir(), "solutions");
        if (!CommonAndroidUtils.recursiveDelete(directory)) {
            throw new IOException("Could not delete directory: " + directory.getAbsolutePath());
        }
    }
}