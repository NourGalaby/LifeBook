package com.example.hassan.test2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    DBAdapter db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new DBAdapter(this);
        db.open();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    public void display(String s) {
        TextView t = (TextView) findViewById(R.id.editText);
        t.setText(s);
    }

    public void onClick_AddRecord(View v) {
        display("Clicked Add Record");
        db.insertRow("post");
    }

    public void onClick_ClearAll(View v) {
        display("Clicked Clear all");
        db.clearAll();
    }

    public void onClick_Display(View v) {
        display("Clicked Display");
        Cursor c = db.getAllRows();
        StringBuilder s = new StringBuilder("");
        if(c.moveToFirst())
            do {
                int id = c.getInt(0);
                String text = c.getString(1);
                String date = c.getString(2);
                s.append("Record #" + id + ": " + text + " ==> " + date + "\n");
            } while(c.moveToNext());
        c.close();
        display(s.toString());
    }
}

class DBAdapter {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "App";
    private static final String DATABASE_TABLE = "records";
    public static final String TEXT_KEY = "_post";
    public static final String ID_KEY = "_post_id";
    public static final String TIMESTAMP_KEY = "_timestamp";
    private static final String[] ALL_KEYS = new String[]{ID_KEY, TEXT_KEY, TIMESTAMP_KEY};
    private static final String CreateQuery =
            "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE +
            "(" + ID_KEY + " integer primary key AUTOINCREMENT, " +
            TEXT_KEY + " varchar(255) not null, " +
            TIMESTAMP_KEY + " DATETIME DEFAULT CURRENT_TIMESTAMP);";

    private final Context context;
    private DatabaseInit dbHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context context) {
        this.context = context;
        dbHelper = new DatabaseInit(context);
    }

    public DBAdapter open() {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insertRow(String text) {
        db.execSQL("INSERT INTO "+ DATABASE_TABLE + "("+ TEXT_KEY+") VALUES (\'"+text+"\');");
    }

    public void clearAll() {
        Cursor c = getAllRows();
        long rowId = c.getColumnIndex(ID_KEY);
        if(c.moveToFirst())
            do {
                deleteRow(c.getLong((int)rowId));
            } while(c.moveToNext());
    }

    public boolean deleteRow(long rowId) {
        String where = ID_KEY + "=" + rowId;
        return db.delete(DATABASE_TABLE, where, null) != 0;
    }

    public Cursor getAllRows() {
        Cursor c = db.query(true, DATABASE_TABLE, ALL_KEYS,
                null, null, null, null, null, null);
        if(c != null)
            c.moveToFirst();
        return c;
    }

    private static class DatabaseInit extends SQLiteOpenHelper {

        public DatabaseInit(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CreateQuery);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("Message", "Upgrading application's database from version " + oldVersion
                    + " to " + newVersion + ", which will destroy all old data!");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }
}
