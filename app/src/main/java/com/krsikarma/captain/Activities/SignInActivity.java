package com.krsikarma.captain.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.krsikarma.captain.R;
import com.krsikarma.captain.Utility.DefaultTextConfig;
import com.krsikarma.captain.Utility.Utils;

public class SignInActivity extends AppCompatActivity {

    EditText et_phone;
    Button btn_sign_in;
    String phone_number;
    Utils utils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DefaultTextConfig.adjustFontScale(SignInActivity.this);
        setContentView(R.layout.activity_sign_in);

        init();


        btn_sign_in.setOnClickListener(view -> {

            if (et_phone.getText().toString().trim().length() != 10) {
                utils.alertDialogOK(SignInActivity.this, getString(R.string.error_text), getString(R.string.error_enter_10_digit_text));
            } else if (et_phone.getText().toString().charAt(0) != '9' &&
                    et_phone.getText().toString().charAt(0) != '8' &&
                    et_phone.getText().toString().charAt(0) != '7') {
                utils.alertDialogOK(SignInActivity.this, getString(R.string.error_text), getString(R.string.error_enter_valid_text));
            } else {
                phone_number = "+91" + et_phone.getText().toString().trim();
                showMessageOptions(getString(R.string.send_otp_to_text) + " " + phone_number, getString(R.string.send_otp_confirm_text));

            }


        });

    }

    private void init() {
        et_phone = (EditText) findViewById(R.id.et_phone);
        btn_sign_in = (Button) findViewById(R.id.btn_sign_in);
        utils = new Utils();
    }

    public void showMessageOptions(String title, String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
        builder.setCancelable(true);

        builder.setPositiveButton(getString(R.string.ok_text), (dialog, which) -> {

            Intent intent = new Intent(getApplicationContext(), EnterOtpActivity.class);
            intent.putExtra("phone_number", phone_number);
            startActivity(intent);

        });
        builder.setNegativeButton(getString(R.string.cancel_text), (dialog, which) -> dialog.dismiss());

        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();

    }
}