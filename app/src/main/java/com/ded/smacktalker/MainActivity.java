package com.ded.smacktalker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {



    protected final static String DEBUGTAG = "DED";
    private BluetoothAdapter btAdapter;
    private Button Bluetooth;
    protected String userID = "Alpha";

    EditText newMessageText;
    myDBHandler dbHandler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Since the last three parameters are constants of the class, null is passed.
        dbHandler = new myDBHandler(this, null, null, 1);

        //Create text view for user written messages
        newMessageText = (EditText) findViewById(R.id.newMessageText);

        /*//Add item onClickListener
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
        populateListView();
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
            String statusText = name + ":" + address;

            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 0, 0);
            toast.makeText(MainActivity.this, "Bluetooth Already On: " + statusText, toast.LENGTH_LONG).show();
        }

    }

    //Message is ready to be sent.
    public void sendButtonClicked(View view) {
        if (!newMessageText.getText().toString().equals("")) {
            //Only run if newMessageText is not empty
            //Intialize a calendar to current date
            Calendar c = Calendar.getInstance();
            //Create format for the date
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //Format the date and set it to a string
            String timeStamp = df.format(c.getTime());

            //Add to database a new MessageData object with fields.
            dbHandler.addMessage(new MessageData(newMessageText.getText().toString(), timeStamp, userID));

            //Clear text field
            newMessageText.setText("");
            Log.d(DEBUGTAG, "EditText cleared");

            //Refresh listView
            populateListView();
        } else {
            Log.d(DEBUGTAG, "Message Field Empty");
        }
    }

    private void populateListView() {
        Cursor myCursor = dbHandler.getAllRows();
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
        //Send message as if it was received from someone else.
        dbHandler.addMessage(new MessageData(newMessageText.getText().toString(), "Test", "Not You"));
        newMessageText.setText("");
        populateListView();
        createNotification(newMessageText.getText().toString(), 0);
    }

    public void createNotification(String notifyText, int notifyID){
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
        mBuilder.setContentTitle("Notification Alert, Click Me!");
        mBuilder.setContentText(notifyText);
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

        // notificationID allows you to update the notification later on.
        //mBuilder.build() returns a Notification containing above specifications.
        mNotifyMgr.notify(notifyID, mBuilder.build());
    }

}