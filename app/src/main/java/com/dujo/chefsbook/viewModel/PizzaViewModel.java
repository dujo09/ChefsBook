package com.dujo.chefsbook.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dujo.chefsbook.data.model.Pizza;
import com.dujo.chefsbook.data.repository.FirebaseRepository;

import java.util.List;

public class PizzaViewModel extends ViewModel {
    private final FirebaseRepository repo;
    private final MutableLiveData<List<Pizza>> pizzas = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public PizzaViewModel() {
        repo = new FirebaseRepository();
        repo.fetchPizzas(pizzas, error);
    }

    public LiveData<List<Pizza>> getPizzas() {
        return pizzas;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }
}
