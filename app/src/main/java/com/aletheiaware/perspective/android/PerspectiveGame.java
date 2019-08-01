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

import com.aletheiaware.joy.android.scene.GLScene;
import com.aletheiaware.joy.scene.AttributeNode;
import com.aletheiaware.joy.scene.SceneGraphNode;
import com.aletheiaware.perspective.Perspective;
import com.aletheiaware.perspective.android.utils.PerspectiveAndroidUtils;

public abstract class PerspectiveGame extends Perspective {

    public PerspectiveGame(GLScene scene, int size) {
        super(scene, size);
    }

    @Override
    public SceneGraphNode getSceneGraphNode(String program, String name, String type, String mesh) {
        return PerspectiveAndroidUtils.getSceneGraphNode(program, name, type, mesh);
    }

    @Override
    public AttributeNode getAttributeNode(String program, String name, String type, String colour) {
        return PerspectiveAndroidUtils.getAttributeNode(program, type, colour);
    }
}
