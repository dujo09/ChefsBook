package com.dujo.chefsbook.ui.recipe;

import static com.dujo.chefsbook.ui.addRecipe.AddRecipeActivity.EXTRA_CATEGORY_ID;
import static com.dujo.chefsbook.ui.addRecipe.AddRecipeActivity.EXTRA_RECIPE_ID;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dujo.chefsbook.R;
import com.dujo.chefsbook.data.model.Recipe;
import com.dujo.chefsbook.data.model.User;
import com.dujo.chefsbook.ui.addRecipe.AddRecipeActivity;
import com.dujo.chefsbook.ui.login.LoginActivity;
import com.dujo.chefsbook.ui.recipeCategory.RecipeCategoryListActivity;
import com.dujo.chefsbook.ui.recipeDetail.RecipeDetailActivity;
import com.dujo.chefsbook.ui.register.RegisterActivity;
import com.dujo.chefsbook.viewModel.RecipeViewModel;
import com.dujo.chefsbook.viewModel.SharedUserViewModel;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RecipeListActivity extends AppCompatActivity {
    private static final String TAG = "FoodListActivity";
    private RecyclerView rv;
    private TextView tvTitle;
    private final List<Recipe> recipes = new ArrayList<>();
    private RecipeAdapter recipeAdapter;
    private RecipeViewModel recipeViewModel;

    private FirebaseFirestore db;
    private Button btnAddRecipe;
    private String categoryId;

    private Button btnLogin;
    private Button btnRegister;
    private Button btnLogout;
    private SharedUserViewModel userViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnAddRecipe = findViewById(R.id.btnAddRecipe);
        btnLogout = findViewById(R.id.btnLogout);
        tvTitle = findViewById(R.id.tvRecipesTitle);

        btnRegister.setOnClickListener(v -> {
            Intent i = new Intent(this, RegisterActivity.class);
            startActivity(i);
        });

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


        rv = findViewById(R.id.rvRecipes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        recipeAdapter = new RecipeAdapter(recipe -> {
            Intent i = new Intent(this, RecipeDetailActivity.class);
            i.putExtra(EXTRA_RECIPE_ID, recipe.getId());
            startActivity(i);
        });
        rv.setAdapter(recipeAdapter);

        btnAddRecipe.setOnClickListener(v -> {
            Intent i = new Intent(this, AddRecipeActivity.class);
            i.putExtra(EXTRA_CATEGORY_ID, categoryId);
            startActivity(i);
        });

        userViewModel = new ViewModelProvider(this).get(SharedUserViewModel.class);
        userViewModel.getUser().observe(this, this::updateUi);

        categoryId = getIntent().getStringExtra(RecipeCategoryListActivity.EXTRA_CATEGORY_ID);
        String categoryName = getIntent().getStringExtra(RecipeCategoryListActivity.EXTRA_CATEGORY_NAME);
        if (categoryName != null) tvTitle.setText(categoryName);

        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        recipeViewModel.getSelectedCategoryId().setValue(categoryId);
        recipeViewModel.getFilteredRecipes().observe(this, recipes -> {
            if (recipes != null) recipeAdapter.submitList(recipes);
        });
    }

    private void updateUi(User user) {
        if (user != null) {
            btnRegister.setVisibility(View.GONE);
            btnRegister.setEnabled(false);
            btnAddRecipe.setVisibility(View.VISIBLE);
            btnAddRecipe.setEnabled(true);
            btnLogin.setVisibility(View.GONE);
            btnLogin.setEnabled(false);
            btnLogout.setVisibility(View.VISIBLE);
            btnLogout.setEnabled(true);
        } else {
            btnRegister.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(true);
            btnAddRecipe.setVisibility(View.GONE);
            btnAddRecipe.setEnabled(false);
            btnLogin.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(true);
            btnLogout.setVisibility(View.GONE);
            btnLogout.setEnabled(false);
        }
    }
}
