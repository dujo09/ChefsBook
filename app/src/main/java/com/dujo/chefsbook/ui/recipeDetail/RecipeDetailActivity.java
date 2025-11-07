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

        db = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getUid();

        recipeId = getIntent().getStringExtra(EXTRA_RECIPE_ID);
        if (TextUtils.isEmpty(recipeId)) {
            finish(); // nothing to show
            return;
        }

        // load recipe and ratings
        loadRecipe();
        observeRatings(); // will compute average

        // update button
        btnUpdate.setOnClickListener(v -> onUpdateClicked());

        // rating changed by user -> save rating if allowed
        ratingBar.setOnRatingBarChangeListener((rb, rating, fromUser) -> {
            if (!fromUser) return;
            if (currentUid == null) {
                Toast.makeText(this, "Sign in to rate", Toast.LENGTH_SHORT).show();
                return;
            }
            // allow rating only if not owner
            if (ownerUid != null && ownerUid.equals(currentUid)) {
                // owner should not rate here; show message and reset UI
                Toast.makeText(this, "Owners cannot rate their own recipes here", Toast.LENGTH_SHORT).show();
                loadUserRatingToUI(); // reload user rating (to previous)
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

            // populate UI
            etName.setText(recipe.getName());
            etDescription.setText(recipe.getDescription());

            // if owner -> editable fields, show badge
            boolean isOwner = currentUid != null && currentUid.equals(ownerUid);
            etName.setEnabled(isOwner);
            etDescription.setEnabled(isOwner);
            btnUpdate.setEnabled(isOwner);
            tvOwnerBadge.setVisibility(isOwner ? View.VISIBLE : View.GONE);

            // rating: if owner -> disable rating UI; else enable rating UI and load user's rating
            ratingBar.setIsIndicator(isOwner); // if owner, indicator (not editable)
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

    // update recipe fields (owner only)
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

    // --------------------------------------
    // Ratings handling: store per-user rating at recipes/{id}/ratings/{uid}
    // --------------------------------------
    private void submitRating(float rating) {
        // write user rating doc
        DocumentReference rRef = db.collection("recipes").document(recipeId)
                .collection("ratings").document(currentUid);

        Map<String, Object> data = new HashMap<>();
        data.put("uid", currentUid);
        data.put("rating", rating);
        data.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        rRef.set(data)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RecipeDetailActivity.this, "Thanks for rating", Toast.LENGTH_SHORT).show();
                    // after write, average will update due to observeRatings listener
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RecipeDetailActivity.this, "Rating failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    loadUserRatingToUI(); // revert to previous
                });
    }

    // load the current user's rating to the UI (non-blocking)
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

    // observe all ratings and compute average (real-time)
    private void observeRatings() {
        CollectionReference ratingsRef = db.collection("recipes").document(recipeId).collection("ratings");
        ratingsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    // ignore or show
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
}
