package com.dujo.chefsbook.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dujo.chefsbook.data.model.FoodCategory;
import com.dujo.chefsbook.data.repository.FoodCategoryRepository;

import java.util.List;

public class FoodCategoryViewModel extends ViewModel {
    private final MutableLiveData<List<FoodCategory>> pizzas = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public FoodCategoryViewModel() {
        FoodCategoryRepository repo = new FoodCategoryRepository();
        repo.getFoodCategories(pizzas, error);
    }

    public LiveData<List<FoodCategory>> getPizzas() {
        return pizzas;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }
}
