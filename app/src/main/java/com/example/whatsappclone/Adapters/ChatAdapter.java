package com.example.whatsappclone.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsappclone.Models.MessageModel;
import com.example.whatsappclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

public class ChatAdapter extends RecyclerView.Adapter {

    ArrayList<MessageModel> messageModels;
    Context context;
    Key publickey;
    String recieverid;
    public static final int SENDER_VIEW_TYPE = 1;
    public static final int RECIEVER_VIEW_TYPE = 2;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ChatAdapter(ArrayList<MessageModel> messageModels, Context context, String recieverid) {
        this.messageModels = messageModels;
        this.context = context;
        this.recieverid = recieverid;
        publickey = getpublickey();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ChatAdapter(ArrayList<MessageModel> messageModels, Context context) {
        this.messageModels = messageModels;
        this.context = context;

        publickey = getpublickey();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_reciever, parent, false);
            return new RecieverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (messageModels.get(position).getUid().equals(FirebaseAuth.getInstance().getUid())) {
            return SENDER_VIEW_TYPE;
        } else {
            return RECIEVER_VIEW_TYPE;
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MessageModel messageModel = messageModels.get(position);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Delete this message ?")
                        .setPositiveButton("DELETE FOR ME", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                String senderroom = FirebaseAuth.getInstance().getUid() + recieverid;

                                database.getReference().child("chats").child(senderroom).child(messageModel.getMessegeid()).setValue(null);
                                database.getReference().child("group chats").child(messageModel.getMessegeid()).setValue(null);

                                dialogInterface.dismiss();
                            }
                        }).setNegativeButton("DELETE FOR EVERYONE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        String senderroom = FirebaseAuth.getInstance().getUid() + recieverid;
                        String recieverroom = recieverid + FirebaseAuth.getInstance().getUid();
                        database.getReference().child("chats").child(senderroom).child(messageModel.getMessegeid()).setValue(null);

                        database.getReference().child("chats").child(recieverroom).child(messageModel.getMessegeid()).setValue(null);
                        database.getReference().child("group chats").child(messageModel.getMessegeid()).setValue(null);


                        dialogInterface.dismiss();

                    }
                }).show();

                return false;
            }
        });
        if (holder.getClass() == SenderViewHolder.class) {
            String decodedmsg = decrypt(messageModel.getMessage(), publickey);

            if (gethashedmsg(decodedmsg).equals(messageModel.getHashmessage())) {
                ((SenderViewHolder) holder).sendermsg.setText(decodedmsg);
                ((SenderViewHolder) holder).sendertime.setText(messageModel.getTimestamp());
            }

        } else {
            String decodedmsg = decrypt(messageModel.getMessage(), publickey);
            if (gethashedmsg(decodedmsg).equals(messageModel.getHashmessage())) {
                ((RecieverViewHolder) holder).recievermsg.setText(decodedmsg);
                ((RecieverViewHolder) holder).recievertime.setText(messageModel.getTimestamp());
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageModels.size();
    }

    public class RecieverViewHolder extends RecyclerView.ViewHolder {

        TextView recievermsg, recievertime;

        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            recievermsg = itemView.findViewById(R.id.recievertext);
            recievertime = itemView.findViewById(R.id.recievertime);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {

        TextView sendermsg, sendertime;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            sendermsg = itemView.findViewById(R.id.sendertext);
            sendertime = itemView.findViewById(R.id.sendertime);
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
