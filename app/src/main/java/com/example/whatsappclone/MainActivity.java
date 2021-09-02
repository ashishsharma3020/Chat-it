package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.whatsappclone.Adapters.FragmentsAdapter;


import com.example.whatsappclone.Models.Users;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    TabLayout tabLayout;
    ViewPager viewPager;
    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount account;
    ProgressDialog pd;
    FirebaseDatabase database;
    FirebaseUser user;
    SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences("com.example.whatsappclone.name", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("privatekeystring", "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCqJeSO+7alUJcb+mEX6/THx8vIpN7gta+hT2V7Y9fs/mF7xKp972sgTq1Sf+pmRW4hg3dkhYaT/QL3HGEjtJcDOB/kHM6XTfrlntJotuboJOglvA5VTm23jOGgvOZJwGjECEESobKQqHPsJkt1Em4cQhFr1xZwGu6v91mVpwYAZoy3po4S4LXZhsfl1vFEc3ysoNvxi3oLQq6cTd50wpVbfnno8vYO1MXUBK/aLfWotQnrDRPW+/i2XLI6qh3LI1YcngxYAchQKayaHllIZKqnOAkGbSI7vmv7ED6Obkr0OGAihI6vZsxKhD4Ij6aT7TlQsjyNUW0aLJxfFHPSVbhpAgMBAAECggEAauAudxnpmmHK6UoW3v3Z3fvEJ55YWXIGhh7nMeB/a2BGP4ZDSaOv+LUy7dVnmSIXkXmQ71VoOWD+EdaDtffju6PvdEwcALJ83OssY5Yv6LX2FG3R2wfOpzMZLuw5Ay6XIDvHFDr5fSAy4K0VTM5s19cwNjr+3yArueB3Jyeo/2piZ6NBZYKrf9XPhZjfJcgjeKDn7Wyv37LFw+c6A4/j5TgzdG5tyezH+4vDz1z7KCeRz1LM+maXb0ToTean1MMd3DE2wwJVFGMBEGURe+Kpps5xtKzyDgEFdwzee/fWjM0P81opClL9fQhKJWMZg0m2sq3n/MOPn8Z+Qy/5vbUvAQKBgQDvSPOoakCk+v0YTRFzKDp5Xm1yLZ9zfC+EiDY9DGtN7H7xy5/FQb7foD8piSAiRPvi1+9VyPV2ttQF5mtXVGbjHxR8FtiMXHHqyBMZGirJstLKNDqPak/NeScD2NSf3j3VwnpQs3eUpD27ai9t9wiETBV47wJ+71IdKzSKgQkMeQKBgQC2CJboCrs/dPK8kpzK63wa617ai8m3Dmr54RFWcTJuJPt5D7G57ykdm+lyshxmMbIAVsJ5essoRnmW1QduJixCCnczK9lSshW5twqazmtlJKb2MR25Syzz5wzgwazOaqrseqr7NFaMgpTSTrGeX6f80SR3CW9T1dFkds+/1zUvcQKBgAH6mnlEF7DFmp4+1Owc4w7p5wHQusbbuhDgh4JNvf1w/UPGqVWaS5fR3XPcfHWMGIExw/rzpIQM0wgRdcXNHuS19xdnoPbYNOD6Mp48hyoP3oppkCpreKl8XQQ9INUJo31HFHfiZBTInvIT2LySFHHzpyHO6Dhghk9TciXLF2YRAoGBAIFt3g6jArx95NHb1QwbH6TsUgdGeWhlIf8F3pP97IOQXBWZ6R5xRGS/JN/ecH7hOtUu2xwRCT5bzbxHP/87uSBD3LlPpjg8U2I3GZ00w/uoceZ+ycmkABRz5h4w1zP1eA6oscXnUtqZRkO6nGQlU4ZZFnplBPkxAK9UIyceV7yhAoGAA1Cbj00l5nGgaAW79/VPTGrCb2JmaQiqbzG8Y5YdM2YPtbAMOCaRJ+gGRBFWJYnBRdkJJRYZ9CSfcaq+EI4ch58PUemohp0bi1bAwGFF8UZZcvdHe7yKYSo9Qz3WsmgFaBHEthVwklOUBxOeVlX0GIO5zqnsbqrkHRQtYuJbEkQ=");
        editor.putString("publickeystring", "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqiXkjvu2pVCXG/phF+v0x8fLyKTe4LWvoU9le2PX7P5he8Sqfe9rIE6tUn/qZkVuIYN3ZIWGk/0C9xxhI7SXAzgf5BzOl0365Z7SaLbm6CToJbwOVU5tt4zhoLzmScBoxAhBEqGykKhz7CZLdRJuHEIRa9cWcBrur/dZlacGAGaMt6aOEuC12YbH5dbxRHN8rKDb8Yt6C0KunE3edMKVW3556PL2DtTF1ASv2i31qLUJ6w0T1vv4tlyyOqodyyNWHJ4MWAHIUCmsmh5ZSGSqpzgJBm0iO75r+xA+jm5K9DhgIoSOr2bMSoQ+CI+mk+05ULI8jVFtGiycXxRz0lW4aQIDAQAB");
        editor.apply();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        account = GoogleSignIn.getLastSignedInAccount(this);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        user = auth.getCurrentUser();
        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new FragmentsAdapter(getSupportFragmentManager(), 1));
        pd = new ProgressDialog(this);
        pd.setTitle("Deleting account");
        pd.setMessage("wait a moment..");
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.groupchat:
                startActivity(new Intent(MainActivity.this, GroupChatActivity.class));

                break;
            case R.id.settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));

                break;
            case R.id.logout:
                mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        auth.signOut();
                        startActivity(new Intent(MainActivity.this, SignInActivity.class));
                        finish();

                    }
                });
                break;
            case R.id.delete:

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Account")
                        .setMessage("you wll not able to access this account anymore. Are you sure you want to delete this account ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                pd.show();
                                database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                            String recieverid = dataSnapshot.getKey();
                                            String senderid = auth.getUid();
                                            String senderroom = senderid + recieverid;
                                            String recieverroom = recieverid + senderid;
                                            database.getReference().child("chats").child(senderroom).setValue(null);
                                            database.getReference().child("chats").child(recieverroom).setValue(null);

                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                FirebaseStorage.getInstance().getReference().child("profile_picture").child(auth.getUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (preferences.getInt("method", 1) == 1) {
                                            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                                            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    mGoogleSignInClient.revokeAccess().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            database.getReference().child("Users").child(auth.getUid()).setValue(null);
                                                            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    pd.dismiss();
                                                                    Toast.makeText(MainActivity.this, "Your account has been deleted", Toast.LENGTH_SHORT).show();
                                                                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                                                                    finish();

                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        } else if (preferences.getInt("method", 1) == 2) {
                                            database.getReference().child("Users").child(auth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                                @Override
                                                public void onSuccess(DataSnapshot dataSnapshot) {
                                                    String email = dataSnapshot.getValue(Users.class).getMail();
                                                    String password = dataSnapshot.getValue(Users.class).getMail();
                                                    AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                                                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            database.getReference().child("Users").child(auth.getUid()).setValue(null);
                                                            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    pd.dismiss();
                                                                    Toast.makeText(MainActivity.this, "Your account has been deleted", Toast.LENGTH_SHORT).show();
                                                                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                                                                    finish();

                                                                }
                                                            });

                                                        }
                                                    });
                                                }
                                            });

                                        } else {
                                            database.getReference().child("Users").child(auth.getUid()).setValue(null);
                                            pd.dismiss();
                                            auth.signOut();
                                            Toast.makeText(MainActivity.this, "Your account has been deleted", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(MainActivity.this, SignInActivity.class));
                                            finish();


                                        }


                                    }
                                });
                                dialogInterface.dismiss();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                    }
                }).show();


                break;
            case R.id.invite:
                FirebaseStorage.getInstance().getReference().child("ashapps/chatit.apk").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Chat It");
                        intent.putExtra(Intent.EXTRA_TEXT, "Hey, " +
                                "Hope you doing well. " + user.getDisplayName() + " is inviting you to use this awesome app 'Chat It' for messaging, calling and sharing media. Download it at free from this link  " + uri.toString());
                        startActivity(Intent.createChooser(intent, "invite to"));
                    }
                });
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
