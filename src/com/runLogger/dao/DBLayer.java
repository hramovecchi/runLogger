package com.runLogger.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

/**
 * Manages all the access to the DB
 * */
public class DBLayer {

	public static final String KEY_ROWID = "_id";
	public static final String KEY_LENGTH = "length";
	public static final String KEY_DATE = "date";
	public static final String KEY_TIME = "time";
	
	public static final String KEY_RUNID = "run_id";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	
	public static final String DESC_ORDER = KEY_DATE+" desc"+","+KEY_TIME+" desc";
	public static final String ASC_ORDER = KEY_DATE+","+KEY_TIME;
	
	private static final int KEY_NULL_RUNID = 0;
	/**
	* Tag for logging messages
	*/
	public static final String TAG = "DB";

	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;

	private Context mCtx = null;

	/**
	* Database and tables names
	*/
	private static final String DATABASE_NAME = "run_logger";
	private static final String DATABASE_TABLE_LOGS = "run_logs";
	private static final int DATABASE_VERSION = 2;
	
	private static final String DATABASE_TABLE_LOCATIONS = "run_locations";
	
	/**
	 * Creation statements
	 * */
	private static final String DATABASE_CREATE_LOGS =
	"create table " + DATABASE_TABLE_LOGS + " ( "
	+ KEY_ROWID + " integer primary key autoincrement, "
	+ KEY_LENGTH + " real not null,"
	+ KEY_DATE + " varchar(20) not null,"
	+ KEY_TIME + " varchar(20) not null);";
	
	private static final String DATABASE_CREATE_LOCATIONS =
	"create table " + DATABASE_TABLE_LOCATIONS + " ( "
	+ KEY_ROWID + " integer primary key autoincrement, "
	+ KEY_RUNID + " integer,"
	+ KEY_LATITUDE + " real not null,"
	+ KEY_LONGITUDE + " real not null);";
	

