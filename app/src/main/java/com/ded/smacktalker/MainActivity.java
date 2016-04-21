package com.ded.smacktalker;

import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    protected static String userID = "Bob";
    private static final String USERIDKEY = "userID";

    EditText newMessageText;
    ListAdapter myListAdapter;
    ListView listView;
    myDBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        //Null is the default value. If no userID is saved, the default value assigned will ne null.
        userID = prefs.getString(USERIDKEY, null);

        //Since the last three parameters are constants of the class, null is passed.
        dbHandler = new myDBHandler(this, null, null, 1);

        if(userID != null){

            //UserID has not been set.
            DialogFragment newFragment = new SetUserIDDialog();
            newFragment.show(getFragmentManager(), USERIDKEY);

            //Save the userID to preferences.
            SharedPreferences.Editor editor = prefs.edit();
            //Stores the userID under the key specified in the final USERIDKEY
            editor.putString(USERIDKEY, userID);
            editor.commit();
        }

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
        userID = newMessageText.getText().toString();
        newMessageText.setText("");
        Log.d(DEBUGTAG, "userID updated");
    }
}