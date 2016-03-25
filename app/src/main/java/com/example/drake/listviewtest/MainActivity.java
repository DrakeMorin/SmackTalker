package com.example.drake.listviewtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    protected final String DEBUGTAG = "DED";
    public final String FILENAME = "SmackTalkerMessages.ded";
    protected String userID;

    protected ArrayList<MessageData> messages;

    protected ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Load any previous messages
        messages = loadFile();

        //Temporary
        messages.add(new MessageData("Hi this is a message", 32, "Sender Person"));
        messages.add(new MessageData("This is another message", 7, "Receiver Person"));
        messages.add(new MessageData("SO MANY MESSAGES!!!1!!", 1008, "Sender Person"));
        messages.add(new MessageData("Does a fourth message work?", 2, "Receiver Person"));

        //Create text view for user written messages
        final EditText newMessageText = (EditText) findViewById(R.id.newMessageText);
        //Create button for sendButton
        Button sendButton = (Button) findViewById(R.id.sendButton);

         //Sets up custom List adapter defined in Custom Adapter
        final ListAdapter myListAdapter = new CustomAdapter(this, messages);
        Log.d(DEBUGTAG, "Custom Adapter created");

        //Creates listView object in Java
        listView = (ListView) findViewById(R.id.listView);
        Log.d(DEBUGTAG, "ListView created");

        //Sets the adapter for data to the above adapter
        listView.setAdapter(myListAdapter);
        Log.d(DEBUGTAG, "Adapter set");

        //Add item onClickListener
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        //Get the string value of the view that was touched at position # (which is stored in position
                        //This WILL enable copy and pasting later.
                        String message = String.valueOf(parent.getItemAtPosition(position));
                        Log.d("DED", message);
                    }
                }
        );

        //Create clickListener for send button
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!newMessageText.getText().toString().equals("")) {
                    //If statement only runs if the text field has text in it.
                    //Add message data: message, time, sender; to ArrayList
                    messages.add(new MessageData(newMessageText.getText().toString(), 0, userID));

                    Log.d(DEBUGTAG, "Button: ArrayList Size: " + messages.size());
                    //Clear text field
                    newMessageText.setText("");

                    Log.d(DEBUGTAG, "EditText cleared");


                    listView.invalidateViews();
                    //listView.deferNotifyDataSetChanged();
                    Log.d(DEBUGTAG, "ListView refreshed");

                    //Call method to send message through Bluetooth
                } else {
                    Log.d(DEBUGTAG, "No message to send.");
                }
            }
        });
    }

    protected ArrayList<MessageData> loadFile(){
        //Read file of previous messages sent.
        ArrayList<MessageData> messages;
        try {
            FileInputStream fis = new FileInputStream(FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);

            //Sets messages ArrayList equal to object read from file.
            messages = (ArrayList<MessageData>) ois.readObject();

            ois.close();
            fis.close();

            //Return updated ArrayList
            return messages;
        }catch (Exception e){
            Log.d(DEBUGTAG, "Unable to read file");
        }
        return messages = new ArrayList<MessageData>();
    }

    protected void saveFile(ArrayList<MessageData> messages){
       //Serialize array containing message data
        try{
            //ObjectOutputStream oos =  openFileOutput("SmackTalkerMessages", Context.MODE_PRIVATE);
            FileOutputStream fos = new FileOutputStream(FILENAME);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            //Serializes the object to save file
            oos.writeObject(messages);

            oos.close();
            fos.close();

        }catch (Exception e){
            Log.d(DEBUGTAG, "Unable to serialize object");
        }
    }
}
