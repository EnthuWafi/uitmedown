package com.enth.uitmedown.presentation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide; // Make sure you have Glide dependency
import com.enth.uitmedown.R;
import com.enth.uitmedown.databinding.ActivityCreateItemBinding;
import com.enth.uitmedown.model.FileModel;
import com.enth.uitmedown.model.Item;
import com.enth.uitmedown.model.User;
import com.enth.uitmedown.remote.ApiUtils;
import com.enth.uitmedown.remote.ItemService;
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

public class CreateItemActivity extends AppCompatActivity {

    private ActivityCreateItemBinding binding;
    private Uri selectedImageUri = null;
    private ItemService itemService;
    private SharedPrefManager spm;

    // Edit Mode Tracking
    private boolean isEditMode = false;
    private Item itemToEdit = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateItemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            boolean isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            int keyboardHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            v.setPadding(0, 0, 0, isKeyboardVisible ? keyboardHeight : 0);
            return insets;
        });

        itemService = ApiUtils.getItemService();
        spm = new SharedPrefManager(getApplicationContext());

        // 1. Check Session
        if (!spm.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 2. Setup Image Picker
        ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        binding.imgPreview.setImageURI(uri);
                        binding.imgPreview.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                    }
                }
        );
        binding.imgPreview.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // 3. CHECK FOR EDIT MODE
        if (getIntent().getSerializableExtra("EDIT_ITEM") != null) {
            isEditMode = true;
            itemToEdit = (Item) getIntent().getSerializableExtra("EDIT_ITEM");
            setupEditModeUI();
        }

        // 4. Single Button Logic
        binding.btnPost.setOnClickListener(v -> {
            if (validateInputs()) {
                handleSubmit();
            }
        });
    }

    private void setupEditModeUI() {
        binding.edtTitle.setText(itemToEdit.getTitle());
        binding.edtPrice.setText(String.valueOf(itemToEdit.getPrice()));
        binding.edtDescription.setText(itemToEdit.getDescription());
        binding.btnPost.setText("Update Item");

        if (itemToEdit.getFile() != null && itemToEdit.getFile().getFile() != null) {

            String relativePath = itemToEdit.getFile().getFile();
            String fullUrl = RetrofitClient.BASE_URL + relativePath;

            Glide.with(this)
                    .load(fullUrl)
                    .centerCrop()
                    .into(binding.imgPreview);
        }
    }

    private boolean validateInputs() {
        if (binding.edtTitle.getText().toString().isEmpty()) {
            binding.edtTitle.setError("Title required");
            return false;
        }
        if (binding.edtPrice.getText().toString().isEmpty()) {
            binding.edtPrice.setError("Price required");
            return false;
        }

        // LOGIC FIX: If creating new, Image is REQUIRED.
        // If Editing, Image is OPTIONAL (can keep old one).
        if (!isEditMode && selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void handleSubmit() {
        if (isEditMode) {
            handleUpdate();
        } else {
            // New Item -> Must have image -> Upload -> Create
            uploadImageAndCreateItem();
        }
    }

    // --- LOGIC FOR UPDATE ---
    private void handleUpdate() {
        // Scenario A: User picked a NEW image
        if (selectedImageUri != null) {
            uploadImageAndUpdateItem(itemToEdit);
        }
        // Scenario B: User kept the OLD image
        else {
            // Update details directly, keep old file_id
            updateItemInDatabase(itemToEdit, null);
        }
    }

    // --- NETWORK CALLS ---

    // 1. Upload NEW Image -> Update Item
    private void uploadImageAndUpdateItem(Item item) {
        setLoading(true);
        File file = FileUtils.getFileFromUri(this, selectedImageUri);

        if (file == null) {
            Toast.makeText(this, "Image Error", Toast.LENGTH_SHORT).show();
            setLoading(false);
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        itemService.uploadFile(spm.getUser().getToken(), body).enqueue(new Callback<FileModel>() {
            @Override
            public void onResponse(Call<FileModel> call, Response<FileModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Image Uploaded -> Update DB with NEW File ID
                    updateItemInDatabase(item, response.body());
                } else {
                    Toast.makeText(CreateItemActivity.this, "Image Upload Failed", Toast.LENGTH_SHORT).show();
                    setLoading(false);
                }
            }
            @Override
            public void onFailure(Call<FileModel> call, Throwable t) {
                setLoading(false);
            }
        });
    }

    // 2. Upload NEW Image -> Create Item
    private void uploadImageAndCreateItem() {
        setLoading(true);
        File file = FileUtils.getFileFromUri(this, selectedImageUri);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        itemService.uploadFile(spm.getUser().getToken(), body).enqueue(new Callback<FileModel>() {
            @Override
            public void onResponse(Call<FileModel> call, Response<FileModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    createItemInDatabase(response.body());
                } else {
                    setLoading(false);
                }
            }
            @Override
            public void onFailure(Call<FileModel> call, Throwable t) {
                setLoading(false);
            }
        });
    }

    // 3. Database Update (Handles both New Image and Old Image)
    private void updateItemInDatabase(Item item, FileModel newFile) {
        User user = spm.getUser();

        item.setTitle(binding.edtTitle.getText().toString());
        item.setPrice(Double.parseDouble(binding.edtPrice.getText().toString()));
        item.setDescription(binding.edtDescription.getText().toString());

        // If we uploaded a new file, update the ID. If null, keep the old ID.
        if (newFile != null) {
            item.setFileId(newFile.getId());
        }

        itemService.updateItem(user.getToken(), item.getItemId(), item).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateItemActivity.this, "Updated Successfully!", Toast.LENGTH_SHORT).show();

                    // Navigate to Main Activity and clear stack to refresh everything
                    Intent intent = new Intent(CreateItemActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    setLoading(false);
                }
            }
            @Override
            public void onFailure(Call<Item> call, Throwable t) {
                setLoading(false);
            }
        });
    }

    private void createItemInDatabase(FileModel fileModel) {
        User user = spm.getUser();
        Item newItem = new Item();
        newItem.setTitle(binding.edtTitle.getText().toString());
        newItem.setPrice(Double.parseDouble(binding.edtPrice.getText().toString()));
        newItem.setDescription(binding.edtDescription.getText().toString());
        newItem.setFileId(fileModel.getId());
        newItem.setSellerId(user.getId());
        newItem.setStatus("Available");

        itemService.createItem(user.getToken(), newItem).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateItemActivity.this, "Item Created!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    setLoading(false);
                }
            }
            @Override
            public void onFailure(Call<Item> call, Throwable t) {
                setLoading(false);
            }
        });
    }

    // Helper to toggle button state
    private void setLoading(boolean isLoading) {
        binding.btnPost.setEnabled(!isLoading);
        binding.btnPost.setText(isLoading ? "Processing..." : (isEditMode ? "Update Item" : "Post Item"));
    }
}