package com.dujo.chefsbook.ui.recipeDetail;

import static com.dujo.chefsbook.utils.Constants.EXTRA_RECIPE_ID;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.dujo.chefsbook.R;
import com.dujo.chefsbook.data.model.Rating;
import com.dujo.chefsbook.data.model.Recipe;
import com.dujo.chefsbook.viewModel.RecipeDetailViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Locale;

public class RecipeDetailActivity extends AppCompatActivity {

  private TextInputEditText etName, etDescription;
  private TextView tvAvgRating;
  private RatingBar ratingBar;
  private Button btnUpdate;

  private String recipeId;
  private String ownerUid;
  private String currentUid;
  private Button btnDelete;
  private FirebaseFirestore db;
  private RecipeDetailViewModel recipeDetailViewModel;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_recipe_details);

    etName = findViewById(R.id.etName);
    etDescription = findViewById(R.id.etDescription);
    tvAvgRating = findViewById(R.id.tvAvgRating);
    ratingBar = findViewById(R.id.ratingBar);
    btnUpdate = findViewById(R.id.btnUpdate);
    btnDelete = findViewById(R.id.btnDelete);

    recipeId = getIntent().getStringExtra(EXTRA_RECIPE_ID);
    if (TextUtils.isEmpty(recipeId)) {
      finish();
      return;
    }

    db = FirebaseFirestore.getInstance();
    currentUid = FirebaseAuth.getInstance().getUid();

    recipeDetailViewModel = new ViewModelProvider(this).get(RecipeDetailViewModel.class);
    recipeDetailViewModel.addListenerToRecipeRatings(recipeId);
    recipeDetailViewModel.getRecipeById(recipeId);
    recipeDetailViewModel
        .getRecipe()
        .observe(
            this,
            recipe -> {
              etName.setText(recipe.getName());
              etDescription.setText(recipe.getDescription());

              boolean isOwner = currentUid != null && currentUid.equals(recipe.getOwnerId());
              etName.setEnabled(isOwner);
              etDescription.setEnabled(isOwner);
              btnUpdate.setEnabled(isOwner);
              btnDelete.setVisibility(isOwner ? View.VISIBLE : View.GONE);

              ratingBar.setIsIndicator(currentUid == null || isOwner);
              if (!isOwner && currentUid != null) {
                recipeDetailViewModel.getUserRatingForRecipe(recipeId, currentUid);
              }
            });
    recipeDetailViewModel
        .getUserRating()
        .observe(
            this,
            rating -> {
              if (rating != null) {
                ratingBar.setRating(rating.getRating());
              } else {
                ratingBar.setRating(0f);
              }
            });
    recipeDetailViewModel
        .getRatings()
        .observe(
            this,
            ratings -> {
              if (ratings == null || ratings.isEmpty()) return;
              tvAvgRating.setText(
                  String.format(
                      Locale.ENGLISH,
                      "%.2f (%d)",
                      ratings.stream().mapToDouble(Rating::getRating).average().orElse(0f),
                      ratings.size()));
            });
    recipeDetailViewModel
        .getError()
        .observe(
            this,
            error -> {
              if (error != null && !error.isEmpty())
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
            });

    btnUpdate.setOnClickListener(v -> onUpdateClicked());

    ratingBar.setOnRatingBarChangeListener(
        (rb, rating, fromUser) -> {
          if (!fromUser) return;
          if (currentUid == null) {
            Toast.makeText(this, "Sign in to rate", Toast.LENGTH_SHORT).show();
            return;
          }
          if (ownerUid != null && ownerUid.equals(currentUid)) {
            Toast.makeText(this, "Owners cannot rate their own recipes here", Toast.LENGTH_SHORT)
                .show();
            return;
          }
          recipeDetailViewModel.rateRecipe(recipeId, currentUid, new Rating(rating));
        });

    btnDelete.setOnClickListener(
        v -> {
          onDeleteClicked();
        });
  }

  private void onUpdateClicked() {
    String newName = etName.getText() != null ? etName.getText().toString().trim() : "";
    String newDesc =
        etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

    if (TextUtils.isEmpty(newName)) {
      etName.setError("Name required");
      return;
    }

    if (TextUtils.isEmpty(newDesc)) {
      etDescription.setError("Description required");
      return;
    }
    Recipe currentRecipe = recipeDetailViewModel.getRecipe().getValue();
    if (currentRecipe == null) return;
    Recipe updatedRecipe =
        new Recipe(
            null,
            currentUid,
            currentRecipe.getRecipeCategoryId(),
            newName,
            newDesc,
            currentRecipe.getRating(),
            currentRecipe.getImageUrl());

    recipeDetailViewModel.updateRecipe(recipeId, updatedRecipe);
    Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
  }

  private void onDeleteClicked() {
    new androidx.appcompat.app.AlertDialog.Builder(this)
        .setTitle("Delete recipe")
        .setMessage("Are you sure you want to delete this recipe? This action cannot be undone.")
        .setPositiveButton("Delete", (dialog, which) -> performDelete())
        .setNegativeButton("Cancel", null)
        .show();
  }

  private void performDelete() {
    if (recipeId == null) return;
    recipeDetailViewModel.deleteRecipe(recipeId);
    finish();
  }
}
