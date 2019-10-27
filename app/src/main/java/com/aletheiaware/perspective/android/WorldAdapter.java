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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.aletheiaware.perspective.PerspectiveProto.World;
import com.aletheiaware.perspective.android.utils.PerspectiveAndroidUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

public class WorldAdapter extends Adapter<ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_WORLD = 1;

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
        sort();
    }

    public synchronized void addWorld(String name, String price) {
        if (!names.contains(name)) {
            names.add(name);
        }
        pricesMap.put(name, price);
        sort();
    }

    private synchronized void sort() {
        // Sort names of free worlds first, then paid worlds second
        Collections.sort(names, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int i1 = -1;
                int i2 = -1;
                for (int i = 0; i < PerspectiveAndroidUtils.FREE_WORLDS.length; i++) {
                    String w = PerspectiveAndroidUtils.FREE_WORLDS[i];
                    if (o1.equals(w)) {
                        i1 = i;
                    }
                    if (o2.equals(w)) {
                        i2 = i;
                    }
                }
                for (int i = 0; i < PerspectiveAndroidUtils.PAID_WORLDS.length; i++) {
                    String w = PerspectiveAndroidUtils.PAID_WORLDS[i];
                    if (o1.equals(w)) {
                        i1 = i + PerspectiveAndroidUtils.FREE_WORLDS.length;
                    }
                    if (o2.equals(w)) {
                        i2 = i + PerspectiveAndroidUtils.FREE_WORLDS.length;
                    }
                }
                return Integer.compare(i1, i2);
            }
        });
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
        if (viewType == TYPE_HEADER) {
            return new StarCountHolder(activity.getLayoutInflater().inflate(R.layout.star_count, parent, false));
        }
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == 0) {
            updateStarCountHolder((StarCountHolder) holder);
        } else {
            WorldViewHolder wvh = (WorldViewHolder) holder;
            if (names.isEmpty()) {
                wvh.setEmptyView();
            } else {
                String name = names.get(position - 1);
                World world = worldsMap.get(name);
                if (world == null) {
                    String price = pricesMap.get(name);
                    if (price == null) {
                        price = "?";
                    }
                    wvh.set(name, price);
                } else {
                    int s = 0;
                    Integer stars = starsMap.get(name);
                    if (stars != null) {
                        s = stars;
                    }
                    int puzzles = world.getPuzzleCount();
                    int worldStars = 0;
                    if (puzzles > 0) {
                        worldStars = s / puzzles;
                    }
                    wvh.set(world, worldStars);
                }
            }
        }
    }

    private synchronized void updateStarCountHolder(StarCountHolder holder) {
        int earned = 0;
        int max = 0;
        for (String name : names) {
            Integer i = starsMap.get(name);
            if (i != null) {
                earned += i;
            }
            World w = worldsMap.get(name);
            if (w != null) {
                max += w.getPuzzleCount() * PerspectiveAndroidUtils.MAX_STARS;
            }
        }
        holder.setStars(earned, max);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_WORLD;
    }

    @Override
    public int getItemCount() {
        int count = 1; // Header
        if (names.isEmpty()) {
            return count + 1;// Empty view
        }
        return count + names.size();
    }

    static class StarCountHolder extends ViewHolder {

        private final TextView starCount;

        StarCountHolder(View view) {
            super(view);
            starCount = view.findViewById(R.id.world_star_count);
        }

        void setStars(int earned, int max) {
            starCount.setText(starCount.getContext().getString(R.string.star_count_format, earned, max));
        }
    }

    static class WorldViewHolder extends ViewHolder {

        private final TextView itemName;
        private final View[] itemStars = new View[PerspectiveAndroidUtils.MAX_STARS];
        private final Button itemBuy;
        private String name;
        private World world;

        WorldViewHolder(View view) {
            super(view);
            itemName = view.findViewById(R.id.world_item_name);
            itemStars[0] = view.findViewById(R.id.world_item_star1);
            itemStars[1] = view.findViewById(R.id.world_item_star2);
            itemStars[2] = view.findViewById(R.id.world_item_star3);
            itemStars[3] = view.findViewById(R.id.world_item_star4);
            itemStars[4] = view.findViewById(R.id.world_item_star5);
            itemBuy = view.findViewById(R.id.item_world_buy);
        }

        void set(World world, int stars) {
            setWorld(world);
            setName(world.getName());
            itemBuy.setVisibility(View.GONE);
            for (int i = 0; i < PerspectiveAndroidUtils.MAX_STARS; i++) {
                itemStars[i].setVisibility(stars > i ? View.VISIBLE : View.INVISIBLE);
            }
        }

        void set(String name, String price) {
            setWorld(null);
            setName(name);
            itemBuy.setText(price);
            itemBuy.setVisibility(View.VISIBLE);
            for (View itemStar : itemStars) {
                itemStar.setVisibility(View.GONE);
            }
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
            for (View itemStar : itemStars) {
                itemStar.setVisibility(View.GONE);
            }
        }
    }

}
