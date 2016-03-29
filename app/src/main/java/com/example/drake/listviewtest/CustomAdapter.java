package com.example.drake.listviewtest;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;


//TEST COMMENT//
//BLUETOOTH BRANCH//

/**
 * Created by Drake on 2016-03-01.
 */
class CustomAdapter extends ArrayAdapter<MessageData> {

    ArrayList<MessageData> messageDataArrayList;

    private int _id;
    //IDs of all images which will accessed using the number (minus 1) of the letter corresponding
    //to each image. For example, the picture for A would be at index 0, Z at index 25.
    //NOTE: Non-letter chars will all be assigned ID 26 which will be generic.
    int[] myImageArray = new int[]{
            (int) R.drawable.me,
            (int) R.drawable.you,
            (int) R.drawable.img,
            (int) R.drawable.you,
            (int) R.drawable.me,
            (int) R.drawable.you,
            (int) R.drawable.me,
            (int) R.drawable.you,
            (int) R.drawable.me,
            (int) R.drawable.you,
            (int) R.drawable.me,
            (int) R.drawable.you,
            (int) R.drawable.me,
            (int) R.drawable.you,
            (int) R.drawable.me,
            (int) R.drawable.you,
            (int) R.drawable.me,
            (int) R.drawable.you,
            (int) R.drawable.me,
            (int) R.drawable.you,
            (int) R.drawable.me,
            (int) R.drawable.you,
            (int) R.drawable.me,
            (int) R.drawable.you,
            (int) R.drawable.me,
            (int) R.drawable.you,
    };

    public CustomAdapter(Context context, ArrayList<MessageData> resource) {

        //resource is the array being dealt with
        super(context,R.layout.custom_row, resource);
        //R.layout.custom_row is the custom defined ListView Row

        messageDataArrayList = resource;
        Log.d("DED", " CustomAdapter constructor finished");

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        //Sets this variable to the first character that appears in the sender ID
        //This will be used to show a picture corresponding to each different letter
        //NOTE: When implementing the rest of this, surround the set image in a try/catch
        //If the char does not have a picture with that name, it will fail and show a generic image instead


        //Returns the first char of the senderID for the message.
        char senderChar = messageDataArrayList.get(position).getSenderID().toLowerCase().charAt(0);

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
            senderChar = 26;
        }


        //Inflate just means get ready to render.
        //Context is just background information
        LayoutInflater myInflater = LayoutInflater.from(getContext());
        View customView = myInflater.inflate(R.layout.custom_row, parent, false);

        //Sets string to the current array string
        String singleMessageItem = messageDataArrayList.get(position).getMessage();

        //Creates text view
        TextView myTextView = (TextView) customView.findViewById(R.id.listRowText);
        //Creates image view
        ImageView myImageView = (ImageView) customView.findViewById((R.id.listRowImage));

        //Set text box to text item
        myTextView.setText(singleMessageItem);

        //Set image to the img in res
        myImageView.setImageResource(myImageArray[position]);

        Log.d("DED", "List view created: " + position);

        return customView;
    }
}
