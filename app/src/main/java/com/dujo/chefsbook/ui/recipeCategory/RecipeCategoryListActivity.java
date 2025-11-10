package com.dujo.chefsbook.ui.recipeCategory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.dujo.chefsbook.ui.addRecipe.AddRecipeActivity;
import com.dujo.chefsbook.ui.login.LoginActivity;
import com.dujo.chefsbook.ui.recipe.RecipeListActivity;
import com.dujo.chefsbook.utils.Constants;
import com.dujo.chefsbook.viewModel.RecipeCategoryViewModel;
import com.dujo.chefsbook.viewModel.SharedUserViewModel;
import com.firebase.ui.auth.AuthUI;

public class RecipeCategoryListActivity extends AppCompatActivity {
    private static final String TAG = "RecipeCategoryListActivity";
    private RecipeCategoryViewModel recipeCategoryViewModel;
    private RecipeCategoryAdapter recipeCategoryAdapter;
    private SharedUserViewModel userViewModel;
    private TextView tvStatus;
    private Button btnLogin;
    private Button btnAddRecipe;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_category_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rvRecipeCategories), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvStatus = findViewById(R.id.tvStatus);
        btnLogin = findViewById(R.id.btnLogin);
        btnAddRecipe = findViewById(R.id.btnAddRecipe);
        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {
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

        btnLogin.setOnClickListener(v -> {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
        });

        btnAddRecipe.setOnClickListener(v -> {
            Intent i = new Intent(this, AddRecipeActivity.class);
            startActivity(i);
        });

        userViewModel = new ViewModelProvider(this).get(SharedUserViewModel.class);
        userViewModel.getUser().observe(this, this::updateUserSessionUI);

        recipeCategoryAdapter = new RecipeCategoryAdapter(recipeCategory -> {
            Intent i = new Intent(this, RecipeListActivity.class);
            i.putExtra(Constants.EXTRA_CATEGORY_ID, recipeCategory.getId());
            i.putExtra(Constants.EXTRA_CATEGORY_NAME, recipeCategory.getName());
            startActivity(i);
        });

        RecyclerView recyclerView = findViewById(R.id.rvRecipeCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recipeCategoryAdapter);

        recipeCategoryViewModel = new ViewModelProvider(this).get(RecipeCategoryViewModel.class);
        recipeCategoryViewModel.getRecipeCategories().observe(this, pizzas -> {
            if (pizzas != null) recipeCategoryAdapter.submitList(pizzas);
        });
        recipeCategoryViewModel.getError().observe(this, s -> {
            if (s != null) Toast.makeText(this, s, Toast.LENGTH_LONG).show();
        });
    }

    private void updateUserSessionUI(User user) {
        if (user != null) {
            tvStatus.setText(user.username);
            btnAddRecipe.setVisibility(View.VISIBLE);
            btnAddRecipe.setEnabled(true);
            btnLogin.setVisibility(View.GONE);
            btnLogin.setEnabled(false);
            btnLogout.setVisibility(View.VISIBLE);
            btnLogout.setEnabled(true);
        } else {
            tvStatus.setText("");
            btnAddRecipe.setVisibility(View.GONE);
            btnAddRecipe.setEnabled(false);
            btnLogin.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(true);
            btnLogout.setVisibility(View.GONE);
            btnLogout.setEnabled(false);
        }
    }
}