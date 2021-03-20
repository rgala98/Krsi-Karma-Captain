package com.krsikarma.captain.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.krsikarma.captain.Models.Transaction;
import com.krsikarma.captain.R;
import com.krsikarma.captain.Utility.DefaultTextConfig;
import com.krsikarma.captain.Utility.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RaiseAComplaintActivity extends AppCompatActivity {

    public static final String TAG = "RaiseAComplaintActivity";
    // From imported layout
    View view_1;
    Button btn_raise_complaint;
    Button btn_cancel;

    int position;
    Transaction transaction;

    TextView tv_status, tv_date, tv_amount;
    ImageView img_status, img_back;
    EditText et_complaint;
    Button btn_submit;
    String phoneLanguage;
    Utils utils;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DefaultTextConfig.adjustFontScale(RaiseAComplaintActivity.this);
        setContentView(R.layout.activity_raise_a_complaint);

        init();

        if(transaction!=null) {

            if (transaction.getRequest_status().equalsIgnoreCase("pending")) {

                img_status.setImageResource(R.drawable.ic_pending);
                tv_amount.setTextColor(getColor(R.color.yellow));
            } else if (transaction.getRequest_status().equalsIgnoreCase("rejected")) {

                img_status.setImageResource(R.drawable.ic_cancelled);
                tv_amount.setTextColor(getColor(R.color.brand_color));

            } else if (transaction.getRequest_status().equalsIgnoreCase("cancelled")) {

                img_status.setImageResource(R.drawable.ic_cancelled);
                tv_amount.setTextColor(getColor(R.color.brand_color));

            } else if (transaction.getRequest_status().equalsIgnoreCase("received")) {

                img_status.setImageResource(R.drawable.ic_received);
                tv_amount.setTextColor(getColor(R.color.green));

            }
        }

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!et_complaint.getText().toString().trim().isEmpty()){
                    sendComplaint();
                }else{
                    utils.alertDialogOK(RaiseAComplaintActivity.this,getString(R.string.error_text), getString(R.string.complete_all_fields));
                }

            }
        });

    }

    private void init(){

        view_1 = (View) findViewById(R.id.view_1);
        btn_raise_complaint = (Button) findViewById(R.id.btn_raise_complaint);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);

        view_1.setVisibility(View.GONE);
        btn_raise_complaint.setVisibility(View.GONE);
        btn_cancel.setVisibility(View.GONE);


        tv_status = (TextView) findViewById(R.id.tv_status);
        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_amount = (TextView) findViewById(R.id.tv_amount);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_status = (ImageView) findViewById(R.id.img_status);
        btn_submit = (Button) findViewById(R.id.btn_submit);
        et_complaint = (EditText) findViewById(R.id.et_complaint);

        phoneLanguage = Locale.getDefault().getLanguage();

        position = getIntent().getIntExtra("position", 0);
        transaction = (Transaction) getIntent().getParcelableArrayListExtra("transactionArrayList").get(position);

        if(transaction!=null){
            tv_amount.setText("₹ " + transaction.getRequest_amount().substring(0, transaction.getRequest_amount().length() - 2));
            tv_date.setText(transaction.getDate_created());
            tv_status.setText(transaction.getRequest_status());

            if(phoneLanguage.equals("hi")){
                utils.translateEnglishToHindi(transaction.getRequest_status(), tv_status);
            }
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

    }

    private void sendComplaint(){
        Map<String, Object> data = new HashMap<>();
        data.put(getString(R.string.date_created), new Date());
        data.put(getString(R.string.withdraw_request_document_id), transaction.getDocument_id());
        data.put(getString(R.string.request_amount), transaction.getRequest_amount());
        data.put(getString(R.string.withdraw_request_date), transaction.getDate_created());
        data.put(getString(R.string.message_fb), et_complaint.getText().toString().trim());


        db.collection(getString(R.string.withdraw_request_complaints))
                .document()
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), getString(R.string.message_sent), Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "error " + e.getMessage());
                    }
                });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}