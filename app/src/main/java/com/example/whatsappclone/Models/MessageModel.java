package com.example.whatsappclone.Models;

public class MessageModel {
    String uid;
    String message, messegeid, hashmessage;
    String timestamp;


    public MessageModel(String uid, String message, String timestamp) {
        this.uid = uid;
        this.message = message;
        this.timestamp = timestamp;
    }

    public MessageModel(String uid, String message) {
        this.uid = uid;
        this.message = message;
    }


    public MessageModel() {
    }

    public String getHashmessage() {
        return hashmessage;
    }

    public void setHashmessage(String hashmessage) {
        this.hashmessage = hashmessage;
    }

    public String getMessegeid() {
        return messegeid;
    }

    public void setMessegeid(String messegeid) {
        this.messegeid = messegeid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
