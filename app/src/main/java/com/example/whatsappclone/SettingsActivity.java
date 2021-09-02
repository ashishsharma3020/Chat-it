package com.example.whatsappclone;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsappclone.Models.Users;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ImageView back, plus, pro, edituser, editstatus;
    TextView progres;
    AppCompatButton save;
    EditText stuser, stbio;
    Uri file;


    public static int HAS_IMAGE_CHANGED;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().hide();
        HAS_IMAGE_CHANGED = 0;
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        back = findViewById(R.id.stback);
        plus = findViewById(R.id.stplus);
        pro = findViewById(R.id.stprofile);
        progres = findViewById(R.id.sttext);
        save = findViewById(R.id.stsave);
        stuser = findViewById(R.id.stetusername);
        stbio = findViewById(R.id.stetstatus);
        edituser = findViewById(R.id.editusername);
        editstatus = findViewById(R.id.editstatus);


        stuser.setFocusable(false);
        stbio.setFocusable(false);

        edituser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Toast.makeText(SettingsActivity.this, "bt 1 clicked", Toast.LENGTH_SHORT).show();

                stuser.setFocusableInTouchMode(true);
                //stuser.setClickable(true);
            }
        });
        editstatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(SettingsActivity.this, "bt 2 clicked", Toast.LENGTH_SHORT).show();

                stbio.setFocusableInTouchMode(true);
                // stbio.setClickable(true);
            }
        });

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    file = data.getData();
                    pro.setImageURI(file);
                    HAS_IMAGE_CHANGED = 1;

                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                finish();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stuser.setFocusable(false);
                stbio.setFocusable(false);

                if (HAS_IMAGE_CHANGED == 1) {
                    final StorageReference reference = storage.getReference().child("profile_picture").child(auth.getUid());
                    reference.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            progres.setText("");
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    database.getReference().child("Users").child(auth.getUid()).child("profilepic").setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    });

                                }
                            });
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                            progres.setText("uploading.." + progress + "%");

                        }
                    });

                }
                Toast.makeText(SettingsActivity.this, "your profile has been updated", Toast.LENGTH_SHORT).show();
                String stusername = stuser.getText().toString();
                String stbiot = stbio.getText().toString();
                HashMap<String, Object> obj = new HashMap<>();
                obj.put("username", stusername);
                obj.put("bio", stbiot);
                database.getReference().child("Users").child(auth.getUid()).updateChildren(obj);

            }
        });
        database.getReference().child("Users").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);
                Picasso.get().load(users.getProfilepic()).placeholder(R.drawable.ic_avatar).into(pro);
                stuser.setText(users.getUsername());
                stbio.setText(users.getbio());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    activityResultLauncher.launch(intent);
                }


            }
        });

    }


}