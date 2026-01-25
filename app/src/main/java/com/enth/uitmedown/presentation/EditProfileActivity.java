package com.enth.uitmedown.presentation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.enth.uitmedown.databinding.ActivityEditProfileBinding;
import com.enth.uitmedown.model.FileModel;
import com.enth.uitmedown.model.User;
import com.enth.uitmedown.remote.ApiUtils;
import com.enth.uitmedown.remote.RetrofitClient;
import com.enth.uitmedown.sharedpref.SharedPrefManager;
import com.enth.uitmedown.utils.FileUtils;
import java.io.File;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private SharedPrefManager spm;
    private User currentUser;
    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        spm = new SharedPrefManager(this);
        currentUser = spm.getUser();

        setupUI();

        // Image Picker Logic
        ActivityResultLauncher<String> imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        binding.imgEditProfile.setImageURI(uri);
                    }
                }
        );

        binding.btnChangePhoto.setOnClickListener(v -> imagePicker.launch("image/*"));

        binding.btnSaveProfile.setOnClickListener(v -> {
            if (validateInput()) {
                saveProfile();
            }
        });
    }

    private void setupUI() {
        binding.edtUsername.setText(currentUser.getUsername());
        binding.edtPhone.setText(currentUser.getPhoneNumber());
        binding.edtEmail.setText(currentUser.getEmail());

        // Load existing profile pic
        if (currentUser.getPictureFile() != null) {
            String fullUrl = RetrofitClient.BASE_URL + currentUser.getPictureFile().getFile();
            Glide.with(this).load(fullUrl).circleCrop().into(binding.imgEditProfile);
        }
    }

    private void saveProfile() {
        binding.btnSaveProfile.setEnabled(false);
        binding.btnSaveProfile.setText("Saving...");

        // Scenario 1: User picked a new image -> Upload first
        if (selectedImageUri != null) {
            uploadImageAndUpdate();
        }
        // Scenario 2: Text only update
        else {
            updateUserInDatabase(null);
        }
    }

    private void uploadImageAndUpdate() {
        File file = FileUtils.getFileFromUri(this, selectedImageUri);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        // Reuse UserService to upload
        ApiUtils.getUserService().uploadProfilePicture(currentUser.getToken(), body).enqueue(new Callback<FileModel>() {
            @Override
            public void onResponse(Call<FileModel> call, Response<FileModel> response) {
                if (response.isSuccessful()) {
                    updateUserInDatabase(response.body());
                } else {
                    Toast.makeText(EditProfileActivity.this, "Image Upload Failed", Toast.LENGTH_SHORT).show();
                    resetButton();
                }
            }
            @Override
            public void onFailure(Call<FileModel> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                resetButton();
            }
        });
    }

    private void updateUserInDatabase(FileModel newFile) {
        // 1. Update local object with input fields
        currentUser.setUsername(binding.edtUsername.getText().toString());
        currentUser.setPhoneNumber(binding.edtPhone.getText().toString());

        // 2. If new image was uploaded, attach the ID
        if (newFile != null) {
            currentUser.setPictureFileId(newFile.getId());
            currentUser.setPictureFile(newFile);
        }

        // 3. Call API
        ApiUtils.getUserService().updateUser(currentUser.getToken(), currentUser.getId(), currentUser)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful()) {
                            // CRITICAL: Save new data to SharedPrefs
                            User updatedUser = response.body();
                            if (updatedUser != null) {
                                updatedUser.setToken(currentUser.getToken());
                                spm.storeUser(updatedUser);
                            } else {
                                spm.storeUser(currentUser);
                            }

                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Update Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                            resetButton();
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Toast.makeText(EditProfileActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                        resetButton();
                    }
                });
    }

    private boolean validateInput() {
        if (binding.edtUsername.getText().toString().trim().isEmpty()) {
            binding.edtUsername.setError("Username is required");
            return false;
        }
        return true;
    }

    private void resetButton() {
        binding.btnSaveProfile.setEnabled(true);
        binding.btnSaveProfile.setText("Save Changes");
    }
}