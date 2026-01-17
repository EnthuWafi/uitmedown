package com.enth.uitmedown.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.enth.uitmedown.R;
import com.enth.uitmedown.databinding.ActivityLoginBinding;
import com.enth.uitmedown.model.FailLogin;
import com.enth.uitmedown.model.User;
import com.enth.uitmedown.remote.ApiUtils;
import com.enth.uitmedown.remote.UserService;
import com.enth.uitmedown.sharedpref.SharedPrefManager;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private EditText edtUsername;
    private EditText edtPassword;
    private SharedPrefManager spm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());

        // 2. Load UI if not logged in
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.login, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtUsername = binding.edtUsername;
        edtPassword = binding.edtPassword;

        Button btnLogin = binding.btnLogin;
        btnLogin.setOnClickListener(v -> loginClicked(v));

        binding.textViewRegister.setOnClickListener(v -> {
            // startActivity(new Intent(this, RegisterActivity.class));
        });

        spm = new SharedPrefManager(this);
        if (spm.isLoggedIn()) {
            validateSession(spm.getUser());
        } else {
            showLoginForm();
        }
    }

    public void loginClicked(View view) {
        String username = edtUsername.getText().toString();
        String password = edtPassword.getText().toString();

        if (validateLogin(username, password)) {
            doLogin(username, password);
        }
    }

    private void doLogin(String username, String password) {
        UserService userService = ApiUtils.getUserService();
        Call<User> call;
        showLoading();

        if (username.contains("@")) {
            call = userService.loginEmail(username, password);
        } else {
            call = userService.login(username, password);
        }

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    if (user != null && user.getToken() != null) {
                        displayToast("Login successful");

                        // Store user session
                        spm.storeUser(user);

                        // Forward to correct screen
                        redirectUser(user);
                    } else {
                        displayToast("Login error: Empty response");
                        showLoginForm();
                    }
                } else {
                    // Handle Errors
                    try {
                        String errorResp = response.errorBody().string();
                        FailLogin e = new Gson().fromJson(errorResp, FailLogin.class);
                        displayToast(e.getError().getMessage());
                    } catch (Exception e) {
                        Log.e("MyApp:", e.toString());
                        displayToast("Login Failed");
                        showLoginForm();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                displayToast("Error connecting to server.");
                Log.e("MyApp:", t.toString());
                showLoginForm();
            }
        });
    }

    /**
     * Handles the logic for where to send the user based on their role.
     */
    private void redirectUser(User user) {
        Intent intent;

        // CHECK ROLE
        if ("admin".equalsIgnoreCase(user.getRole())) {
            // If admin, go to Admin Dashboard
            intent = new Intent(LoginActivity.this, AdminMainActivity.class);
            // displayToast("Welcome Admin");
        } else {
            // If normal user, go to Main Activity
            intent = new Intent(LoginActivity.this, MainActivity.class);
        }

        // Clear the back stack so pressing 'Back' doesn't return to Login
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean validateLogin(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            displayToast("Username is required");
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            displayToast("Password is required");
            return false;
        }
        return true;
    }

    public void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void validateSession(User user) {
        showLoading();

        ApiUtils.getUserService().getUser(user.getToken(), user.getId()).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {

                    if(response.body() != null) spm.storeUser(response.body());

                    redirectUser(user);
                } else {
                    displayToast("Session expired. Please login again.");
                    spm.logout(); // Clear bad data
                    showLoginForm(); // Let them login again
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                displayToast("Network error. Could not verify session.");
                showLoginForm();
            }
        });
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.cardContainer.setVisibility(View.GONE);
        binding.textViewRegister.setVisibility(View.GONE);
    }

    private void showLoginForm() {
        binding.progressBar.setVisibility(View.GONE);
        binding.cardContainer.setVisibility(View.VISIBLE);
        binding.textViewRegister.setVisibility(View.VISIBLE);
    }

}