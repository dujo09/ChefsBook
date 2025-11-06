package com.dujo.chefsbook.ui.recipe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.dujo.chefsbook.R;
import com.dujo.chefsbook.data.model.Recipe;

public class RecipeAdapter extends ListAdapter<Recipe, RecipeAdapter.RecipeVH> {

    public interface OnItemClick {
        void onClick(Recipe recipe);
    }

    private final OnItemClick listener;

    public RecipeAdapter(OnItemClick listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Recipe> DIFF_CALLBACK = new DiffUtil.ItemCallback<Recipe>() {
        @Override
        public boolean areItemsTheSame(@NonNull Recipe oldItem, @NonNull Recipe newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Recipe oldItem, @NonNull Recipe newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getDescription().equals(newItem.getDescription()) &&
                    oldItem.getRating() == newItem.getRating() &&
                    oldItem.getRecipeCategoryId().equals(newItem.getRecipeCategoryId());
        }
    };

    @NonNull
    @Override
    public RecipeVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new RecipeVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeVH holder, int position) {
        Recipe recipe = getItem(position);
        holder.bind(recipe);
    }

    public class RecipeVH extends RecyclerView.ViewHolder {
        TextView tvRecipeName, tvRecipeDesc, tvRecipeRating;

        RecipeVH(@NonNull View itemView) {
            super(itemView);
            tvRecipeName = itemView.findViewById(R.id.tvRecipeName);
            tvRecipeDesc = itemView.findViewById(R.id.tvRecipeDesc);
            tvRecipeRating = itemView.findViewById(R.id.tvRecipeRating);
            itemView.setOnClickListener(v -> {
                // TODO replace
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onClick(getItem(pos));
                }
            });
        }

        void bind(Recipe recipe) {
            tvRecipeName.setText(recipe.getName());
            tvRecipeDesc.setText(recipe.getDescription());
            tvRecipeRating.setText(String.valueOf(recipe.getRating()));
        }
    }
}
