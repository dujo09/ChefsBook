package com.dujo.chefsbook.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dujo.chefsbook.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private static final String PREFS = "user_prefs";
    private static final String KEY_USER = "cached_user";
    private static UserRepository instance;
    private final SharedPreferences prefs;
    private final FirebaseFirestore firestore;
    private final MutableLiveData<User> userLive = new MutableLiveData<>();
    private final Gson gson = new Gson();

    private UserRepository(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        firestore = FirebaseFirestore.getInstance();
        String json = prefs.getString(KEY_USER, null);
        if (json != null) {
            try {
                User cached = gson.fromJson(json, User.class);
                userLive.postValue(cached);
            } catch (Exception e) {
                Log.w(TAG, "failed to parse cached user", e);
            }
        }
    }

    public static synchronized UserRepository getInstance(Context ctx) {
        if (instance == null) instance = new UserRepository(ctx);
        return instance;
    }

    public LiveData<User> getUserLive() {
        return userLive;
    }

    public void refreshFromServer() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            userLive.postValue(null);
            return;
        }
        firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User u = doc.toObject(User.class);
                        if (u != null) {
                            u.uid = uid;
                            if (u.email == null)
                                u.email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                            userLive.postValue(u);
                            saveToPrefs(u);
                        }
                    } else {
                        userLive.postValue(null);
                        prefs.edit().remove(KEY_USER).apply();
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "refresh failed", e));
    }

    public void saveToPrefs(User u) {
        try {
            String json = gson.toJson(u);
            prefs.edit().putString(KEY_USER, json).apply();
        } catch (Exception e) {
            Log.w(TAG, "save prefs failed", e);
        }
    }

    public void clearCache() {
        prefs.edit().remove(KEY_USER).apply();
        userLive.postValue(null);
    }
}
