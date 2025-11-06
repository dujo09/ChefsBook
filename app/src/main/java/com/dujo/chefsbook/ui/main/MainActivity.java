package com.dujo.chefsbook.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dujo.chefsbook.R;
import com.dujo.chefsbook.data.model.User;
import com.dujo.chefsbook.viewModel.PizzaViewModel;
import com.dujo.chefsbook.viewModel.SharedUserViewModel;
import com.firebase.ui.auth.AuthUI;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextView tvStatus;
    private Button btnSignOut;

    private PizzaViewModel pizzaViewModel;
    private PizzaAdapter pizzaAdapter;
    private SharedUserViewModel userViewModel;

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

        tvStatus = findViewById(R.id.tvStatus);
        btnSignOut = findViewById(R.id.btnSignOut);

        btnSignOut.setOnClickListener(v -> {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Signed out");
                            userViewModel.clear();
                        } else {
                            Log.e(TAG, "Sign out failed", task.getException());
                        }
                    });
        });

        userViewModel = new ViewModelProvider(this).get(SharedUserViewModel.class);
        userViewModel.getUser().observe(this, this::updateUi);
        pizzaViewModel = new ViewModelProvider(this).get(PizzaViewModel.class);
        RecyclerView rv = findViewById(R.id.rvPizzas);
        pizzaAdapter = new PizzaAdapter(p -> {
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(pizzaAdapter);

        pizzaViewModel.getPizzas().observe(this, pizzas -> {
            Log.i("DUJO", "onCreate: pizzas " + pizzas);
            if (pizzas != null) pizzaAdapter.submitList(pizzas);
        });

        pizzaViewModel.getError().observe(this, s -> {
            if (s != null) Toast.makeText(this, s, Toast.LENGTH_LONG).show();
        });
    }

    private void updateUi(User user) {
        if (user != null) {
            tvStatus.setText(user.username);
        } else {
            tvStatus.setText("");
        }
    }
}