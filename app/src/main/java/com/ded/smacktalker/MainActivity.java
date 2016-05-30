package com.ded.smacktalker;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.view.menu.ActionMenuItemView;
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

    //This bool will store if the panic mode has been activated
    static boolean panicMode = false;

    EditText newMessageText;
    ListView myListView;
    Menu menu;
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
    static boolean inBack = false;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private String mConnectedDeviceName = null;

    //String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    //Local bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    //Member object for the class services
    private BluetoothChatService mChatService = null;

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

        //Null is the default value. If no userID is saved, the default value assigned will be null.
        userID = prefs.getString(USERIDKEY, null);

        if(userID == null){
            //UserID has not been set
            setUserID();
        }


        myListView = (ListView) findViewById(R.id.listView);
        //Add item onClickListener
        myListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //Get the string value of the view that was touched at position # (which is stored in position
                        //This WILL enable copy to clipboard

                        //Get cursor from myListView
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

        //Populate myListView with previous messages
        populateListView();

        //BLUETOOTH STUFF
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = MainActivity.this;
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //When the app is resumed from background, assume all messages are read.
        //Clear the unread messages stored in the stringbuilder.
        unread.delete(0, unread.length());

        //App is now in the foreground, not the back.
        inBack = false;

        //Clear any unread message notifications
        deleteNotifications();

        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Set bool saying app is running in background
        inBack = true;
    }

    //Set up the UI and background operations for chat.
    private void setupChat() {

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(MainActivity.this,  mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    //Makes this device discoverable.
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    //The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            //Connected to a device.
                            onConversationStart();
                            newMessageText.setText("");
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            break;
                    }
                    break;
                case Constants.MESSAGE_READ:
                    //When a message is received
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    onMessageReceived(new MessageData(readMessage, "12:00", mConnectedDeviceName));
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != MainActivity.this) {
                        Toast.makeText(MainActivity.this, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != MainActivity.this) {
                        Toast.makeText(MainActivity.this, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
        }
    }

    //Establish connection with other divice
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    //Sends a message.
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(MainActivity.this, "Not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the message bytes and tell the BluetoothChatService to write
        byte[] send = message.getBytes();
        mChatService.write(send);

        // Reset out string buffer to zero and clear the edit text field
        mOutStringBuffer.setLength(0);
    }

    //INFLATES ACTION BAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        Log.d(DEBUGTAG, "Options Menu Inflated");
        return true;
    }

    //Check for if any of the action bar items are clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_bluetooth:
                Log.d(DEBUGTAG, "Bluetooth button pressed");
                btButtonClick();
                return true;

            case R.id.action_settings:
                //User clicked on the settings
                setUserID();
                return true;

            case R.id.action_panic:
                if(!panicMode) {
                    //Turn on panic mode
                    panicMode = true;
                    populateListView();

                    //Change icon to on
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        //If running Android Lollipop or higher, use getDrawable(int, theme)
                        menu.getItem(1).setIcon(getResources().getDrawable(R.drawable.panic_icon_white, getTheme()));
                    }else{
                        //If running lower than Android Lollipop, use getDrawable(int)
                        menu.getItem(1).setIcon(getResources().getDrawable(R.drawable.panic_icon_white));
                    }


                }else{
                    //Turn off panic mode
                    panicMode = false;
                    populateListView();

                    //Change icon to off
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        //If running Android Lollipop or higher, use getDrawable(int, theme)
                        menu.getItem(1).setIcon(getResources().getDrawable(R.drawable.panic_icon_black, getTheme()));
                    }else{
                        //If running lower than Android Lollipop, use getDrawable(int)
                        menu.getItem(1).setIcon(getResources().getDrawable(R.drawable.panic_icon_black));
                    }
                }
                return true;

            default:
                //User's action unrecognized, use super class to handle it
                return super.onOptionsItemSelected(item);
        }
    }

    //What happens if btButton is clicked;
    public void btButtonClick() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //The bluetooth is already on, so show DeviceList
        if(btAdapter.isEnabled()){
            Intent i = new Intent(MainActivity.this,  DeviceListActivity.class);
            startActivity(i);
            Log.d(DEBUGTAG, "Opening Device List");
        }

        //Bluetooth is not on, turn it on
        else{

            //Sends request to the device to turn on bluetooth radio
            String actionStateChanged = BluetoothAdapter.ACTION_STATE_CHANGED;
            String actionRequestEnable = BluetoothAdapter.ACTION_REQUEST_ENABLE;
            IntentFilter Filter = new IntentFilter(actionStateChanged);
            startActivityForResult(new Intent(actionRequestEnable), 0);
            Log.d(DEBUGTAG, "turned on bluetooth");

            Intent i = new Intent(MainActivity.this,  DeviceListActivity.class);
            startActivity(i);
            //Saves information from bluetooth connection, don't know if this will actually be used yet
            String address = btAdapter.getAddress();
            String name = btAdapter.getName();
            String statusText = name + ":" + address;


        }
    }

    protected void onConversationStart(){
        //This method is to be called when a conversation is started with someone.
        //This will load any past messages if a table exists.

        //Set the name that will reference corresponding database table.
        //currentTable = userID /*+ senderID*/;

        //This method checks if a table already exists, otherwise it creates one.
        dbHandler.createTable(userID + mConnectedDeviceName);

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

        //Update myListView
        populateListView();
    }

    //Message is ready to be sent.
    public void sendButtonClicked(View view) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(MainActivity.this, "Not connected", Toast.LENGTH_SHORT).show();
        }else {
            if (!newMessageText.getText().toString().equals("")) {
                //Only run if newMessageText is not empty
                //Initialize a calendar to current date
                Calendar c = Calendar.getInstance();
                //Create format for the date
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA);
                //Format the date and set it to a string
                String timeStamp = df.format(c.getTime());

                String message = newMessageText.getText().toString();
                //Add to database a new MessageData object with fields.
                dbHandler.addMessage(currentTable, new MessageData(message, timeStamp, userID));

                //Clear text field
                newMessageText.setText("");
                Log.d(DEBUGTAG, "EditText cleared");

                //Refresh myListView
                populateListView();

                // Get the message bytes and tell the BluetoothChatService to write
                byte[] send = message.getBytes();
                mChatService.write(send);

                // Reset out string buffer to zero and clear the edit text field
                mOutStringBuffer.setLength(0);

            } else {
                Toast.makeText(MainActivity.this, "No message to send", Toast.LENGTH_SHORT).show();
                Log.d(DEBUGTAG, "Message Field Empty");
            }
        }
    }

    protected void onMessageReceived(MessageData md){
        //This method is to be called when a bluetooth message is received
        //It adds the message to the database and refreshes the myListView
        dbHandler.addMessage(currentTable, md);

        if(inBack && unread.length() != 0){
            //If this is the first unread message, put the senderID at the top of the notification text
            unread.append(md.getSenderID());
            unread.append(": \n");
        }

        if(inBack){
            //The app is in the background, the message is unread
            unread.append(md.getMessage());
            unread.append("\n");
            //Create notification
            createNotification();
        }

        //Refresh the list view
        populateListView();
    }

    private void populateListView() {
        //Will only populate the myListView if panic mode is off.

        Cursor myCursor = dbHandler.getAllRows(currentTable);
        //What data you are going to populate the data with
        String[] fromFieldNames = new String[]{myDBHandler.COLUMN_MESSAGETEXT, myDBHandler.COLUMN_SENDERID, myDBHandler.COLUMN_TIME, myDBHandler.COLUMN_IMGID};

        //Where the data is going to go.
        int[] toViewIDs = new int[]{R.id.listRowMessage, R.id.listRowSender, R.id.listRowTime, R.id.listRowImage};

        //Define cursorAdapter, instantiated next line.
        SimpleCursorAdapter myCursorAdapter;
        //Get the context, the defined layout being used, the cursor, the columns being read, the location of info being stored, 0
        myCursorAdapter = new SimpleCursorAdapter(getBaseContext(), R.layout.custom_row, myCursor, fromFieldNames, toViewIDs, 0);

        //Set myListView
        myListView = (ListView) findViewById(R.id.listView);



        if(panicMode){
            //Do not show messages; set adapter to null
            myListView.setAdapter(null);
        }else {
            //Messages can be shown; set myListView adapter to the cursorAdapter
            myListView.setAdapter(myCursorAdapter);
        }
    }

    //For testing purposes.
    public void testButtonClicked(View view){
        //Change table name for testing.
        currentTable = newMessageText.getText().toString();
        onConversationStart();
        newMessageText.setText("");
    }

    private void createNotification(){
        //This will store and build the notification and its data
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        //Set notification info
        mBuilder.setSmallIcon(R.drawable.logo_round);
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