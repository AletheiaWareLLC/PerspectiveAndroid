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

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
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
import com.aletheiaware.joy.android.scene.GLUtils;
import com.aletheiaware.joy.android.scene.GLVertexNormalMesh;
import com.aletheiaware.joy.android.scene.GLVertexNormalMeshNode;
import com.aletheiaware.joy.scene.Animation;
import com.aletheiaware.joy.scene.Attribute;
import com.aletheiaware.joy.scene.AttributeNode;
import com.aletheiaware.joy.scene.Matrix;
import com.aletheiaware.joy.scene.MatrixTransformationNode;
import com.aletheiaware.joy.scene.MeshLoader;
import com.aletheiaware.joy.scene.Vector;
import com.aletheiaware.perspective.android.R;
import com.aletheiaware.perspective.utils.PerspectiveUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

    private MediaPlayer mediaPlayer;
    private SoundPool soundPool;
    private Map<String, Integer> soundMap = new HashMap<>();
    private String musicName;
    private String soundName;
    private String meshName;
    private DebugView debugView;
    private GLScene scene;
    private MatrixTransformationNode rotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(attrs)
                .build();
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                System.out.println("SoundPool.onLoadComplete: " + sampleId + " " + status);
            }
        });

        Spinner musicSpinner = findViewById(R.id.debug_music_spinner);
        final ArrayAdapter<CharSequence> musicAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        musicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        musicSpinner.setAdapter(musicAdapter);
        musicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                musicName = musicAdapter.getItem(position) + "";
                updateMusic();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO
            }
        });

        Spinner soundSpinner = findViewById(R.id.debug_sound_spinner);
        final ArrayAdapter<CharSequence> soundAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        soundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        soundSpinner.setAdapter(soundAdapter);
        soundSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                soundName = soundAdapter.getItem(position) + "";
                updateSound();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO
            }
        });

        Spinner meshSpinner = findViewById(R.id.debug_mesh_spinner);
        final ArrayAdapter<CharSequence> meshAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        meshAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        meshSpinner.setAdapter(meshAdapter);
        meshSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                meshName = meshAdapter.getItem(position) + "";
                updateSight();
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
                    String[] musics = assets.list("music/");
                    if (musics != null) {
                        for (String s : musics) {
                            System.out.println("Music Name: " + s);
                            musicAdapter.add(s);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    String[] sounds = assets.list("sound/");
                    if (sounds != null) {
                        for (String s : sounds) {
                            System.out.println("Sound Name: " + s);
                            int id = soundPool.load(assets.openFd("sound/" + s), 1);
                            System.out.println("Sound ID: " + id);
                            soundMap.put(s, id);
                            soundAdapter.add(s);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        super.onDestroy();
    }

    private MediaPlayer createMediaPlayer() {
        MediaPlayer mp = new MediaPlayer();
        mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                System.out.println("onInfo: " + mp.toString() + " " + what + " " + extra);
                return false;
            }
        });
        mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                System.out.println("onBufferingUpdate: " + mp.toString() + " " + percent);
            }
        });
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                System.out.println("onCompletion: " + mp.toString());
            }
        });
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                System.out.println("onError: " + mp.toString() + " " + what + " " + extra);
                return false;
            }
        });
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                System.out.println("onPrepared: " + mp.toString());
            }
        });
        return mp;
    }

    private void updateMusic() {
        if (musicName == null || musicName.equals("")) {
            System.out.println("No music name");
        } else {
            System.out.println("Music Name: " + musicName);
            try {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
                AssetFileDescriptor afd = getAssets().openFd("music/" + musicName);
                mediaPlayer = createMediaPlayer();
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setLooping(true);
                afd.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateSound() {
        if (soundName == null || soundName.equals("")) {
            System.out.println("No sound name");
        } else {
            System.out.println("Sound Name: " + soundName);
            Integer id = soundMap.get(soundName);
            System.out.println("Sound ID: " + id);
            if (id != null) {
                int result = soundPool.play(id, 1, 1, 1, 0, 1);
                System.out.println("Playing Sound Result: " + result);
            }
        }
    }

    private void updateSight() {
        rotation.clear();
        GLColourAttribute colourAttribute = new GLColourAttribute(program, PerspectiveUtils.DEFAULT_FG_COLOUR);
        AttributeNode attributeNode = new AttributeNode(new Attribute[]{colourAttribute});
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
