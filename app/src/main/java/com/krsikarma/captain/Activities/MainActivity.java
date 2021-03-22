package com.krsikarma.captain.Activities;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.krsikarma.captain.Adapters.JobRequestsRecyclerAdapter;
import com.krsikarma.captain.Models.JobRequest;
import com.krsikarma.captain.R;
import com.krsikarma.captain.Utility.DefaultTextConfig;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.security.auth.login.LoginException;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private static final int RC_APP_UPDATE = 11;
    public static final int AUTOCOMPLETE_REQUEST_CODE = 10;

    public static final String CHANNEL_ID = "NOTIF";
    public static final String CHANNEL_NAME = "Notifications";
    public static final String CHANNEL_DESC = "This channel is for all notifications";

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore db;
    ListenerRegistration listenerRegistration;

    JobRequestsRecyclerAdapter mAdapter;
    ArrayList<JobRequest> jobRequestArrayList;

    RecyclerView recyclerView;
    ImageView img_user;
    ConstraintLayout main_constraint;
    InstallStateUpdatedListener installStateUpdatedListener;
    Double driver_latitude, driver_longitude;
    private AppUpdateManager mAppUpdateManager;


    String profile_img_url;
    String document_id;
    Long order_id;

    ConstraintLayout current_order_view;
    TextView tv_current_order_address;
    Button btn_current_order;
    TextView tv_location;

    String phoneLanguage;
    String new_address;
    Double new_address_latitude, new_address_longitude;

    PlacesClient placesClient;
    String places_api_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.Theme_KrsiKarmaCaptain);
        super.onCreate(savedInstanceState);

        DefaultTextConfig.adjustFontScale(MainActivity.this);
        setContentView(R.layout.activity_main);

        init();

        checkForLogOut();


        img_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                intent.putExtra("profile_photo_url", profile_img_url);
                startActivity(intent);
            }
        });

        btn_current_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), OrderDetailsActivity.class);
                intent.putExtra("order_id", order_id);
                startActivity(intent);
            }
        });




        tv_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSearchCalled();
            }
        });

        //Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

        }
    }

    private void init() {

        img_user = (ImageView) findViewById(R.id.img_user);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        jobRequestArrayList = new ArrayList<>();
        main_constraint = (ConstraintLayout) findViewById(R.id.main_constraint);

        current_order_view = (ConstraintLayout) findViewById(R.id.current_order_view);
        tv_current_order_address = (TextView) findViewById(R.id.tv_current_order_address);
        btn_current_order = (Button) findViewById(R.id.btn_current_order);
        tv_location = (TextView) findViewById(R.id.tv_location);

        phoneLanguage = Locale.getDefault().getLanguage();

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        //places
        places_api_key = getString(R.string.PLACES_API_KEY);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), places_api_key);
        }
        placesClient = Places.createClient(this);


    }


    private void popupSnackbarForCompleteUpdate() {

        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.main_constraint),
                        getString(R.string.update_app),
                        Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction(getString(R.string.install_now), view -> {
            if (mAppUpdateManager != null) {
                mAppUpdateManager.completeUpdate();
            }
        });


        snackbar.setActionTextColor(getResources().getColor(R.color.white));
        snackbar.show();
    }

    private void checkForLogOut() {

        if (firebaseUser != null) {
            DocumentReference doc_ref = FirebaseFirestore.getInstance().collection(getString(R.string.partners)).document(firebaseUser.getUid());

            listenerRegistration = doc_ref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {

                    if (error != null) {
                        return;
                    }

                    if (!snapshot.exists()) {
                        finishAffinity();
                        Intent intent = new Intent(getApplicationContext(), GetStartedActivity.class);
                        startActivity(intent);

                    } else {
                        //snapshot exists
                        //check if documents are added
                        if (snapshot.getString(getString(R.string.aadhar_url)) == null) {
                            finishAffinity();
                            Intent intent = new Intent(getApplicationContext(), AddDocumentsActivity.class);
                            startActivity(intent);
                        }
                        if (snapshot.getString(getString(R.string.driving_license_url)) == null) {
                            finishAffinity();
                            Intent intent = new Intent(getApplicationContext(), AddDocumentsActivity.class);
                            startActivity(intent);
                        } else if (snapshot.getString(getString(R.string.bank_full_name)) == null) {
                            finishAffinity();
                            Intent intent = new Intent(getApplicationContext(), BankDetailsActivity.class);
                            startActivity(intent);
                        }

                        //Get driver Lat Long
                        if (snapshot.get(getString(R.string.latitude)) != null) {
                            driver_latitude = snapshot.getDouble(getString(R.string.latitude));
                        }

                        if (snapshot.get(getString(R.string.longitude)) != null) {
                            driver_longitude = snapshot.getDouble(getString(R.string.longitude));
                        }

                        //Set the service location
                        if(snapshot.get((getString(R.string.user_address)))!=null){
                            String service_area = snapshot.getString(getString(R.string.user_address));
                            tv_location.setText(service_area);
                        }

                        //set profile image
                        if (snapshot.getString(getString(R.string.profile_img_url))!=null) {
                            profile_img_url = snapshot.getString(getString(R.string.profile_img_url));
                            Log.i(TAG, "profile photo " + profile_img_url);

                            Glide.with(getApplicationContext())
                                    .load(profile_img_url)
                                    .placeholder(R.drawable.ic_user)
                                    .centerCrop()
                                    .into(img_user);
                        }

                        setNotificationToken();
                        getData();
                        checkForCurrentOrder();


                    }
                }
            });
        }


    }

    private void getData() {

        listenerRegistration = db.collection(getString(R.string.orders))
                .orderBy(getString(R.string.order_date_time), Query.Direction.DESCENDING)
                .whereEqualTo(getString(R.string.order_status), getString(R.string.order_status_type_requested))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        String requester_name = "";
                        String requester_service_name = "";

                        Double requester_quantity = null;
                        String requester_service_id = "";
                        String requester_postal_code = "";
                        String requester_user_id = "";
                        String request_status = "";
                        Double request_address_lat = null;
                        Double request_address_long = null;
                        String service_price = "";
                        String requester_acres = "";
                        String requester_address = "";
                        String request_date = "";
                        String request_time = "";


                        jobRequestArrayList.clear();
                        for (QueryDocumentSnapshot doc : value) {

                            if (doc.get(getString(R.string.order_quantity)) != null) {
                                requester_quantity = doc.getDouble(getString(R.string.order_quantity));
                            }

                            if (doc.get(getString(R.string.service_name)) != null) {
                                requester_service_name = doc.getString(getString(R.string.service_name));
                            }

                            if (doc.get(getString(R.string.user_name)) != null) {
                                requester_name = doc.getString(getString(R.string.user_name));
                            }


                            if (doc.get(getString(R.string.service_id)) != null) {
                                requester_service_id = doc.getString(getString(R.string.service_id));
                            }

                            if (doc.get(getString(R.string.order_postal_code)) != null) {
                                requester_postal_code = doc.getString(getString(R.string.order_postal_code));
                            }

                            if (doc.get(getString(R.string.user_id)) != null) {
                                requester_user_id = doc.getString(getString(R.string.user_id));
                            }


                            if (doc.get(getString(R.string.order_status)) != null) {
                                request_status = doc.getString(getString(R.string.order_status));
                            }

                            if (doc.get(getString(R.string.order_latitude)) != null) {
                                request_address_lat = doc.getDouble(getString(R.string.order_latitude));
                            }

                            if (doc.get(getString(R.string.order_longitude)) != null) {
                                request_address_long = doc.getDouble(getString(R.string.order_longitude));
                            }


                            Double order_rate = null, order_quantity = null;

                            if (doc.get(getString(R.string.order_rate)) != null) {
                                order_rate = doc.getDouble(getString(R.string.order_rate));

                            }

                            if (doc.get(getString(R.string.order_quantity)) != null) {
                                order_quantity = doc.getDouble(getString(R.string.order_quantity));
                                requester_acres = String.valueOf(order_quantity);
                                requester_acres = requester_acres.substring(0, requester_acres.length() - 2);
                            }

                            if (order_rate != null && order_quantity != null) {
                                service_price = String.valueOf(order_rate * order_quantity);
                                service_price = service_price.substring(0, service_price.length() - 2);
                            }

                            if (doc.get(getString(R.string.order_address)) != null) {
                                requester_address = doc.getString(getString(R.string.order_address));
                            }

                            Timestamp ts_date = null;
                            String current_date_time, request_date_time;
                            Date dt_current_date_time, dt_request_date_time;
                            if (doc.get(getString(R.string.order_date_time)) != null) {
                                ts_date = doc.getTimestamp(getString(R.string.order_date_time));
                                SimpleDateFormat sfd_viewFormat = new SimpleDateFormat("d MMMM, yyyy");
                                request_date = sfd_viewFormat.format(ts_date.toDate());


                                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");
                                request_time = timeFormat.format(ts_date.toDate());


                                SimpleDateFormat sfd_full_format = new SimpleDateFormat("d MMMM, yyyy hh:mm aa");
                                current_date_time = sfd_full_format.format(new Date());
                                request_date_time = sfd_full_format.format(ts_date.toDate());


                                try {
                                    dt_current_date_time = sfd_full_format.parse(current_date_time);
                                    dt_request_date_time = sfd_full_format.parse(request_date_time);

                                    int time_value = dt_request_date_time.compareTo(dt_current_date_time);
                                    if (time_value > 0) {
                                        //the request date is after current time hence can be displayed

                                        if (driver_latitude != null && driver_longitude != null) {
                                            float distance = calculateDistanceWithDriver(request_address_lat, request_address_long);
                                            Log.i(TAG, "distance is " + distance);

                                            if (distance < 100000) {
                                                jobRequestArrayList.add(new JobRequest(
                                                        requester_name,
                                                        requester_quantity,
                                                        requester_service_id,
                                                        requester_service_name,
                                                        requester_postal_code,
                                                        requester_user_id,
                                                        request_status,
                                                        request_address_lat,
                                                        request_address_long,
                                                        service_price,
                                                        requester_acres,
                                                        requester_address,
                                                        request_date,
                                                        request_time,
                                                        doc.getId()

                                                ));

                                            }
                                        }


                                    }


                                } catch (ParseException parseException) {
                                    parseException.printStackTrace();
                                }


                            }


                        }

                        mAdapter = new JobRequestsRecyclerAdapter(jobRequestArrayList, phoneLanguage,MainActivity.this);
                        recyclerView.setAdapter(mAdapter);
                    }
                });




    }

    private void checkForCurrentOrder(){
        if(firebaseUser!=null) {
            listenerRegistration = db.collection(getString(R.string.orders))
                    .whereEqualTo(getString(R.string.partner_id), firebaseUser.getUid())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                            if (error != null) {
                                Log.e(TAG, "Error is ", error);
                                return;
                            }
                            document_id = "";
                            for (QueryDocumentSnapshot doc : value) {
                                if(doc.get(getString(R.string.order_status))!=null){
                                    if(doc.get(getString(R.string.order_status)).equals(getString(R.string.order_status_type_driver_assigned))
                                            || doc.get(getString(R.string.order_status)).equals(getString(R.string.order_status_type_ongoing))
                                            ||  doc.get(getString(R.string.order_status)).equals(getString(R.string.order_status_type_payment_pending))
                                    ){

                                        document_id = doc.getId();

                                        if (doc.get(getString(R.string.order_id)) != null) {
                                            order_id = doc.getLong(getString(R.string.order_id));

                                        }

                                        if(doc.get(getString(R.string.order_address))!=null){
                                            tv_current_order_address.setText(doc.getString(getString(R.string.order_address)));
                                        }

                                    }
                                }


                            }

                            //if no current order
                            Log.i(TAG, "onEvent: DOC ID IS" + document_id);
                            if (document_id == null || document_id.isEmpty()) {
                                current_order_view.setVisibility(View.GONE);

                            } else {
                                current_order_view.setVisibility(View.VISIBLE);

                            }
                        }
                    });
        }
    }

    private float calculateDistanceWithDriver(Double service_lat, Double service_long) {

        if (driver_latitude != null && driver_longitude != null) {
            float[] results = new float[1];
            Location.distanceBetween(driver_latitude, driver_longitude, service_lat, service_long, results);
            float distance = results[0];
            return distance;
        }

        return 0;

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);

                new_address = place.getAddress();
                new_address_latitude = place.getLatLng().latitude;
                new_address_longitude = place.getLatLng().longitude;
                tv_location.setText(new_address);

                updateServiceLocation();


                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(new_address_latitude, new_address_longitude, 1);
