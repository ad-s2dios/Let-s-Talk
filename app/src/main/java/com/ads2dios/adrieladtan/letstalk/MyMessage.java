package com.ads2dios.adrieladtan.letstalk;

public class MyMessage {
    private String id;
    private String senderID;
    private String messageString;

    public MyMessage(){
        // for calls to DataSnapshot.getValue(MyMessage.class)
    }

    public MyMessage(String mID, String mSenderID, String mMessageString) {
        this.id = mID;
        this.senderID = mSenderID;
        this.messageString = mMessageString;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getMessageString() {
        return messageString;
    }

    public void setMessageString(String messageString) {
        this.messageString = messageString;
    }
}
