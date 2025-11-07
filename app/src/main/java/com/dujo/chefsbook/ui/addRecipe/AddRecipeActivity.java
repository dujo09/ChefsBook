package com.dujo.chefsbook.ui.addRecipe;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.dujo.chefsbook.data.repository.RecipeCategoryRepository.RECIPE_CATEGORY_COLLECTION;
import static com.dujo.chefsbook.data.repository.RecipeRepository.RECIPE_COLLECTION;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.dujo.chefsbook.R;
import com.dujo.chefsbook.data.model.Recipe;
import com.dujo.chefsbook.data.model.RecipeCategory;
import com.dujo.chefsbook.ui.recipe.RecipeListActivity;
import com.dujo.chefsbook.ui.recipeCategory.RecipeCategoryListActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
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

    private com.google.android.material.textfield.TextInputEditText etImageUrl;
    private ImageView ivPreview;

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
        etImageUrl = findViewById(R.id.etImageUrl);
        ivPreview = findViewById(R.id.ivPreview);

        db = FirebaseFirestore.getInstance();

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        initialCategoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);

        loadCategories();

        etImageUrl.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                showPreviewFromUrl(etImageUrl.getText() != null ? etImageUrl.getText().toString().trim() : null);
            }
        });

        btnSave.setOnClickListener(v -> {
            Recipe recipe = saveRecipe();
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

    private void showPreviewFromUrl(@Nullable String url) {
        if (url == null || url.isEmpty() || !Patterns.WEB_URL.matcher(url).matches()) {
            ivPreview.setVisibility(View.GONE);
            return;
        }
        ivPreview.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(url)
                .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                .error(android.R.drawable.ic_menu_report_image)
                .into(ivPreview);
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
            String imageUrl = ds.getString("imageUrl");
            RecipeCategory c = new RecipeCategory(id, name, imageUrl);
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

    private Recipe saveRecipe() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        float rating = 0;
        String imageUrl = etImageUrl.getText() != null ? etImageUrl.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name required");
            return null;
        }

        // require image URL
        if (TextUtils.isEmpty(imageUrl) || !Patterns.WEB_URL.matcher(imageUrl).matches()) {
            etImageUrl.setError("Please enter a valid image URL (http/https)");
            etImageUrl.requestFocus();
            return null;
        }

        // optional: quick content-type check by URL ending (jpg/png) - not mandatory
        if (!(imageUrl.endsWith(".jpg") || imageUrl.endsWith(".jpeg") || imageUrl.endsWith(".png") || imageUrl.contains("img") )) {
            // just a soft warning; you can enforce stricter rules if you want
        }

        // preview the image (immediate feedback)
        showPreviewFromUrl(imageUrl);

        // determine selected category id
        String selectedCategoryId = null;
        int pos = spinnerCategory.getSelectedItemPosition();
        if (pos >= 0 && pos < categories.size()) selectedCategoryId = categories.get(pos).getId();

        // build recipe map and save to Firestore
        setUiEnabled(false);
        progress.setVisibility(View.VISIBLE);

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("description", description);
        data.put("rating", rating);
        data.put("recipeCategoryId", selectedCategoryId);
        data.put("imageUrl", imageUrl);
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) data.put("ownerUid", uid);
        data.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        String docId = FirebaseFirestore.getInstance().collection("recipes").document().getId();
        FirebaseFirestore.getInstance().collection("recipes").document(docId)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(AddRecipeActivity.this, "Recipe saved", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    setUiEnabled(true);
                    Toast.makeText(AddRecipeActivity.this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
        return Recipe.fromMap(docId, data);
    }

    private void setUiEnabled(boolean enabled) {
        etName.setEnabled(enabled);
        etDescription.setEnabled(enabled);
        btnSave.setEnabled(enabled);
        btnCancel.setEnabled(enabled);
        spinnerCategory.setEnabled(enabled);
    }
}