//                    String postalCode = addresses.get(0).getPostalCode();
//                    String city = addresses.get(0).getLocality();
//                    String state = addresses.get(0).getAdminArea();
//                    String country = addresses.get(0).getCountryName();
//                    String knownName = addresses.get(0).getFeatureName();
//
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

    private void updateServiceLocation(){
        Map<String, Object> data = new HashMap<>();
        data.put(getString(R.string.user_address), new_address);
        data.put(getString(R.string.latitude), new_address_latitude);
        data.put(getString(R.string.longitude), new_address_longitude);

        if(firebaseUser!=null) {
            db.collection(getString(R.string.partners)).document(firebaseUser.getUid())
                    .update(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "Service location updated");
                            getData();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, "Error in updating service location " + e.getMessage());
                        }
                    });
        }
    }

    private void setNotificationToken(){

        if(firebaseUser!=null) {

            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                return;
                            }

                            // Get new FCM registration token
                            String token = task.getResult();

                            final DocumentReference doc_id = db.collection(getString(R.string.partners)).document(firebaseUser.getUid());


                            doc_id.update("token", FieldValue.arrayUnion(token)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.i(TAG, "Token updated");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i(TAG, "An error occurred : " + e.getMessage());
                                }
                            });


                        }
                    });
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (firebaseUser == null) {
            finishAffinity();
            Intent intent = new Intent(getApplicationContext(), GetStartedActivity.class);
            startActivity(intent);
        }else{
            checkForLogOut();
            checkForCurrentOrder();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        installStateUpdatedListener = new InstallStateUpdatedListener() {
            @Override
            public void onStateUpdate(InstallState state) {
                if (state.installStatus() == InstallStatus.DOWNLOADED) {
                    //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                    popupSnackbarForCompleteUpdate();
                } else if (state.installStatus() == InstallStatus.INSTALLED) {
                    if (mAppUpdateManager != null) {
                        mAppUpdateManager.unregisterListener(installStateUpdatedListener);
                    }

                } else {
                    Log.i(TAG, "InstallStateUpdatedListener: state: " + state.installStatus());
                }
            }
        };

        mAppUpdateManager = AppUpdateManagerFactory.create(this);

        mAppUpdateManager.registerListener(installStateUpdatedListener);

        mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE /*AppUpdateType.IMMEDIATE*/)) {

                try {
                    mAppUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo, AppUpdateType.FLEXIBLE /*AppUpdateType.IMMEDIATE*/, MainActivity.this, RC_APP_UPDATE);

                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }

            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                popupSnackbarForCompleteUpdate();
            } else {
                Log.e(TAG, "checkForAppUpdateAvailability: something else");
            }
        });
    }
}