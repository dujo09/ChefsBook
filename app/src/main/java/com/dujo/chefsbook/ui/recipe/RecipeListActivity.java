package com.dujo.chefsbook.ui.recipe;

import static com.dujo.chefsbook.utils.Constants.EXTRA_CATEGORY_ID;
import static com.dujo.chefsbook.utils.Constants.EXTRA_RECIPE_ID;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.dujo.chefsbook.utils.Constants;
import com.dujo.chefsbook.viewModel.RecipeViewModel;
import com.dujo.chefsbook.viewModel.SharedUserViewModel;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RecipeListActivity extends AppCompatActivity {
    private static final String TAG = "RecipeListActivity";
    private RecipeAdapter recipeAdapter;
    private RecipeViewModel recipeViewModel;
    private SharedUserViewModel userViewModel;
    private Button btnAddRecipe, btnLogin, btnLogout;
    private TextView tvStatus;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        btnLogin = findViewById(R.id.btnLogin);
        btnAddRecipe = findViewById(R.id.btnAddRecipe);
        btnLogout = findViewById(R.id.btnLogout);
        tvStatus = findViewById(R.id.tvStatus);
        recyclerView = findViewById(R.id.rvRecipes);

        String categoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        String categoryName = getIntent().getStringExtra(Constants.EXTRA_CATEGORY_NAME);

        recipeAdapter = new RecipeAdapter(recipe -> {
            Intent i = new Intent(this, RecipeDetailActivity.class);
            i.putExtra(EXTRA_RECIPE_ID, recipe.getId());
            startActivity(i);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recipeAdapter);

        userViewModel = new ViewModelProvider(this).get(SharedUserViewModel.class);
        userViewModel.getUser().observe(this, this::updateUserSessionUI);
        updateUserSessionUI(userViewModel.getUser().getValue());

        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);
        recipeViewModel.getSelectedCategoryId().setValue(categoryId);
        recipeViewModel.getFilteredRecipes().observe(this, recipes -> {
            if (recipes != null) recipeAdapter.submitList(recipes);
        });

        btnAddRecipe.setOnClickListener(v -> {
            Intent i = new Intent(this, AddRecipeActivity.class);
            i.putExtra(EXTRA_CATEGORY_ID, categoryId);
            startActivity(i);
        });

        btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Logging out...", Toast.LENGTH_LONG).show();
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Logged out", Toast.LENGTH_LONG).show();
                            Log.d(TAG, "Logged out");
                            userViewModel.clear();
                        } else {
                            Log.e(TAG, "Logout failed", task.getException());
                        }
                    });
        });

        btnLogin.setOnClickListener(v -> {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
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
