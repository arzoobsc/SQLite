package com.ap.sqlite;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class AddUpdateRecordActivity extends AppCompatActivity {

    //    views
    private CircularImageView profileIv;
    private EditText nameEt, phoneEt, emailEt, dobEt, bioEt;
    private FloatingActionButton saveBtn;

    //    permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 101;
    //    image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 102;
    private static final int IMAGE_PICK_GALLERY_CODE = 103;
    //    arrays of permission
    private String[] cameraPermissions; //  camera and storage
    private String[] storagePermissions; //  only storage
    //    variables     (will contain data to save)
    private Uri imageUri;
    private String id, name, phone, email, dob, bio, addedTime, updatedTime;
    private boolean isEditMode = false;

    //    db helper
    private MyDbHelper dbHelper;

    //    actionbar
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);

//        init
        actionBar = getSupportActionBar();
//        title
        actionBar.setTitle("Add Record");
//        back button
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

//        init views
        profileIv = findViewById(R.id.profileIv);
        nameEt = findViewById(R.id.nameEt);
        phoneEt = findViewById(R.id.phoneEt);
        emailEt = findViewById(R.id.emailEt);
        dobEt = findViewById(R.id.dobEt);
        bioEt = findViewById(R.id.bioEt);
        saveBtn = findViewById(R.id.saveBtn);

//        get data from intent
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("isEditMode", false);
        if (isEditMode) {
//            updating data

            actionBar.setTitle("Update Data");

            id = intent.getStringExtra("ID");
            name = intent.getStringExtra("NAME");
            phone = intent.getStringExtra("PHONE");
            email = intent.getStringExtra("EMAIL");
            dob = intent.getStringExtra("DOB");
            bio = intent.getStringExtra("BIO");
            imageUri = Uri.parse(intent.getStringExtra("IMAGE"));
            addedTime = intent.getStringExtra("ADDED_TIME");
            updatedTime = intent.getStringExtra("UPDATED_TIME");

//            set data to views
            nameEt.setText(name);
            phoneEt.setText(phone);
            emailEt.setText(email);
            dobEt.setText(dob);
            bioEt.setText(bio);

//            if no image was selected while adding data, imageUri value will be "null"
            if (imageUri.toString().equals("nill")) {
//                no image, set default
                profileIv.setImageResource(R.drawable.ic_person_black);
            } else {
//                have image, set
                profileIv.setImageURI(imageUri);
            }
        } else {
//            add data
        }

//        init db helper
        dbHelper = new MyDbHelper(this);

//        init permission arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

//        click image view to show image pick dialog
        profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                show image pick dialog
                imagePickDialog();
            }
        });

//        click save button to save record
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData();
            }
        });
    }

    private void inputData() {
//        get data
        name = "" + nameEt.getText().toString().trim();
        phone = "" + phoneEt.getText().toString().trim();
        email = "" + emailEt.getText().toString().trim();
        dob = "" + dobEt.getText().toString().trim();
        bio = "" + bioEt.getText().toString().trim();

        if (isEditMode) {
//            update data

            String timestamp = ""+System.currentTimeMillis();
            dbHelper.updateRecord(
                    ""+id,
                    ""+name,
                    ""+imageUri,
                    ""+bio,
                    ""+phone,
                    ""+email,
                    ""+dob,
                    ""+addedTime,   //  added time will be same
                    ""+timestamp    //updated time will be changed
            );
            Toast.makeText(this, "Updated...", Toast.LENGTH_SHORT).show();
        } else {
//            new data

            //        save to db
            String timestamp = "" + System.currentTimeMillis();
            long id = dbHelper.insertRecord(
                    "" + name,
                    "" + imageUri,
                    "" + bio,
                    "" + phone,
                    "" + email,
                    "" + dob,
                    "" + timestamp,
                    "" + timestamp);
            Toast.makeText(this, "Record added against ID: " + id, Toast.LENGTH_SHORT).show();
        }

    }

    private void imagePickDialog() {
//        options to display in dialog
        String[] options = {"Camera", "Gallery"};
//        dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        title
        builder.setTitle("Pick Image From");
//        set items/options
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                handle clicks
                if (i == 0) {
//                    Camera clicked
                    if (!checkCameraPermissions()) {
                        requestCameraPermission();
                    } else {
//                        permission already granted
                        pickFromCamera();
                    }
                } else if (i == 1) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
//                        permission already granted
                        pickFromGallery();
                    }
                }

            }
        });

//        create/show dialog
        builder.create().show();
    }

    private void pickFromGallery() {
//        intent to pick image from gallery, the image will be returned in onActivityResult method
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
//        intent to pick image from gallery, the image will be returned in onActivityResult method
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Image title");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image description");
//        put image uri
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

//        intent to open camera for image
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission() {
//        check if storage permission is enable of not
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestStoragePermission() {
//        request the storage permission
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermissions() {
//        check if camera permission is enable of not
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission() {
//        request the camera permission
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void copyFileOrDirectory(String srcDir, String desDir){
//        create folder in specific directory
        try {
            File src = new File(srcDir);
            File des = new File(desDir, src.getName());
            if (src.isDirectory()){
                String[] files = src.list();
                int filesLength = files.length;
                for (String file: files){
                    String src1 = new File(src, file).getPath();
                    String des1 = des.getPath();

                    copyFileOrDirectory(src1, des1);
                }
            }else {
                copyFile(src, des);
            }
        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void copyFile(File srcDir, File desDir) throws IOException {
        if (!desDir.getParentFile().exists()){
            desDir.mkdirs();    //  create if not exist
        }
        if (!desDir.exists()){
            desDir.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(srcDir).getChannel();
            destination = new FileOutputStream(desDir).getChannel();
            destination.transferFrom(source, 0, source.size());

            imageUri = Uri.parse(desDir.getPath()); //  uri of saved image
            Log.d("ImagePath", "copyFile: "+imageUri);
        }catch (Exception e){
//            if there is any error saving image
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }finally {
//            close resources
            if (source != null){
                source.close();
            }
            if (destination != null){
                destination.close();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();    //  go back by clicking back button of actionbar
        return super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        result of permission allowed/denied

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
//                if allowed returns true otherwise false
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted) {
//                    both permission allowed
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Camera & Storage permissions are required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
//                if allowed returns true otherwise false
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
//                    storage permission allowed
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Storage permissions are required", Toast.LENGTH_SHORT).show();

                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        imaged picked from camera or gallery will be received here

        if (resultCode == RESULT_OK) {
//            image is picked

            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
//                picked from gallery

//                crop image
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
//                picked from camera

//                crop image
                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this);
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//                cropped image received
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();
                    imageUri = resultUri;
//                    set image
                    profileIv.setImageURI(resultUri);

//                    documentation link of getDir() is available in description to learn more about it **part 05**
                    copyFileOrDirectory(""+imageUri.getPath(), ""+getDir("SqliteRecordImages", MODE_PRIVATE));
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                    error
                    Exception error = result.getError();
                    Toast.makeText(this, "" + error, Toast.LENGTH_SHORT).show();

                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}




















