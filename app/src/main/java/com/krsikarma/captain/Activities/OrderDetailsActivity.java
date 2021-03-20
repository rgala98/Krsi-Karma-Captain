package com.krsikarma.captain.Activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.krsikarma.captain.R;
import com.krsikarma.captain.Utility.DefaultTextConfig;
import com.krsikarma.captain.Utility.Utils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderDetailsActivity extends AppCompatActivity {


    public static final String TAG = "OrderDetailsActivity";
    ImageView img_back;
    TextView tv_service_name;
    TextView tv_metric_rate;
    TextView tv_final_amount;
    TextView tv_order_number;
    TextView tv_date;
    TextView tv_address;
    TextView tv_customer_name;
    TextView tv_customer_phone;
    TextView tv_get_directions;
    TextView tv_order_number_text;
    TextView tv_payment_text;
    TextView tv_payment;
    ImageView img_user;
    Button btn_cancel;
    Button btn_start;

    EditText et_number_1, et_number_2, et_number_3, et_number_4;

    FirebaseFirestore db;
    ListenerRegistration listenerRegistration;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;

    String user_id;
    String phone_number;
    Double latitude;
    Double longitude;
    String document_id;
    Long order_id;

    String phone_language;

    Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DefaultTextConfig.adjustFontScale(OrderDetailsActivity.this);
        setContentView(R.layout.activity_order_details);

        init();

        img_back.setOnClickListener(view -> onBackPressed());
        getData();

        tv_get_directions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String strUri = "http://maps.google.com/maps?q=loc:" + latitude + "," + longitude + " (" + "Krsi Karma" + ")";
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));

                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");

                startActivity(intent);
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> data = new HashMap<>();
                data.put(getString(R.string.order_status), getString(R.string.order_status_type_requested));
                data.put(getString(R.string.partner_id), null);


                AlertDialog.Builder builder;
                AlertDialog alert;

                builder = new AlertDialog.Builder(OrderDetailsActivity.this);
                builder.setMessage(getString(R.string.are_you_sure_cancel))
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                db.collection(getString(R.string.orders)).document(document_id)
                                        .update(data)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.i(TAG, "User has cancelled this order");
                                                finish();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                Log.i(TAG, "An error occurred " + e.getLocalizedMessage());

                                            }
                                        });
                            }
                        });
                alert = builder.create();
                alert.setTitle("");
                alert.show();


            }
        });

        btn_start.setOnClickListener(view -> {

            if (btn_start.getText().toString().equals(getString(R.string.start))) {
                //Start service

                LayoutInflater li = LayoutInflater.from(getApplicationContext());
                View otpView = li.inflate(R.layout.dialog_otp, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        OrderDetailsActivity.this);

                alertDialogBuilder.setView(otpView);
                alertDialogBuilder.setTitle(getString(R.string.otp_verification_text));

                et_number_1 = (EditText) otpView.findViewById(R.id.et_number_1);
                et_number_2 = (EditText) otpView.findViewById(R.id.et_number_2);
                et_number_3 = (EditText) otpView.findViewById(R.id.et_number_3);
                et_number_4 = (EditText) otpView.findViewById(R.id.et_number_4);
                shiftEditTextFocus();

                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.ok_text),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        String otp = et_number_1.getText().toString().trim() + et_number_2.getText().toString().trim() + et_number_3.getText().toString().trim() + et_number_4.getText().toString().trim();

                                        if (otp == null || otp.isEmpty() || otp.length() != 4) {
                                            utils.alertDialogOK(OrderDetailsActivity.this, getString(R.string.error_text), getString(R.string.correct_otp_text));
                                        } else {
                                            String orderid = String.valueOf(order_id);
                                            if (otp.equals(orderid.substring(orderid.length() - 4))) {

                                                Map<String, Object> data = new HashMap<>();
                                                data.put(getString(R.string.order_status), getString(R.string.order_status_type_ongoing));

                                                db.collection(getString(R.string.orders)).document(document_id)
                                                        .update(data)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                btn_start.setText(getString(R.string.end));
                                                                btn_cancel.setVisibility(View.GONE);
                                                                tv_order_number.setVisibility(View.VISIBLE);
                                                                tv_order_number_text.setVisibility(View.VISIBLE);
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                btn_start.setText(getString(R.string.start));
                                                                Log.i(TAG, "Error " + e.getMessage());
                                                            }
                                                        });

                                            } else {
                                                utils.alertDialogOK(OrderDetailsActivity.this, getString(R.string.error_text), getString(R.string.correct_otp_text));

                                            }
                                        }
                                    }
                                })
                        .setNegativeButton(getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();


            } else if (btn_start.getText().toString().equals(getString(R.string.end))) {
                //End Service

                //Change status to payment pending




                Map<String, Object> data = new HashMap<>();
                data.put(getString(R.string.order_status), getString(R.string.order_status_type_payment_pending));

                AlertDialog.Builder builder;
                AlertDialog alert;

                builder = new AlertDialog.Builder(OrderDetailsActivity.this);
                builder.setMessage(getString(R.string.are_you_sure_end))
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.end), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                db.collection(getString(R.string.orders)).document(document_id)
                                        .update(data)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                btn_start.setText(getString(R.string.collect_cash));
                                                btn_cancel.setVisibility(View.GONE);
                                                //utils.alertDialogOK(OrderDetailsActivity.this, "", getString(R.string.end_order_note));

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.i(TAG, "Error " + e.getMessage());
                                            }
                                        });
                            }
                        });


                alert = builder.create();
                alert.setTitle("");
                alert.show();


            }else if(btn_start.getText().toString().equals(getString(R.string.collect_cash))){
                //Order status is completed

                Map<String, Object> data = new HashMap<>();
                data.put(getString(R.string.order_status), getString(R.string.order_status_type_completed));
                data.put(getString(R.string.payment_mode), getString(R.string.cash));

                AlertDialog.Builder builder;
                AlertDialog alert;

                builder = new AlertDialog.Builder(OrderDetailsActivity.this);
                builder.setMessage(getString(R.string.are_you_sure_collect_cash))
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                db.collection(getString(R.string.orders)).document(document_id)
                                        .update(data)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                btn_start.setVisibility(View.GONE);
                                                btn_cancel.setVisibility(View.GONE);

                                                Toast.makeText(getApplicationContext(),getString(R.string.order_completed), Toast.LENGTH_LONG).show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.i(TAG, "Error " + e.getMessage());
                                            }
                                        });
                            }
                        });


                alert = builder.create();
                alert.setTitle("");
                alert.show();

            }

        });


    }

    private void init() {

        img_back = (ImageView) findViewById(R.id.img_back);
        tv_service_name = (TextView) findViewById(R.id.tv_service_name);
        tv_metric_rate = (TextView) findViewById(R.id.tv_metric_rate);
        tv_final_amount = (TextView) findViewById(R.id.tv_final_amount_1);
        tv_order_number = (TextView) findViewById(R.id.tv_order_number);
        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_address = (TextView) findViewById(R.id.tv_address);
        tv_customer_name = (TextView) findViewById(R.id.tv_customer_name);
        tv_customer_phone = (TextView) findViewById(R.id.tv_customer_phone);
        img_user = (ImageView) findViewById(R.id.img_user);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);

        tv_order_number_text = (TextView) findViewById(R.id.tv_order_number_text);
        tv_order_number_text.setVisibility(View.GONE);
        tv_order_number.setVisibility(View.GONE);

        tv_payment_text = (TextView) findViewById(R.id.tv_payment_text);
        tv_payment = (TextView) findViewById(R.id.tv_payment);
        tv_payment_text.setVisibility(View.GONE);
        tv_payment.setVisibility(View.GONE);

        btn_start = (Button) findViewById(R.id.btn_start);
        tv_get_directions = (TextView) findViewById(R.id.tv_get_directions);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        order_id = getIntent().getLongExtra("order_id", 0);
        phone_language = Locale.getDefault().getLanguage();
        utils = new Utils();
    }

    private void shiftEditTextFocus() {
        et_number_1.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (et_number_1.getText().toString().length() == 1)     //size as per your requirement
                {
                    et_number_2.requestFocus();
                }
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {


            }

            public void afterTextChanged(Editable s) {

            }

        });

        et_number_2.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (et_number_2.getText().toString().length() == 1)     //size as per your requirement
                {
                    et_number_3.requestFocus();
                }

                if (TextUtils.isEmpty(et_number_2.getText().toString())) {
                    et_number_1.requestFocus();
                }
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {

            }

            public void afterTextChanged(Editable s) {

            }

        });

        et_number_3.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (et_number_3.getText().toString().length() == 1)     //size as per your requirement
                {
                    et_number_4.requestFocus();
                }

                if (TextUtils.isEmpty(et_number_3.getText().toString())) {
                    et_number_2.requestFocus();
                }
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {

            }

            public void afterTextChanged(Editable s) {

            }

        });
        et_number_4.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(et_number_4.getText().toString())) {
                    et_number_3.requestFocus();
                }
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {

            }

            public void afterTextChanged(Editable s) {

            }

        });

    }

    private void getData() {

        if (order_id == 0) {
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


                            String service_id = "";
                            String service_name = "";
                            String service_image_url = "";
                            String order_id = "";

                            String order_date = "";
                            String order_time = "";
                            String order_status = "";
                            String order_address = "";
                            String order_quantity = "";
                            String order_rate = "";
                            String order_metric = "";
                            String order_amount = "";
                            String date_created = "";


                            for (QueryDocumentSnapshot doc : value) {
                                document_id = doc.getId();
                                if (doc.get(getString(R.string.order_id)) != null) {
                                    order_id = String.valueOf(doc.getLong(getString(R.string.order_id)));
                                    tv_order_number.setText(order_id);
                                }

                                if (doc.get(getString(R.string.service_id)) != null) {
                                    service_id = doc.getString(getString(R.string.service_id));
                                }

                                if (doc.get(getString(R.string.service_name)) != null) {
                                    service_name = doc.getString(getString(R.string.service_name));

                                }

                                if (doc.get(getString(R.string.order_latitude)) != null) {
                                    latitude = doc.getDouble(getString(R.string.order_latitude));
                                }

                                if (doc.get(getString(R.string.order_longitude)) != null) {
                                    longitude = doc.getDouble(getString(R.string.order_longitude));
                                }

                                if (doc.get(getString(R.string.image_url)) != null) {
                                    service_image_url = doc.getString(getString(R.string.image_url));

                                }

                                if (doc.get(getString(R.string.user_id)) != null) {
                                    user_id = doc.getString(getString(R.string.user_id));
                                    getUserDetails();
                                }


                                if (doc.get(getString(R.string.order_status)) != null) {
                                    order_status = doc.getString(getString(R.string.order_status));

                                    if (order_status.equals(getString(R.string.order_status_type_ongoing))) {
                                        btn_cancel.setVisibility(View.GONE);
                                        btn_start.setVisibility(View.VISIBLE);
                                        btn_start.setText(getString(R.string.end));
                                        tv_order_number.setVisibility(View.VISIBLE);
                                        tv_order_number_text.setVisibility(View.VISIBLE);

                                    }

                                    if (order_status.equals(getString(R.string.order_status_type_driver_assigned))) {
                                        btn_start.setVisibility(View.VISIBLE);
                                        btn_cancel.setVisibility(View.VISIBLE);
                                        tv_order_number.setVisibility(View.GONE);
                                        tv_order_number_text.setVisibility(View.GONE);
                                    }

                                    if (order_status.equals(getString(R.string.order_status_type_requested))) {
                                        btn_cancel.setVisibility(View.GONE);
                                        btn_start.setVisibility(View.GONE);
                                        tv_order_number.setVisibility(View.GONE);
                                        tv_order_number_text.setVisibility(View.GONE);

                                    }

                                    if (order_status.equals(getString(R.string.order_status_type_completed))) {
                                        btn_cancel.setVisibility(View.GONE);
                                        btn_start.setVisibility(View.GONE);
                                        tv_order_number.setVisibility(View.VISIBLE);
                                        tv_order_number_text.setVisibility(View.VISIBLE);
                                        tv_payment.setVisibility(View.VISIBLE);
                                        tv_payment_text.setVisibility(View.VISIBLE);

                                        if(doc.get(getString(R.string.payment_mode))!=null){
                                            tv_payment.setText(getString(R.string.paid_via) + " " + doc.getString(getString(R.string.payment_mode)));
                                        }

                                    }

                                    if (order_status.equals(getString(R.string.order_status_type_payment_pending))) {
                                        btn_cancel.setVisibility(View.GONE);
                                        btn_start.setVisibility(View.VISIBLE);
                                        btn_start.setText(getString(R.string.collect_cash));
                                        tv_order_number.setVisibility(View.VISIBLE);
                                        tv_order_number_text.setVisibility(View.VISIBLE);

                                    }
                                }

                                if (doc.get(getString(R.string.order_metric)) != null) {
                                    order_metric = doc.getString(getString(R.string.order_metric));
                                }


                                Double double_order_rate = null, double_order_quantity = null;
                                if (doc.get(getString(R.string.order_rate)) != null) {
                                    double_order_rate = doc.getDouble(getString(R.string.order_rate));
                                    order_rate = String.valueOf(double_order_rate);
                                    order_rate = order_rate.substring(0, order_rate.length() - 2);

                                }

                                if (doc.get(getString(R.string.order_quantity)) != null) {
                                    double_order_quantity = doc.getDouble(getString(R.string.order_quantity));
                                    order_quantity = String.valueOf(double_order_quantity);
                                    order_quantity = order_quantity.substring(0, order_quantity.length() - 2);

                                }

                                if (double_order_rate != null && double_order_quantity != null) {
                                    order_amount = String.valueOf(double_order_rate * double_order_quantity);
                                    order_amount = order_amount.substring(0, order_amount.length() - 2);
                                }

                                if (doc.get(getString(R.string.order_address)) != null) {
                                    order_address = doc.getString(getString(R.string.order_address));
                                }


                                if (doc.get(getString(R.string.date_created)) != null) {
                                    Timestamp ts_date = doc.getTimestamp(getString(R.string.date_created));
                                    SimpleDateFormat sfd_viewFormat = new SimpleDateFormat("d MMMM, yyyy");
                                    date_created = sfd_viewFormat.format(ts_date.toDate());

                                }

                                if (doc.get(getString(R.string.order_date_time)) != null) {
                                    Timestamp ts_date = doc.getTimestamp(getString(R.string.order_date_time));
                                    SimpleDateFormat sfd_viewFormat = new SimpleDateFormat("d MMMM, yyyy");
                                    order_date = sfd_viewFormat.format(ts_date.toDate());


                                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");
                                    order_time = timeFormat.format(ts_date.toDate());


                                }
                                tv_service_name.setText(service_name);
                                tv_metric_rate.setText(order_quantity + " " + getString(R.string.acres) + " x ₹ " + order_rate);
                                tv_final_amount.setText("₹ " + order_amount);
                                tv_date.setText(order_date + " at " + order_time);
                                tv_address.setText(order_address);

                                if (phone_language.equals("hi")) {
                                    utils.translateEnglishToHindi(service_name, tv_service_name);
                                    utils.translateEnglishToHindi(order_address, tv_address);
                                }


                            }
                        }
                    });

        } else {

            listenerRegistration = db.collection(getString(R.string.orders))
                    .whereEqualTo(getString(R.string.order_id), order_id)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                            if (error != null) {
                                Log.e(TAG, "Error is ", error);
                                return;
                            }


                            String service_id = "";
                            String service_name = "";
                            String service_image_url = "";
                            String order_id = "";

                            String order_date = "";
                            String order_time = "";
                            String order_status = "";
                            String order_address = "";
                            String order_quantity = "";
                            String order_rate = "";
                            String order_metric = "";
                            String order_amount = "";
                            String date_created = "";


                            for (QueryDocumentSnapshot doc : value) {
                                document_id = doc.getId();
                                if (doc.get(getString(R.string.order_id)) != null) {
                                    order_id = String.valueOf(doc.getLong(getString(R.string.order_id)));
                                    tv_order_number.setText(order_id);
                                }

                                if (doc.get(getString(R.string.service_id)) != null) {
                                    service_id = doc.getString(getString(R.string.service_id));
                                }

                                if (doc.get(getString(R.string.service_name)) != null) {
                                    service_name = doc.getString(getString(R.string.service_name));

                                }

                                if (doc.get(getString(R.string.order_latitude)) != null) {
                                    latitude = doc.getDouble(getString(R.string.order_latitude));
                                }

                                if (doc.get(getString(R.string.order_longitude)) != null) {
                                    longitude = doc.getDouble(getString(R.string.order_longitude));
                                }

                                if (doc.get(getString(R.string.image_url)) != null) {
                                    service_image_url = doc.getString(getString(R.string.image_url));

                                }

                                if (doc.get(getString(R.string.user_id)) != null) {
                                    user_id = doc.getString(getString(R.string.user_id));
                                    getUserDetails();
                                }


                                if (doc.get(getString(R.string.order_status)) != null) {
                                    order_status = doc.getString(getString(R.string.order_status));

                                    if (order_status.equals(getString(R.string.order_status_type_ongoing))) {
                                        btn_cancel.setVisibility(View.GONE);
                                        btn_start.setVisibility(View.VISIBLE);
                                        btn_start.setText(getString(R.string.end));
                                        tv_order_number.setVisibility(View.VISIBLE);
                                        tv_order_number_text.setVisibility(View.VISIBLE);

                                    }

                                    if (order_status.equals(getString(R.string.order_status_type_driver_assigned))) {
                                        btn_start.setVisibility(View.VISIBLE);
                                        btn_cancel.setVisibility(View.VISIBLE);
                                        tv_order_number.setVisibility(View.GONE);
                                        tv_order_number_text.setVisibility(View.GONE);
                                    }

                                    if (order_status.equals(getString(R.string.order_status_type_requested))) {
                                        btn_cancel.setVisibility(View.GONE);
                                        btn_start.setVisibility(View.GONE);
                                        tv_order_number.setVisibility(View.GONE);
                                        tv_order_number_text.setVisibility(View.GONE);

                                    }

                                    if (order_status.equals(getString(R.string.order_status_type_completed))) {
                                        btn_cancel.setVisibility(View.GONE);
                                        btn_start.setVisibility(View.GONE);
                                        tv_order_number.setVisibility(View.VISIBLE);
                                        tv_order_number_text.setVisibility(View.VISIBLE);
                                        tv_payment.setVisibility(View.VISIBLE);
                                        tv_payment_text.setVisibility(View.VISIBLE);

                                        if(doc.get(getString(R.string.payment_mode))!=null){
                                            tv_payment.setText(getString(R.string.paid_via) + " " + doc.getString(getString(R.string.payment_mode)));
                                        }

                                    }

                                    if (order_status.equals(getString(R.string.order_status_type_payment_pending))) {
                                        btn_cancel.setVisibility(View.GONE);
                                        btn_start.setVisibility(View.VISIBLE);
                                        btn_start.setText(getString(R.string.collect_cash));
                                        tv_order_number.setVisibility(View.VISIBLE);
                                        tv_order_number_text.setVisibility(View.VISIBLE);

                                    }

                                }

                                if (doc.get(getString(R.string.order_metric)) != null) {
                                    order_metric = doc.getString(getString(R.string.order_metric));
                                }


                                Double double_order_rate = null, double_order_quantity = null;
                                if (doc.get(getString(R.string.order_rate)) != null) {
                                    double_order_rate = doc.getDouble(getString(R.string.order_rate));
                                    order_rate = String.valueOf(double_order_rate);
                                    order_rate = order_rate.substring(0, order_rate.length() - 2);

                                }

                                if (doc.get(getString(R.string.order_quantity)) != null) {
                                    double_order_quantity = doc.getDouble(getString(R.string.order_quantity));
                                    order_quantity = String.valueOf(double_order_quantity);
                                    order_quantity = order_quantity.substring(0, order_quantity.length() - 2);

                                }

                                if (double_order_rate != null && double_order_quantity != null) {
                                    order_amount = String.valueOf(double_order_rate * double_order_quantity);
                                    order_amount = order_amount.substring(0, order_amount.length() - 2);
                                }

                                if (doc.get(getString(R.string.order_address)) != null) {
                                    order_address = doc.getString(getString(R.string.order_address));
                                }


                                if (doc.get(getString(R.string.date_created)) != null) {
                                    Timestamp ts_date = doc.getTimestamp(getString(R.string.date_created));
                                    SimpleDateFormat sfd_viewFormat = new SimpleDateFormat("d MMMM, yyyy");
                                    date_created = sfd_viewFormat.format(ts_date.toDate());

                                }

                                if (doc.get(getString(R.string.order_date_time)) != null) {
                                    Timestamp ts_date = doc.getTimestamp(getString(R.string.order_date_time));
                                    SimpleDateFormat sfd_viewFormat = new SimpleDateFormat("d MMMM, yyyy");
                                    order_date = sfd_viewFormat.format(ts_date.toDate());


                                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");
                                    order_time = timeFormat.format(ts_date.toDate());


                                }
                                tv_service_name.setText(service_name);
                                tv_metric_rate.setText(order_quantity + " " + getString(R.string.acres) + " x ₹ " + order_rate);
                                tv_final_amount.setText("₹ " + order_amount);
                                tv_date.setText(order_date + " at " + order_time);
                                tv_address.setText(order_address);

                                if (phone_language.equals("hi")) {
                                    utils.translateEnglishToHindi(service_name, tv_service_name);
                                    utils.translateEnglishToHindi(order_address, tv_address);
                                }


                            }
                        }
                    });
        }


    }

    private void getUserDetails() {

        if (user_id != null) {
            listenerRegistration = db.collection(getString(R.string.users)).document(user_id)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                Log.e(TAG, "Error is ", error);
                                return;
                            }

                            String first_name = "";
                            String last_name = "";
                            String full_name;

                            if (snapshot.get(getString(R.string.first_name)) != null) {
                                first_name = snapshot.getString(getString(R.string.first_name));
                            }

                            if (snapshot.get(getString(R.string.last_name)) != null) {
                                last_name = snapshot.getString(getString(R.string.last_name));
                            }

                            full_name = first_name + " " + last_name;
                            tv_customer_name.setText(full_name);

                            if (phone_language.equals("hi")) {
                                utils.translateEnglishToHindi(full_name, tv_customer_name);
                            }

                            if (snapshot.get(getString(R.string.phone_number)) != null) {
                                phone_number = snapshot.getString(getString(R.string.phone_number));
                            }

                            tv_customer_phone.setText(getString(R.string.call) + "(+91 " + phone_number.substring(3) + ")");

                            if (snapshot.get(getString(R.string.profile_img_url)) != null) {

                                String profile_image = snapshot.getString(getString(R.string.profile_img_url));
                                Glide.with(getApplicationContext())
                                        .load(profile_image)
                                        .into(img_user);
                            }
                        }
                    });

        }

        tv_customer_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPermissionListeners();
            }
        });
    }

    private void createPermissionListeners() {

        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CALL_PHONE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {

                if (report.areAllPermissionsGranted()) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone_number));
                    startActivity(intent);
                }

                // check for permanent denial of any permission
                if (report.isAnyPermissionPermanentlyDenied()) {
                    Log.i(TAG, "Permissions permanently denied. Open Settings");


                    AlertDialog.Builder builder;
                    AlertDialog alert;

                    builder = new AlertDialog.Builder(OrderDetailsActivity.this);
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
                Toast.makeText(getApplicationContext(), "Error occurred! " + error.toString(), Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Error Occurred" + error.toString());
            }
        }).check();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration = null;
        }

        utils.closeTranslator();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}