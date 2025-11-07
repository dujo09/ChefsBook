package com.dujo.chefsbook.ui.recipeCategory;

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
import com.dujo.chefsbook.data.model.RecipeCategory;

public class RecipeCategoryAdapter extends ListAdapter<RecipeCategory, RecipeCategoryAdapter.RecipeCategoryVH> {

    private static final DiffUtil.ItemCallback<RecipeCategory> DIFF_CALLBACK = new DiffUtil.ItemCallback<RecipeCategory>() {
        @Override
        public boolean areItemsTheSame(@NonNull RecipeCategory oldItem, @NonNull RecipeCategory newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull RecipeCategory oldItem, @NonNull RecipeCategory newItem) {
            return oldItem.getName().equals(newItem.getName());
        }
    };
    private final OnItemClick listener;

    public RecipeCategoryAdapter(OnItemClick listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeCategoryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_category, parent, false);
        return new RecipeCategoryVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeCategoryVH holder, int position) {
        RecipeCategory recipeCategory = getItem(position);
        holder.bind(recipeCategory);
    }

    public interface OnItemClick {
        void onClick(RecipeCategory recipeCategory);
    }

    public class RecipeCategoryVH extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivRecipeCategoryImage;

        RecipeCategoryVH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            ivRecipeCategoryImage = itemView.findViewById(R.id.ivCategoryImage);

            itemView.setOnClickListener(v -> {
                // TODO replace
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onClick(getItem(pos));
                }
            });
        }

        void bind(RecipeCategory recipeCategory) {
            tvName.setText(recipeCategory.getName());

            int radiusPx = (int) (itemView.getResources().getDisplayMetrics().density * 12); // 12dp
            RequestOptions options = new RequestOptions()
                    .transform(new CenterCrop(), new RoundedCorners(radiusPx));

            Glide.with(ivRecipeCategoryImage.getContext())
                    .load(recipeCategory.getImageUrl())
                    .apply(options)
                    .into(ivRecipeCategoryImage);
        }
    }
}
