package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsappclone.Adapters.ChatAdapter;
import com.example.whatsappclone.Models.MessageModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

public class GroupChatActivity extends AppCompatActivity {

    FirebaseDatabase database;
    FirebaseAuth auth;
    TextView textView;
    ImageView imageView, send, menu;
    ImageView back;
    EditText editchat;
    RecyclerView chatrecyclerview;
    Key privatekey;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        getSupportActionBar().hide();
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        textView = findViewById(R.id.gcusernamechat);
        imageView = findViewById(R.id.gcprofilechat);
        back = findViewById(R.id.gcback);
        chatrecyclerview = findViewById(R.id.gcrecyclerviewchat);
        send = findViewById(R.id.gcsendchat);
        editchat = findViewById(R.id.gceditchat);
        menu = findViewById(R.id.gcmenu);

        privatekey = getprivatekey();

        final String senderid = auth.getUid();
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GroupChatActivity.this, MainActivity.class));
                finish();
            }
        });
        final ArrayList<MessageModel> messageModels = new ArrayList<>();
        final ChatAdapter chatAdapter = new ChatAdapter(messageModels, this);
        chatrecyclerview.setAdapter(chatAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        chatrecyclerview.setLayoutManager(linearLayoutManager);
        textView.setText("Friends");

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(GroupChatActivity.this)
                        .setTitle("Clear Chat")
                        .setMessage("Are you sure you want to delete all messages in this chat")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                database.getReference().child("group chats").setValue(null);

                                dialogInterface.dismiss();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                    }
                }).show();


            }
        });

        database.getReference().child("group chats")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageModels.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MessageModel model = dataSnapshot.getValue(MessageModel.class);
                            model.setMessegeid(dataSnapshot.getKey());
                            messageModels.add(model);
                        }
                        chatAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editchat.getText().toString().isEmpty()) {
                    Toast.makeText(GroupChatActivity.this, "blank message can not be sent", Toast.LENGTH_SHORT).show();

                    return;
                }
                String message = editchat.getText().toString();
                String message1 = encrypt(message, privatekey);
                final MessageModel model = new MessageModel(senderid, message1);
                model.setTimestamp(gettimestamp());
                model.setHashmessage(gethashedmsg(message));
                editchat.setText("");

                database.getReference().child("group chats").push().setValue(model)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        });

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    Key getprivatekey() {
        SharedPreferences preferences = getSharedPreferences("com.example.whatsappclone.name", Context.MODE_PRIVATE);
        String privatekey = preferences.getString("privatekeystring", null);


        byte[] pbincpk = Base64.getDecoder().decode(privatekey);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec privatekeyspec = new PKCS8EncodedKeySpec(pbincpk);
            return keyFactory.generatePrivate(privatekeyspec);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String encrypt(String data, Key privateKey) {


        // Encode the original data with the RSA private key
        byte encoded[] = null;
        try {
            Cipher c = null;
            c = Cipher.getInstance("RSA");
            try {
                c.init(Cipher.ENCRYPT_MODE, privateKey);
            } catch (InvalidKeyException invalidKeyException) {
                invalidKeyException.printStackTrace();
            }
            try {
                encoded = c.doFinal(data.getBytes());
            } catch (BadPaddingException badPaddingException) {
                badPaddingException.printStackTrace();
            } catch (IllegalBlockSizeException illegalBlockSizeException) {
                illegalBlockSizeException.printStackTrace();
            }
        } catch (Exception e) {

        }
        return Base64.getEncoder().encodeToString(encoded);
    }

    public String gettimestamp() {

        Calendar cal = Calendar.getInstance();
        int h = cal.get(Calendar.HOUR);
        int m = cal.get(Calendar.MINUTE);
        int t = cal.get(Calendar.AM_PM);
        String AM_PM;
        if (t == 0)
            AM_PM = "am";
        else
            AM_PM = "pm";
        String min;
        if (m < 10)
            min = "0" + m;
        else
            min = m + "";


        return h + ":" + min + "" + AM_PM;
    }

    public String gethashedmsg(String data) {

        MessageDigest messageDigest = null;
        byte[] digest = new byte[0];

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(data.getBytes());
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        BigInteger bigInt = new BigInteger(1, digest);
        String hashed = bigInt.toString(16);

        while (hashed.length() < 32) {
            hashed = "0" + hashed;
        }

        return hashed;

    }
}