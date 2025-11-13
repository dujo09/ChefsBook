package com.dujo.chefsbook.data.repository;

import androidx.lifecycle.MutableLiveData;
import com.dujo.chefsbook.data.model.Recipe;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class RecipeRepository {
  public static final String RECIPE_COLLECTION = "recipes";
  private final CollectionReference recipeCollection;

  public RecipeRepository() {
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    recipeCollection = firestore.collection(RECIPE_COLLECTION);
  }

  public void addListenerToRecipesByCategory(
      String recipeCategoryId,
      MutableLiveData<List<Recipe>> recipes,
      MutableLiveData<String> error) {
    recipeCollection.addSnapshotListener(
        (snapshots, e) -> {
          if (e != null) {
            error.postValue(e.getMessage());
            return;
          }
          if (snapshots == null) return;
          List<Recipe> list = new ArrayList<>();
          for (DocumentSnapshot doc : snapshots.getDocuments()) {
            Recipe recipe = doc.toObject(Recipe.class);
            if (recipe == null) continue;
            if (!recipe.getRecipeCategoryId().equals(recipeCategoryId)) continue;
            recipe.setId(doc.getId());
            list.add(recipe);
          }
          recipes.postValue(list);
        });
  }

  public void addRecipe(
      Recipe recipe, MutableLiveData<Recipe> addedRecipe, MutableLiveData<String> error) {
    recipeCollection
        .add(recipe)
        .addOnSuccessListener(
            documentReference ->
                addedRecipe.postValue(documentReference.get().getResult().toObject(Recipe.class)))
        .addOnFailureListener(e -> error.postValue(e.getMessage()));
  }
}
