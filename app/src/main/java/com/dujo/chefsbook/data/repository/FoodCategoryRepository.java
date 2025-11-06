package com.dujo.chefsbook.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.dujo.chefsbook.data.model.FoodCategory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FoodCategoryRepository {
    public static final String FOOD_CATEGORY_COLLECTION = "food_categories";
    private final FirebaseAuth auth;
    private final CollectionReference foodCategoryCollection;

    public FoodCategoryRepository() {
        auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        foodCategoryCollection = firestore.collection(FOOD_CATEGORY_COLLECTION);
    }

    public void login(String email, String password, OnCompleteListener<AuthResult> callback) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(callback);
    }

    public void register(String email, String password, OnCompleteListener<AuthResult> callback) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(callback);
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void signOut() {
        auth.signOut();
    }

    public void getFoodCategories(MutableLiveData<List<FoodCategory>> liveList, MutableLiveData<String> error) {
        foodCategoryCollection.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                error.postValue(e.getMessage());
                return;
            }
            if (snapshots == null) return;
            List<FoodCategory> list = new ArrayList<>();
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                FoodCategory p = doc.toObject(FoodCategory.class);
                if (p != null) {
                    p.setId(doc.getId());
                    list.add(p);
                }
            }
            liveList.postValue(list);
        });
    }
}
