package com.example.firebaseapp.models;

public class ModelChat {
    String sender, receiver, msg, timestamp;
    boolean isSeen;

    public ModelChat(){

    }

    public ModelChat(String sender, String receiver, String msg, String timestamp, boolean isSeen) {
        this.sender = sender;
        this.receiver = receiver;
        this.msg = msg;
        this.timestamp = timestamp;
        this.isSeen = isSeen;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}
