package com.example.whatsappclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hbb20.CountryCodePicker;

public class PhoneActivity extends AppCompatActivity {

    Button verify;
    CountryCodePicker ccp;
    EditText phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        verify = findViewById(R.id.otp);
        ccp = findViewById(R.id.ccp);
        phone = findViewById(R.id.etphone);
        ccp.registerCarrierNumberEditText(phone);


        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PhoneActivity.this, OtpActivity.class);
                intent.putExtra("mobile", ccp.getFullNumberWithPlus().replace(" ", ""));
                startActivity(intent);
                finish();

            }
        });
    }
}
