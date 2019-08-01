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

package com.aletheiaware.perspective.android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.os.Environment;
import android.os.PowerManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.aletheiaware.joy.android.scene.GLScene;
import com.aletheiaware.perspective.Perspective;
import com.aletheiaware.perspective.android.ui.GameActivity;
import com.aletheiaware.perspective.android.utils.PerspectiveAndroidUtils;
import com.aletheiaware.perspective.utils.PerspectiveUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.concurrent.CountDownLatch;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class GameActivityInstrumentedTest {

    private IntentsTestRule<GameActivity> intentsTestRule = new IntentsTestRule<>(GameActivity.class, false, false);

    @Rule
    public RuleChain ruleChain = RuleChain.outerRule(GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            .around(intentsTestRule);

    private static PowerManager.WakeLock wakeLock;

    @BeforeClass
    public static void setUpClass() {
        PowerManager power = (PowerManager) InstrumentationRegistry.getTargetContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = power.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "GameActivityInstrumentedTest");
        wakeLock.acquire();
    }

    @AfterClass
    public static void tearDownClass() {
        wakeLock.release();
    }

    @Test
    public void screenshotLogo() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(PerspectiveAndroidUtils.WORLD_EXTRA, PerspectiveAndroidUtils.GROUND_ZERO_WORLD);
        intent.putExtra(PerspectiveAndroidUtils.PUZZLE_EXTRA, 6);
        GameActivity activity = intentsTestRule.launchActivity(intent);
        Perspective perspective = activity.getPerspective();
        perspective.rotate(0,(float) (Math.PI / 4));
        perspective.rotate((float) (Math.PI / 8),0);
        GLScene scene = activity.getScene();
        scene.putFloatArray(GLScene.BACKGROUND, PerspectiveUtils.LIGHT_BLUE);
        Thread.sleep(1000);
        captureScreenshot(scene, "com.aletheiaware.perspective.android.GameActivity-logo.png");
        activity.finish();
    }

    @Test
    public void screenshotTutorial0() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.TUTORIAL_WORLD, 0);
    }

    @Test
    public void screenshotTutorial1() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.TUTORIAL_WORLD, 1);
    }

    @Test
    public void screenshotTutorial2() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.TUTORIAL_WORLD, 2);
    }

    @Test
    public void screenshotGround00() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.GROUND_ZERO_WORLD, 0);
    }

    @Test
    public void screenshotGround01() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.GROUND_ZERO_WORLD, 1);
    }

    @Test
    public void screenshotGround02() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.GROUND_ZERO_WORLD, 2);
    }

    @Test
    public void screenshotGround03() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.GROUND_ZERO_WORLD, 3);
    }

    @Test
    public void screenshotGround04() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.GROUND_ZERO_WORLD, 4);
    }

    @Test
    public void screenshotGround05() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.GROUND_ZERO_WORLD, 5);
    }

    @Test
    public void screenshotGround06() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.GROUND_ZERO_WORLD, 6);
    }

    @Test
    public void screenshotGround07() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.GROUND_ZERO_WORLD, 7);
    }

    @Test
    public void screenshotGround08() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.GROUND_ZERO_WORLD, 8);
    }

    @Test
    public void screenshotGround09() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.GROUND_ZERO_WORLD, 9);
    }

    @Test
    public void screenshotAlpha10() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.ALPHA_ONE_WORLD, 0);
    }

    @Test
    public void screenshotAlpha11() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.ALPHA_ONE_WORLD, 1);
    }

    @Test
    public void screenshotAlpha12() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.ALPHA_ONE_WORLD, 2);
    }

    @Test
    public void screenshotAlpha13() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.ALPHA_ONE_WORLD, 3);
    }

    @Test
    public void screenshotAlpha14() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.ALPHA_ONE_WORLD, 4);
    }

    @Test
    public void screenshotAlpha15() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.ALPHA_ONE_WORLD, 5);
    }

    @Test
    public void screenshotAlpha16() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.ALPHA_ONE_WORLD, 6);
    }

    @Test
    public void screenshotAlpha17() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.ALPHA_ONE_WORLD, 7);
    }

    @Test
    public void screenshotAlpha18() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.ALPHA_ONE_WORLD, 8);
    }

    @Test
    public void screenshotAlpha19() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.ALPHA_ONE_WORLD, 9);
    }

    @Test
    public void screenshotPortal20() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.PORTAL_TWO_WORLD, 0);
    }

    @Test
    public void screenshotPortal21() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.PORTAL_TWO_WORLD, 1);
    }

    @Test
    public void screenshotPortal22() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.PORTAL_TWO_WORLD, 2);
    }

    @Test
    public void screenshotPortal23() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.PORTAL_TWO_WORLD, 3);
    }

    @Test
    public void screenshotPortal24() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.PORTAL_TWO_WORLD, 4);
    }

    @Test
    public void screenshotPortal25() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.PORTAL_TWO_WORLD, 5);
    }

    @Test
    public void screenshotPortal26() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.PORTAL_TWO_WORLD, 6);
    }

    @Test
    public void screenshotPortal27() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.PORTAL_TWO_WORLD, 7);
    }

    @Test
    public void screenshotPortal28() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.PORTAL_TWO_WORLD, 8);
    }

    @Test
    public void screenshotPortal29() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.PORTAL_TWO_WORLD, 9);
    }

    @Test
    public void screenshotSea30() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.SEA_THREE_WORLD, 0);
    }

    @Test
    public void screenshotSea31() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.SEA_THREE_WORLD, 1);
    }

    @Test
    public void screenshotSea32() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.SEA_THREE_WORLD, 2);
    }

    @Test
    public void screenshotSea33() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.SEA_THREE_WORLD, 3);
    }

    @Test
    public void screenshotSea34() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.SEA_THREE_WORLD, 4);
    }

    @Test
    public void screenshotSea35() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.SEA_THREE_WORLD, 5);
    }

    @Test
    public void screenshotSea36() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.SEA_THREE_WORLD, 6);
    }

    @Test
    public void screenshotSea37() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.SEA_THREE_WORLD, 7);
    }

    @Test
    public void screenshotSea38() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.SEA_THREE_WORLD, 8);
    }

    @Test
    public void screenshotSea39() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.SEA_THREE_WORLD, 9);
    }

    @Test
    public void screenshotHigh50() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.HIGH_FIVE_WORLD, 0);
    }

    @Test
    public void screenshotHigh51() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.HIGH_FIVE_WORLD, 1);
    }

    @Test
    public void screenshotHigh52() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.HIGH_FIVE_WORLD, 2);
    }

    @Test
    public void screenshotHigh53() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.HIGH_FIVE_WORLD, 3);
    }

    @Test
    public void screenshotHigh54() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.HIGH_FIVE_WORLD, 4);
    }

    @Test
    public void screenshotHigh55() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.HIGH_FIVE_WORLD, 5);
    }

    @Test
    public void screenshotHigh56() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.HIGH_FIVE_WORLD, 6);
    }

    @Test
    public void screenshotHigh57() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.HIGH_FIVE_WORLD, 7);
    }

    @Test
    public void screenshotHigh58() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.HIGH_FIVE_WORLD, 8);
    }

    @Test
    public void screenshotHigh59() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.HIGH_FIVE_WORLD, 9);
    }

    @Test
    public void screenshotMagic80() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.MAGIC_EIGHT_WORLD, 0);
    }

    @Test
    public void screenshotMagic81() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.MAGIC_EIGHT_WORLD, 1);
    }

    @Test
    public void screenshotMagic82() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.MAGIC_EIGHT_WORLD, 2);
    }

    @Test
    public void screenshotMagic83() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.MAGIC_EIGHT_WORLD, 3);
    }

    @Test
    public void screenshotMagic84() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.MAGIC_EIGHT_WORLD, 4);
    }

    @Test
    public void screenshotMagic85() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.MAGIC_EIGHT_WORLD, 5);
    }

    @Test
    public void screenshotMagic86() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.MAGIC_EIGHT_WORLD, 6);
    }

    @Test
    public void screenshotMagic87() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.MAGIC_EIGHT_WORLD, 7);
    }

    @Test
    public void screenshotMagic88() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.MAGIC_EIGHT_WORLD, 8);
    }

    @Test
    public void screenshotMagic89() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.MAGIC_EIGHT_WORLD, 9);
    }

    @Test
    public void screenshotCloud90() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.CLOUD_NINE_WORLD, 0);
    }

    @Test
    public void screenshotCloud91() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.CLOUD_NINE_WORLD, 1);
    }

    @Test
    public void screenshotCloud92() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.CLOUD_NINE_WORLD, 2);
    }

    @Test
    public void screenshotCloud93() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.CLOUD_NINE_WORLD, 3);
    }

    @Test
    public void screenshotCloud94() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.CLOUD_NINE_WORLD, 4);
    }

    @Test
    public void screenshotCloud95() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.CLOUD_NINE_WORLD, 5);
    }

    @Test
    public void screenshotCloud96() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.CLOUD_NINE_WORLD, 6);
    }

    @Test
    public void screenshotCloud97() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.CLOUD_NINE_WORLD, 7);
    }

    @Test
    public void screenshotCloud98() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.CLOUD_NINE_WORLD, 8);
    }

    @Test
    public void screenshotCloud99() throws Exception {
        captureScreenshot(PerspectiveAndroidUtils.CLOUD_NINE_WORLD, 9);
    }

    private void captureScreenshot(String world, int puzzle) throws Exception {
        String name = "com.aletheiaware.perspective.android.GameActivity-" + world + "-" + puzzle + ".png";
        Log.d(PerspectiveUtils.TAG, "Capturing " + name);
        Intent intent = new Intent();
        intent.putExtra(PerspectiveAndroidUtils.WORLD_EXTRA, world);
        intent.putExtra(PerspectiveAndroidUtils.PUZZLE_EXTRA, puzzle);
        GameActivity activity = intentsTestRule.launchActivity(intent);
        Thread.sleep(1000);
        captureScreenshot(activity.getScene(), name);
        activity.finish();
    }

    public void captureScreenshot(final GLScene scene, final String name) {
        final CountDownLatch latch = new CountDownLatch(1);
        scene.setFrameCallback(new GLScene.FrameCallback() {
            @Override
            public boolean onFrame() {
                Log.d(PerspectiveUtils.TAG, "Frame callback");
                int[] viewport = scene.getViewport();
                int x = viewport[0];
                int y = viewport[1];
                int w = viewport[2];
                int h = viewport[3];
                int[] array = new int[w * h];
                int[] source = new int[w * h];
                IntBuffer buffer = IntBuffer.wrap(array);
                buffer.position(0);
                GLES20.glReadPixels(x, y, w, h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
                for (int i = 0; i < h; i++) {
                    int stride = i * w;
                    int offset = (h - i - 1) * w;
                    for (int j = 0; j < w; j++) {
                        int pixel = array[stride + j];
                        int alpha = pixel & 0xff000000;
                        int red = (pixel << 16) & 0x00ff0000;
                        int green = pixel & 0x0000ff00;
                        int blue = (pixel >> 16) & 0x000000ff;
                        source[offset + j] = alpha | red | green | blue;
                    }
                }
                Bitmap bitmap = Bitmap.createBitmap(source, w, h, Bitmap.Config.ARGB_8888);
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File file = new File(dir, name);
                try (FileOutputStream out = new FileOutputStream(file)){
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
                return false;
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
