package com.dujo.chefsbook.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.dujo.chefsbook.data.model.Recipe;
import com.dujo.chefsbook.data.model.RecipeCategory;
import com.dujo.chefsbook.data.repository.RecipeCategoryRepository;
import com.dujo.chefsbook.data.repository.RecipeRepository;
import java.util.List;

public class AddRecipeViewModel extends ViewModel {
  private final RecipeRepository recipeRepository;
  private final RecipeCategoryRepository recipeCategoryRepository;
  private final MutableLiveData<List<RecipeCategory>> recipeCategories = new MutableLiveData<>();
  private final MutableLiveData<String> error = new MutableLiveData<>();
  private final MutableLiveData<Recipe> addedRecipe = new MutableLiveData<>();

  public AddRecipeViewModel() {
    recipeRepository = new RecipeRepository();
    recipeCategoryRepository = new RecipeCategoryRepository();
    recipeCategoryRepository.getRecipeCategories(recipeCategories, error);
  }

  public void addRecipe(Recipe recipe) {
    recipeRepository.addRecipe(recipe, addedRecipe, error);
  }

  public LiveData<String> getError() {
    return error;
  }

  public LiveData<Recipe> getAddedRecipe() {
    return addedRecipe;
  }

  public LiveData<List<RecipeCategory>> getRecipeCategories() {
    return recipeCategories;
  }
}
