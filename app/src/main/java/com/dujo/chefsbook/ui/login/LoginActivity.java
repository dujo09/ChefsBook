package com.dujo.chefsbook.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.dujo.chefsbook.R;
import com.dujo.chefsbook.data.repository.UserRepository;
import com.dujo.chefsbook.ui.register.RegisterActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
  private static final String TAG = "LoginActivity";
  private FirebaseAuth auth;
  private TextInputEditText etEmail, etPassword;
  private TextView tvError, tvRegister;
  private Button btnSubmit;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    auth = FirebaseAuth.getInstance();
    etEmail = findViewById(R.id.etEmail);
    etPassword = findViewById(R.id.etPassword);
    tvError = findViewById(R.id.tvLoginError);
    btnSubmit = findViewById(R.id.btnLogin);
    tvRegister = findViewById(R.id.tvRegister);

    btnSubmit.setOnClickListener(v -> attemptLogin());
    tvRegister.setOnClickListener(
        v -> {
          Intent i = new Intent(this, RegisterActivity.class);
          startActivity(i);
        });
  }

  private void attemptLogin() {
    tvError.setVisibility(View.GONE);
    btnSubmit.setEnabled(false);

    String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
    String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

    if (TextUtils.isEmpty(email)) {
      etEmail.setError("Email required");
      return;
    }
    if (TextUtils.isEmpty(password)) {
      etPassword.setError("Password required");
      return;
    }

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(
            this,
            task -> {
              btnSubmit.setEnabled(true);
              if (task.isSuccessful()) {
                UserRepository.getInstance(this).refreshFromServer();
                finish();
              } else {
                Log.w(TAG, "Login failed", task.getException());
                tvError.setText(
                    task.getException() != null
                        ? task.getException().getMessage()
                        : "Authentication failed");
                tvError.setVisibility(View.VISIBLE);
                Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT)
                    .show();
              }
            });
  }
}
