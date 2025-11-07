package com.dujo.chefsbook.ui.recipe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
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
        ImageView ivRecipeImage;

        RecipeVH(@NonNull View itemView) {
            super(itemView);
            tvRecipeName = itemView.findViewById(R.id.tvRecipeName);
            tvRecipeDesc = itemView.findViewById(R.id.tvRecipeDesc);
            tvRecipeRating = itemView.findViewById(R.id.tvRecipeRating);
            ivRecipeImage = itemView.findViewById(R.id.ivRecipeImage);

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

            int radiusPx = (int) (itemView.getResources().getDisplayMetrics().density * 12);
            RequestOptions requestOptions = new RequestOptions()
                    .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                    .placeholder(R.drawable.placeholder_recipe)
                    .error(R.drawable.placeholder_recipe);

            Glide.with(ivRecipeImage.getContext())
                    .load(recipe.getImageUrl())
                    .apply(requestOptions)
                    .into(ivRecipeImage);
        }
    }
}
