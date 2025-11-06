package com.dujo.chefsbook.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.dujo.chefsbook.R;
import com.dujo.chefsbook.data.model.FoodCategory;

public class FoodCategoryAdapter extends ListAdapter<FoodCategory, FoodCategoryAdapter.FoodCategoryVH> {

    public interface OnItemClick {
        void onClick(FoodCategory foodCategory);
    }

    private final OnItemClick listener;

    public FoodCategoryAdapter(OnItemClick listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<FoodCategory> DIFF_CALLBACK = new DiffUtil.ItemCallback<FoodCategory>() {
        @Override
        public boolean areItemsTheSame(@NonNull FoodCategory oldItem, @NonNull FoodCategory newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull FoodCategory oldItem, @NonNull FoodCategory newItem) {
            return oldItem.getName().equals(newItem.getName());
        }
    };

    @NonNull
    @Override
    public FoodCategoryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_category, parent, false);
        return new FoodCategoryVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodCategoryVH holder, int position) {
        FoodCategory foodCategory = getItem(position);
        holder.bind(foodCategory);
    }

    public class FoodCategoryVH extends RecyclerView.ViewHolder {
        TextView tvName;

        FoodCategoryVH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            itemView.setOnClickListener(v -> {
                // TODO replace
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onClick(getItem(pos));
                }
            });
        }

        void bind(FoodCategory foodCategory) {
            tvName.setText(foodCategory.getName());
        }
    }
}
