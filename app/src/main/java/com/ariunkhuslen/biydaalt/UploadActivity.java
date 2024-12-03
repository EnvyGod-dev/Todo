package com.ariunkhuslen.biydaalt;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class UploadActivity extends AppCompatActivity {
    private ImageView uploadImage;
    private Button saveButton;
    private EditText uploadTopic, uploadDesc, uploadLang;
    private Uri uri;
    private String localImagePath;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        uri = result.getData().getData();
                        if (uri != null) {
                            uploadImage.setImageURI(uri);
                        } else {
                            Toast.makeText(UploadActivity.this, "Failed to get image", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(UploadActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Initialize views
        uploadImage = findViewById(R.id.uploadImage);
        uploadTopic = findViewById(R.id.uploadTopic);
        uploadDesc = findViewById(R.id.uploadDesc);
        uploadLang = findViewById(R.id.uploadLang);
        saveButton = findViewById(R.id.saveButton);

        // Set click listener to select an image
        uploadImage.setOnClickListener(v -> {
            Intent photoPicker = new Intent(Intent.ACTION_PICK);
            photoPicker.setType("image/*");
            activityResultLauncher.launch(photoPicker);
        });

        // Set click listener to save data
        saveButton.setOnClickListener(v -> {
            if (uri != null) {
                saveImageLocallyAndSaveData();
            } else {
                Toast.makeText(UploadActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), uri));
        } else {
            return MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        }
    }

    private void saveImageLocallyAndSaveData() {
        try {
            Bitmap bitmap = getBitmapFromUri(uri);
            File file = new File(getFilesDir(), "image_" + System.currentTimeMillis() + ".jpg");

            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            }

            localImagePath = file.getAbsolutePath();
            uploadData();

        } catch (IOException e) {
            Toast.makeText(this, "Failed to save image locally", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void uploadData() {
        String title = uploadTopic.getText().toString().trim();
        String desc = uploadDesc.getText().toString().trim();
        String lang = uploadLang.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty() || lang.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        DataClass dataClass = new DataClass(title, desc, lang, localImagePath);

        // Show a progress dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Android_tut");
        databaseReference.child(title).setValue(dataClass)
                .addOnCompleteListener(task -> {
                    dialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(UploadActivity.this, "Successfully saved", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(UploadActivity.this, "Failed to save data", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(UploadActivity.this, "Save Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
