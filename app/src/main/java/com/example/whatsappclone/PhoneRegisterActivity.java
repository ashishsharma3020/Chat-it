package com.example.whatsappclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.whatsappclone.Models.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class PhoneRegisterActivity extends AppCompatActivity {

    Button register;
    EditText username, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_register);
        getSupportActionBar().hide();
        register = findViewById(R.id.phoneregister);
        username = findViewById(R.id.phoneusername1);
        email = findViewById(R.id.phoneemail);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (username.getText().toString().isEmpty()) {
                    username.setError("Username can not be blank");
                    return;
                }
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Users users = new Users();
                users.setUserid(user.getUid());
                users.setUsername(username.getText().toString());
                users.setMail(email.getText().toString());
                users.setPhonenumber(getIntent().getStringExtra("number"));
                FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).setValue(users);
                Toast.makeText(PhoneRegisterActivity.this, "you are successful registered", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(PhoneRegisterActivity.this, MainActivity.class));
                finish();

            }
        });
    }
}