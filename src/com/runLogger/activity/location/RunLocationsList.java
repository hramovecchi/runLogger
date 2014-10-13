package com.runLogger.activity.location;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

import com.runLogger.R;
import com.runLogger.cursor.RunLocationsCursorAdapter;
import com.runLogger.dao.DBLayer;


/**
 * Presents the records of the history table
 * */
public class RunLocationsList extends ListActivity {
	
	private DBLayer mDbHelper;
    private Cursor myCursor;
    private SimpleCursorAdapter locations;
    private Long runId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.run_locations);
		mDbHelper = new DBLayer(this);
        mDbHelper.open();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		fillData();
	}
	
	/**
     * Fetches all the records from DB and fills the history table with that info 
     * */
	private void fillData() {
        // Get all of the notes from the database and create the item list
		Bundle extras = getIntent().getExtras();
		
		//We search the actual values into the extras, and then we show them
        if (extras != null) {
        	runId = extras.getLong(DBLayer.KEY_RUNID);
        }
		myCursor = mDbHelper.getLocations(runId);
        startManagingCursor(myCursor);

        String[] from = new String[] { DBLayer.KEY_LATITUDE, DBLayer.KEY_LONGITUDE };
        int[] to = new int[] { R.id.rowLatitude, R.id.rowLongitude };
        
        // Now create an array adapter and set it to display using our row
        locations = new RunLocationsCursorAdapter(this, R.layout.run_locations_entry, myCursor, from, to);
        setListAdapter(locations);
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDbHelper.close();
	}

}
