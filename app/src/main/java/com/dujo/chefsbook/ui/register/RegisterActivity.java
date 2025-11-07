package com.dujo.chefsbook.ui.register;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dujo.chefsbook.R;
import com.dujo.chefsbook.data.repository.UserRepository;
import com.dujo.chefsbook.ui.recipeCategory.RecipeCategoryListActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextInputEditText etEmail, etUsername, etPassword;
    private TextView tvError;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tvError = findViewById(R.id.tvRegisterError);
        btnSubmit = findViewById(R.id.btnSubmitRegister);

        btnSubmit.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        tvError.setVisibility(View.GONE);
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String usernameRaw = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String username = normalizeUsername(usernameRaw);
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email required");
            return;
        }
        if (TextUtils.isEmpty(username) || username.length() < 3) {
            etUsername.setError("Valid username required");
            return;
        }
        if (!username.matches("^[a-z0-9._-]{3,30}$")) {
            etUsername.setError("Invalid username format");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password (min 6 characters)");
            return;
        }

        btnSubmit.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        btnSubmit.setEnabled(true);
                        Log.w(TAG, "createUser failed", task.getException());
                        tvError.setText(task.getException() != null ? task.getException().getMessage() : "Registration failed");
                        tvError.setVisibility(View.VISIBLE);
                        return;
                    }

                    FirebaseUser user = auth.getCurrentUser();
                    if (user == null) {
                        btnSubmit.setEnabled(true);
                        tvError.setText("Registration error: user null");
                        tvError.setVisibility(View.VISIBLE);
                        return;
                    }
                    String uid = user.getUid();

                    DocumentReference usernameRef = db.collection("usernames").document(username);
                    DocumentReference userRef = db.collection("users").document(uid);

                    db.runTransaction((Transaction.Function<Void>) transaction -> {
                        DocumentSnapshot snap = transaction.get(usernameRef);
                        if (snap.exists()) {
                            throw new FirebaseFirestoreException("Username taken", FirebaseFirestoreException.Code.ABORTED);
                        }

                        Map<String, Object> uname = new HashMap<>();
                        uname.put("uid", uid);
                        uname.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
                        transaction.set(usernameRef, uname);

                        Map<String, Object> profile = new HashMap<>();
                        profile.put("username", username);
                        profile.put("role", "user");
                        profile.put("email", email);
                        profile.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
                        transaction.set(userRef, profile, SetOptions.merge());

                        return null;
                    }).addOnSuccessListener(aVoid -> {
                        UserRepository.getInstance(this).refreshFromServer();
                        Log.i(TAG, "Registered and claimed username: " + username);
                        startActivity(new Intent(RegisterActivity.this, RecipeCategoryListActivity.class));
                        finish();
                    }).addOnFailureListener(e -> {
                        btnSubmit.setEnabled(true);
                        Log.w(TAG, "Transaction failed", e);

                        if (e instanceof FirebaseFirestoreException
                                && ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.ABORTED) {

                            tvError.setText("Username already taken, choose another.");
                            tvError.setVisibility(View.VISIBLE);

                            FirebaseUser toDelete = FirebaseAuth.getInstance().getCurrentUser();
                            if (toDelete != null) {
                                toDelete.delete().addOnCompleteListener(delTask -> {
                                    if (delTask.isSuccessful()) {
                                        Log.i(TAG, "Deleted orphan auth user after collision");
                                    } else {
                                        Log.e(TAG, "Failed to delete orphan auth user", delTask.getException());
                                    }
                                });
                            }
                        } else {
                            tvError.setText(e.getMessage());
                            tvError.setVisibility(View.VISIBLE);
                        }
                    });
                });
    }

    private String normalizeUsername(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase().replaceAll("\\s+", "");
    }
}
