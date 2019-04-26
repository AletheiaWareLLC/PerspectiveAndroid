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

import android.graphics.Color;
import android.support.annotation.WorkerThread;

import com.aletheiaware.bc.BC.Channel;
import com.aletheiaware.bc.android.utils.BCAndroidUtils;
import com.aletheiaware.joy.JoyProto.Mesh;
import com.aletheiaware.joy.JoyProto.Shader;
import com.aletheiaware.joy.android.scene.GLCameraNode;
import com.aletheiaware.joy.android.scene.GLColourAttribute;
import com.aletheiaware.joy.android.scene.GLLightNode;
import com.aletheiaware.joy.android.scene.GLProgram;
import com.aletheiaware.joy.android.scene.GLProgramNode;
import com.aletheiaware.joy.android.scene.GLScene;
import com.aletheiaware.joy.android.scene.GLVertexMesh;
import com.aletheiaware.joy.android.scene.GLVertexMeshNode;
import com.aletheiaware.joy.android.scene.GLVertexNormalMesh;
import com.aletheiaware.joy.android.scene.GLVertexNormalMeshNode;
import com.aletheiaware.joy.scene.AttributeNode;
import com.aletheiaware.joy.scene.MatrixTransformationNode;
import com.aletheiaware.joy.scene.MeshLoader;
import com.aletheiaware.joy.scene.Scene;
import com.aletheiaware.joy.scene.SceneGraphNode;
import com.aletheiaware.perspective.PerspectiveProto.Puzzle;
import com.aletheiaware.perspective.PerspectiveProto.World;
import com.aletheiaware.perspective.android.BuildConfig;
import com.aletheiaware.perspective.utils.PerspectiveUtils;
import com.google.protobuf.ByteString;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyPair;

public class PerspectiveAndroidUtils {

    public static final int ACCESS_ACTIVITY = BCAndroidUtils.ACCESS_ACTIVITY;
    public static final int ACCOUNT_ACTIVITY = BCAndroidUtils.ACCOUNT_ACTIVITY;
    public static final int GAME_ACTIVITY = 102;
    public static final int SETTINGS_ACTIVITY = 103;
    public static final String PUZZLE_EXTRA = "puzzle";
    public static final String WORLD_EXTRA = "world";

    private PerspectiveAndroidUtils() {}

    public static String getPerspectiveHostname() {
        return BuildConfig.DEBUG ? PerspectiveUtils.PERSPECTIVE_HOST_TEST : PerspectiveUtils.PERSPECTIVE_HOST;
    }

    @WorkerThread
    public static InetAddress getPerspectiveHost() {
        try {
            return InetAddress.getByName(getPerspectiveHostname());
        } catch (Exception e) {
            /* Ignored */
            e.printStackTrace();
        }
        return null;
    }

    public static String getPerspectiveWebsite() {
        return "https://" + getPerspectiveHostname();
    }

    public static GLScene createScene(String alias, KeyPair keys, File cache, InetAddress host, String worldId) {
        final GLScene scene = new GLScene();
        Channel meshes = PerspectiveUtils.getMeshesChannel(worldId, cache, host);
        // Vertex Meshes
        new MeshLoader(meshes, alias, keys, "box") {
            @Override
            public void onMesh(Mesh mesh) throws IOException {
                scene.putVertexMesh(mesh.getName(), new GLVertexMesh(mesh));
            }
        }.start();
        // Vertex Normal Meshes
        new MeshLoader(meshes, alias, keys, "block", "goal", "portal", "sphere") {
            @Override
            public void onMesh(Mesh mesh) throws IOException {
                scene.putVertexNormalMesh(mesh.getName(), new GLVertexNormalMesh(mesh));
            }
        }.start();

        return scene;
    }

    public static MatrixTransformationNode createBasicSceneGraph(String alias, KeyPair keys, File cache, InetAddress host, GLScene scene, String worldId, World world) throws IOException {
        ByteString hash = world.getShaderOrThrow("basic");
        Channel shaders = PerspectiveUtils.getShadersChannel(worldId, cache, host);
        Shader shader = PerspectiveUtils.getShader(shaders, alias, keys, hash.toByteArray());
        GLProgram basicProgram = new GLProgram(shader);

        GLProgramNode basicNode = new GLProgramNode(basicProgram);
        scene.putProgramNode("basic", basicNode);

        GLLightNode light = new GLLightNode("basic", "light");
        basicNode.addChild(light);

        GLCameraNode camera = new GLCameraNode();
        light.addChild(camera);

        MatrixTransformationNode basicRotation = new MatrixTransformationNode("main-rotation");
        camera.addChild(basicRotation);

        return basicRotation;
    }

    public static MatrixTransformationNode createLineSceneGraph(String alias, KeyPair keys, File cache, InetAddress host, GLScene scene, String worldId, World world) throws IOException {
        ByteString hash = world.getShaderOrThrow("line");
        Channel shaders = PerspectiveUtils.getShadersChannel(worldId, cache, host);
        Shader shader = PerspectiveUtils.getShader(shaders, alias, keys, hash.toByteArray());
        GLProgram lineProgram = new GLProgram(shader);

        GLProgramNode lineNode = new GLProgramNode(lineProgram);
        scene.putProgramNode("line", lineNode);

        MatrixTransformationNode lineRotation = new MatrixTransformationNode("main-rotation");
        lineNode.addChild(lineRotation);

        return lineRotation;
    }

    public static World getWorld(String alias, KeyPair keys, File cache, InetAddress host, byte[] worldId) throws IOException {
        Channel worlds = PerspectiveUtils.getWorldsChannel(cache, host);
        return PerspectiveUtils.getWorld(worlds, alias, keys, worldId);
    }

    public static Puzzle getPuzzle(String alias, KeyPair keys, File cache, InetAddress host, String worldId, World world, int puzzleIndex) throws IOException {
        if (puzzleIndex < world.getPuzzleCount()) {
            ByteString hash = world.getPuzzle(puzzleIndex);
            Channel puzzles = PerspectiveUtils.getPuzzlesChannel(worldId, cache, host);
            return PerspectiveUtils.getPuzzle(puzzles, alias, keys, hash.toByteArray());
        }
        return null;
    }

    public static SceneGraphNode getSceneGraphNode(String program, String name, String type) {
        switch (type) {
            case "box":
                return new GLVertexMeshNode(program, type);
            case "block":
            case "goal":
            case "portal":
            case "sphere":
                return new GLVertexNormalMeshNode(program, type);
            default:
                System.err.println("Unrecognized: " + program + " " + name + " " + type);
        }
        return null;
    }

    public static AttributeNode getAttributeNode(String program, String name, String type, String colour) {
        if (name.equals("outline") && type.equals("box") && colour.equals("multi-colour")) {
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
}
