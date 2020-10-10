package com.example.pickycopy;

public class BaseMessage {
    String senderName,recieverName,sentTime,recievedTime,senderText,recieverText,downloadUrl;
    String senderId,recieverId;

    public BaseMessage(String senderName, String recieverName, String sentTime, String recievedTime, String senderText, String recieverText, String senderId,String recieverId,String downloadUrl) {
        this.senderName = senderName;
        this.recieverName = recieverName;
        this.sentTime = sentTime;
        this.recievedTime = recievedTime;
        this.senderText = senderText;
        this.recieverText = recieverText;
        this.senderId=senderId;
        this.recieverId=recieverId;
        this.downloadUrl=downloadUrl;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getRecieverName() {
        return recieverName;
    }

    public String getSentTime() {
        return sentTime;
    }

    public String getRecievedTime() {
        return recievedTime;
    }

    public String getSenderText() {
        return senderText;
    }

    public String getRecieverText() {
        return recieverText;
    }
    public String getSenderId(){return senderId;}

    public String getRecieverId() {return recieverId;}

    public String getUrl() {return downloadUrl;}
}
