package com.ded.smacktalker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    protected final static String DEBUGTAG = "DED";
    private BluetoothAdapter btAdapter;
    private Button Bluetooth;
    protected static String userID;
    private static final String USERIDKEY = "userID";
    //This is a constant ID to track the device regardless of the set userID
    private static String deviceID;
    private static final String DEVICEKEY = "deviceID";

    //This String will store the deviceID of the other person in the conversation
    private static String oDeviceID;

    EditText newMessageText;
    ListView listView;
    myDBHandler dbHandler;

    //This will store the name of the table for the current conversation.
    String currentTable = myDBHandler.TABLE_MESSAGES;

    //Used for randomly generated userIDs
    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd;

    //Preferences which carry across run time sessions
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    //Will contain all messages currently unread
    StringBuilder unread;
    //Will store whether the app is in the fore or background
    private boolean inBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Since the last three parameters are constants of the class, null is passed.
        dbHandler = new myDBHandler(this, null, null, 1);

        //Variable instantiation
        newMessageText = (EditText) findViewById(R.id.newMessageText);
        prefs = getPreferences(MODE_PRIVATE);
        editor = prefs.edit();
        unread = new StringBuilder();
        rnd = new SecureRandom();

        //Get deviceID from preferences
        deviceID = prefs.getString(DEVICEKEY, null);

        if(deviceID == null){
            //One in 57 billion chance of two users having the same deviceID with this method
            //Create randomized deviceID
            int len = 6;
            StringBuilder sb = new StringBuilder( len );
            //Random # created which points to a string of all possible chars.
            //Appends that char onto the userID and repeats until desired length.
            for( int i = 0; i < len; i++ ) {
                sb.append(AB.charAt(rnd.nextInt(AB.length())));
            }
            deviceID = sb.toString();

            //Stores the userID under the key specified in the final USERIDKEY
            editor.putString(DEVICEKEY, deviceID);
            editor.apply();
        }

        //Null is the default value. If no userID is saved, the default value assigned will ne null.
        userID = prefs.getString(USERIDKEY, null);

        if(userID == null){
            //UserID has not been set
            setUserID();
        }

        listView = (ListView) findViewById(R.id.listView);
        //Add item onClickListener
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //Get the string value of the view that was touched at position # (which is stored in position
                        //This WILL enable copy to clipboard

                        //Get cursor from listView
                        SQLiteCursor c  = (SQLiteCursor) parent.getItemAtPosition(position);
                        //Move cursor position to the corresponding item touched
                        c.moveToPosition(position);
                        //Get the Message from the MESSAGETEXT column in the database
                        String message = c.getString(c.getColumnIndex(myDBHandler.COLUMN_MESSAGETEXT));

                        //Save message to clipboard
                        //Get handle for clipboard service
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        //Create text clip
                        ClipData clip = ClipData.newPlainText(DEBUGTAG, message);
                        //Add text clip to clipboard
                        clipboard.setPrimaryClip(clip);

                        Toast.makeText(MainActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        //Populate listView with previous messages
        populateListView();
    }

    @Override
    protected void onResume() {
        //When the app is resumed from background, assume all messages are read.
        //Clear the unread messages stored in the stringbuilder.
        unread.delete(0, unread.length());

        //App is now in the foreground, not the back.
        inBack = false;

        //Clear any unread message notifications
        deleteNotifications();
        super.onResume();
    }

    @Override
    protected void onPause() {
        //Set bool saying app is running in background
        inBack = true;
        super.onPause();
    }

    //INFLATES ACTION BAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Log.d(DEBUGTAG, "Options Menu Inflated");
        return true;
    }

    //CHECKS FOR IF ANY OF THE ITEMS IN THE ACTION BAR ARE PRESSED
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_bluetooth) {
            Log.d(DEBUGTAG, "Bluetooth button pressed");
            btButtonClick();
        }
        return true;
    }

    //IF BLUETOOTH BUTTON IS CLICKED, TURN ON/OFF BLUETOOTH AND ALERT THE USER
    public void btButtonClick() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //BLUETOOTH IS ALREADY ON, TOAST THE USER
        if(btAdapter.isEnabled()){
            String address = btAdapter.getAddress();
            String name = btAdapter.getName();
            String statusText = name + ": " + address;

            Toast.makeText(MainActivity.this, "Bluetooth Already On: " + statusText, Toast.LENGTH_LONG).show();
        }
    }

    private void onConversationStart(){
        //This method is to be called when a conversation is started with someone.
        //This will load any past messages if a table exists.

        //Set the name that will reference corresponding database table.
        //currentTable = userID /*+ senderID*/;

        //This method checks if a table already exists, otherwise it creates one.
        dbHandler.createTable(currentTable);

        //Send our deviceID
        //Receive their deviceID
        oDeviceID = null;

        //Now check to see if both tables are the same and up to date.
        //This should resolve any issues if BT connection is lost before a message is received.
        //REQUIREMENT: BOTH PARTIES MUST SEND THE SIZE OF THEIR TABLE USING .getCount()
        /* RESTORE ONCE BLUETOOTH IS WORKING
        //Store our table size
        int mRowCount = dbHandler.getCount(currentTable);
        //Get and store their table size
        int yRowCount = 0; //Call method to get the partners row count. Essentially, they pass each other their mRowCount

        if(mRowCount > yRowCount){
            //Our table has messages they haven't received
            //Send our table information
        }else if(mRowCount < yRowCount){
            //Their table has messages we haven't received
            //Receive their table information
        }else{
            //Our tables are perfectly in sync.
        }*/

        //Update listView
        populateListView();
    }

    //Message is ready to be sent.
    public void sendButtonClicked(View view) {
        if (!newMessageText.getText().toString().equals("")) {
            //Only run if newMessageText is not empty
            //Initialize a calendar to current date
            Calendar c = Calendar.getInstance();
            //Create format for the date
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA);
            //Format the date and set it to a string
            String timeStamp = df.format(c.getTime());

            //Add to database a new MessageData object with fields.
            dbHandler.addMessage(currentTable, new MessageData(newMessageText.getText().toString(), timeStamp, userID));

            //Clear text field
            newMessageText.setText("");
            Log.d(DEBUGTAG, "EditText cleared");

            //Refresh listView
            populateListView();
        } else {
            Log.d(DEBUGTAG, "Message Field Empty");
        }
    }

    public void onMessageReceived(MessageData md){
        //This method is to be called when a bluetooth message is received
        //It adds the message to the database and refreshes the listView
        dbHandler.addMessage(currentTable, md);

        if(unread.length() != 0){
            //If this is the first unread message, put the senderID at the top of the notification text
            unread.append(md.getSenderID());
            unread.append(": \n");
        }

        if(inBack){
            //The app is in the background, the message is unread
            unread.append(md.getMessage());
            unread.append("\n");
        }

        //Refresh the list view
        populateListView();
    }

    private void populateListView() {
        Cursor myCursor = dbHandler.getAllRows(currentTable);
        //What data you are going to populate the data with
        String [] fromFieldNames = new String[] {myDBHandler.COLUMN_MESSAGETEXT, myDBHandler.COLUMN_SENDERID, myDBHandler.COLUMN_TIME, myDBHandler.COLUMN_IMGID};

        //Where the data is going to go.
        int[] toViewIDs = new int[] {R.id.listRowMessage, R.id.listRowSender, R.id.listRowTime, R.id.listRowImage};

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
    public void testButtonClicked(View view){
        //Change table name for testing.
        currentTable = newMessageText.getText().toString();
        onConversationStart();
        newMessageText.setText("");
    }

    public void createNotification(){
        //Toast.makeText(MainActivity.this, "Toast Message", Toast.LENGTH_LONG).show();

        /*
        * Once Bluetooth is working, revisit this method. The goal is that as more messages are
        * received but not read, this method will update the notification to show all unread messages
        * separated by hard returns. This will require tracking what messages have been received
        * since the app was last opened, and adding those messages to String notifyText.
        *
        * Finally, the notification ID should be unique to each conversation. As such, I will
        * base it off the integer value of the sender's userID. This should ensure that if you receive
        * multiple messages from different conversations, they all get their own notification.
         */

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.img);
        mBuilder.setContentTitle("SmackTalker: Unread Messages");
        mBuilder.setContentText(unread.toString());
        //Notification will disappear when clicked on.
        mBuilder.setAutoCancel(true);

        //Creates intent, with the context from MainActivity.
        Intent resultIntent = new Intent(MainActivity.this, MainActivity.class);

        //This PendingIntent opens the MainActivity class (for when notification is clicked)
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //Set the on notification click behaviour to PendingIntent
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //notificationID allows you to update the notification later on.
        //mBuilder.build() returns a Notification containing above specifications.
        mNotifyMgr.notify(0, mBuilder.build());
    }

    private void deleteNotifications(){
        //Clear all notifications. This will run when the .onResume() is called.
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.cancelAll();
    }

    private void setUserID(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        //Create EditText to be used in dialog
        final EditText userIDText = new EditText(this);

        //Set dialog title
        //dialogBuilder.setTitle("Username");
        //Set dialog message
        dialogBuilder.setMessage("Please enter your username");
        //Add edit text to dialog
        dialogBuilder.setView(userIDText);

        //This button will set userID
        dialogBuilder.setPositiveButton("Set Username", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userID = userIDText.getText().toString();
                Toast.makeText(MainActivity.this, "UserID set", Toast.LENGTH_SHORT).show();

                //Stores the userID under the key specified in the final USERIDKEY
                editor.putString(USERIDKEY, userID);
                editor.apply();
            }
        });
        //This button will randomly generate a userID
        dialogBuilder.setNegativeButton("Randomize", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Assign randomly generated userID

                int len = 8;
                StringBuilder sb = new StringBuilder( len );
                rnd = new SecureRandom();
                //Random # created which points to a string of all possible chars.
                //Appends that char onto the userID and repeats until desired length.
                for( int i = 0; i < len; i++ ) {
                    sb.append(AB.charAt(rnd.nextInt(AB.length())));
                }
                userID = sb.toString();
                Toast.makeText(MainActivity.this, "UserID randomized", Toast.LENGTH_SHORT).show();

                //Stores the userID under the key specified in the final USERIDKEY
                editor.putString(USERIDKEY, userID);
                editor.apply();
            }
        });

        //Create the dialog
        AlertDialog aDialog = dialogBuilder.create();
        //Show the dialog
        aDialog.show();
    }
}