package com.krsikarma.captain.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.krsikarma.captain.R;
import com.krsikarma.captain.Utility.DefaultTextConfig;
import com.krsikarma.captain.Utility.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ContactUsActivity extends AppCompatActivity {

    public static final String TAG = "ContactUsActivity";
    EditText et_message;
    Button btn_submit;
    ImageView img_back;

    FirebaseFirestore db;
    FirebaseUser firebaseUser;
    FirebaseAuth mAuth;

    Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DefaultTextConfig.adjustFontScale(ContactUsActivity.this);
        setContentView(R.layout.activity_contact_us);

        init();

        

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!et_message.getText().toString().trim().isEmpty()){
                    sendMessage();
                }else{
                    utils.alertDialogOK(ContactUsActivity.this, getString(R.string.error_text), getString(R.string.complete_all_fields));
                }

            }
        });

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void init(){

        et_message = findViewById(R.id.et_message);
        btn_submit = findViewById(R.id.btn_submit);
        img_back = findViewById(R.id.img_back);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        utils = new Utils();

    }

    private void sendMessage(){


        Map<String, Object> data = new HashMap<>();
        data.put(getString(R.string.date_created), new Date());
        data.put(getString(R.string.user_id), firebaseUser.getUid());
        data.put(getString(R.string.user_type), "partner");
        data.put(getString(R.string.message_fb), et_message.getText().toString().trim());


        db.collection(getString(R.string.contact_us_fb)).document()
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finish();
                        Toast.makeText(getApplicationContext(), getString(R.string.message_sent), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "error", e);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}