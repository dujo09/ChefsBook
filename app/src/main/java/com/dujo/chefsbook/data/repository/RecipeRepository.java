package com.dujo.chefsbook.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.dujo.chefsbook.data.model.Recipe;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeRepository {
    public static final String RECIPE_COLLECTION = "recipes";
    private final FirebaseAuth auth;
    private final CollectionReference recipeCollection;

    public RecipeRepository() {
        auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        recipeCollection = firestore.collection(RECIPE_COLLECTION);
    }

    public void getRecipes(MutableLiveData<List<Recipe>> liveList, MutableLiveData<String> error) {
        recipeCollection.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                error.postValue(e.getMessage());
                return;
            }
            if (snapshots == null) return;
            List<Recipe> list = new ArrayList<>();
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                Recipe p = doc.toObject(Recipe.class);
                if (p != null) {
                    p.setId(doc.getId());
                    list.add(p);
                }
            }
            liveList.postValue(list);
        });
    }
}
