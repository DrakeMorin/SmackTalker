package com.ded.smacktalker;


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * This class is the template for storing messages and their corresponding information
 */
class MessageData implements Serializable{

    /**
     * The message text
     */
    String message;

    /**
     * The time stamp for a message
     */
    String time;

    /**
     * The userID for the sender of a message
     */
    String senderID;

    /**
     * The array index value for myImageArray to determine what image is to be shown
     */
    int imgID;

    /**
     * Contains the int value to all sender icons (A to Z & asterisks)
     */
    int[] myImageArray = new int[]{
            R.mipmap.a_img,
            R.mipmap.b_img,
            R.mipmap.c_img,
            R.mipmap.d_img,
            R.mipmap.e_img,
            R.mipmap.f_img,
            R.mipmap.g_img,
            R.mipmap.h_img,
            R.mipmap.i_img,
            R.mipmap.j_img,
            R.mipmap.k_img,
            R.mipmap.l_img,
            R.mipmap.m_img,
            R.mipmap.n_img,
            R.mipmap.o_img,
            R.mipmap.p_img,
            R.mipmap.q_img,
            R.mipmap.r_img,
            R.mipmap.s_img,
            R.mipmap.t_img,
            R.mipmap.u_img,
            R.mipmap.v_img,
            R.mipmap.w_img,
            R.mipmap.x_img,
            R.mipmap.y_img,
            R.mipmap.z_img,
            R.mipmap.special_img,
    };

    /**
     * Constructor for MessageData. Sets message, time, senderID, and imgID
     * @param message The message to be stored
     * @param senderID The senderID to be stored
     */
    public MessageData(String message, String senderID) {
        //Constructor that sets data
        this.message = message;
        this.time = setTimeStamp();
        this.senderID = senderID;
        this.imgID = parseSenderID();
    }

    /**
     * @return The message stored
     */
    @Override
    public String toString() {
        return message;
    }

    /**
     * Determines what index value represents the first char of senderID.
     * If the first char is not a letter, an asterisk is assigned.
     * @return The array index value
     */
    private int parseSenderID(){
        //This method determines what the first char of senderID is, and assigns an int
        //to it based on what letter it is (0 - 26). A is 0, Z is 25.
        //Special letters are all assigned 26

        //Returns the first char of the senderID for the message.
        char senderChar = senderID.toLowerCase().charAt(0);
        //This code determines what number the char is as part of the alphabet.
        int charID;
        //This should avoid crashes when the first char in the senderID is not a letter.
        if(Character.isLetter(senderChar)) {
            int temp = (int) senderChar;
            //Note the 96 is a constant for lower case.
            if (temp <= 122 & temp >= 97) {
                charID = (temp - 96 - 1);
            } else {
                charID = temp - 1;
            }
            //The -1 for the charID is to allow for array indexes which start at 0, not 1.
        }else{
            charID = 26;
        }

        //Return the image id value.
        return myImageArray[charID];
    }

    /**
     * Determines the time and formats it to the desired string
     * @return The timestamp for the message
     */
    private String setTimeStamp(){
        //Initialize a calendar to current date
        Calendar c = Calendar.getInstance();
        //Create format for the date
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA);
        //Return the formatted date as a String
        return df.format(c.getTime());
    }

    //GETTER METHODS

    public String getMessage() { return message; }

    public String getTime() {
        return time;
    }

    public String getSenderID() {
        return senderID;
    }

    public int getImgID() { return imgID; }
}
