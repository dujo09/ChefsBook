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
import com.dujo.chefsbook.data.model.Pizza;

public class PizzaAdapter extends ListAdapter<Pizza, PizzaAdapter.PizzaVH> {

    public interface OnItemClick {
        void onClick(Pizza pizza);
    }

    private final OnItemClick listener;

    public PizzaAdapter(OnItemClick listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Pizza> DIFF_CALLBACK = new DiffUtil.ItemCallback<Pizza>() {
        @Override
        public boolean areItemsTheSame(@NonNull Pizza oldItem, @NonNull Pizza newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Pizza oldItem, @NonNull Pizza newItem) {
            return oldItem.getName().equals(newItem.getName())
                    && oldItem.getDescription().equals(newItem.getDescription())
                    && oldItem.getPrice() == newItem.getPrice();
        }
    };

    @NonNull
    @Override
    public PizzaVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pizza, parent, false);
        return new PizzaVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PizzaVH holder, int position) {
        Pizza pizza = getItem(position);
        holder.bind(pizza);
    }

    class PizzaVH extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvPrice;

        PizzaVH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            itemView.setOnClickListener(v -> {
                // TODO replace
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onClick(getItem(pos));
                }
            });
        }

        void bind(Pizza p) {
            tvName.setText(p.getName());
            tvDesc.setText(p.getDescription());
            tvPrice.setText(String.format("€ %.2f", p.getPrice()));
        }
    }
}
