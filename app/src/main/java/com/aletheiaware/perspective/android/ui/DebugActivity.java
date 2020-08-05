/*
 * Copyright 2020 Aletheia Ware LLC
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

import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

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
import com.aletheiaware.joy.scene.Animation;
import com.aletheiaware.joy.scene.AttributeNode;
import com.aletheiaware.joy.scene.Matrix;
import com.aletheiaware.joy.scene.MatrixTransformationNode;
import com.aletheiaware.joy.scene.MeshLoader;
import com.aletheiaware.joy.scene.Vector;
import com.aletheiaware.perspective.android.R;
import com.aletheiaware.perspective.utils.PerspectiveUtils;

import java.io.IOException;

public class DebugActivity extends AppCompatActivity {

    private final float[] cameraFrustum = new float[2];
    private final float[] light = new float[4];
    private final Matrix model = new Matrix();
    private final Matrix view = new Matrix();
    private final Matrix projection = new Matrix();
    private final Matrix mv = new Matrix();
    private final Matrix mvp = new Matrix();
    private final Matrix mainRotation = new Matrix();
    private final Matrix inverseRotation = new Matrix();
    private final Matrix tempRotation = new Matrix();
    private final Vector cameraEye = new Vector();
    private final Vector cameraLookAt = new Vector();
    private final Vector cameraUp = new Vector();
    private final String program = "debug";

    private String meshName;
    private DebugView debugView;
    private GLScene scene;
    private MatrixTransformationNode rotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        Spinner meshSpinner = findViewById(R.id.debug_mesh_spinner);
        final ArrayAdapter<CharSequence> meshAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        meshAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        meshSpinner.setAdapter(meshAdapter);
        meshSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                meshName = meshAdapter.getItem(position) + "";
                updateMesh();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO
            }
        });

        debugView = findViewById(R.id.debug_view);
        scene = new GLScene();
        debugView.setScene(scene);

        new Thread() {
            @Override
            public void run() {
                final AssetManager assets = getAssets();
                try {
                    String[] meshes = assets.list("mesh/");
                    if (meshes != null) {
                        for (String m : meshes) {
                            System.out.println("Mesh Name: " + m);
                            meshAdapter.add(m);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                float size = 3;
                float distance = (size * size) / 2f;
                System.out.println("Distance: " + distance);

                // Colours
                for (int i = 0; i < PerspectiveUtils.COLOUR_NAMES.length; i++) {
                    scene.putFloatArray(PerspectiveUtils.COLOUR_NAMES[i], PerspectiveUtils.COLOURS[i]);
                }
                scene.putFloatArray(GLScene.BACKGROUND, PerspectiveUtils.BLACK);
                // Light
                // Ensure light is always outside
                light[0] = 0;
                light[1] = 0;
                light[2] = size / 2f;
                light[3] = 1.0f;
                scene.putFloatArray("light", light);
                // MVP
                scene.putMatrix("model", model.makeIdentity());
                scene.putMatrix("view", view.makeIdentity());
                scene.putMatrix("projection", projection.makeIdentity());
                scene.putMatrix("model-view", mv.makeIdentity());
                scene.putMatrix("model-view-projection", mvp.makeIdentity());
                // Rotation
                scene.putMatrix("main-rotation", mainRotation.makeIdentity());
                scene.putMatrix("inverse-rotation", inverseRotation.makeIdentity());
                scene.putMatrix("temp-rotation", tempRotation.makeIdentity());
                // Camera
                // Ensure camera is always outside
                cameraEye.set(0.0f, 0.0f, distance);
                // Looking at the center
                cameraLookAt.set(0.0f, 0.0f, 0.0f);
                // Head pointing up Y axis
                cameraUp.set(0.0f, 1.0f, 0.0f);
                scene.putVector("camera-eye", cameraEye);
                scene.putVector("camera-look-at", cameraLookAt);
                scene.putVector("camera-up", cameraUp);
                // Frustum
                // Crop the scene proportionally
                cameraFrustum[0] = size * 0.5f;
                cameraFrustum[1] = distance + size;
                scene.putFloatArray("camera-frustum", cameraFrustum);
                // Viewport
                scene.putIntArray("camera-viewport", scene.getViewport());

                GLProgram debugProgram = new GLProgram(Shader.newBuilder()
                        .setName(program)
                        .setVertexSource("#if __VERSION__ >= 130\n" +
                                "  #define attribute in\n" +
                                "  #define varying out\n" +
                                "#endif\n" +
                                "\n" +
                                "uniform mat4 u_MVMatrix;\n" +
                                "uniform mat4 u_MVPMatrix;\n" +
                                "attribute vec4 a_Position;\n" +
                                "attribute vec3 a_Normal;\n" +
                                "varying vec3 v_Position;\n" +
                                "varying vec3 v_Normal;\n" +
                                "\n" +
                                "void main() {\n" +
                                "    v_Position = vec3(u_MVMatrix * a_Position);\n" +
                                "    vec3 norm = vec3(u_MVMatrix * vec4(a_Normal, 0.0));\n" +
                                "    v_Normal = norm / length(norm);\n" +
                                "    gl_Position = u_MVPMatrix * a_Position;\n" +
                                "}")
                        .setFragmentSource("#if __VERSION__ >= 130\n" +
                                "  #define varying in\n" +
                                "  out vec4 mgl_FragColour;\n" +
                                "#else\n" +
                                "  #define mgl_FragColour gl_FragColor\n" +
                                "#endif\n" +
                                "\n" +
                                "#ifdef GL_ES\n" +
                                "  #define MEDIUMP mediump\n" +
                                "  precision MEDIUMP float;\n" +
                                "#else\n" +
                                "  #define MEDIUMP\n" +
                                "#endif\n" +
                                "\n" +
                                "uniform MEDIUMP vec3 u_LightPos;\n" +
                                "uniform MEDIUMP vec4 u_Colour;\n" +
                                "varying MEDIUMP vec3 v_Position;\n" +
                                "varying MEDIUMP vec3 v_Normal;\n" +
                                "\n" +
                                "void main() {\n" +
                                "    vec3 diff = u_LightPos - v_Position;\n" +
                                "    vec3 lightVector = normalize(diff);\n" +
                                "    float distance = length(diff);\n" +
                                "    float diffuse = max(dot(v_Normal, lightVector), 0.0);\n" +
                                "    diffuse = diffuse * (1.0 / (1.0 + (0.10 * distance)));\n" +
                                "    diffuse = diffuse + 0.3;\n" +
                                "    mgl_FragColour = u_Colour * diffuse;\n" +
                                "    mgl_FragColour.a = 1.0;\n" +
                                "}")
                        .addAttributes("a_Position")
                        .addAttributes("a_Normal")
                        .addUniforms("u_LightPos")
                        .addUniforms("u_MVMatrix")
                        .addUniforms("u_MVPMatrix")
                        .addUniforms("u_Colour")
                        .build());

                GLProgramNode programNode = new GLProgramNode(debugProgram);
                scene.putProgramNode(program, programNode);

                GLLightNode light = new GLLightNode(program, "light");
                programNode.addChild(light);

                GLCameraNode camera = new GLCameraNode(program);
                light.addChild(camera);

                rotation = new MatrixTransformationNode("main-rotation");
                rotation.setAnimation(new Animation() {
                    @Override
                    public boolean tick() {
                        tempRotation.makeRotationAxis(0.01f, new Vector(1, 1, 1));
                        mainRotation.makeMultiplication(mainRotation, tempRotation);
                        return false;
                    }
                });
                camera.addChild(rotation);
            }
        }.start();
    }

    private void updateMesh() {
        rotation.clear();
        GLColourAttribute colourAttribute = new GLColourAttribute(program, PerspectiveUtils.DEFAULT_FG_COLOUR);
        AttributeNode attributeNode = new AttributeNode(colourAttribute);
        rotation.addChild(attributeNode);

        if (meshName == null || meshName.equals("")) {
            System.out.println("No mesh name");
        } else {
            GLVertexNormalMeshNode meshNode = new GLVertexNormalMeshNode(program, meshName);
            attributeNode.addChild(meshNode);

            if (scene.getVertexNormalMesh(meshName) == null) {
                try {
                    new MeshLoader(getAssets().open("mesh/" + meshName)) {
                        @Override
                        public void onMesh(Mesh mesh) throws IOException {
                            System.out.println("Loaded Mesh: " + mesh.getName());
                            scene.putVertexNormalMesh(meshName, new GLVertexNormalMesh(mesh));
                        }
                    }.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
