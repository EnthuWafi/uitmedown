package com.enth.uitmedown.presentation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.enth.uitmedown.R;
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
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateItemActivity extends AppCompatActivity {

    private ImageView imgPreview;
    private EditText edtTitle, edtPrice, edtDescription;
    private Button btnPost;

    private Uri selectedImageUri = null; // To store the location of the image user picked
    private ItemService itemService;

    private SharedPrefManager spm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_item);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Init Views
        imgPreview = findViewById(R.id.imgPreview);
        edtTitle = findViewById(R.id.edtTitle);
        edtPrice = findViewById(R.id.edtPrice);
        edtDescription = findViewById(R.id.edtDescription);
        btnPost = findViewById(R.id.btnPost);

        itemService = ApiUtils.getItemService();

        spm = new SharedPrefManager(getApplicationContext());
        if (!spm.isLoggedIn()) { // no session record
            // forward to Login Page
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }
        // 2. Image Picker Logic
        // This is the "New Way" (ActivityResultLauncher) - cleaner than onActivityResult
        ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        imgPreview.setImageURI(uri); // Show preview to user
                        imgPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                }
        );

        // Click Image to open Gallery
        imgPreview.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // 3. Post Button Logic
        btnPost.setOnClickListener(v -> {
            if (validateInputs()) {
                uploadImageAndCreateItem();
            }
        });
    }

    private boolean validateInputs() {
        if (edtTitle.getText().toString().isEmpty()) {
            edtTitle.setError("Title required");
            return false;
        }
        if (edtPrice.getText().toString().isEmpty()) {
            edtPrice.setError("Price required");
            return false;
        }
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // --- PHASE 1: UPLOAD THE IMAGE ---
    private void uploadImageAndCreateItem() {
        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();
        btnPost.setEnabled(false); // Prevent double clicks

        // Convert URI to a standard File object
        File file = FileUtils.getFileFromUri(this, selectedImageUri);

        if (file == null) {
            Toast.makeText(this, "Error processing image file", Toast.LENGTH_SHORT).show();
            btnPost.setEnabled(true);
            return;
        }

        User user = spm.getUser();

        // Prepare the RequestBody
        // "image/*" tells server this is an image
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        // Execute API Call
        itemService.uploadFile(user.getToken(), body).enqueue(new Callback<FileModel>() {
            @Override
            public void onResponse(Call<FileModel> call, Response<FileModel> response) {
                if (response.isSuccessful()) {
                    FileModel fileModel = response.body();

                    createItemInDatabase(fileModel);

                } else {
                    Toast.makeText(CreateItemActivity.this, "Upload Failed: " + response.message(), Toast.LENGTH_SHORT).show();
                    btnPost.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<FileModel> call, Throwable throwable) {
                Toast.makeText(CreateItemActivity.this, "Upload Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                btnPost.setEnabled(true);
            }
        });
    }

    // --- PHASE 2: CREATE THE ITEM ---
    private void createItemInDatabase(FileModel fileModel) {
        User user = spm.getUser();

        String serverPath = fileModel.getFile();

        String fullUrl = RetrofitClient.BASE_URL + serverPath;


        String title = edtTitle.getText().toString();
        Double price = Double.parseDouble(edtPrice.getText().toString());
        String desc = edtDescription.getText().toString();

        Item newItem = new Item();
        newItem.setTitle(title);
        newItem.setPrice(price);
        newItem.setDescription(desc);
        newItem.setImageUrl(fullUrl);
        newItem.setFileId(fileModel.getId());
        newItem.setSellerId(user.getId());
        newItem.setStatus("Available");

        itemService.createItem(user.getToken(), newItem).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateItemActivity.this, "Item Posted!", Toast.LENGTH_LONG).show();
                    finish(); // Close this screen and go back to Main
                } else {
                    Toast.makeText(CreateItemActivity.this, "Database Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    btnPost.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<Item> call, Throwable t) {
                Toast.makeText(CreateItemActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
                btnPost.setEnabled(true);
            }
        });
    }
}