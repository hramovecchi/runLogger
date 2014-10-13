package com.runLogger.activity.runEntry;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.runLogger.R;
import com.runLogger.dao.DBLayer;
import com.runLogger.utils.DateTimeFormatter;
import com.runLogger.utils.UnitHandler;
import com.runLogger.utils.dateSlider.DateSlider;
import com.runLogger.utils.dateSlider.DefaultDateSlider;
import com.runLogger.utils.dateSlider.DateSlider.OnDateSetListener;

/**
 * Activity that Handles the Confirmation of changes, and deletion, over the records of the history table
 */
public class ValueEntryActivity extends Activity {
	
	private Long rowId;
	protected EditText lengthEntry;
	protected final int DATE_DIALOG_ID = 101;
	protected DateSlider mDateSlider;
	protected Calendar selectedDate;
	protected Button mPickDate;
	protected String lengthSavedInBundle;
	protected String dateSavedInBundle;
	protected UnitHandler unitHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.length_entry);
		if (savedInstanceState != null){
			lengthSavedInBundle = savedInstanceState.getString("lengthEntry");
			selectedDate = (Calendar)savedInstanceState.getSerializable("selectedDate");
		}
		bindViewWithListeners();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		((TextView)findViewById(R.id.textView1)).setText("Length" + " (in "+PreferenceManager.getDefaultSharedPreferences(this).getString("lengthUnit_list_preference", "")+")");
		unitHandler = UnitHandler.getInstance(PreferenceManager.getDefaultSharedPreferences(this).getString("lengthUnit_list_preference", ""));   
        populateView();
	}
	
	@Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DATE_DIALOG_ID:
        	mDateSlider = getDateSlider();
        	return mDateSlider;
        }
        return null;
    }
	
	protected OnDateSetListener getOnDateSetListener(){
		return new DateSlider.OnDateSetListener() {
            public void onDateSet(DateSlider view, Calendar selectedDate) {
            	updateDate(selectedDate);
            };
		};
	}
	
	/**
    *  update the Date with the calendar object
    *  @param the calendar selected
    */ 
	protected void updateDate(Calendar calendar){
	   selectedDate = calendar;
	   mPickDate.setText(getDateStringValue(calendar));
	}
	
	protected String getDateStringValue(Calendar c){
	   	return DateTimeFormatter.getFormattedDate(c, DateTimeFormatter.DAY_MONTH_YEAR_ORDER);
	}
	
	protected void bindViewWithListeners(){
		lengthEntry = (EditText)findViewById(R.id.editWeight);
		mPickDate = (Button) findViewById(R.id.pickDate);
		mPickDate.setOnClickListener(getPickDateOnClickListener());
		findViewById(R.id.button1).setOnClickListener(getSaveOnClickListener());
		findViewById(R.id.button2).setOnClickListener(getDeleteRowOnClickListener());
	}
	
	protected void populateView(){
		Bundle extras = getIntent().getExtras();
		
		//We search the actual values into the extras, and then we show them
        if (extras != null) {
        	rowId = null;
            String yearMonthDay[] = extras.getString(DBLayer.KEY_DATE).split("-");
            Float weight = extras.getFloat(DBLayer.KEY_LENGTH);
            rowId = extras.getLong(DBLayer.KEY_ROWID);
            
            if (selectedDate == null) {
            	selectedDate = Calendar.getInstance();
            	selectedDate.set(Integer.valueOf(yearMonthDay[0]), Integer.valueOf(yearMonthDay[1])-1, 
            			Integer.valueOf(yearMonthDay[2]));	
            }
            mPickDate.setText(DateTimeFormatter.getFormattedDate(selectedDate, DateTimeFormatter.DAY_MONTH_YEAR_ORDER));
            
            if (lengthSavedInBundle == null)
            	lengthEntry.setText(unitHandler.getValueToDisplay(weight).toString());
            else 
            	lengthEntry.setText(lengthSavedInBundle);
        }
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("lengthEntry", lengthEntry.getText().toString());
		outState.putSerializable("selectedDate", selectedDate);
	}
	
	/**
     * @return the DateSlider needed in the activity
     */
	protected DateSlider getDateSlider(){
		return new DefaultDateSlider(this,getOnDateSetListener(),selectedDate);
	}
	
	protected OnClickListener getPickDateOnClickListener(){
		return new OnClickListener() {
			public void onClick(View arg0) {
				 //call the internal showDialog method using the predefined ID
				 showDialog(DATE_DIALOG_ID);
			 }        	
		 };
	}
	
    @Override
    protected void onPause() {
    	super.onPause();
    	lengthSavedInBundle = lengthEntry.getText().toString();
    }
	
	private OnClickListener getSaveOnClickListener(){
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				String weightValue = lengthEntry.getText().toString();
				if (weightValue.length()>0 && (Float.valueOf(weightValue).compareTo(Float.valueOf(0)) > 0)){
					DBLayer mDbHelper = new DBLayer(ValueEntryActivity.this);
		            mDbHelper.open();
		            mDbHelper.updateLength(rowId, unitHandler.getValueToStore(Float.valueOf(weightValue)), 
		               DateTimeFormatter.getFormattedDate(selectedDate, DateTimeFormatter.YEAR_MONTH_DAY_ORDER));
		            mDbHelper.close();
		            Toast.makeText(ValueEntryActivity.this,getString(R.string.row_successfully_updated),Toast.LENGTH_LONG).show();
		            ValueEntryActivity.this.finish();
				} else {
					Toast.makeText(ValueEntryActivity.this,
		      				getString(R.string.insert_valid_length_value),
		      				Toast.LENGTH_LONG).show();
				}
			}
		};
	}
	
	private OnClickListener getDeleteRowOnClickListener(){
		return new OnClickListener(){
			@Override
			public void onClick(View v){
				AlertDialog.Builder builder = new AlertDialog.Builder(ValueEntryActivity.this);
            	builder.setMessage(getString(R.string.confirm_delete_entry))
            	       .setCancelable(false)
            	       .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            	           public void onClick(DialogInterface dialog, int id) {
            	        	   DBLayer mDbHelper = new DBLayer(ValueEntryActivity.this);
            	               mDbHelper.open();
            	               mDbHelper.deleteLength(rowId);
            	               
            	               //Delete locations with this run_id if exists
            	               mDbHelper.deleteLocations(rowId);
            	               mDbHelper.close();
            	               ValueEntryActivity.this.finish();
            	           }
            	       })
            	       .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            	           public void onClick(DialogInterface dialog, int id) {
            	                dialog.cancel();
            	           }
            	       });
            	AlertDialog alert = builder.create();
            	alert.show();
			}
		};
	}
}