	/********************************************************
	* Think of this as a driver for your database
	* interaction. You can just copy and paste this part in
	* all your database interaction classes.
	*********************************************************/
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context ctx) {
			super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE_LOGS);
			db.execSQL(DATABASE_CREATE_LOCATIONS);
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP IF TABLE EXISTS "+DATABASE_TABLE_LOGS);
			db.execSQL("DROP IF TABLE EXISTS "+DATABASE_TABLE_LOCATIONS);
			onCreate(db);
		}
	}

	public DBLayer(Context ctx) {
		mCtx = ctx;
	}
	/********************************************************
	* This opens a connection to the database but if
	* something goes wrong, it will throw an exception.
	********************************************************/
	public DBLayer open () throws SQLException {
		dbHelper = new DatabaseHelper(mCtx);
		db = dbHelper.getWritableDatabase();
		return this;
	}
	
	public void close() {
		if (db.isOpen())
			dbHelper.close();
	}
	
	/**
	 * Fetches the length, date and _id for all the registers in the run_logs table
	 * */
	public Cursor fetchAllLengths(String order) {
		try {
			return db.query(DATABASE_TABLE_LOGS,
					new String[] {KEY_LENGTH, KEY_DATE, KEY_ROWID},
					null, null,null, null, order);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return null;
		}
	}
	
	/**
	 * Fetches the length, date, time and _id for all the registers in the run_logs table
	 * */
	public Cursor fetchAllLengthsWithTime() {
		try {
			return db.query(DATABASE_TABLE_LOGS,
					new String[] {KEY_LENGTH, KEY_DATE, KEY_TIME, KEY_ROWID},
					null, null, null, null,KEY_DATE);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return null;
		}
	}

	/**
	 * @param rowId
	 * Fetches the register identified by the ID passed as a parameter
	 * */
	public Cursor fetchLength(long rowId) throws SQLException {
		Cursor ret = db.query(DATABASE_TABLE_LOGS,
				new String[] {KEY_LENGTH, KEY_DATE, KEY_ROWID},
				KEY_ROWID+"="+rowId, null, null, null, null);
		if (ret != null) {
			ret.moveToFirst();
		}
		return ret;
	}
	
	/**
	 * @param date
	 * Fetches the registers posterior to the date passed as a parameter
	 * */
	public Cursor fetchLengthAfterDate(String date, String order) throws SQLException {
		if ("".equals(date)) {
			return fetchAllLengthsWithTime();
		} else {
			Cursor ret = db.query(DATABASE_TABLE_LOGS,
					new String[] {KEY_LENGTH, KEY_DATE, KEY_TIME, KEY_ROWID},
					KEY_DATE + ">=\"" + date + "\"", null, null,
					null, order);
			return ret;
		}
	}
	
	/**
	 * Fetches the last record registered (it will not necessarily be the newest one)
	 * */
	public Cursor fetchLastlength() throws SQLException {
		Cursor ret = db.query(DATABASE_TABLE_LOGS, new String[] {
				KEY_LENGTH},
					null, null, null,
					null, KEY_ROWID + " desc","1");
		if (ret != null) {
			ret.moveToFirst();
		}
		return ret;
	}

	/**
	 * @param rowId
	 * @param length
	 * @param date
	 * @return Boolean indicating if the update was successful
	 * Updates a Length row
	 */
	public boolean updateLength(long rowId, Float length, String date) {
		ContentValues vals = new ContentValues();
		vals.put(KEY_LENGTH, length);
		vals.put(KEY_DATE, date);
		return db.update(DATABASE_TABLE_LOGS, vals,KEY_ROWID+"="+rowId, null) > 0;
	}
	
	/**
	 * @param length
	 * @param date
	 * @param time
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 * Inserts a new record (date, time, length) into the DB
	 */
	public long insertLength(String date, String time, Float length) {
		ContentValues vals = new ContentValues();
		vals.put(KEY_LENGTH, length);
		vals.put(KEY_DATE, date);
		vals.put(KEY_TIME, time);
		return db.insert(DATABASE_TABLE_LOGS, null, vals);
	}

	/**
	 * @param rowId id of the row to be deleted <br>
	 * Deletes a length row matching the rowId param
	 * */
	public boolean deleteLength(long rowId) {
		return db.delete(DATABASE_TABLE_LOGS, KEY_ROWID+"="+rowId, null) > 0;
	}
	
	/**
	 * Deletes all the run_logs values
	 */
	public void deleteAllRuns(){
		db.delete(DATABASE_TABLE_LOGS, null, null);
	}
	
	/**
	 * Fetches the oldest record registered
	 * */
	public Cursor fetchOldestLength() throws SQLException {
		Cursor ret = db.query(DATABASE_TABLE_LOGS, new String[] {
				KEY_DATE},
					null, null, null,
					null, ASC_ORDER ,"1");
		if (ret != null) {
			ret.moveToFirst();
		}
		return ret;
	}
	
	//BEGIN Location Table methods
	/**
	 * 
	 * @param run_id
	 * @return Return the locations matching with the run_id param
	 */
	public Cursor getLocations(long run_id){
		Cursor ret = db.query(DATABASE_TABLE_LOCATIONS,
				new String[] {KEY_LATITUDE, KEY_LONGITUDE, KEY_RUNID, KEY_ROWID},
				KEY_RUNID+"="+run_id, null, null,
				null, KEY_ROWID);
		return ret;
	}
	
	/**
	 * @param run_Id
	 * @return Delete the Locations with the run_id param
	 */
	public boolean deleteLocations(long run_Id){
		int deletedRows = db.delete(DATABASE_TABLE_LOCATIONS, KEY_RUNID+"="+run_Id, null);
		Log.i("Database", "Deleted Rows "+ deletedRows);
		return  deletedRows > 0;
	}
	
	/**
	 * Deletes all unused Locations 
	 * @return the number of rows deleted
	 */
	public boolean deleteUnUsedLocations(){
		int deletedRows = db.delete(DATABASE_TABLE_LOCATIONS, KEY_RUNID+"="+KEY_NULL_RUNID, null);
		Log.i("Database", "Deleted Rows "+ deletedRows);
		return deletedRows > 0;
	}
	
	/**
	 * Deletes all the run_location values
	 */
	public void deleteAllLocations(){
		db.delete(DATABASE_TABLE_LOCATIONS, null, null);
	}
	
	/**
	 * Inserts a new Location row
	 * @param latitude
	 * @param longitude
	 * @return the row ID of the newly inserted row, or -1 if an error occurred <br>
	 */
	public long insertLocation(double latitude, double longitude) {
		ContentValues vals = new ContentValues();
		vals.put(KEY_LATITUDE, Double.valueOf(latitude));
		vals.put(KEY_LONGITUDE, Double.valueOf(longitude));
		vals.put(KEY_RUNID, KEY_NULL_RUNID);
		long rowId = db.insert(DATABASE_TABLE_LOCATIONS, null, vals);
		return rowId;
	}
	
	/**
	 * @param run_Id
	 * @return Boolean indicating if the update was successful
	 * Sets the run_id to last saved locations
	 */
	public boolean setRunIdToLastSavedLocations(long run_Id) {
		ContentValues vals = new ContentValues();
		vals.put(KEY_RUNID, run_Id);
		int updatedRows = db.update(DATABASE_TABLE_LOCATIONS, vals,KEY_RUNID+"="+KEY_NULL_RUNID, null);
		return updatedRows > 0;
	}
	
	/**
	 * Returns the number of all the rows in the run_locations Table
	 * @return long to know the number of rows in the table DATABASE_TABLE_LOCATIONS
	 */
	public long countAllRunLocations(){
		return DatabaseUtils.queryNumEntries(db, DATABASE_TABLE_LOCATIONS);
	}
	
	/**
	 * Returns the number of rows with the given run_id 
	 * @param run_id
	 * @return long to know the number of rows in the table DATABASE_TABLE_LOCATIONS with the given run_id
	 */
	public long countRunLocations(long run_id){
		SQLiteStatement s = db.compileStatement("select count(*) from " +DATABASE_TABLE_LOCATIONS+ " where run_id="+run_id);
		return s.simpleQueryForLong();
	}
	
	/**
	 * Returns if there are locations not saved with a valid run_id value
	 * @return true if exists locations not saved with a proper run_id
	 */
	public boolean existLocationsToBeSaved(){
		return countRunLocations(KEY_NULL_RUNID)>0;
	}
	
	//END Location Table methods

}

