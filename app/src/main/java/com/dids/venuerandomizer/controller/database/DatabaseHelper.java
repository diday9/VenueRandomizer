package com.dids.venuerandomizer.controller.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dids.venuerandomizer.model.DatabaseVenue;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TABLE = "venue";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_ADDRESS = "address";

    private static final String CREATE_TABLE = "create table " + TABLE + " (" + COLUMN_ID +
            " text not null," + COLUMN_NAME + " text not null, " + COLUMN_CATEGORY +
            " text not null, " + COLUMN_ADDRESS + " text not null" + " )";

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + CREATE_TABLE;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "FindMeAPlace.db";
    private static DatabaseHelper mSingleton;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (mSingleton == null) {
            mSingleton = new DatabaseHelper(context);
        }
        return mSingleton;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DROP_TABLE);
        onCreate(sqLiteDatabase);
    }

    public void createEntry(DatabaseVenue venue) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, venue.getId());
        values.put(COLUMN_NAME, venue.getName());
        values.put(COLUMN_CATEGORY, venue.getCategory());
        values.put(COLUMN_ADDRESS, venue.getAddress());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE, null, values);
        db.close();
    }

    public List<DatabaseVenue> getAllEntries() {
        List<DatabaseVenue> list = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        String[] columns = {COLUMN_ID, COLUMN_NAME, COLUMN_CATEGORY, COLUMN_ADDRESS};
        Cursor cursor = db.query(TABLE, columns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DatabaseVenue venue = cursorToEntry(cursor, true);
            list.add(venue);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return list;
    }

    public boolean exists(String id) {
        SQLiteDatabase db = getWritableDatabase();
        String query = "Select * from " + TABLE + " where " + COLUMN_ID + " =?";
        Cursor cursor = db.rawQuery(query, new String[] {id});
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    private DatabaseVenue cursorToEntry(Cursor cursor, boolean isShort) {
        return new DatabaseVenue(cursor.getString(0), cursor.getString(1),
                cursor.getString(2), cursor.getString(3));
    }

    public void deleteEntry(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE, COLUMN_ID + "=?", new String[]{id});
        db.close();
    }
}
