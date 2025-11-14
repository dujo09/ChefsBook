package com.dujo.chefsbook.data.repository;

import androidx.lifecycle.MutableLiveData;
import com.dujo.chefsbook.data.model.Rating;
import com.dujo.chefsbook.data.model.Recipe;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class RecipeRepository {
  public static final String RECIPE_COLLECTION = "recipes";
  public static final String RATINGS_COLLECTION = "ratings";
  private final CollectionReference recipeCollection;

  public RecipeRepository() {
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    recipeCollection = firestore.collection(RECIPE_COLLECTION);
  }

  public void addListenerToRecipesByCategory(
      String recipeCategoryId,
      MutableLiveData<List<Recipe>> recipes,
      MutableLiveData<String> error) {
    recipeCollection
        .whereEqualTo("recipeCategoryId", recipeCategoryId)
        .addSnapshotListener(
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
                recipe.setId(doc.getId());
                list.add(recipe);
              }
              recipes.postValue(list);
            });
  }

  public void addListenerToRecipeRatings(
      String recipeId, MutableLiveData<List<Rating>> ratings, MutableLiveData<String> error) {
    recipeCollection
        .document(recipeId)
        .collection(RATINGS_COLLECTION)
        .addSnapshotListener(
            (snapshots, e) -> {
              if (e != null) {
                error.postValue(e.getMessage());
                return;
              }
              if (snapshots == null) return;
              List<Rating> list = new ArrayList<>();
              for (DocumentSnapshot doc : snapshots.getDocuments()) {
                Rating rating = doc.toObject(Rating.class);
                if (rating != null) {
                  list.add(rating);
                }
              }
              ratings.postValue(list);
            });
  }

  public void getRecipeById(
      String id, MutableLiveData<Recipe> recipe, MutableLiveData<String> error) {
    recipeCollection
        .document(id)
        .get()
        .addOnSuccessListener(
            documentSnapshot -> {
              recipe.postValue(documentSnapshot.toObject(Recipe.class));
            })
        .addOnFailureListener(e -> error.postValue(e.getMessage()));
  }

  public void getUserRatingForRecipe(
      String recipeId,
      String userId,
      MutableLiveData<Rating> userRating,
      MutableLiveData<String> error) {
    recipeCollection
        .document(recipeId)
        .collection(RATINGS_COLLECTION)
        .document(userId)
        .get()
        .addOnSuccessListener(
            documentSnapshot -> {
              Rating rating = documentSnapshot.toObject(Rating.class);
              if (rating != null) {
                userRating.postValue(rating);
              }
            })
        .addOnFailureListener(e -> error.postValue(e.getMessage()));
  }

  public void addRecipe(
      Recipe recipe, MutableLiveData<Recipe> addedRecipe, MutableLiveData<String> error) {
    recipeCollection
        .add(recipe)
        .addOnSuccessListener(documentReference -> addedRecipe.postValue(recipe))
        .addOnFailureListener(e -> error.postValue(e.getMessage()));
  }

  public void updateRecipe(String id, Recipe recipe, MutableLiveData<String> error) {
    recipeCollection
        .document(id)
        .set(recipe)
        .addOnFailureListener(e -> error.postValue(e.getMessage()));
  }

  public void deleteRecipe(String id, MutableLiveData<String> error) {
    recipeCollection
        .document(id)
        .delete()
        .addOnFailureListener(e -> error.postValue(e.getMessage()));
  }

  public void rateRecipe(
      String recipeId,
      String userId,
      MutableLiveData<Rating> userRating,
      Rating newRating,
      MutableLiveData<String> error) {
    recipeCollection
        .document(recipeId)
        .collection(RATINGS_COLLECTION)
        .document(userId)
        .set(newRating)
        .addOnSuccessListener(
            unused -> {
              userRating.postValue(newRating);

              recipeCollection
                  .document(recipeId)
                  .collection(RATINGS_COLLECTION)
                  .get()
                  .addOnSuccessListener(
                      snapshots -> {
                        float averageRating = 0.0f;
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                          Rating rating = doc.toObject(Rating.class);
                          if (rating != null) {
                            averageRating += rating.getRating();
                          }
                        }
                        averageRating = averageRating / snapshots.size();

                        recipeCollection
                            .document(recipeId)
                            .update("rating", averageRating)
                            .addOnFailureListener(e -> error.postValue(e.getMessage()));
                      })
                  .addOnFailureListener(e -> error.postValue(e.getMessage()));
            })
        .addOnFailureListener(e -> error.postValue(e.getMessage()));
  }
}
