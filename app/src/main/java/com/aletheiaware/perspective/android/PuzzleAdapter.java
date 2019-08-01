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
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aletheiaware.perspective.PerspectiveProto.Solution;
import com.aletheiaware.perspective.PerspectiveProto.World;
import com.aletheiaware.perspective.android.utils.PerspectiveAndroidUtils;

import java.io.IOException;

public class PuzzleAdapter extends Adapter<PuzzleAdapter.PuzzleViewHolder> {

    public interface Callback {
        void onSelection(String world, int puzzle);
    }

    private final Activity activity;
    private final World world;
    private final Callback callback;
    private final int[] stars;

    public PuzzleAdapter(final Activity activity, final World world, Callback callback) {
        this.activity = activity;
        this.world = world;
        this.callback = callback;
        final int puzzles = world.getPuzzleCount();
        stars = new int[puzzles];
        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < puzzles; i++) {
                    int target = world.getPuzzle(i).getTarget();
                    stars[i] = -1;
                    Solution s = null;
                    try {
                        s = PerspectiveAndroidUtils.loadSolution(activity, world.getName(), i);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (s != null) {
                        int score = s.getScore();
                        if (score <= target) {
                            stars[i] = 3;
                        } else if (score <= target + 1) {
                            stars[i] = 2;
                        } else if (score <= target + 2) {
                            stars[i] = 1;
                        } else {
                            stars[i] = 0;
                        }
                    }
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }.start();
    }

    @NonNull
    @Override
    public PuzzleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LinearLayout view = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.puzzle_list_item, parent, false);
        final PuzzleViewHolder holder = new PuzzleViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onSelection(world.getName(), holder.getPuzzle());
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PuzzleViewHolder holder, int position) {
        int count = world.getPuzzleCount();
        if (count <= 0) {
            holder.setEmptyView();
        } else {
            holder.set(position, stars[position]);
        }
    }

    @Override
    public int getItemCount() {
        int count = world.getPuzzleCount();
        if (count <= 0) {
            return 1;// For empty view
        }
        return count;
    }

    static class PuzzleViewHolder extends RecyclerView.ViewHolder {

        private final TextView itemName;
        private final View itemStar1;
        private final View itemStar2;
        private final View itemStar3;
        private int puzzle;

        PuzzleViewHolder(LinearLayout view) {
            super(view);
            itemName = view.findViewById(R.id.puzzle_list_text);
            itemStar1 = view.findViewById(R.id.puzzle_list_star1);
            itemStar2 = view.findViewById(R.id.puzzle_list_star2);
            itemStar3 = view.findViewById(R.id.puzzle_list_star3);
        }

        void set(int puzzle, int stars) {
            this.puzzle = puzzle;
            itemName.setText(String.valueOf(puzzle + 1));
            if (stars < 0) {
                // TODO puzzle has not been solved yet
            }
            itemStar1.setVisibility(stars > 0 ? View.VISIBLE : View.GONE);
            itemStar2.setVisibility(stars > 1 ? View.VISIBLE : View.GONE);
            itemStar3.setVisibility(stars > 2 ? View.VISIBLE : View.GONE);
        }

        int getPuzzle() {
            return puzzle;
        }

        void setEmptyView() {
            puzzle = -1;
            itemName.setText(R.string.empty_puzzle_list);
            itemStar1.setVisibility(View.GONE);
            itemStar2.setVisibility(View.GONE);
            itemStar3.setVisibility(View.GONE);
        }
    }
}
