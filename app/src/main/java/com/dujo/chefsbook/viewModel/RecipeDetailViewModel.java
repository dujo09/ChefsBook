package com.dujo.chefsbook.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.dujo.chefsbook.data.model.Rating;
import com.dujo.chefsbook.data.model.Recipe;
import com.dujo.chefsbook.data.repository.RecipeRepository;
import java.util.List;

public class RecipeDetailViewModel extends ViewModel {
  private final RecipeRepository recipeRepository;
  private final MutableLiveData<Recipe> recipe = new MutableLiveData<>();
  private final MutableLiveData<Float> userRating = new MutableLiveData<>();
  private final MutableLiveData<List<Float>> ratings = new MutableLiveData<>();
  private final MutableLiveData<String> error = new MutableLiveData<>();
  private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

  public RecipeDetailViewModel() {
    recipeRepository = new RecipeRepository();
  }

  public void addListenerToRecipesByCategory(String recipeId) {
    recipeRepository.addListenerToRecipeRatings(recipeId, ratings, error);
  }

  public void getRecipeById(String id) {
    recipeRepository.getRecipeById(id, recipe, error);
  }

  public void getUserRatingForRecipe(String recipeId, String userId) {
    recipeRepository.getUserRatingForRecipe(recipeId, userId, userRating, error);
  }

  public void rateRecipe(String recipeId, String userId, Rating rating) {
    recipeRepository.rateRecipe(recipeId, userId, userRating, rating, error);
    Recipe updatedRecipe = new Recipe(recipe.getValue());
    updatedRecipe.setRating(rating.getRating());
    updateRecipe(recipeId, updatedRecipe);
  }

  public void updateRecipe(String recipeId, Recipe recipe) {
    recipeRepository.updateRecipe(recipeId, recipe, error);
  }

  public void deleteRecipe(String recipeId) {
    recipeRepository.deleteRecipe(recipeId, error);
  }

  public LiveData<Float> getUserRating() {
    return userRating;
  }

  public LiveData<List<Float>> getRatings() {
    return ratings;
  }

  public LiveData<Recipe> getRecipe() {
    return recipe;
  }

  public LiveData<String> getError() {
    return error;
  }

  public LiveData<Boolean> getLoading() {
    return loading;
  }
}
