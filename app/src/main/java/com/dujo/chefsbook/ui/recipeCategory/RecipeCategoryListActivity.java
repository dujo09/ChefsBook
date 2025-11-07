package com.dujo.chefsbook.ui.recipeCategory;

import android.content.Intent;
import android.os.Bundle;
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
import com.dujo.chefsbook.viewModel.RecipeCategoryViewModel;
import com.dujo.chefsbook.viewModel.SharedUserViewModel;

public class RecipeCategoryListActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final String EXTRA_CATEGORY_ID = "extra:category_id";
    public static final String EXTRA_CATEGORY_NAME = "extra:category_name";

    private TextView tvStatus;
    private Button btnSignOut;
    private Button btnAddRecipe;

    private RecipeCategoryViewModel recipeCategoryViewModel;
    private RecipeCategoryAdapter recipeCategoryAdapter;
    private SharedUserViewModel userViewModel;

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
        btnSignOut = findViewById(R.id.btnSignOut);
        btnAddRecipe = findViewById(R.id.btnAddRecipe);

        btnSignOut.setOnClickListener(v -> {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
//            AuthUI.getInstance()
//                    .signOut(this)
//                    .addOnCompleteListener(task -> {
//                        if (task.isSuccessful()) {
//                            Log.d(TAG, "Signed out");
//                            userViewModel.clear();
//                        } else {
//                            Log.e(TAG, "Sign out failed", task.getException());
//                        }
//                    });
        });

        btnAddRecipe.setOnClickListener(v -> {
            Intent i = new Intent(this, AddRecipeActivity.class);
            startActivity(i);
        });

        userViewModel = new ViewModelProvider(this).get(SharedUserViewModel.class);
        userViewModel.getUser().observe(this, this::updateUi);

        recipeCategoryViewModel = new ViewModelProvider(this).get(RecipeCategoryViewModel.class);
        RecyclerView recyclerView = findViewById(R.id.rvRecipeCategories);
        recipeCategoryAdapter = new RecipeCategoryAdapter(recipeCategory -> {
            Intent i = new Intent(this, RecipeListActivity.class);
            i.putExtra(EXTRA_CATEGORY_ID, recipeCategory.getId());
            i.putExtra(EXTRA_CATEGORY_NAME, recipeCategory.getName());
            startActivity(i);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recipeCategoryAdapter);

        recipeCategoryViewModel.getRecipeCategories().observe(this, pizzas -> {
            if (pizzas != null) recipeCategoryAdapter.submitList(pizzas);
        });

        recipeCategoryViewModel.getError().observe(this, s -> {
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