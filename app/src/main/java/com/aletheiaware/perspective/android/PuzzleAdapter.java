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
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aletheiaware.perspective.PerspectiveProto.World;

public class PuzzleAdapter extends Adapter<PuzzleAdapter.PuzzleViewHolder> {

    public interface Callback {
        void onSelect(String world, int puzzle);
    }

    private final Activity activity;
    private final World world;
    private final Callback callback;
    private final int[] stars;

    public PuzzleAdapter(final Activity activity, World world, int[] stars, Callback callback) {
        this.activity = activity;
        this.world = world;
        this.stars = (stars == null) ? new int[world.getPuzzleCount()] : stars;
        this.callback = callback;
    }

    @NonNull
    @Override
    public PuzzleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final CardView view = (CardView) activity.getLayoutInflater().inflate(R.layout.puzzle_list_item, parent, false);
        final PuzzleViewHolder holder = new PuzzleViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int puzzle = holder.getPuzzle();
                if (!holder.isLocked() && puzzle > 0) {
                    callback.onSelect(world.getName(), puzzle);
                }
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
            boolean locked = true;
            if (position == 0) {// First puzzle always unlocked
                locked = false;
            } else if (stars[position - 1] >= 0) {// Puzzle is unlocked if previous is solved
                locked = false;
            }
            holder.set(position + 1, stars[position], locked);
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

        private final Context context;
        private final CardView itemCard;
        private final TextView itemName;
        private final View itemStar1;
        private final View itemStar2;
        private final View itemStar3;
        private final View itemStar4;
        private final View itemStar5;
        private final ImageView itemLock;
        private int puzzle;
        private boolean locked;

        PuzzleViewHolder(CardView view) {
            super(view);
            context = view.getContext();
            itemCard = view;
            itemName = view.findViewById(R.id.puzzle_list_text);
            itemStar1 = view.findViewById(R.id.puzzle_list_star1);
            itemStar2 = view.findViewById(R.id.puzzle_list_star2);
            itemStar3 = view.findViewById(R.id.puzzle_list_star3);
            itemStar4 = view.findViewById(R.id.puzzle_list_star4);
            itemStar5 = view.findViewById(R.id.puzzle_list_star5);
            itemLock = view.findViewById(R.id.puzzle_list_locked);
        }

        void set(int puzzle, int stars, boolean locked) {
            this.puzzle = puzzle;
            this.locked = locked;
            itemCard.setCardBackgroundColor(ContextCompat.getColor(context, locked ? R.color.grey : R.color.white));
            itemName.setText(String.valueOf(puzzle));
            itemName.setTextColor(ContextCompat.getColor(context, locked ? R.color.dark_grey : R.color.accent));
            itemStar1.setVisibility(stars > 0 ? View.VISIBLE : View.INVISIBLE);
            itemStar2.setVisibility(stars > 1 ? View.VISIBLE : View.INVISIBLE);
            itemStar3.setVisibility(stars > 2 ? View.VISIBLE : View.INVISIBLE);
            itemStar4.setVisibility(stars > 3 ? View.VISIBLE : View.INVISIBLE);
            itemStar5.setVisibility(stars > 4 ? View.VISIBLE : View.INVISIBLE);
            itemLock.setVisibility(locked ? View.VISIBLE : View.INVISIBLE);
        }

        int getPuzzle() {
            return puzzle;
        }

        boolean isLocked() {
            return locked;
        }

        void setEmptyView() {
            puzzle = -1;
            itemCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.grey));
            itemName.setText(R.string.empty_puzzle_list);
            itemName.setTextColor(ContextCompat.getColor(context, R.color.dark_grey));
            itemStar1.setVisibility(View.INVISIBLE);
            itemStar2.setVisibility(View.INVISIBLE);
            itemStar3.setVisibility(View.INVISIBLE);
            itemStar4.setVisibility(View.INVISIBLE);
            itemStar5.setVisibility(View.INVISIBLE);
            itemLock.setVisibility(View.INVISIBLE);
        }
    }
}
