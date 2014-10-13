package com.runLogger.activity.history;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.runLogger.R;
import com.runLogger.activity.location.MapLocationActivity;
import com.runLogger.activity.location.RunLocationsList;
import com.runLogger.activity.runEntry.ValueEntryActivity;
import com.runLogger.cursor.HistoryValuesCursorAdapter;
import com.runLogger.dao.DBLayer;
import com.runLogger.utils.UnitHandler;


/**
 * Presents the records of the history table
 * */
public class HistoryList extends ListActivity {
	
	private DBLayer mDbHelper;
    private Cursor myCursor;
    private int EDIT_HISTORY_ROW = 100;
    private SimpleCursorAdapter runs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.length_history);
		registerForContextMenu(getListView());
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
	@SuppressWarnings("static-access")
	private void fillData() {
        // Get all of the notes from the database and create the item list
		myCursor = mDbHelper.fetchAllLengths(mDbHelper.DESC_ORDER);
        startManagingCursor(myCursor);

        String[] from = new String[] { DBLayer.KEY_DATE, DBLayer.KEY_LENGTH };
        int[] to = new int[] { R.id.rowDate, R.id.rowLength };
        
        UnitHandler unitHandler = UnitHandler.getInstance(PreferenceManager.getDefaultSharedPreferences(this).getString("lengthUnit_list_preference", ""));
        
        // Now create an array adapter and set it to display using our row
        runs = new HistoryValuesCursorAdapter(this, R.layout.length_history_entry, myCursor, from, to, unitHandler);
        setListAdapter(runs);
    }
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.run_location_context_menu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    long id = HistoryList.this.getListView().getItemIdAtPosition(info.position);
	   
		switch (item.getItemId()) {
		case R.id.menu_show_route:
			if (mDbHelper.countRunLocations(id) > 0){
				Intent intent = new Intent(this, MapLocationActivity.class);
				intent.putExtra(DBLayer.KEY_RUNID, id);
				startActivity(intent);
			} else {
				Toast.makeText(HistoryList.this,
						getString(R.string.no_route_to_show), Toast.LENGTH_SHORT
						).show();
			}
			
			return true;
		case R.id.menu_show_locations:
			Intent i = new Intent(this, RunLocationsList.class);
	        i.putExtra(DBLayer.KEY_RUNID, id);
			startActivity(i);
			return true;	
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.history_menu,menu);
        return true;
    }
	
	public boolean onOptionsItemSelected (MenuItem item){
        switch (item.getItemId()){
            case R.id.clearHistoryOption:
            	AlertDialog.Builder builder = new AlertDialog.Builder(this);
            	builder.setMessage(getString(R.string.confirm_delete_history))
            	       .setCancelable(false)
            	       .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            	           public void onClick(DialogInterface dialog, int id) {
            	                HistoryList.this.mDbHelper.deleteAllRuns();
            	                HistoryList.this.finish();
            	           }
            	       })
            	       .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            	           public void onClick(DialogInterface dialog, int id) {
            	                dialog.cancel();
            	           }
            	       });
            	AlertDialog alert = builder.create();
            	alert.show();
            	return true;
            default:
            	return super.onOptionsItemSelected(item);
        }   
    }
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Cursor c = runs.getCursor();
        c.moveToPosition(position);
        Intent i = new Intent(this, ValueEntryActivity.class);
        i.putExtra(DBLayer.KEY_ROWID, id);
        i.putExtra(DBLayer.KEY_DATE, c.getString(
                c.getColumnIndexOrThrow(DBLayer.KEY_DATE)));
        i.putExtra(DBLayer.KEY_LENGTH, c.getFloat(
                c.getColumnIndexOrThrow(DBLayer.KEY_LENGTH)));
        startActivityForResult(i, EDIT_HISTORY_ROW);
	}
	
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDbHelper.close();
	}

}
