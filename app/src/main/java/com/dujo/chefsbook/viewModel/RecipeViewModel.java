package com.dujo.chefsbook.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dujo.chefsbook.data.model.Recipe;
import com.dujo.chefsbook.data.repository.RecipeRepository;

import java.util.ArrayList;
import java.util.List;

public class RecipeViewModel extends ViewModel {
    private final MutableLiveData<List<Recipe>> recipes = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public MutableLiveData<String> getSelectedCategoryId() {
        return selectedCategoryId;
    }

    private final MutableLiveData<String> selectedCategoryId = new MutableLiveData<>();
    private final MediatorLiveData<List<Recipe>> filteredRecipes = new MediatorLiveData<>();

    public RecipeViewModel() {
        RecipeRepository recipeRepository = new RecipeRepository();
        recipeRepository.getRecipes(recipes, error);

        filteredRecipes.addSource(recipes, list -> recompute(list, selectedCategoryId.getValue()));
        filteredRecipes.addSource(selectedCategoryId, id -> recompute(recipes.getValue(), id));
    }

    private void recompute(List<Recipe> list, String categoryId) {
        List<Recipe> out = new ArrayList<>();
        if (list == null || categoryId == null) {
            filteredRecipes.setValue(out);
            return;
        }
        for (Recipe recipe : list) {
            if (categoryId.equals(recipe.getRecipeCategoryId())) out.add(recipe);
        }
        filteredRecipes.setValue(out);
    }

    public LiveData<List<Recipe>> getFilteredRecipes() {
        return filteredRecipes;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }
}
