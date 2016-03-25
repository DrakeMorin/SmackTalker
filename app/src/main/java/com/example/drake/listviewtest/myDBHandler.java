package com.example.drake.listviewtest;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class myDBHandler extends SQLiteOpenHelper{

    //If you update the structure of the database, change this constant for compatibility
    private static final int DATABASE_VERSION = 1;
    //Name of database, must end in .db
    private static final String DATABASE_NAME = "SmackTalker.db";
    //Name of the table within the database
    public static final String TABLE_MESSAGES = "MessageHistory";

    //Every column in the table should have its own constant here.
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MESSAGETEXT = "messagetext";
    public static final String COLUMN_SENDERID = "senderID";
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
                COLUMN_ID + " INTERGER PRIMARY KEY AUTOINCREMENT" + //Unique int identify, automatically incrementing
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
    }
}
