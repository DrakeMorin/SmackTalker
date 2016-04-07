package com.example.drake.listviewtest;


class MessageData {
    //Objects of this class will be used to hold the text messages sent and received

    //Holds the sent message
    String message;

    //Holds the time message was sent
    String time;

    //Holds name of sender
    String senderID;

    //Holds the ID of the img to be shown based on the first char in senderID
    int imgID;

    int[] myImageArray = new int[]{
            R.drawable.a_img,
            R.drawable.b_img,
            R.drawable.c_img,
            R.drawable.d_img,
            R.drawable.e_img,
            R.drawable.f_img,
            R.drawable.g_img,
            R.drawable.h_img,
            R.drawable.i_img,
            R.drawable.j_img,
            R.drawable.k_img,
            R.drawable.l_img,
            R.drawable.m_img,
            R.drawable.n_img,
            R.drawable.o_img,
            R.drawable.p_img,
            R.drawable.q_img,
            R.drawable.r_img,
            R.drawable.s_img,
            R.drawable.t_img,
            R.drawable.u_img,
            R.drawable.v_img,
            R.drawable.w_img,
            R.drawable.x_img,
            R.drawable.y_img,
            R.drawable.z_img,
            R.drawable.special_img,
    };

    public MessageData(String message, String time, String senderID) {
        //Constructor that sets data
        this.message = message;
        this.time = time;
        this.senderID = senderID;
        this.imgID = parseSenderID();
    }

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
