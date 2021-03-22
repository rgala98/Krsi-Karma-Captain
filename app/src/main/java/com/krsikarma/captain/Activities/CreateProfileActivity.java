package com.krsikarma.captain.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
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
import com.krsikarma.captain.Adapters.ServiceTypeAdapter;
import com.krsikarma.captain.Models.ServiceType;
import com.krsikarma.captain.R;
import com.krsikarma.captain.Utility.DefaultTextConfig;
import com.krsikarma.captain.Utility.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CreateProfileActivity extends AppCompatActivity {
    public static final String TAG = "CreateProfileActivity";
    public static final int RESULT_LOAD_IMAGE_MULTIPLE = 18;
    public static final int RESULT_CAMERA = 23;
    public static final int AUTOCOMPLETE_REQUEST_CODE = 10;

    RecyclerView service_recycler_view;
    FlexboxLayoutManager recycler_view_manager;
    ServiceTypeAdapter serviceTypeAdapter;

    ArrayList<ServiceType> serviceTypeArrayList;
    ArrayList<ServiceType> selectedServiceTypeList;
    ArrayList<ServiceType> previouslySelectedServices;


    ImageView img_profile;
    EditText et_first_name;
    EditText et_last_name;
    TextView tv_select_address;
    TextView tv_phone_number;
    Button btn_register;
    ProgressBar progressBar;


    ListenerRegistration listenerRegistration;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    StorageTask uploadTask;
    Bitmap bitmapImage;


    String phone_number;
    String first_name;
    String last_name;
    String address;
    String postalCode;
    Double address_latitude;
    Double address_longitude;


    PlacesClient placesClient;
    String places_api_key;

    Utils utils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DefaultTextConfig.adjustFontScale(CreateProfileActivity.this);
        setContentView(R.layout.activity_create_profile);

        init();

        getAllServiceTypes();

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_register.setEnabled(false);
                selectedServiceTypeList = serviceTypeAdapter.getUsersServices();


                first_name = utils.capitalizeString(et_first_name.getText().toString().trim());
                last_name = utils.capitalizeString(et_last_name.getText().toString().trim());

                if (!utils.isStringNull(first_name) && !utils.isStringNull(last_name) && !first_name.isEmpty() && !last_name.isEmpty() && selectedServiceTypeList != null && !selectedServiceTypeList.isEmpty()) {
                    progressBar.setVisibility(View.VISIBLE);
                    addData();

                } else {
                    progressBar.setVisibility(View.GONE);
                    btn_register.setEnabled(true);
                    utils.alertDialogOK(CreateProfileActivity.this, getString(R.string.error_text), getString(R.string.complete_all_fields));
                }


            }
        });

        img_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                createPermissionListeners();
            }
        });


        tv_select_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSearchCalled();
            }
        });

    }

    private void init() {

        tv_select_address = (TextView) findViewById(R.id.tv_select_address);
        et_first_name = (EditText) findViewById(R.id.et_first_name);
        et_last_name = (EditText) findViewById(R.id.et_last_name);
        tv_phone_number = (TextView) findViewById(R.id.tv_phone);
        img_profile = (ImageView) findViewById(R.id.img_profile);
        btn_register = (Button) findViewById(R.id.btn_register);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);


        service_recycler_view = (RecyclerView) findViewById(R.id.recycler_view);

        recycler_view_manager = new FlexboxLayoutManager(getApplicationContext());
        recycler_view_manager.setFlexDirection(FlexDirection.ROW);
        recycler_view_manager.setJustifyContent(JustifyContent.SPACE_EVENLY);
        recycler_view_manager.setFlexWrap(FlexWrap.WRAP);

        serviceTypeArrayList = new ArrayList<>();
        selectedServiceTypeList = new ArrayList<>();
        previouslySelectedServices = new ArrayList<>();
        service_recycler_view.setLayoutManager(recycler_view_manager);
        serviceTypeAdapter = new ServiceTypeAdapter(CreateProfileActivity.this, serviceTypeArrayList, previouslySelectedServices);
        service_recycler_view.setAdapter(serviceTypeAdapter);
        phone_number = getIntent().getStringExtra("phone_number");
        if (phone_number != null) {
            tv_phone_number.setText(phone_number.substring(3));
        }

        utils = new Utils();


        //places
        places_api_key = getString(R.string.PLACES_API_KEY);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), places_api_key);
        }
        placesClient = Places.createClient(this);

        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();



    }

    public void onSearchCalled() {
        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields).setCountry("IN")
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    private void getAllServiceTypes() {

        listenerRegistration = db.collection(getString(R.string.services))
                .orderBy(getString(R.string.name))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        String service_id;
                        String service_name = "";


                        for (QueryDocumentSnapshot doc : value) {
                            service_id = doc.getId();
                            if (doc.get(getString(R.string.name)) != null) {
                                service_name = doc.getString(getString(R.string.name));
                            }

                            serviceTypeArrayList.add(new ServiceType(
                                    service_name,
                                    service_id
                            ));

                        }

                        serviceTypeAdapter.notifyDataSetChanged();
                    }
                });

    }

    private void createPermissionListeners() {

        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {

                if (report.areAllPermissionsGranted()) {
                    setProfilePicture();
                }

                // check for permanent denial of any permission
                if (report.isAnyPermissionPermanentlyDenied()) {
                    Log.i(TAG, "Permissions permanently denied. Open Settings");


                    AlertDialog.Builder builder;
                    AlertDialog alert;

                    builder = new AlertDialog.Builder(CreateProfileActivity.this);
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

    private void setProfilePicture() {

        final String takePhoto = getString(R.string.take_photo);
        final String chooseFromLibrary = getString(R.string.choose_from_gallery);


        final CharSequence[] items = {takePhoto, chooseFromLibrary};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {


                if (items[item].equals(chooseFromLibrary)) {

                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE_MULTIPLE);

                } else if (items[item].equals(takePhoto)) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, RESULT_CAMERA);

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
                    img_profile.setImageBitmap(bitmapImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }


        }

        if (requestCode == RESULT_CAMERA && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            bitmapImage = (Bitmap) extras.get("data");
            img_profile.setImageBitmap(bitmapImage);
        }

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);

                address = place.getAddress();
                address_latitude = place.getLatLng().latitude;
                address_longitude = place.getLatLng().longitude;
                tv_select_address.setText(address);


                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(address_latitude, address_longitude, 1);
                    postalCode = addresses.get(0).getPostalCode();
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String knownName = addresses.get(0).getFeatureName();

                    Log.i(TAG, "city is " + city);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void addData() {
        progressBar.setVisibility(View.VISIBLE);
        if (bitmapImage != null) {
            final StorageReference ref = storageReference.child("profile_images/" + UUID.randomUUID().toString());

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

                        String profile_image_url = task.getResult().toString();


                        //adding data to firestore
                        Map<String, Object> data = new HashMap<>();
                        data.put(getString(R.string.user_id), firebaseUser.getUid());
                        data.put(getString(R.string.first_name), first_name);
                        data.put(getString(R.string.last_name), last_name);
                        data.put(getString(R.string.phone_number), phone_number);
                        data.put(getString(R.string.profile_img_url), profile_image_url);
                        data.put(getString(R.string.date_created), new Date());
                        data.put(getString(R.string.selected_service_type), serviceTypeAdapter.getUsersServices());
                        data.put(getString(R.string.is_driver_approved), false);
                        data.put(getString(R.string.is_rc_verified), false);
                        data.put(getString(R.string.is_aadhar_verified), false);
                        data.put(getString(R.string.is_dl_verified), false);
                        data.put(getString(R.string.latitude), address_latitude);
                        data.put(getString(R.string.longitude), address_longitude);
                        data.put(getString(R.string.user_address), address);


                        db.collection(getString(R.string.partners)).document(firebaseUser.getUid())
                                .set(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressBar.setVisibility(View.GONE);
                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                        finishAffinity();
                                        Intent intent = new Intent(getApplicationContext(), AddDocumentsActivity.class);
                                        intent.putExtra("activity_from", "FromCreateProfile");
                                        startActivity(intent);
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
                        Log.i(TAG, "An Error Occurred in uploading image" + task.getException().getMessage());
                    }
                }
            });
        } else {
            //adding data to firestore
            Map<String, Object> data = new HashMap<>();
            data.put(getString(R.string.user_id), firebaseUser.getUid());
            data.put(getString(R.string.first_name), first_name);
            data.put(getString(R.string.last_name), last_name);
            data.put(getString(R.string.phone_number), phone_number);
            data.put(getString(R.string.date_created), new Date());
            data.put(getString(R.string.selected_service_type), serviceTypeAdapter.getUsersServices());
            data.put(getString(R.string.user_address), address);
            data.put(getString(R.string.is_driver_approved), false);
            data.put(getString(R.string.is_rc_verified), false);
            data.put(getString(R.string.is_aadhar_verified), false);
            data.put(getString(R.string.is_dl_verified), false);
            data.put(getString(R.string.latitude), address_latitude);
            data.put(getString(R.string.longitude), address_longitude);


            db.collection(getString(R.string.partners)).document(firebaseUser.getUid())
                    .set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                            progressBar.setVisibility(View.GONE);
                            finishAffinity();
                            Intent intent = new Intent(getApplicationContext(), AddDocumentsActivity.class);
                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}