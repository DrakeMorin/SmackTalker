package com.example.drake.listviewtest;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class myDBHandler extends SQLiteOpenHelper{

    //If you update the structure of the database, change this constant for compatibility
    private static final int DATABASE_VERSION = 1;
    //Name of database, must end in .db
    private static final String DATABASE_NAME = "SmackTalker.db";
    //Name of the table within the database
    public static final String TABLE_MESSAGES = "messagehistory";

    //Every column in the table should have its own constant here.
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MESSAGETEXT = "messagetext";
    public static final String COLUMN_SENDERID = "senderid";
    public static final String COLUMN_TIME = "time";

    public myDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
        //For housekeeping, passes information to the super class which does background stuff.
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //What happens when the table is created for the first time.

        //This string is an SQL command which will create the table.
        //For the columns in the table, you must add them so the string
        //Looks like tableName(column1 <> column2 <> column3 <>);
        //Where the <> is the details about that column
        String query = "CREATE TABLE " + TABLE_MESSAGES + "(" +
                COLUMN_ID + " INTERGER PRIMARY KEY AUTOINCREMENT" + //Unique int identifier, automatically incrementing
                COLUMN_MESSAGETEXT + " TEXT " +
                COLUMN_SENDERID + " TEXT " +
                COLUMN_TIME + "  " +
                ");";

        //Passes the above command to SQL to execute
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //What happens when you upgrade the version of the database.
        //Delete old table
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        //Call onCreate to make a new table.
        onCreate(db);
    }

    //Add a new row to the database
    public void addMessage(MessageData message){
        //Allows you to set values for several columns for one row, in one go.
        ContentValues values = new ContentValues();
        //Adds the saved message string to the MESSAGETEXT column
        values.put(COLUMN_MESSAGETEXT, message.getMessage());

        //Creates a database we can write to!
        SQLiteDatabase db = getWritableDatabase();

        //Inserts a new row in
        db.insert(TABLE_MESSAGES, null, values);

        //Closes database, saves android some memory.
        db.close();
    }

    //Delete a row from the database
    public void deleteMessage(String messageID){
        //Creates a database we can delete from!
        SQLiteDatabase db = getWritableDatabase();

        //Deletes from the table, where the MESSAGETEXT column matches the parameter passed in.
        db.execSQL("DELETE FROM " + TABLE_MESSAGES + " WHERE " + COLUMN_MESSAGETEXT +
                "=\"" + messageID + "\";");

        //Close database
        db.close();
    }

    //Print out the database as a string
    public String databaseToString(){
        String dbString = "";

        //Creates a database we can write to!
        SQLiteDatabase db = getWritableDatabase();

        //"Select all from the Table where all conditions are met (1 means this is always true)
        String query = "SELECT * FROM " + TABLE_MESSAGES + " WHERE 1";

        //Cursor will point to a location in your results.
        Cursor c = db.rawQuery(query, null);
        //Move to the first row in your results
        c.moveToFirst();

        while(!c.isAfterLast()){
            //While there is still results to go
            if (c.getString(c.getColumnIndex(COLUMN_MESSAGETEXT)) != null){
                dbString += c.getString(c.getColumnIndex(COLUMN_MESSAGETEXT));

                //Adds a new line
                dbString += "\n";
            }
        }
        db.close();
        return dbString;
    }
}
