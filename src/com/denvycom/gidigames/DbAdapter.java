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

package com.denvycom.gidigames;
 

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple  database access helper class. Defines the basic CRUD operations
 * for the LEXIS Database
 * 

 */
public class DbAdapter {


	//sCORETable
	public static final String _ROWID = "OD";
	public static final String KEY_GAME_TITLE= "title";
	public static final String KEY_GAME_DIFFICULTY = "difficulty";
	public static final String KEY_GAME_MOVES= "moves";
	public static final String KEY_GAME_SUBTITLE= "subtitle";
	public static final String KEY_GAME_TIME= "time";
	public static final String COLUMN_NAME_MODIFICATION_DATE = "modified";
    public static final String COLUMN_NAME_CREATE_DATE = "created";
	private static final String DATABASE_NAME = "gidigames";
	
	
	//Language Game Table 
	public static final String KEY_LANG_LANGUAGE= "lang_language";
	public static final String KEY_LANG_ROOTWORD = "lang_rootword";
	public static final String KEY_LANG_ENGLISHTRANS= "lang_englishtrans"; 
	
	private static final String TABLE_SCORES = "scores"; 
	private static final String TABLE_LEXIS = "lexis"; 
	private static final int DATABASE_VERSION = 3 ;

	private static final String TAG = "DbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

    
    Long now = Long.valueOf(System.currentTimeMillis());

	/**
	 * Database creation sql statement
	 */
	private static final String CREATE_SCORES_SQL ="CREATE TABLE " + TABLE_SCORES + " ("
			+ _ROWID + " INTEGER PRIMARY KEY,"
			+ KEY_GAME_TITLE + " TEXT,"
			+ KEY_GAME_TIME + " INT,"
			+ KEY_GAME_DIFFICULTY + " TEXT,"
			+ KEY_GAME_MOVES + " INT,"
			+ KEY_GAME_SUBTITLE + " TEXT,"
			+ COLUMN_NAME_CREATE_DATE + " INTEGER,"
			+ COLUMN_NAME_MODIFICATION_DATE + " INTEGER"
			+ ");";
	
	private static final String CREATE_LEXIS_SQL ="CREATE TABLE " + TABLE_LEXIS + " ("
			+ _ROWID + " INTEGER PRIMARY KEY,"
			+ KEY_LANG_ROOTWORD+ " TEXT,"
			+ KEY_LANG_LANGUAGE + " TEXT,"
			+ KEY_LANG_ENGLISHTRANS + " TEXT,"
			+ KEY_GAME_DIFFICULTY + " TEXT,"
			+ COLUMN_NAME_CREATE_DATE + " INTEGER,"
			+ COLUMN_NAME_MODIFICATION_DATE + " INTEGER"
			+ ");";
    
	 

	private final Context mCtx;


	private static class DatabaseHelper extends SQLiteOpenHelper {


		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL(CREATE_SCORES_SQL); 
			db.execSQL(CREATE_LEXIS_SQL); 

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_LEXIS);
			
