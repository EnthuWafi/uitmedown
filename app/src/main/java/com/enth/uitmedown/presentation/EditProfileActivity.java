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
import java.util.HashMap;
import java.util.Map;

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

        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupUI() {
        if (currentUser == null) return;

        binding.edtUsername.setText(currentUser.getUsername());
        binding.edtPhone.setText(currentUser.getPhoneNumber());

        // Email is usually read-only/disabled for edits
        binding.edtEmail.setText(currentUser.getEmail());
        binding.edtEmail.setEnabled(false);

        // Load existing profile pic
        if (currentUser.getPictureFile() != null) {
            String fullUrl = RetrofitClient.BASE_URL + currentUser.getPictureFile().getFile();
            Glide.with(this)
                    .load(fullUrl)
                    .circleCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery) // Safety placeholder
                    .into(binding.imgEditProfile);
        }
    }

    private void saveProfile() {
        binding.btnSaveProfile.setEnabled(false);
        binding.btnSaveProfile.setText("Saving...");

        // 1. If user picked a new image, upload it first
        if (selectedImageUri != null) {
            uploadImageAndUpdate();
        } else {
            // 2. Otherwise just update text fields
            updateUserInDatabase(null);
        }
    }

    private void uploadImageAndUpdate() {
        File file = FileUtils.getFileFromUri(this, selectedImageUri);
        // Safety check: FileUtils might fail if URI is weird
        if (file == null) {
            Toast.makeText(this, "Could not process image file", Toast.LENGTH_SHORT).show();
            resetButton();
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        ApiUtils.getUserService().uploadProfilePicture(currentUser.getToken(), body).enqueue(new Callback<FileModel>() {
            @Override
            public void onResponse(Call<FileModel> call, Response<FileModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Upload success! Now update the user profile with this new File ID
                    updateUserInDatabase(response.body());
                } else {
                    Toast.makeText(EditProfileActivity.this, "Image Upload Failed", Toast.LENGTH_SHORT).show();
                    resetButton();
                }
            }
            @Override
            public void onFailure(Call<FileModel> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Network Error during upload", Toast.LENGTH_SHORT).show();
                resetButton();
            }
        });
    }

    private void updateUserInDatabase(FileModel newFile) {
        String newUsername = binding.edtUsername.getText().toString().trim();
        String newPhone = binding.edtPhone.getText().toString().trim();

        // --- THE FIX: USE A MAP INSTEAD OF THE USER OBJECT ---
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", newUsername);
        updates.put("phone_number", newPhone);

        // Only add picture_file_id if we actually uploaded a new one
        if (newFile != null) {
            updates.put("picture_file_id", newFile.getId());
        }

        ApiUtils.getUserService().updateUserField(currentUser.getToken(), currentUser.getId(), updates)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful()) {
                            // 1. Update the Local User Object Manually
                            // (We trust our inputs more than the server echo sometimes)
                            currentUser.setUsername(newUsername);
                            currentUser.setPhoneNumber(newPhone);
                            if (newFile != null) {
                                currentUser.setPictureFile(newFile);
                                currentUser.setPictureFileId(newFile.getId());
                            }

                            // 2. Save to SharedPrefs so the app remembers
                            spm.storeUser(currentUser);

                            Toast.makeText(EditProfileActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();

                            // 3. Return OK to previous screen (ProfileFragment)
                            Intent resultIntent = new Intent();
                            setResult(RESULT_OK, resultIntent);
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