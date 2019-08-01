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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aletheiaware.perspective.PerspectiveProto.World;

import java.util.ArrayList;
import java.util.List;

public class WorldAdapter extends Adapter<WorldAdapter.WorldViewHolder> {

    private final Activity activity;
    private final List<World> worlds = new ArrayList<>();
    private final PuzzleAdapter.Callback callback;

    public WorldAdapter(Activity activity, PuzzleAdapter.Callback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    public synchronized void addWorld(World world) {
        worlds.add(world);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @NonNull
    @Override
    public WorldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LinearLayout view = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.world_list_item, parent, false);
        return new WorldViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorldViewHolder holder, int position) {
        if (worlds.isEmpty()) {
            holder.setEmptyView();
        } else {
            World world = worlds.get(position);
            holder.set(world, new PuzzleAdapter(activity, world, callback));
        }
    }

    @Override
    public int getItemCount() {
        if (worlds.isEmpty()) {
            return 1;// For empty view
        }
        return worlds.size();
    }

    static class WorldViewHolder extends RecyclerView.ViewHolder {

        private TextView itemName;
        private RecyclerView itemPuzzles;

        WorldViewHolder(LinearLayout view) {
            super(view);
            itemName = view.findViewById(R.id.world_item_name);
            itemPuzzles = view.findViewById(R.id.world_puzzle_recycler);
            itemPuzzles.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false));
        }

        void set(World world, Adapter<PuzzleAdapter.PuzzleViewHolder> adapter) {
            itemName.setText(world.getName().toUpperCase());
            itemPuzzles.setAdapter(adapter);
        }

        void setEmptyView() {
            itemName.setText(R.string.empty_world_list);
        }
    }

}
