package com.enth.uitmedown.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.enth.uitmedown.databinding.ActivityChangePasswordBinding;
import com.enth.uitmedown.model.User;
import com.enth.uitmedown.remote.ApiUtils;
import com.enth.uitmedown.sharedpref.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSubmitChange.setOnClickListener(v -> attemptChangePassword());
    }

    private void attemptChangePassword() {
        String oldPass = binding.edtOldPass.getText().toString();
        String newPass = binding.edtNewPass.getText().toString();
        String confirmPass = binding.edtConfirmPass.getText().toString();

        if (oldPass.isEmpty() || newPass.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            binding.edtConfirmPass.setError("Passwords do not match");
            return;
        }

        if (newPass.length() < 6) {
            binding.edtNewPass.setError("Password must be at least 6 characters");
            return;
        }

        // 2. API Call
        binding.btnSubmitChange.setEnabled(false);
        binding.btnSubmitChange.setText("Updating...");

        User user = new SharedPrefManager(this).getUser();

        ApiUtils.getUserService().changePassword(user.getEmail(), oldPass, newPass)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful()) {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("MESSAGE", "Password changed successfully!");
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            Toast.makeText(ChangePasswordActivity.this, "Failed: Incorrect old password", Toast.LENGTH_LONG).show();
                            resetButton();
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Toast.makeText(ChangePasswordActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                        resetButton();
                    }
                });
    }

    private void resetButton() {
        binding.btnSubmitChange.setEnabled(true);
        binding.btnSubmitChange.setText("Update Password");
    }
}