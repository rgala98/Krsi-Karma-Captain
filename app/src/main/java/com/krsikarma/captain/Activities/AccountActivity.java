package com.krsikarma.captain.Activities;

import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.krsikarma.captain.Adapters.ServiceTypeAdapter;
import com.krsikarma.captain.Models.ServiceType;
import com.krsikarma.captain.R;
import com.krsikarma.captain.Utility.DefaultTextConfig;
import com.krsikarma.captain.Utility.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {

    public static final String TAG = "AccountActivity";
    Button btn_submit;
    TextView tv_phone;
    EditText et_first_name;
    EditText et_last_name;
    ImageView img_back;
    ProgressBar progressBar;

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore db;
    ListenerRegistration listenerRegistration;

    Utils utils;


    String first_name;
    String last_name;
    String phone_number;

    RecyclerView service_recycler_view;
    FlexboxLayoutManager recycler_view_manager;
    ServiceTypeAdapter serviceTypeAdapter;

    ArrayList<ServiceType> serviceTypeArrayList;
    ArrayList<ServiceType> selectedServiceTypeList;
    ArrayList<ServiceType> previouslySelectedServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DefaultTextConfig.adjustFontScale(AccountActivity.this);
        setContentView(R.layout.activity_account);

        init();
        getAllServiceTypes();



        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedServiceTypeList = serviceTypeAdapter.getUsersServices();


                if (!et_first_name.getText().toString().trim().isEmpty() && !et_last_name.getText().toString().trim().isEmpty() && !selectedServiceTypeList.isEmpty()) {
                    btn_submit.setEnabled(false);
                    updateData(selectedServiceTypeList);
                } else {
                    utils.alertDialogOK(AccountActivity.this, getString(R.string.error_text), getString(R.string.complete_all_fields));
                }
            }
        });
    }

    private void init() {
        btn_submit = (Button) findViewById(R.id.btn_submit);
        et_first_name = (EditText) findViewById(R.id.et_first_name);
        et_last_name = (EditText) findViewById(R.id.et_last_name);


        tv_phone = (TextView) findViewById(R.id.tv_phone);
        img_back = (ImageView) findViewById(R.id.img_back);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        utils = new Utils();

        service_recycler_view = (RecyclerView) findViewById(R.id.recycler_view);

        recycler_view_manager = new FlexboxLayoutManager(getApplicationContext());
        recycler_view_manager.setFlexDirection(FlexDirection.ROW);
        recycler_view_manager.setJustifyContent(JustifyContent.SPACE_EVENLY);
        recycler_view_manager.setFlexWrap(FlexWrap.WRAP);

        serviceTypeArrayList = new ArrayList<>();
        selectedServiceTypeList = new ArrayList<>();
        previouslySelectedServices = new ArrayList<>();
        service_recycler_view.setLayoutManager(recycler_view_manager);


    }

    private void getData() {
        if (firebaseUser != null) {
            listenerRegistration = db.collection(getString(R.string.partners)).document(firebaseUser.getUid())
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {

                            if (error != null) {
                                Log.i(TAG, "An error occurred " + error.getLocalizedMessage());
                                return;
                            }

                            Boolean isDriverApproved = false;

                            if (snapshot != null && snapshot.exists()) {

                                if (snapshot.get(getString(R.string.is_driver_approved)) != null) {
                                    isDriverApproved = snapshot.getBoolean(getString(R.string.is_driver_approved));

                                }

                                if (isDriverApproved) {
                                    et_last_name.setEnabled(false);
                                    et_first_name.setEnabled(false);
                                } else {
                                    et_last_name.setEnabled(true);
                                    et_first_name.setEnabled(true);
                                }

                                if (!utils.isStringNull(snapshot.getString(getString(R.string.first_name)))) {
                                    first_name = snapshot.getString(getString(R.string.first_name));
                                    et_first_name.setText(first_name);
                                }

                                if (!utils.isStringNull(snapshot.getString(getString(R.string.last_name)))) {
                                    last_name = snapshot.getString(getString(R.string.last_name));
                                    et_last_name.setText(last_name);
                                }

                                if (!utils.isStringNull(snapshot.getString(getString(R.string.phone_number)))) {
                                    phone_number = snapshot.getString(getString(R.string.phone_number));
                                    tv_phone.setText(phone_number.substring(3));
                                }



                                if (snapshot.get(getString(R.string.selected_service_type)) != null) {
                                    previouslySelectedServices = (ArrayList<ServiceType>) snapshot.get(getString(R.string.selected_service_type));




                                    serviceTypeAdapter = new ServiceTypeAdapter(AccountActivity.this, serviceTypeArrayList, previouslySelectedServices);
                                    service_recycler_view.setAdapter(serviceTypeAdapter);
                                }


                            }


                        }
                    });
        }

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


                        serviceTypeArrayList.clear();
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

                        getData();

                    }
                });

    }

    private void updateData(ArrayList<ServiceType> selectedServices) {
        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> data = new HashMap<>();
        data.put(getString(R.string.first_name), et_first_name.getText().toString().trim());
        data.put(getString(R.string.last_name), et_last_name.getText().toString().trim());
        data.put(getString(R.string.selected_service_type), selectedServices);




        db.collection(getString(R.string.partners)).document(firebaseUser.getUid())
                .update(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Data is updated");
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), getString(R.string.updated), Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Log.i(TAG, "An error occurred " + e.getLocalizedMessage());
                        Toast.makeText(getApplicationContext(), getString(R.string.error_text), Toast.LENGTH_SHORT).show();
                        btn_submit.setEnabled(true);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration = null;
        }
    }
}