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

    myDBHandler dbHandler;

    EditText newMessageText;
    ListAdapter myListAdapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Since the last three parameters are constants of the class, null is passed.
        dbHandler = new myDBHandler(this, null, null, 1);

        /*//Temporary
        messages.add(new MessageData("Hi this is a message", 32, "Sender Person"));
        messages.add(new MessageData("This is another message", 7, "Receiver Person"));
        messages.add(new MessageData("SO MANY MESSAGES!!!1!!", 1008, "Sender Person"));
        messages.add(new MessageData("Does a fourth message work?", 2, "Receiver Person"));*/

        //Create text view for user written messages
        newMessageText = (EditText) findViewById(R.id.newMessageText);

        /* //Sets up custom List adapter defined in Custom Adapter
        myListAdapter = new CustomAdapter(this, messages);
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
        );*/
    }

    //Message is ready to be sent.
    public void sendButtonClicked(View view){
        if (!newMessageText.getText().toString().equals("")) {
            //Only run if newMessageText is not empty
            //Add to database a new MessageData object with fields.
            dbHandler.addMessage(new MessageData(newMessageText.getText().toString(), 0, userID));

            //Clear text field
            newMessageText.setText("");
            Log.d(DEBUGTAG, "EditText cleared");
        }
    }

    //For testing purposes.
    public void addButtonClicked(View view){
        Log.d(DEBUGTAG, "Temp");
    }
}
