package com.dujo.chefsbook.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.dujo.chefsbook.data.model.Recipe;
import com.dujo.chefsbook.data.repository.RecipeRepository;
import java.util.List;

public class RecipeListViewModel extends ViewModel {
  private final RecipeRepository recipeRepository;
  private final MutableLiveData<List<Recipe>> recipes = new MutableLiveData<>();
  private final MutableLiveData<String> error = new MutableLiveData<>();
  private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

  public RecipeListViewModel() {
    recipeRepository = new RecipeRepository();
  }

  public void addListenerToRecipesByCategory(String recipeCategoryId) {
    recipeRepository.addListenerToRecipesByCategory(recipeCategoryId, recipes, error);
  }

  public LiveData<List<Recipe>> getRecipesByCategoryId() {
    return recipes;
  }

  public LiveData<String> getError() {
    return error;
  }

  public LiveData<Boolean> getLoading() {
    return loading;
  }
}
