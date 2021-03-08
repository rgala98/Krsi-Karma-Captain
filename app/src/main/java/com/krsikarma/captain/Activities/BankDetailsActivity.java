package com.krsikarma.captain.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.krsikarma.captain.R;
import com.krsikarma.captain.Utility.DefaultTextConfig;
import com.krsikarma.captain.Utility.Utils;

import java.util.HashMap;
import java.util.Map;

public class BankDetailsActivity extends AppCompatActivity {

    public static final String TAG = "BankDetailsActivity";
    EditText et_full_name, et_bank_name, et_account_number, et_ifsc_code, et_branch_name;
    Button btn_continue;
    ProgressBar progressBar;
    Utils utils;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DefaultTextConfig.adjustFontScale(BankDetailsActivity.this);
        setContentView(R.layout.activity_bank_details);

        init();

        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String full_name = et_full_name.getText().toString().trim();
                String bank_name = et_bank_name.getText().toString().trim();
                String account_no = et_account_number.getText().toString().trim();
                String ifsc_code = et_ifsc_code.getText().toString().trim();
                String branch_name = et_branch_name.getText().toString().trim();

                if (!full_name.isEmpty() || !bank_name.isEmpty() || !account_no.isEmpty() || !ifsc_code.isEmpty() || !branch_name.isEmpty()) {

                    Map<String, Object> data = new HashMap<>();
                    data.put(getString(R.string.bank_full_name), full_name);
                    data.put(getString(R.string.fb_bank_name), bank_name);
                    data.put(getString(R.string.fb_account_no), account_no);
                    data.put(getString(R.string.fb_ifsc_code), ifsc_code);
                    data.put(getString(R.string.fb_branch_name), branch_name);

                    uploadData(data);

                } else {
                    utils.alertDialogOK(BankDetailsActivity.this, getString(R.string.error_text), getString(R.string.complete_all_fields));
                }

            }
        });
    }

    private void init() {
        et_full_name = (EditText) findViewById(R.id.et_full_name);
        et_bank_name = (EditText) findViewById(R.id.et_bank_name);
        et_account_number = (EditText) findViewById(R.id.et_account_number);
        et_ifsc_code = (EditText) findViewById(R.id.et_ifsc_code);
        et_branch_name = (EditText) findViewById(R.id.et_branch_name);
        btn_continue = (Button) findViewById(R.id.btn_continue);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        utils = new Utils();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
    }

    private void uploadData(Map<String, Object> data) {

        progressBar.setVisibility(View.VISIBLE);
        db.collection(getString(R.string.partners)).document(firebaseUser.getUid())
                .update(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.GONE);
                        finishAffinity();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Log.i(TAG, "An error occurred" + e.getLocalizedMessage());

                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}