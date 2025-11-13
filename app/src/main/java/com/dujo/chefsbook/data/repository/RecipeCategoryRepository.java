package com.dujo.chefsbook.data.repository;

import androidx.lifecycle.MutableLiveData;
import com.dujo.chefsbook.data.model.RecipeCategory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class RecipeCategoryRepository {
  public static final String RECIPE_CATEGORY_COLLECTION = "recipe_categories";
  private final FirebaseAuth auth;
  private final CollectionReference recipeCategoryCollection;

  public RecipeCategoryRepository() {
    auth = FirebaseAuth.getInstance();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    recipeCategoryCollection = firestore.collection(RECIPE_CATEGORY_COLLECTION);
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

  public void getRecipeCategories(
      MutableLiveData<List<RecipeCategory>> liveList, MutableLiveData<String> error) {
    recipeCategoryCollection.addSnapshotListener(
        (snapshots, e) -> {
          if (e != null) {
            error.postValue(e.getMessage());
            return;
          }
          if (snapshots == null) return;
          List<RecipeCategory> list = new ArrayList<>();
          for (DocumentSnapshot doc : snapshots.getDocuments()) {
            RecipeCategory p = doc.toObject(RecipeCategory.class);
            if (p != null) {
              p.setId(doc.getId());
              list.add(p);
            }
          }
          liveList.postValue(list);
        });
  }
}
