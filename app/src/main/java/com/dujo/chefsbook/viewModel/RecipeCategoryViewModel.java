package com.dujo.chefsbook.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.dujo.chefsbook.data.model.RecipeCategory;
import com.dujo.chefsbook.data.repository.RecipeCategoryRepository;
import java.util.List;

public class RecipeCategoryViewModel extends ViewModel {
  private final MutableLiveData<List<RecipeCategory>> recipeCategories = new MutableLiveData<>();
  private final MutableLiveData<String> error = new MutableLiveData<>();
  private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

  public RecipeCategoryViewModel() {
    RecipeCategoryRepository repo = new RecipeCategoryRepository();
    repo.getRecipeCategories(recipeCategories, error);
  }

  public LiveData<List<RecipeCategory>> getRecipeCategories() {
    return recipeCategories;
  }

  public LiveData<String> getError() {
    return error;
  }

  public LiveData<Boolean> getLoading() {
    return loading;
  }
}
