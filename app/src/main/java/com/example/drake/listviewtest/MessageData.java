package com.example.drake.listviewtest;

/**
 * Created by Drake on 2016-03-08.
 */
class MessageData {

    //Objects of this class will be used to hold the text messages sent and received

    //Holds the sent message
    String message;

    //Holds the time message was sent
    String time;

    //Holds name of sender
    String senderID;

    public MessageData(String message, String time, String senderID) {
        //Constructor that sets data
        this.message = message;
        this.time = time;
        this.senderID = senderID;
    }

    //GETTER METHODS

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public String getSenderID() {
        return senderID;
    }
}
