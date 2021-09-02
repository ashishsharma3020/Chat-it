package com.example.whatsappclone.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsappclone.ChatDetailActivity;
import com.example.whatsappclone.Models.Users;
import com.example.whatsappclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    ArrayList<Users> list;
    Context context;
    Key publickey;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public UsersAdapter(ArrayList<Users> list, Context context) {
        this.list = list;
        this.context = context;
        publickey = getpublickey();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_show_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users users = list.get(position);
        Picasso.get().load(users.getProfilepic()).placeholder(R.drawable.ic_avatar).into(holder.image);
        holder.username.setText(users.getUsername());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatDetailActivity.class);
                intent.putExtra("userid", users.getUserid());
                intent.putExtra("profilepic", users.getProfilepic());
                intent.putExtra("username", users.getUsername());
                context.startActivity(intent);
            }
        });
        FirebaseDatabase.getInstance().getReference().child("chats")
                .child(FirebaseAuth.getInstance().getUid() + users.getUserid())
                .orderByChild("timestamp").limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChildren()) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                                String decodemsg = decrypt(snapshot1.child("message").getValue().toString(), publickey);
                                holder.lastmessage.setText(decodemsg);
                                holder.timeview.setText(snapshot1.child("timestamp").getValue().toString());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    @Override
    public int getItemCount() {
        return list.size();

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView username, lastmessage, timeview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.usernamec);
            lastmessage = itemView.findViewById(R.id.lastmessage);
            timeview = itemView.findViewById(R.id.timeview);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    Key getpublickey() {
        SharedPreferences preferences = context.getSharedPreferences("com.example.whatsappclone.name", 0);
        String publickey = preferences.getString("publickeystring", null);
        byte[] bincpk = Base64.getDecoder().decode(publickey);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec publickeyspec = new X509EncodedKeySpec(bincpk);
            return keyFactory.generatePublic(publickeyspec);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String decrypt(String encryptedstring, Key publicKey) {

        byte[] encoded = Base64.getDecoder().decode(encryptedstring);
        try {
            byte[] decodedBytes = null;
            try {
                Cipher c = null;
                c = Cipher.getInstance("RSA");
                try {
                    c.init(Cipher.DECRYPT_MODE, publicKey);
                } catch (InvalidKeyException invalidKeyException) {
                    invalidKeyException.printStackTrace();
                }
                try {
                    decodedBytes = c.doFinal(encoded);
                } catch (BadPaddingException badPaddingException) {
                    badPaddingException.printStackTrace();
                } catch (IllegalBlockSizeException illegalBlockSizeException) {
                    illegalBlockSizeException.printStackTrace();
                }
            } catch (Exception e) {

            }
            return new String(decodedBytes);

        } catch (Exception e) {
            return "";
        }
    }
}
