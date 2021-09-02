package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {
    EditText e1, e2, e3, e4, e5, e6;
    Button verify;
    String phonenumber, mverificationid;
    String otp;
    FirebaseAuth auth;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        auth = FirebaseAuth.getInstance();
        e1 = findViewById(R.id.et1);
        e2 = findViewById(R.id.et2);
        e3 = findViewById(R.id.et3);
        e4 = findViewById(R.id.et4);
        e5 = findViewById(R.id.et5);
        e6 = findViewById(R.id.et6);
        e1.addTextChangedListener(new GenericTextWatcher(e1, e2));
        e2.addTextChangedListener(new GenericTextWatcher(e2, e3));
        e3.addTextChangedListener(new GenericTextWatcher(e3, e4));
        e4.addTextChangedListener(new GenericTextWatcher(e4, e5));
        e5.addTextChangedListener(new GenericTextWatcher(e5, e6));
        e6.addTextChangedListener(new GenericTextWatcher(e6, null));
        e1.setOnKeyListener(new GenericKeyEvent(e1, null));
        e2.setOnKeyListener(new GenericKeyEvent(e2, e1));
        e3.setOnKeyListener(new GenericKeyEvent(e3, e2));
        e4.setOnKeyListener(new GenericKeyEvent(e4, e3));
        e5.setOnKeyListener(new GenericKeyEvent(e5, e4));
        e6.setOnKeyListener(new GenericKeyEvent(e6, e5));
        verify = findViewById(R.id.verify);
        phonenumber = getIntent().getStringExtra("mobile");

        preferences = getSharedPreferences("com.example.whatsappclone.name", Context.MODE_PRIVATE);
        editor = preferences.edit();

        initiateotp();
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                otp = e1.getText().toString() + e2.getText().toString() + e3.getText().toString() + e4.getText().toString() + e5.getText().toString() + e6.getText().toString();
                if (otp.length() != 6)
                    Toast.makeText(OtpActivity.this, "invalid OTP ", Toast.LENGTH_SHORT).show();
                else {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mverificationid, otp);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });


    }

    public void initiateotp() {
        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {


                if (e instanceof FirebaseAuthInvalidCredentialsException) {

                    Toast.makeText(OtpActivity.this, "invalid number formate ", Toast.LENGTH_SHORT).show();


                } else if (e instanceof FirebaseTooManyRequestsException) {

                    Toast.makeText(OtpActivity.this, "limit exceeded ", Toast.LENGTH_SHORT).show();


                } else {
                    Toast.makeText(OtpActivity.this, "some other error ", Toast.LENGTH_SHORT).show();

                }


            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mverificationid = verificationId;

            }
        };
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phonenumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);


    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            editor.putInt("method", 3);
                            editor.apply();
                            Toast.makeText(OtpActivity.this, "sign in successful ", Toast.LENGTH_SHORT).show();

                            Intent intent=new Intent(OtpActivity.this,PhoneRegisterActivity.class);
                            intent.putExtra("number",phonenumber);
                            startActivity(intent);
                            finish();

                        } else {


                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(OtpActivity.this, "Invalid OTP,resend OTP", Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                });
    }

    public class GenericKeyEvent implements View.OnKeyListener {
        EditText c1, p1;

        public GenericKeyEvent(EditText c1, EditText p1) {
            this.c1 = c1;
            this.p1 = p1;
        }

        @Override
        public boolean onKey(View view, int i, KeyEvent keyEvent) {

            c1.setText(null);
            if (p1 != null)
                p1.requestFocus();

            return false;
        }

    }

    public class GenericTextWatcher implements TextWatcher {
        EditText c1, p1;

        public GenericTextWatcher(EditText c1, EditText p1) {
            this.c1 = c1;
            this.p1 = p1;

        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                                      int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int
                i2) {

        }


        @Override
        public void afterTextChanged(Editable editable) {

            String text = editable.toString();
            if (text.length() == 1) {
                switch (c1.getId()) {
                    case R.id.et1:
                        p1.requestFocus();
                        break;
                    case R.id.et2:
                        p1.requestFocus();
                        break;
                    case R.id.et3:
                        p1.requestFocus();
                        break;
                    case R.id.et4:
                        p1.requestFocus();
                        break;
                    case R.id.et5:
                        p1.requestFocus();
                        break;
                }
            }

        }

    }
}