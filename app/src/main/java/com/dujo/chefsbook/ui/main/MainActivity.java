package com.dujo.chefsbook.ui.main;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dujo.chefsbook.R;
import com.dujo.chefsbook.viewModel.PizzaViewModel;

public class MainActivity extends AppCompatActivity {
    private PizzaViewModel pizzaViewModel;
    private PizzaAdapter pizzaAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rvPizzas), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pizzaViewModel = new ViewModelProvider(this).get(PizzaViewModel.class);
        RecyclerView rv = findViewById(R.id.rvPizzas);
        pizzaAdapter = new PizzaAdapter(p -> {
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(pizzaAdapter);

        pizzaViewModel.getPizzas().observe(this, pizzas -> {
            if (pizzas != null) pizzaAdapter.submitList(pizzas);
        });
    }
}