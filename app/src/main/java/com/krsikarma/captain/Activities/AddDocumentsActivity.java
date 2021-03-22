package com.krsikarma.captain.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.krsikarma.captain.R;
import com.krsikarma.captain.Utility.DefaultTextConfig;
import com.krsikarma.captain.Utility.Utils;


public class AddDocumentsActivity extends AppCompatActivity {

    public static final String TAG = "AddDocumentsActivity";


    TextView tv_aadhar_card, tv_driving_license, tv_vehicle_rc;
    Button btn_register;

    ListenerRegistration listenerRegistration;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;

    String aadhar_url;
    String rc_url;
    String dl_url;
    String activity_from;

    ImageView img_aadhar_check;
    ImageView img_dl_check;
    ImageView img_rc_check;

    Utils utils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DefaultTextConfig.adjustFontScale(AddDocumentsActivity.this);
        setContentView(R.layout.activity_add_documents);

        init();


        tv_aadhar_card.setOnClickListener(view -> {
            getData();
            Intent intent = new Intent(getApplicationContext(), AddPhotoActivity.class);
            intent.putExtra("document_name", "aadhar_card");
            intent.putExtra("heading", tv_aadhar_card.getText());

            startActivity(intent);
        });

        tv_driving_license.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), AddPhotoActivity.class);
            intent.putExtra("document_name", "driving_license");
            intent.putExtra("heading", tv_driving_license.getText());

            startActivity(intent);
        });

        tv_vehicle_rc.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), AddPhotoActivity.class);
            intent.putExtra("document_name", "vehicle_rc");
            intent.putExtra("heading", tv_vehicle_rc.getText());

            startActivity(intent);
        });


        getData();

        //TODO: THIS IS A FLAW

        if(activity_from.equals("FromCreateProfile")){
            btn_register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (aadhar_url != null && !aadhar_url.isEmpty() && dl_url != null && !dl_url.isEmpty()) {
                        finishAffinity();
                        Intent intent = new Intent(getApplicationContext(), BankDetailsActivity.class);
                        intent.putExtra("activity_from", "FromAddDocuments");
                        startActivity(intent);

                    } else {
                        utils.alertDialogOK(AddDocumentsActivity.this, getString(R.string.error_text), getString(R.string.fill_aadhar_dl));
                    }
                }
            });
        }else{
            btn_register.setText(getString(R.string.done));
            btn_register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }


    }

    private void init() {

        tv_aadhar_card = (TextView) findViewById(R.id.tv_aadhar_card);
        tv_driving_license = (TextView) findViewById(R.id.tv_driving_license);
        tv_vehicle_rc = (TextView) findViewById(R.id.tv_vehicle_rc);
        btn_register = (Button) findViewById(R.id.btn_register);

        img_aadhar_check = (ImageView) findViewById(R.id.img_aadhar_check);
        img_dl_check = (ImageView) findViewById(R.id.img_dl_check);
        img_rc_check = (ImageView) findViewById(R.id.img_rc_check);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        utils = new Utils();

        activity_from = getIntent().getStringExtra("activity_from");
        if (activity_from == null) {
            activity_from = "";
        }
    }

    private void getData() {
        listenerRegistration = db.collection(getString(R.string.partners)).document(firebaseUser.getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "Error occurred ", error);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            if (snapshot.getString(getString(R.string.aadhar_url)) != null) {
                                aadhar_url = snapshot.getString(getString(R.string.aadhar_url));
                                tv_aadhar_card.setTextColor(getColor(R.color.black));
                                img_aadhar_check.setImageDrawable(getDrawable(R.drawable.ic_check_green));
                            } else {
                                tv_aadhar_card.setTextColor(getColor(R.color.gray));
                                img_aadhar_check.setImageDrawable(getDrawable(R.drawable.ic_cancelled));
                            }

                            if (snapshot.getString(getString(R.string.rc_url)) != null) {
                                rc_url = snapshot.getString(getString(R.string.rc_url));
                                tv_vehicle_rc.setTextColor(getColor(R.color.black));
                                img_rc_check.setImageDrawable(getDrawable(R.drawable.ic_check_green));
                            } else {
                                tv_vehicle_rc.setTextColor(getColor(R.color.gray));
                                img_rc_check.setImageDrawable(getDrawable(R.drawable.ic_cancelled));
                            }

                            if (snapshot.getString(getString(R.string.driving_license_url)) != null) {
                                dl_url = snapshot.getString(getString(R.string.driving_license_url));
                                tv_driving_license.setTextColor(getColor(R.color.black));
                                img_dl_check.setImageDrawable(getDrawable(R.drawable.ic_check_green));
                            } else {
                                tv_driving_license.setTextColor(getColor(R.color.gray));
                                img_dl_check.setImageDrawable(getDrawable(R.drawable.ic_cancelled));
                            }
                        }
                    }
                });
    }





    @Override
    protected void onStop() {
        super.onStop();
        if(listenerRegistration!=null){
            listenerRegistration=null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}