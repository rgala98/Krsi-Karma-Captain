package com.krsikarma.captain.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.krsikarma.captain.R;
import com.krsikarma.captain.Utility.DefaultTextConfig;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DefaultTextConfig.adjustFontScale(AccountActivity.this);
        setContentView(R.layout.activity_account);

        init();
    }

    private void init(){}
}