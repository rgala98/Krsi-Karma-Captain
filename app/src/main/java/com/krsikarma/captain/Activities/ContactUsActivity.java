package com.krsikarma.captain.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.krsikarma.captain.R;
import com.krsikarma.captain.Utility.DefaultTextConfig;

public class ContactUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DefaultTextConfig.adjustFontScale(ContactUsActivity.this);
        setContentView(R.layout.activity_contact_us);

        init();

        // ON SUBMIT SUCCESSFUL SHOW A TOAST "Thank you for contacting us! We will reach out to you shortly."
    }

    private void init(){

    }
}