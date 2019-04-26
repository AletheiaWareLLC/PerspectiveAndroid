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

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aletheiaware.bc.utils.BCUtils;
import com.aletheiaware.perspective.PerspectiveProto.World;
import com.aletheiaware.perspective.utils.PerspectiveUtils;
import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class WorldAdapter extends Adapter<WorldAdapter.ViewHolder> {

    private final Activity activity;
    private final LayoutInflater inflater;
    private final Map<ByteString, World> worlds = new HashMap<>();
    private final Map<ByteString, Long> timestamps = new HashMap<>();
    private final List<ByteString> sorted = new ArrayList<>();

    public WorldAdapter(Activity activity) {
        this.activity = activity;
        this.inflater = activity.getLayoutInflater();
    }

    public abstract void onSelection(ByteString hash, World world);

    public synchronized void addWorld(ByteString recordHash, long timestamp, World world) {
        if (world == null) {
            throw new NullPointerException();
        }
        if (worlds.put(recordHash, world) == null) {
            sorted.add(recordHash);// Only add if new
            timestamps.put(recordHash, timestamp);
            sort();
        }
    }

    public synchronized void sort() {
        PerspectiveUtils.sort(sorted, timestamps);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.world_list_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ByteString hash = holder.getHash();
                World world = worlds.get(hash);
                if (hash != null) {
                    onSelection(hash, world);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (sorted.isEmpty()) {
            holder.setEmptyView();
        } else {
            ByteString hash = sorted.get(position);
            Long time = timestamps.get(hash);
            World world = worlds.get(hash);
            holder.set(hash, time, world);
        }
    }

    @Override
    public int getItemCount() {
        if (sorted.isEmpty()) {
            return 1;// For empty view
        }
        return sorted.size();
    }

    public boolean isEmpty() {
        return sorted.isEmpty();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ByteString hash;

        private TextView itemTime;
        private TextView itemName;

        ViewHolder(LinearLayout view) {
            super(view);
            itemTime = view.findViewById(R.id.list_item_time);
            itemName = view.findViewById(R.id.list_item_name);
        }

        void set(ByteString hash, Long time, World world) {
            this.hash = hash;
            itemTime.setText(BCUtils.timeToString(time));
            itemName.setText(world.getName());
        }

        ByteString getHash() {
            return hash;
        }

        void setEmptyView() {
            hash = null;
            itemName.setText(R.string.empty_list);
        }
    }
}
