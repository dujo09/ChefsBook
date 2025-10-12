package com.dujo.chefsbook.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.dujo.chefsbook.R;
import com.dujo.chefsbook.model.Pizza;
import com.dujo.chefsbook.viewModel.PizzaViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private PizzaViewModel pizzaViewModel;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.llContainer), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pizzaViewModel = new ViewModelProvider(this).get(PizzaViewModel.class);
        linearLayout = findViewById(R.id.llContainer);

        renderList(pizzaViewModel.getPizzas());
    }

    private void renderList(List<Pizza> pizzas) {
        linearLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Pizza pizza : pizzas) {
            View item = inflater.inflate(R.layout.item_pizza, linearLayout, false);
            TextView tvName = item.findViewById(R.id.tvName);
            TextView tvDesc = item.findViewById(R.id.tvDesc);
            TextView tvPrice = item.findViewById(R.id.tvPrice);

            tvName.setText(pizza.getName());
            tvDesc.setText(pizza.getDescription());
            tvPrice.setText(String.format("€ %.2f", pizza.getPrice()));

            linearLayout.addView(item);
        }
    }
}