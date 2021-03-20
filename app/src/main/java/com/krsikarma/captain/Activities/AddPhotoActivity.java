package com.krsikarma.captain.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.krsikarma.captain.R;
import com.krsikarma.captain.Utility.DefaultTextConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddPhotoActivity extends AppCompatActivity {

    public static final String TAG = "AddPhotoActivity";
    public static final int RESULT_LOAD_IMAGE_MULTIPLE = 18;
    public static final int RESULT_CAMERA = 23;


    Bitmap bitmapImage;


    ImageView img_back;
    TextView tv_heading;
    CardView image_card_view;
    ImageView img_document;

    ProgressBar progressBar;

    Boolean isDeleted = false;

    String document_name;

    ListenerRegistration listenerRegistration;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    StorageTask uploadTask;

    String previous_aadhar_url, previous_dl_url, previous_rc_url, url_to_delete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DefaultTextConfig.adjustFontScale(AddPhotoActivity.this);
        setContentView(R.layout.activity_add_photo);

        init();






        setDocumentImage();

        img_document.setOnClickListener(view -> {
           createPermissionListeners();

        });



        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }

    private void init() {

        img_back = (ImageView) findViewById(R.id.img_back);
        tv_heading = (TextView) findViewById(R.id.tv_heading);
        image_card_view = (CardView) findViewById(R.id.image_card_view);
        img_document = (ImageView) findViewById(R.id.img_document);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        tv_heading.setText(getIntent().getStringExtra("heading"));
        document_name = getIntent().getStringExtra("document_name");
        previous_dl_url = getIntent().getStringExtra("dl_url");
        previous_aadhar_url = getIntent().getStringExtra("aadhar_url");
        previous_rc_url = getIntent().getStringExtra("rc_url");

        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        bitmapImage = null;


    }


    private void createPermissionListeners() {
        Log.i(TAG, "In createPermissionListeners");
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {

                if (report.areAllPermissionsGranted()) {
                    setPicture();
                }

                // check for permanent denial of any permission
                if (report.isAnyPermissionPermanentlyDenied()) {
                    Log.i(TAG, "Permissions permanently denied. Open Settings");

                    AlertDialog.Builder builder;
                    AlertDialog alert;

                    builder = new AlertDialog.Builder(AddPhotoActivity.this);
                    builder.setMessage(getString(R.string.open_settings_permission))
                            .setCancelable(true)
                            .setPositiveButton(getString(R.string.open_settings), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    startActivity(intent);
                                }
                            });
                    alert = builder.create();
                    alert.setTitle(getString(R.string.error_text));
                    alert.show();
                }

            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }

        }).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError error) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_text) + " " + error.toString(), Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Error Occurred" + error.toString());
            }
        }).check();
    }


    private void setPicture() {

        final String takePhoto = getString(R.string.take_photo);
        final String chooseFromLibrary = getString(R.string.choose_from_gallery);
        final String removePhoto = getString(R.string.delete_doc);

        final CharSequence[] items = {takePhoto, chooseFromLibrary, removePhoto};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals(chooseFromLibrary)) {

                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, ""), RESULT_LOAD_IMAGE_MULTIPLE);

                } else if (items[item].equals(takePhoto)) {


                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, RESULT_CAMERA);
                }else if (items[item].equals(removePhoto)) {
                   deleteImage();
                }
            }
        });
        builder.show();


    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE_MULTIPLE && resultCode == Activity.RESULT_OK) {

            if (data.getData() != null) {

                Uri mImageUri = data.getData();
                try {
                    bitmapImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageUri);
                    uploadImage();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (requestCode == RESULT_CAMERA && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            bitmapImage = (Bitmap) extras.get("data");
            uploadImage();

        }


    }

    private void uploadImage() {
        deleteImage();
        if (bitmapImage != null) {
            progressBar.setVisibility(View.VISIBLE);
            final StorageReference ref = storageReference.child("document_images/" + UUID.randomUUID().toString());

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream); // bmp is bitmap from user image file
            bitmapImage.recycle();
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            uploadTask = ref.putBytes(byteArray);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();

                }
            }).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {

                        String image_url = task.getResult().toString();

                        bitmapImage = null;
                        Glide.with(getApplicationContext())
                                .load(image_url)
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .into(img_document);

                        //adding data to firestore
                        Map<String, Object> data = new HashMap<>();

                        if (document_name.equals("aadhar_card")) {
                            data.put(getString(R.string.aadhar_url), image_url);
                            previous_aadhar_url = image_url;
                        } else if (document_name.equals("driving_license")) {
                            data.put(getString(R.string.driving_license_url), image_url);
                            previous_dl_url = image_url;
                        } else if (document_name.equals("vehicle_rc")) {
                            data.put(getString(R.string.rc_url), image_url);
                            previous_rc_url = image_url;
                        }



                        db.collection(getString(R.string.partners)).document(firebaseUser.getUid())
                                .update(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressBar.setVisibility(View.GONE);
                                        Log.d(TAG, "DocumentSnapshot successfully written!");

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                        progressBar.setVisibility(View.GONE);

                                    }
                                });


                    } else {
                        Log.i(TAG, getString(R.string.error_text) + " " + task.getException().getMessage());
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void deleteImage() {


        StorageReference photoRef = null;

        if (document_name.equals("aadhar_card")) {
            if(previous_aadhar_url!=null){
                photoRef = firebaseStorage.getReferenceFromUrl(previous_aadhar_url);
                url_to_delete = getString(R.string.aadhar_url);
        }

        } else if (document_name.equals("driving_license")) {
            if(previous_dl_url!=null){
                photoRef = firebaseStorage.getReferenceFromUrl(previous_dl_url);
                url_to_delete = getString(R.string.driving_license_url);
            }

        } else if (document_name.equals("vehicle_rc")) {
            if(previous_rc_url!=null){
                photoRef = firebaseStorage.getReferenceFromUrl(previous_rc_url);
                url_to_delete = getString(R.string.rc_url);
            }

        }

        if (photoRef != null) {
            photoRef.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG, getString(R.string.error_text) + " " + e.getLocalizedMessage());

                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    //remove url link from database
                    db.collection(getString(R.string.partners)).document(firebaseUser.getUid())
                            .update(url_to_delete, null)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressBar.setVisibility(View.GONE);
                                    Log.d(TAG, "DocumentSnapshot successfully written!");
                                    img_document.setImageResource(R.drawable.img_placeholder);
                                    isDeleted = true;

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error writing document", e);
                                    progressBar.setVisibility(View.GONE);
                                    Log.i(TAG, getString(R.string.error_text) + " " + e.getLocalizedMessage());

                                }
                            });

                }
            });
        }else{
            Log.i(TAG, "Image does not exist at location");
        }

    }

    private void setDocumentImage() {
        img_document.setImageResource(R.drawable.img_placeholder);
        listenerRegistration = db.collection(getString(R.string.partners)).document(firebaseUser.getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "Error occurred ", error);
                            return;
                        }



                        if (snapshot != null && snapshot.exists()) {

                            if (document_name.equals("aadhar_card")) {
                                if (snapshot.getString(getString(R.string.aadhar_url)) != null) {
                                    previous_aadhar_url = snapshot.getString(getString(R.string.aadhar_url));
                                    Glide.with(getApplicationContext())
                                            .load(previous_aadhar_url)
                                            .into(img_document);


                                    if(snapshot.get(getString(R.string.is_aadhar_verified)) != null){
                                        Boolean isDocumentVerified = snapshot.getBoolean(getString(R.string.is_aadhar_verified));
                                        if(isDocumentVerified){
                                            img_document.setEnabled(false);
                                            Toast.makeText(getApplicationContext(), getString(R.string.document_verified_message), Toast.LENGTH_LONG).show();
                                        }else{
                                            img_document.setEnabled(true);
                                        }
                                    }
                                }

                            } else if (document_name.equals("driving_license")) {
                                if (snapshot.getString(getString(R.string.driving_license_url)) != null) {
                                    previous_dl_url = snapshot.getString(getString(R.string.driving_license_url));
                                    Glide.with(getApplicationContext())
                                            .load(previous_dl_url)
                                            .into(img_document);

                                    if(snapshot.get(getString(R.string.is_dl_verified)) != null){
                                        Boolean isDocumentVerified = snapshot.getBoolean(getString(R.string.is_dl_verified));
                                        if(isDocumentVerified){
                                            img_document.setEnabled(false);
                                            Toast.makeText(getApplicationContext(), getString(R.string.document_verified_message), Toast.LENGTH_LONG).show();

                                        }else{
                                            img_document.setEnabled(true);
                                        }
                                    }
                                }

                            } else if (document_name.equals("vehicle_rc")) {
                                if (snapshot.getString(getString(R.string.rc_url)) != null) {
                                    previous_rc_url = snapshot.getString(getString(R.string.rc_url));
                                    Glide.with(getApplicationContext())
                                            .load(previous_rc_url)
                                            .into(img_document);
                                }

                                if(snapshot.get(getString(R.string.is_rc_verified)) != null){
                                    Boolean isDocumentVerified = snapshot.getBoolean(getString(R.string.is_rc_verified));
                                    if(isDocumentVerified){
                                        img_document.setEnabled(false);
                                        Toast.makeText(getApplicationContext(), getString(R.string.document_verified_message), Toast.LENGTH_LONG).show();

                                    }else{
                                        img_document.setEnabled(true);
                                    }
                                }
                            }



                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(listenerRegistration!=null){
            listenerRegistration=null;
        }
    }
}