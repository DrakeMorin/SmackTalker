package com.example.drake.listviewtest;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class MainActivity extends AppCompatActivity {

    protected final static String DEBUGTAG = "DED";
    public final String FILENAME = "SmackTalkerMessages.ded";
    protected String userID;

    myDBHandler dbHandler;

    EditText newMessageText;
    ListAdapter myListAdapter;
    ListView listView;

    GregorianCalendar calendar = new GregorianCalendar();

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
        //Populate listView with previous messages
        //populateListView();
    }

    //Message is ready to be sent.
    public void sendButtonClicked(View view){
        if (!newMessageText.getText().toString().equals("")) {
            //Only run if newMessageText is not empty
            //calendar.getInstance();
            //String timeStamp = calendar.toString();
            String timeStamp = null;
            //Aside: Format "%Y-%m-%d %H:%M:%S"

            //Add to database a new MessageData object with fields.
            dbHandler.addMessage(new MessageData(newMessageText.getText().toString(), timeStamp, userID));

            //Clear text field
            newMessageText.setText("");
            Log.d(DEBUGTAG, "EditText cleared");

            //Refresh listView
            populateListView();
        }else {
            Log.d(DEBUGTAG, "Message Field Empty");
        }
    }

    private void populateListView(){
        Cursor myCursor = dbHandler.getAllRows();
        //What data you are going to populate the data with
        String [] fromFieldNames = new String[] {myDBHandler.COLUMN_ID/*, myDBHandler.COLUMN_MESSAGETEXT, myDBHandler.COLUMN_SENDERID, myDBHandler.COLUMN_TIME*/};
        //Where the data is going to go.
        int[] toViewIDs = new int[] {R.id.listRowText};

        //Define cursorAdapter, instantiated next line.
        SimpleCursorAdapter myCursorAdapter;
        //Get the context, the defined layout being used, the cursor, the columns being read, the location of info being stored, 0
        myCursorAdapter = new SimpleCursorAdapter(getBaseContext(), R.layout.custom_row, myCursor, fromFieldNames, toViewIDs, 0);

        //Set listView
        ListView myListView = (ListView) findViewById(R.id.listView);

        //Sets listView adapter to the cursorAdapter
        myListView.setAdapter(myCursorAdapter);

    }

    //For testing purposes.
    public void addButtonClicked(View view){
        Log.d(DEBUGTAG, "Temp");
    }
}
