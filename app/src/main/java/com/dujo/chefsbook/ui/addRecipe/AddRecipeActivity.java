package com.dujo.chefsbook.ui.addRecipe;

import static com.dujo.chefsbook.utils.Constants.EXTRA_CATEGORY_ID;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.dujo.chefsbook.R;
import com.dujo.chefsbook.data.model.Recipe;
import com.dujo.chefsbook.data.model.RecipeCategory;
import com.dujo.chefsbook.ui.recipe.RecipeListActivity;
import com.dujo.chefsbook.viewModel.AddRecipeViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class AddRecipeActivity extends AppCompatActivity {
  private static final String TAG = "AddRecipeActivity";
  private final List<RecipeCategory> recipeCategories = new ArrayList<>();
  private String initialCategoryId;
  private ArrayAdapter<String> recipeCategorySpinnerAdapter;
  private TextInputEditText etName, etDescription, etImageUrl;
  private Button btnSave, btnCancel;
  private Spinner spinnerCategory;
  private ImageView ivRecipeImage;
  private AddRecipeViewModel addRecipeViewModel;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_recipe);

    etName = findViewById(R.id.etName);
    etDescription = findViewById(R.id.etDescription);
    spinnerCategory = findViewById(R.id.spinnerCategory);
    btnSave = findViewById(R.id.btnSave);
    btnCancel = findViewById(R.id.btnCancel);
    etImageUrl = findViewById(R.id.etImageUrl);
    ivRecipeImage = findViewById(R.id.ivPreview);

    initialCategoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);

    recipeCategorySpinnerAdapter =
        new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
    recipeCategorySpinnerAdapter.setDropDownViewResource(
        android.R.layout.simple_spinner_dropdown_item);
    spinnerCategory.setAdapter(recipeCategorySpinnerAdapter);

    addRecipeViewModel = new ViewModelProvider(this).get(AddRecipeViewModel.class);
    addRecipeViewModel
        .getAddedRecipe()
        .observe(
            this,
            added -> {
              if (added != null) {
                Toast.makeText(AddRecipeActivity.this, "Recipe saved", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, RecipeListActivity.class);
                Log.i(TAG, "onCreate: idddd" + added.getRecipeCategoryId());
                i.putExtra(EXTRA_CATEGORY_ID, added.getRecipeCategoryId());
                startActivity(i);
              }
            });
    addRecipeViewModel
        .getError()
        .observe(
            this,
            error -> {
              if (error != null && !error.isEmpty()) {
                setUiEnabled(true);
                Toast.makeText(AddRecipeActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
              }
            });
    addRecipeViewModel
        .getRecipeCategories()
        .observe(
            this,
            recipeCategories -> {
              if (recipeCategories != null) {
                recipeCategories.forEach(
                    categories -> {
                      this.recipeCategories.add(categories);
                      recipeCategorySpinnerAdapter.add(categories.getName());
                    });
                recipeCategorySpinnerAdapter.notifyDataSetChanged();
              } else {
                this.recipeCategories.clear();
                recipeCategorySpinnerAdapter.clear();
              }
            });

    etImageUrl.setOnFocusChangeListener(
        (v, hasFocus) -> {
          if (!hasFocus) {
            showRecipeImagePreview(
                etImageUrl.getText() != null ? etImageUrl.getText().toString().trim() : null);
          }
        });

    btnSave.setOnClickListener(
        v -> {
          setUiEnabled(false);
          createRecipe();
          setUiEnabled(true);
        });

    btnCancel.setOnClickListener(
        v -> {
          setResult(Activity.RESULT_CANCELED);
          finish();
        });
  }

  private void createRecipe() {
    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
    String description =
        etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
    String imageUrl = etImageUrl.getText() != null ? etImageUrl.getText().toString().trim() : "";

    if (TextUtils.isEmpty(name)) {
      Log.i(TAG, "createRecipe: isEmpty");
      etName.setError("Name is required");
      return;
    }
    if (TextUtils.isEmpty(description)) {
      etDescription.setError("Description is required");
      return;
    }
    if (TextUtils.isEmpty(imageUrl) || !Patterns.WEB_URL.matcher(imageUrl).matches()) {
      etImageUrl.setError("Please enter a valid image URL (http/https)");
      etImageUrl.requestFocus();
      return;
    }

    showRecipeImagePreview(imageUrl);

    String selectedCategoryId = null;
    int pos = spinnerCategory.getSelectedItemPosition();
    if (pos >= 0 && pos < recipeCategories.size())
      selectedCategoryId = recipeCategories.get(pos).getId();

    String uid = FirebaseAuth.getInstance().getUid();

    Recipe createdRecipe =
        new Recipe(null, uid, selectedCategoryId, name, description, 0, imageUrl);
    addRecipeViewModel.addRecipe(createdRecipe);
  }

  private void setUiEnabled(boolean enabled) {
    etName.setEnabled(enabled);
    etDescription.setEnabled(enabled);
    btnSave.setEnabled(enabled);
    btnCancel.setEnabled(enabled);
    spinnerCategory.setEnabled(enabled);
  }

  private void showRecipeImagePreview(String url) {
    if (url == null || url.isEmpty() || !Patterns.WEB_URL.matcher(url).matches()) {
      ivRecipeImage.setVisibility(View.GONE);
      return;
    }
    ivRecipeImage.setVisibility(View.VISIBLE);
    Glide.with(this)
        .load(url)
        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
        .error(android.R.drawable.ic_menu_report_image)
        .into(ivRecipeImage);
  }
}
