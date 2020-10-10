package com.example.pickycopy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.TableLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "chats.db";
    private static final String TAG = "database";
    SQLiteDatabase database;
    String Table_name;

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + "Example" + " ( SentTime TEXT, SentMessage TEXT, receiveTime TEXT, recievedMessage TEXT, recieverName TEXT, SenderId TEXT ,RecieverId TEXT,downloadUrl TEXT);");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query;
        query = "DROP TABLE IF EXISTS Example";
        database.execSQL(query); onCreate(database);
    }

    public void addData(BaseMessage message) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("SentTime", message.getSentTime());
        contentValues.put("SentMessage", message.getSenderText());
        contentValues.put("receiveTime", message.getRecievedTime());
        contentValues.put("recievedMessage", message.getRecieverText());
        contentValues.put("recieverName", message.getRecieverName());
        contentValues.put("SenderId",message.getSenderId());
        contentValues.put("RecieverId",message.getRecieverId());
        contentValues.put("downloadUrl",message.getUrl());
        database.insertWithOnConflict("Example",null,contentValues,SQLiteDatabase.CONFLICT_REPLACE);

        Log.d(TAG, "addData: Adding " +message.getRecieverText()+ " to " + Table_name);

    }
    public Cursor getData(String userId){
        Log.d("gen",""+userId);
        String query = "SELECT * FROM " + "Example "+"WHERE "+"RecieverId =?";
        Cursor data = database.rawQuery(query,new String []{userId});
        return data;
    }
}
