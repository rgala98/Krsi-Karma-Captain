package com.krsikarma.captain.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.krsikarma.captain.R;
import com.krsikarma.captain.Utility.DefaultTextConfig;

public class RaiseAComplaintActivity extends AppCompatActivity {

    // From imported layout
    View view_1;
    Button btn_raise_complaint;
    Button btn_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DefaultTextConfig.adjustFontScale(RaiseAComplaintActivity.this);
        setContentView(R.layout.activity_raise_a_complaint);

        init();

    }

    private void init(){

        view_1 = (View) findViewById(R.id.view_1);
        btn_raise_complaint = (Button) findViewById(R.id.btn_raise_complaint);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);

        view_1.setVisibility(View.GONE);
        btn_raise_complaint.setVisibility(View.GONE);
        btn_cancel.setVisibility(View.GONE);

    }
}