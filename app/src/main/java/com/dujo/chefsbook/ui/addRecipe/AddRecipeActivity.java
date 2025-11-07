package com.dujo.chefsbook.ui.addRecipe;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.dujo.chefsbook.data.repository.RecipeCategoryRepository.RECIPE_CATEGORY_COLLECTION;
import static com.dujo.chefsbook.data.repository.RecipeRepository.RECIPE_COLLECTION;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dujo.chefsbook.R;
import com.dujo.chefsbook.data.model.Recipe;
import com.dujo.chefsbook.data.model.RecipeCategory;
import com.dujo.chefsbook.ui.recipe.RecipeListActivity;
import com.dujo.chefsbook.ui.recipeCategory.RecipeCategoryListActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddRecipeActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_ID = "extra:recipeCategoryId";
    public static final String EXTRA_RECIPE_ID = "extra:recipeId";

    private TextView tvCategory;
    private com.google.android.material.textfield.TextInputEditText etName;
    private com.google.android.material.textfield.TextInputEditText etDescription;
    private Button btnSave, btnCancel;
    private ProgressBar progress;
    private Spinner spinnerCategory;

    private String initialCategoryId; // optional initial category passed in
    private List<RecipeCategory> categories = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        tvCategory = findViewById(R.id.tvCategoryLabel);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        progress = findViewById(R.id.progress);

        db = FirebaseFirestore.getInstance();

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        initialCategoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);

        loadCategories();

        btnSave.setOnClickListener(v -> {
            Recipe recipe = onSave();
            if (recipe == null) return;
            Intent i = new Intent(this, RecipeListActivity.class);
            i.putExtra(RecipeCategoryListActivity.EXTRA_CATEGORY_ID, recipe.getRecipeCategoryId());
            startActivity(i);
        });
        btnCancel.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });
    }

    private void loadCategories() {
        progress.setVisibility(VISIBLE);
        db.collection(RECIPE_CATEGORY_COLLECTION)
                .orderBy("name")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progress.setVisibility(GONE);
                    populateSpinnerFromSnapshot(querySnapshot);
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(GONE);
                    // fallback: show empty spinner entry
                    spinnerAdapter.clear();
                    spinnerAdapter.add("(no category)");
                    spinnerAdapter.notifyDataSetChanged();
                    if (initialCategoryId != null) {
                        // show initial id as text if present
                        spinnerAdapter.clear();
                        spinnerAdapter.add(initialCategoryId);
                        spinnerAdapter.notifyDataSetChanged();
                    }
                    Toast.makeText(AddRecipeActivity.this, "Failed to load categories: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void populateSpinnerFromSnapshot(QuerySnapshot snapshot) {
        categories.clear();
        spinnerAdapter.clear();

        if (snapshot == null || snapshot.isEmpty()) {
            spinnerAdapter.add("(no category)");
            spinnerAdapter.notifyDataSetChanged();
            return;
        }

        for (DocumentSnapshot ds : snapshot.getDocuments()) {
            String id = ds.getId();
            String name = ds.contains("name") ? ds.getString("name") : id;
            RecipeCategory c = new RecipeCategory(id, name);
            categories.add(c);
            spinnerAdapter.add(name);
        }
        spinnerAdapter.notifyDataSetChanged();

        if (initialCategoryId != null) {
            for (int i = 0; i < categories.size(); i++) {
                if (initialCategoryId.equals(categories.get(i).getId())) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }
    }

    private Recipe onSave() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        float rating = 0.0f;

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name required");
            return null;
        }

        if (TextUtils.isEmpty(description)) {
            etName.setError("Description required");
            return null;
        }

        String selectedCategoryId = null;
        int pos = spinnerCategory.getSelectedItemPosition();
        if (pos >= 0 && pos < categories.size()) {
            selectedCategoryId = categories.get(pos).getId();
        } else if (initialCategoryId != null && (categories.isEmpty() || pos == 0)) {
            // fallback to initial id if categories list empty but initial provided
            selectedCategoryId = initialCategoryId;
        }

        setUiEnabled(false);
        progress.setVisibility(VISIBLE);

        Recipe recipe = new Recipe();
        DocumentReference docRef = db.collection(RECIPE_COLLECTION).document();
        recipe.setId(docRef.getId());
        recipe.setName(name);
        recipe.setDescription(description);
        recipe.setRating(rating);
        recipe.setRecipeCategoryId(selectedCategoryId);

        Map<String, Object> data = recipe.toMap();
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid != null) data.put("ownerUid", uid);
        data.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        docRef.set(data)
                .addOnSuccessListener(aVoid -> {
                    progress.setVisibility(GONE);
                    Toast.makeText(AddRecipeActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                    Intent out = new Intent();
                    out.putExtra(EXTRA_RECIPE_ID, recipe.getId());
                    setResult(Activity.RESULT_OK, out);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(GONE);
                    setUiEnabled(true);
                    Toast.makeText(AddRecipeActivity.this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
        return recipe;
    }

    private void setUiEnabled(boolean enabled) {
        etName.setEnabled(enabled);
        etDescription.setEnabled(enabled);
        btnSave.setEnabled(enabled);
        btnCancel.setEnabled(enabled);
        spinnerCategory.setEnabled(enabled);
    }
}
