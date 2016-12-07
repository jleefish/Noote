/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.advmovile.noote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class NooteDbAdapter {

    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";
    public static final String KEY_ROWID = "_id";

    public static final String KEY_DATE = "date";
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_PHOTO = "photo";
    public static final String KEY_AUDIO = "audio";


    private static final String TAG = "NotesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private boolean titleSortAsc = false;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table notes (_id integer primary key autoincrement, "
                    + "title text not null, "
                    + "category text not null, "
                    + "body text not null, "
                    + "photo blob, "
//                    + "audio blob, "
                    + "latitude text, "
                    + "longitude text, "
                    + "date text );";


    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "notes";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public NooteDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public NooteDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the note
     * @param body the body of the note
     * @return rowId or -1 if failed
     */
    public long createNote(String title, String body, String date, String category, String latitude, String longitude, byte[] photo) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_BODY, body);
        initialValues.put(KEY_DATE, date);
        initialValues.put(KEY_CATEGORY, category);
        initialValues.put(KEY_PHOTO, photo);
//        initialValues.put(KEY_AUDIO, audio);
        initialValues.put(KEY_LATITUDE, latitude);
        initialValues.put(KEY_LONGITUDE, longitude);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteNote(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllNotes() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_BODY, KEY_DATE, KEY_CATEGORY, KEY_PHOTO, KEY_LATITUDE, KEY_LONGITUDE}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_TITLE, KEY_BODY, KEY_DATE, KEY_CATEGORY, KEY_PHOTO, KEY_LATITUDE, KEY_LONGITUDE}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateNote(long rowId, String title, String body, String date, String category, String latitude, String longitude, byte[] photo) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_BODY, body);
        args.put(KEY_DATE, date);
        args.put(KEY_CATEGORY, category);
        args.put(KEY_PHOTO, photo);
//        args.put(KEY_AUDIO, audio);
        args.put(KEY_LATITUDE, latitude);
        args.put(KEY_LONGITUDE, longitude);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor filter (CharSequence constraint, String titleSort, String dateSort)  {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(
                DATABASE_TABLE
        );

        String asColumnsToReturn[] = {
                DATABASE_TABLE + "."
                        + KEY_ROWID + "," +
                        DATABASE_TABLE + "."
                        + KEY_TITLE + "," +
                        DATABASE_TABLE + "."
                        + KEY_BODY + "," +
                        DATABASE_TABLE + "."
                        + KEY_DATE + "," +
                        DATABASE_TABLE + "."
                        + KEY_CATEGORY + "," +
                        DATABASE_TABLE + "."
                        + KEY_PHOTO + "," +
                        DATABASE_TABLE + "."
                        + KEY_LATITUDE + "," +
                        DATABASE_TABLE + "."
                        + KEY_LONGITUDE
        };

        String sortString = null;

        if(titleSort != "none" && dateSort == "none"){
            switch(titleSort) {
                case "asc" :
                    sortString = KEY_TITLE;
                    break;
                case "des" :
                    sortString = KEY_TITLE + " DESC";
                    break;
                case "none" :
                    sortString = null;
                    break;
            }
        }

        if(dateSort != "none" && titleSort == "none") {
            switch(dateSort) {
                case "asc" :
                    sortString = KEY_DATE;
                    break;
                case "des" :
                    sortString = KEY_DATE + " DESC";
                    break;
                case "none" :
                    sortString = null;
                    break;
            }
        }


        if (constraint == null  ||  constraint.length () == 0)  {
            //  Return the full list
            return queryBuilder.query(mDb, asColumnsToReturn, null, null, null, null, sortString);
        }  else  {
            String value = "%"+constraint.toString()+"%";

            String conditions =
                    KEY_TITLE + " like ?" + " OR "
                            + KEY_CATEGORY + " like ?" + " OR "
                            + KEY_BODY + " like ? ";

            return mDb.query(DATABASE_TABLE, asColumnsToReturn, conditions, new String[]{value, value, value}, null, null, sortString);
        }
    }
}
