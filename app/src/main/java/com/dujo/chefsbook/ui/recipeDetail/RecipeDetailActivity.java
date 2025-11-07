package com.dujo.chefsbook.ui.recipeDetail;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dujo.chefsbook.R;
import com.dujo.chefsbook.data.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra:recipeId";

    private com.google.android.material.textfield.TextInputEditText etName;
    private com.google.android.material.textfield.TextInputEditText etDescription;
    private TextView tvOwnerBadge, tvAvgRating, tvStatus;
    private RatingBar ratingBar;
    private Button btnUpdate;
    private ProgressBar progress;

    private FirebaseFirestore db;
    private String recipeId;
    private String ownerUid;
    private String currentUid;
    private Button btnDelete;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        tvOwnerBadge = findViewById(R.id.tvOwnerBadge);
        tvAvgRating = findViewById(R.id.tvAvgRating);
        ratingBar = findViewById(R.id.ratingBar);
        btnUpdate = findViewById(R.id.btnUpdate);
        progress = findViewById(R.id.progress);
        tvStatus = findViewById(R.id.tvStatus);
        btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(v -> onDeleteClicked());

        db = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getUid();

        recipeId = getIntent().getStringExtra(EXTRA_RECIPE_ID);
        if (TextUtils.isEmpty(recipeId)) {
            finish();
            return;
        }

        loadRecipe();
        observeRatings();

        btnUpdate.setOnClickListener(v -> onUpdateClicked());

        ratingBar.setOnRatingBarChangeListener((rb, rating, fromUser) -> {
            if (!fromUser) return;
            if (currentUid == null) {
                Toast.makeText(this, "Sign in to rate", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ownerUid != null && ownerUid.equals(currentUid)) {
                Toast.makeText(this, "Owners cannot rate their own recipes here", Toast.LENGTH_SHORT).show();
                loadUserRatingToUI();
                return;
            }
            submitRating(rating);
        });
    }

    private void loadRecipe() {
        setUiLoading(true);
        DocumentReference docRef = db.collection("recipes").document(recipeId);
        docRef.get().addOnSuccessListener(doc -> {
            setUiLoading(false);
            if (!doc.exists()) {
                Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            Recipe recipe = Recipe.fromMap(doc.getId(), doc.getData());
            ownerUid = doc.contains("ownerUid") ? doc.getString("ownerUid") : null;

            etName.setText(recipe.getName());
            etDescription.setText(recipe.getDescription());

            boolean isOwner = currentUid != null && currentUid.equals(ownerUid);
            etName.setEnabled(isOwner);
            etDescription.setEnabled(isOwner);
            btnUpdate.setEnabled(isOwner);
            tvOwnerBadge.setVisibility(isOwner ? View.VISIBLE : View.GONE);
            btnDelete.setVisibility(isOwner ? View.VISIBLE : View.GONE);

            ratingBar.setIsIndicator(isOwner);
            if (!isOwner) {
                loadUserRatingToUI();
            }

        }).addOnFailureListener(e -> {
            setUiLoading(false);
            Toast.makeText(this, "Failed to load recipe: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void setUiLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnUpdate.setEnabled(!loading);
    }

    private void onUpdateClicked() {
        String newName = etName.getText() != null ? etName.getText().toString().trim() : "";
        String newDesc = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

        if (TextUtils.isEmpty(newName)) {
            etName.setError("Name required");
            return;
        }

        setUiLoading(true);
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("description", newDesc);
        updates.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("recipes").document(recipeId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    setUiLoading(false);
                    Toast.makeText(RecipeDetailActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    setUiLoading(false);
                    Toast.makeText(RecipeDetailActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void submitRating(float rating) {
        DocumentReference rRef = db.collection("recipes").document(recipeId)
                .collection("ratings").document(currentUid);

        Map<String, Object> data = new HashMap<>();
        data.put("uid", currentUid);
        data.put("rating", rating);
        data.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        rRef.set(data)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RecipeDetailActivity.this, "Thanks for rating", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RecipeDetailActivity.this, "Rating failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    loadUserRatingToUI();
                });
    }

    private void loadUserRatingToUI() {
        if (currentUid == null) return;
        db.collection("recipes").document(recipeId)
                .collection("ratings").document(currentUid)
                .get()
                .addOnSuccessListener(ds -> {
                    if (ds.exists() && ds.contains("rating")) {
                        Object r = ds.get("rating");
                        float val = 0f;
                        if (r instanceof Number) val = ((Number) r).floatValue();
                        ratingBar.setRating(val);
                    } else {
                        ratingBar.setRating(0f);
                    }
                });
    }

    private void observeRatings() {
        CollectionReference ratingsRef = db.collection("recipes").document(recipeId).collection("ratings");
        ratingsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                if (value == null) return;
                double sum = 0;
                int count = 0;
                for (DocumentSnapshot ds : value.getDocuments()) {
                    Object r = ds.get("rating");
                    if (r instanceof Number) {
                        sum += ((Number) r).doubleValue();
                        count++;
                    }
                }
                final double avg = count == 0 ? 0.0 : (sum / count);
                Map<String, Object> updates = new HashMap<>();
                updates.put("rating", avg);

                db.collection("recipes").document(recipeId)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            setUiLoading(false);
                            Toast.makeText(RecipeDetailActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            setUiLoading(false);
                            Toast.makeText(RecipeDetailActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });

                tvAvgRating.setText(String.format("%.2f (%d)", avg, count));
            }
        });
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
        setUiLoading(true);
        CollectionReference ratingsRef = db.collection("recipes").document(recipeId).collection("ratings");

        ratingsRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    for (DocumentSnapshot ds : querySnapshot.getDocuments()) {
                        batch.delete(ds.getReference());
                    }
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                deleteRecipeDocument();
                            })
                            .addOnFailureListener(e -> {
                                setUiLoading(false);
                                Toast.makeText(RecipeDetailActivity.this, "Failed to clear ratings: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    setUiLoading(false);
                    Toast.makeText(RecipeDetailActivity.this, "Failed to delete ratings: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void deleteRecipeDocument() {
        db.collection("recipes").document(recipeId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    setUiLoading(false);
                    Toast.makeText(RecipeDetailActivity.this, "Recipe deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setUiLoading(false);
                    Toast.makeText(RecipeDetailActivity.this, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
