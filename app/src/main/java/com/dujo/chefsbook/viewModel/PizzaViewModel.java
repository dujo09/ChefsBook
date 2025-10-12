package com.dujo.chefsbook.viewModel;

import androidx.lifecycle.ViewModel;

import com.dujo.chefsbook.model.Pizza;

import java.util.List;

public class PizzaViewModel extends ViewModel {
    private final List<Pizza> pizzas;

    public PizzaViewModel() {
        this.pizzas = List.of(
                new Pizza("1", "Margarita", "Samo sir i šalša.", 13.0),
                new Pizza("2", "Mješana", "Klasična pizza sa šunkom i sirom.", 13.0),
                new Pizza("3", "Tartufata", "Dodatak pancete i bijelog tartufa.", 13.0),
                new Pizza("4", "Seljačka", "Za prave seljobere.", 13.0),
                new Pizza("5", "Slatka", "Sa nutellom i voćem.", 13.0)
        );
    }

    public List<Pizza> getPizzas() {
        return pizzas;
    }
}