			db.execSQL(CREATE_SCORES_SQL); 
			db.execSQL(CREATE_LEXIS_SQL); 
			
		}
	}




	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx the Context within which to work
	 */
	public DbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * Open the data database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException if the database could be neither opened or created
	 */
	public DbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}


	 
	public long createScoreItem(String title, String subtitle, String difficulty, int moves, int time ) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_GAME_TITLE, title);
		initialValues.put(KEY_GAME_SUBTITLE, subtitle);
		initialValues.put(KEY_GAME_DIFFICULTY, difficulty);
		initialValues.put(KEY_GAME_MOVES, moves);
		initialValues.put(KEY_GAME_TIME, time); 
		initialValues.put(COLUMN_NAME_CREATE_DATE, now); 
		initialValues.put(COLUMN_NAME_MODIFICATION_DATE, now); 

		return mDb.insert(TABLE_SCORES, null, initialValues);
	}

	
	

	/**
     * Create a New Language Game Entry
     * 
     * @param  
     * @return  rowId or -1 if failed
     */
    public long createLangGameWord(String rootword, String language, String englishtranslation, String difficulty ) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_LANG_ROOTWORD, rootword);
        initialValues.put(KEY_LANG_LANGUAGE, language);
        initialValues.put(KEY_LANG_ENGLISHTRANS , englishtranslation);
        initialValues.put(KEY_GAME_DIFFICULTY, difficulty);

        return mDb.insert(TABLE_LEXIS, null, initialValues);
    }
    
 
    /**
     * Fetch All Language Game Words
     * 
     
     */
    public Cursor fetchLangGameWords() {
    	 return mDb.query(TABLE_LEXIS, new String[] {_ROWID, KEY_LANG_ROOTWORD,
                 KEY_LANG_LANGUAGE, KEY_LANG_ENGLISHTRANS, KEY_GAME_DIFFICULTY},  null, null, null, null, null);
    }
    
    
    /**
     * Fetch All Language Game Words
     * 
     
     */
    public Cursor fetchWordSet(String thelang) {
    	 return mDb.query(TABLE_LEXIS, new String[] {_ROWID, KEY_LANG_ROOTWORD,
                 KEY_LANG_LANGUAGE, KEY_LANG_ENGLISHTRANS, KEY_GAME_DIFFICULTY},  KEY_LANG_LANGUAGE + " = " + "'" + thelang + "'" , null, null, null, null);
    }
    
    public Cursor fetchAllWords() {
   	 return mDb.query(TABLE_LEXIS, new String[] {_ROWID, KEY_LANG_ROOTWORD,
                KEY_LANG_LANGUAGE, KEY_LANG_ENGLISHTRANS, KEY_GAME_DIFFICULTY}, null , null, null, null, null);
   }
    
	/**
	 * Fetch All Language Game Words
	 * 

	 */
	public Cursor fetchLexisBest(String title, String language, String difficulty) {
		return mDb.query(TABLE_SCORES, new String[] {_ROWID, KEY_GAME_TITLE,
				KEY_GAME_DIFFICULTY, KEY_GAME_MOVES, KEY_GAME_TIME},  
				KEY_GAME_TITLE + "= '" + title + "'" + " " +
						"AND " + KEY_GAME_DIFFICULTY + "= '" + difficulty + "'" 
						+ "AND " + KEY_GAME_SUBTITLE + "= '" + language + "'"
				,  null, null, null, KEY_GAME_MOVES + " ASC",null);
		
		//mDb.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
	}
	
	public Cursor fetchPuzzleBest(String title, String subtitle , String difficulty) {
		return mDb.query(TABLE_SCORES, new String[] {_ROWID, KEY_GAME_TITLE,
				KEY_GAME_DIFFICULTY, KEY_GAME_MOVES, KEY_GAME_TIME},  
				KEY_GAME_TITLE + "= '" + title + "'" + " AND " + KEY_GAME_DIFFICULTY + "= '" + difficulty + "'" 
				+ "AND " + KEY_GAME_SUBTITLE + "= '" + subtitle + "'"
				,  null, null, null, KEY_GAME_MOVES + " ASC",null);
		
		//mDb.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
	}

	
	/**
	 * Fetch All Language Game Words
	 * 

	 */
	public Cursor fetchBestScorebyTime(String title, String difficulty) {
		return mDb.query(TABLE_SCORES, new String[] {_ROWID, KEY_GAME_TITLE,
				KEY_GAME_DIFFICULTY, KEY_GAME_MOVES, KEY_GAME_TIME},  
				KEY_GAME_TITLE + "= '" + title + "'" + " AND " + KEY_GAME_DIFFICULTY + "= '" + difficulty + "'" 
				,  null, null, null, KEY_GAME_TIME,null);
		
		//mDb.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
	}



	 


}
