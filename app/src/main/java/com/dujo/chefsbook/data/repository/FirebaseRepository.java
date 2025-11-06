package com.dujo.chefsbook.data.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.dujo.chefsbook.data.model.Pizza;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FirebaseRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final CollectionReference pizzasRef;

    public FirebaseRepository() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        pizzasRef = firestore.collection("pizzas");
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

    public void fetchPizzas(MutableLiveData<List<Pizza>> liveList, MutableLiveData<String> error) {
        Log.i("TAG", "kopilica fetch: ");
//        firestore.collection("pizzas")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d("TAG", document.getId() + " => " + document.getData());
//
//                            }
//                        } else {
//                            Log.w("TAG", "Error getting documents.", task.getException());
//                        }
//                    }
//                });

        pizzasRef.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                error.postValue(e.getMessage());
                return;
            }
            if (snapshots == null) return;
            List<Pizza> list = new ArrayList<>();
            list.add(new Pizza("2", "wow", "wooooo", 3.2));
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                Pizza p = doc.toObject(Pizza.class);
                if (p != null) {
                    p.setId(doc.getId());
                    list.add(p);
                }
            }
            Log.i("TAG", "kopilica fetch: " + list);
            liveList.postValue(list);
        });
    }

//    public void addPizza(Pizza pizza, OnCompleteListener<DocumentReference> callback) {
//        pizzasRef.add(pizza).addOnCompleteListener(callback);
//    }
//
//    public void updatePizza(Pizza pizza, OnCompleteListener<Void> callback) {
//        if (pizza.getId() == null) {
//            callback.onComplete(Task.forException(new IllegalArgumentException("Pizza id is null")));
//            return;
//        }
//        pizzasRef.document(pizza.getId()).set(pizza).addOnCompleteListener(callback);
//    }
//
//    public void deletePizza(String pizzaId, OnCompleteListener<Void> callback) {
//        pizzasRef.document(pizzaId).delete().addOnCompleteListener(callback);
//    }
}
