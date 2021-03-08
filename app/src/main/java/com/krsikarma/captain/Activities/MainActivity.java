package com.krsikarma.captain.Activities;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.krsikarma.captain.Adapters.JobRequestsRecyclerAdapter;
import com.krsikarma.captain.Models.JobRequest;
import com.krsikarma.captain.R;
import com.krsikarma.captain.Utility.DefaultTextConfig;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private static final int RC_APP_UPDATE = 11;

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

    Boolean isCurrentOrder = false;
    String profile_img_url;
    String document_id;

    TextView tv_current_order;
    String phoneLanguage;
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

        tv_current_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), OrderDetailsActivity.class);
                startActivity(intent);
            }
        });

        getData();
    }

    private void init() {

        img_user = (ImageView) findViewById(R.id.img_user);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        jobRequestArrayList = new ArrayList<>();
        main_constraint = (ConstraintLayout) findViewById(R.id.main_constraint);
        tv_current_order = (TextView) findViewById(R.id.tv_current_order);

        phoneLanguage = Locale.getDefault().getLanguage();

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();


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
                        //check is documents are added
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

        if(firebaseUser!=null) {
            listenerRegistration = db.collection(getString(R.string.orders))
                    .whereEqualTo(getString(R.string.partner_id), firebaseUser.getUid())
                    .whereEqualTo(getString(R.string.order_status), getString(R.string.order_status_type_ongoing))
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                            if (error != null) {
                                Log.e(TAG, "Error is ", error);
                                return;
                            }

                            for (QueryDocumentSnapshot doc : value) {
                                document_id = doc.getId();
                            }

                            //if no current order
                            if (document_id == null || document_id.isEmpty()) {
                                tv_current_order.setVisibility(View.GONE);
                            } else {
                                tv_current_order.setVisibility(View.VISIBLE);
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

    @Override
    protected void onResume() {
        super.onResume();

        if (firebaseUser == null) {
            finishAffinity();
            Intent intent = new Intent(getApplicationContext(), GetStartedActivity.class);
            startActivity(intent);
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