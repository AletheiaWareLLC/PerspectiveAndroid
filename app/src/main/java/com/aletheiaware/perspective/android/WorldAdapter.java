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
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.aletheiaware.perspective.PerspectiveProto.World;
import com.aletheiaware.perspective.android.utils.PerspectiveAndroidUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldAdapter extends Adapter<WorldAdapter.WorldViewHolder> {

    public interface Callback {
        void onSelect(World world);
        void onBuy(String world);
    }

    private final Activity activity;
    private final List<String> names = new ArrayList<>();
    private final Map<String, Integer> starsMap = new HashMap<>();
    private final Map<String, String> pricesMap = new HashMap<>();
    private final Map<String, World> worldsMap = new HashMap<>();
    private final Callback callback;

    public WorldAdapter(Activity activity, Callback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    public synchronized void addWorld(World world, int stars) {
        String name = world.getName();
        if (!names.contains(name)) {
            names.add(name);
        }
        worldsMap.put(name, world);
        starsMap.put(name, stars);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public synchronized void addWorld(String name, String price) {
        if (!names.contains(name)) {
            names.add(name);
        }
        pricesMap.put(name, price);
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
        final View view = activity.getLayoutInflater().inflate(R.layout.world_list_item, parent, false);
        final WorldViewHolder holder = new WorldViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onSelect(holder.getWorld());
            }
        });
        Button buyButton = view.findViewById(R.id.item_world_buy);
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onBuy(holder.getName());
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull WorldViewHolder holder, int position) {
        if (names.isEmpty()) {
            holder.setEmptyView();
        } else {
            String name = names.get(position);
            World world = worldsMap.get(name);
            if (world == null) {
                holder.set(name, pricesMap.get(name));
            } else {
                int s = 0;
                Integer stars = starsMap.get(name);
                if (stars != null) {
                    s = stars;
                }
                holder.set(world, s);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (names.isEmpty()) {
            return 1;// For empty view
        }
        return names.size();
    }

    static class WorldViewHolder extends RecyclerView.ViewHolder {

        private final TextView itemName;
        private final View itemStar1;
        private final View itemStar2;
        private final View itemStar3;
        private final View itemStar4;
        private final View itemStar5;
        private final Button itemBuy;
        private String name;
        private World world;

        WorldViewHolder(View view) {
            super(view);
            itemName = view.findViewById(R.id.world_item_name);
            itemStar1 = view.findViewById(R.id.world_item_star1);
            itemStar2 = view.findViewById(R.id.world_item_star2);
            itemStar3 = view.findViewById(R.id.world_item_star3);
            itemStar4 = view.findViewById(R.id.world_item_star4);
            itemStar5 = view.findViewById(R.id.world_item_star5);
            itemBuy = view.findViewById(R.id.item_world_buy);
        }

        void set(World world, int stars) {
            setWorld(world);
            setName(world.getName());
            itemBuy.setVisibility(View.GONE);
            itemStar1.setVisibility(stars > 0 ? View.VISIBLE : View.INVISIBLE);
            itemStar2.setVisibility(stars > 1 ? View.VISIBLE : View.INVISIBLE);
            itemStar3.setVisibility(stars > 2 ? View.VISIBLE : View.INVISIBLE);
            itemStar4.setVisibility(stars > 3 ? View.VISIBLE : View.INVISIBLE);
            itemStar5.setVisibility(stars > 4 ? View.VISIBLE : View.INVISIBLE);
        }

        void set(String name, String price) {
            setWorld(null);
            setName(name);
            itemBuy.setText(price);
            itemBuy.setVisibility(View.VISIBLE);
            itemStar1.setVisibility(View.GONE);
            itemStar2.setVisibility(View.GONE);
            itemStar3.setVisibility(View.GONE);
            itemStar4.setVisibility(View.GONE);
            itemStar5.setVisibility(View.GONE);
        }

        void setName(String name) {
            this.name = name;
            itemName.setText(PerspectiveAndroidUtils.capitalize(name));
        }

        void setWorld(World world) {
            this.world = world;
        }

        String getName() {
            return name;
        }

        World getWorld() {
            return world;
        }

        void setEmptyView() {
            itemName.setText(R.string.empty_world_list);
            itemBuy.setVisibility(View.GONE);
            itemStar1.setVisibility(View.GONE);
            itemStar2.setVisibility(View.GONE);
            itemStar3.setVisibility(View.GONE);
            itemStar4.setVisibility(View.GONE);
            itemStar5.setVisibility(View.GONE);
        }
    }

}
