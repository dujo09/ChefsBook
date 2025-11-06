package com.dujo.chefsbook.ui.recipe;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dujo.chefsbook.R;
import com.dujo.chefsbook.data.model.Recipe;
import com.dujo.chefsbook.ui.recipeCategory.RecipeCategoryListActivity;
import com.dujo.chefsbook.viewModel.RecipeViewModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RecipeListActivity extends AppCompatActivity {
    private static final String TAG = "FoodListActivity";
    private RecyclerView rv;
    private TextView tvTitle;
    private List<Recipe> recipes = new ArrayList<>();
    private RecipeAdapter recipeAdapter;
    private RecipeViewModel recipeViewModel;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        tvTitle = findViewById(R.id.tvRecipesTitle);
        rv = findViewById(R.id.rvRecipes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        recipeAdapter = new RecipeAdapter(recipe -> {
        });
        rv.setAdapter(recipeAdapter);

        String categoryId = getIntent().getStringExtra(RecipeCategoryListActivity.EXTRA_CATEGORY_ID);
        String categoryName = getIntent().getStringExtra(RecipeCategoryListActivity.EXTRA_CATEGORY_NAME);
        if (categoryName != null) tvTitle.setText(categoryName);

        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        recipeViewModel.getSelectedCategoryId().setValue(categoryId);
        recipeViewModel.getFilteredRecipes().observe(this, recipes -> {
            if (recipes != null) recipeAdapter.submitList(recipes);
        });
    }
}
