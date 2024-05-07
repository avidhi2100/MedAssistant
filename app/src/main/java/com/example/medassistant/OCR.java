package com.example.medassistant;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.medassistant.database.DBHelper;
import com.example.medassistant.entity.Medicine;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class OCR extends AppCompatActivity {

    private MaterialButton inputButton;
    private ShapeableImageView imageView;
    private EditText recognizedTextEt;
    DBHelper dbHelper;

    private static final String TAG = "OCR_ACTIVITY_TAG";
    private Uri imageUri = null;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 101;

    private String[] cameraPermissions;

    private ProgressDialog progressDialog;

    private TextRecognizer textRecognizer;
    FirebaseAuth auth;
    BottomNavigationView bottomNavigationView;

//    CTakesMain medTagger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ocr);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        inputButton = findViewById(R.id.inputImageBtn);
        MaterialButton recognizeTextBtn = findViewById(R.id.recognizedTextBtn);
        imageView = findViewById(R.id.imageIv);
        recognizedTextEt = findViewById(R.id.recognizedTextEt);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.menu_home) {
                // Handle Home button click
                Intent intent = new Intent(this, MedicinesActivity.class);
                startActivity(intent);
                return true;
            } else if (menuItem.getItemId() == R.id.menu_reminders) {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                return true;
            } else if (menuItem.getItemId() == R.id.menu_chat) {
                Intent intent = new Intent(this, ChatbotInterfaceActivity.class);
                startActivity(intent);
                return true;
            }
            else if (menuItem.getItemId() == R.id.menu_logout) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Confirmation")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            Intent intent = new Intent(this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();
                        });
                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
            return false;
        });

        bottomNavigationView.getMenu().findItem(R.id.menu_ocr).setChecked(true);


        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
//        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait....");
        progressDialog.setCancelable(false);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        inputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputImageDialog();
            }
        });

        recognizeTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri == null) {
                    Toast.makeText(OCR.this, "Pick an Image first....", Toast.LENGTH_SHORT).show();
                } else {
                    recognizeTextFromImage();
                }
            }
        });

    }

    private void recognizeTextFromImage() {
        Log.d(TAG, "recognizeTextFromImage:");
        progressDialog.setMessage("Preparing Text....");
        progressDialog.show();

        try {
            InputImage inputImage = InputImage.fromFilePath(this, imageUri);
            progressDialog.setMessage("Detecting Text.....");

            Task<Text> textTaskResult = textRecognizer.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text text) {

                            ObjectMapper objectMapper = new ObjectMapper();
                            dbHelper = new DBHelper(OCR.this);
                            progressDialog.dismiss();

                            String detectedText = text.getText();


                            Log.d(TAG, "onSuccess:recognizeText --> " + detectedText);
                            recognizedTextEt.setText(detectedText);

                            if (!detectedText.isEmpty()){
                                progressDialog = ProgressDialog.show(OCR.this,
                                        "Understand Detected Test",
                                        "Please Wait...",
                                        true);

                                new CallOCRProcessAPI(response -> {
                                    if(progressDialog != null && progressDialog.isShowing()){
                                        progressDialog.dismiss();
                                    }
                                    Log.d("OCRData", response);

                                    JSONObject jsonObject = new JSONObject(response);
                                    String responseData = jsonObject.getString("response");
                                    Log.d("responseData",responseData);

//                                    String jsonString ="{\n  \"medicineName\": \"AMOXICILLIN 500 MG\",\n  \"medicineDosage\": \"TAKE ONE CAPSULE\",\n  \"route\": \"MOUTH 2X PER DAY\",\n  \"refillDate\": \"12-01-2016\",\n  \"doctorName\": \"Dr. Auth Requiet\"\n}";
                                    Medicine medicine = objectMapper.readValue(responseData, Medicine.class);
                                    medicine.setUserEmail(Objects.requireNonNull(auth.getCurrentUser()).getEmail());
                                    dbHelper.addMedicine(medicine);
                                    Toast.makeText(OCR.this, "New Medicine Added", Toast.LENGTH_SHORT).show();

                                }).execute(detectedText);
                            } else {
                                Toast.makeText(OCR.this, "OCR Failed", Toast.LENGTH_SHORT).show();
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Log.d(TAG, "onFailure: ", e);
                            Toast.makeText(OCR.this, "Failed recognizing text due to: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed preparing image due to: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }

    }

    private void showInputImageDialog() {
        PopupMenu popupMenu = new PopupMenu(this, inputButton);

        popupMenu.getMenu().add(Menu.NONE, 1, 1, "CAMERA");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "GALLERY");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == 1) {

                    if (checkCameraPermissions()) {
                        pickImageCamera();
                    } else {
                        requestCameraPermissions();
                    }

                } else if (id == 2) {

                    if (checkStoragePermission()) {
                        pickImageGallery();
                    } else {
                        requestStoragePermission();
                    }

                }

                return true;
            }
        });

    }

    private void pickImageGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Use system file picker for Android 13 and above
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            galleryActivityResultLauncher.launch(intent);
        } else {
            // Older versions of Android require explicit permission to access external storage
            if (checkStoragePermission()) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryActivityResultLauncher.launch(intent);
            } else {
                requestStoragePermission();
            }
        }
    }



    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if (o.getResultCode() == Activity.RESULT_OK) {
                        Intent data = o.getData();
                        assert data != null;
                        imageUri = data.getData();
                        imageView.setImageURI(imageUri);
                    } else {
                        Toast.makeText(OCR.this, "Cancelled....", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void pickImageCamera(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Test Title");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Test DESC");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);

    }

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if (o.getResultCode() == Activity.RESULT_OK) {
                        imageView.setImageURI(imageUri);
                    } else {
                        Toast.makeText(OCR.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private boolean checkStoragePermission() {
        // Check for storage permission required only on versions less than Android 13
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        // No permission check required for Android 13 and above for gallery access
        return true;
    }

    private void requestStoragePermission() {
        // Request storage permission only if the Android version is less than 13
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
        }
    }

    private boolean checkCameraPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        } else {
            boolean cameraResult = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
            boolean storageResult = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            return cameraResult && storageResult;
        }
    }

    private void requestCameraPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageCamera();
                } else {
                    Toast.makeText(this, "Camera Permission is Required", Toast.LENGTH_SHORT).show();
                }
                break;
            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageGallery();
                } else {
                    Toast.makeText(this, "Storage Permission is Required", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private static class CallOCRProcessAPI extends AsyncTask<String, Void, String> {

        public interface ResponseListener {
            void onResponseReceived(String response) throws JsonProcessingException, JSONException;
        }

        private final ResponseListener listener;

        private CallOCRProcessAPI(ResponseListener listener) {
            this.listener = listener;
        }
        @Override
        protected String doInBackground(@NonNull String... params) {
            String urlString = "http://10.0.2.2:5000/ocr";

            Log.d("pararm",params[0].replace("\n", ""));
            String data = "{\"prompt\":\"" + params[0].replace("\n", "") + "\"}";
            Log.d("data", data);
            OutputStream out;

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");

                out = new BufferedOutputStream(urlConnection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
                writer.write(data);
                writer.flush();
                writer.close();
                out.close();

                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Return the response
                    return response.toString();
                } else {
                    // Handle server error here
                    return "Server returned HTTP " + urlConnection.getResponseCode() + " " + urlConnection.getResponseMessage();
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
                return e.getMessage();
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                listener.onResponseReceived(s.trim());
            } catch (JsonProcessingException | JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.getMenu().findItem(R.id.menu_ocr).setChecked(true);

    }
}