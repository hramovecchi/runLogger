package com.runLogger.activity.runEntry;

import java.util.Calendar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.runLogger.R;
import com.runLogger.activity.history.HistoryList;
import com.runLogger.activity.location.LocationActivity;
import com.runLogger.activity.share.ShareHistoryActivity;
import com.runLogger.dao.DBLayer;
import com.runLogger.setup.UserPreferences;
import com.runLogger.utils.DateTimeFormatter;
import com.runLogger.utils.dateSlider.DateSlider;
import com.runLogger.utils.dateSlider.DateTimeSlider;

/**
 * This Activity receives the weight and date entered by the user and commit them to the DB
 */
public class NewValueEntryActivity extends ValueEntryActivity {
	
    private Button mSaveButton;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	if (!isSetupDone()){
    		Intent intent = new Intent(this, UserPreferences.class);
        	startActivity(intent);
        	Toast.makeText(NewValueEntryActivity.this,
        			"Please setup the environment first",
       				Toast.LENGTH_LONG).show();
    	}
    }
    
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }
    
    public boolean onOptionsItemSelected (MenuItem item){
        switch (item.getItemId()){
            case R.id.menuSetup:
            	Intent setupIntent = new Intent(NewValueEntryActivity.this, UserPreferences.class);
            	startActivity(setupIntent);
            	return true;
            case R.id.menuHistory:
            	Intent historyIntent = new Intent(NewValueEntryActivity.this, HistoryList.class);
            	startActivity(historyIntent);
            	return true;
            case R.id.menuShare:
            	Intent shareIntent = new Intent(NewValueEntryActivity.this, ShareHistoryActivity.class);
            	startActivity(shareIntent);
            	return true;
            case R.id.menuRun:
            	Intent runIntent = new Intent(NewValueEntryActivity.this, LocationActivity.class);
            	startActivity(runIntent);
            	return true;
            default:
            	return super.onOptionsItemSelected(item);
        }   
    }
    
    protected String getDateStringValue(Calendar c){
    	return DateTimeFormatter.getFormattedDate(c, DateTimeFormatter.DAY_MONTH_YEAR_ORDER)+
    		" "+DateTimeFormatter.getFormattedTime(c);
    }
    
    @Override
    protected void bindViewWithListeners(){
    	// capture our View elements
    	lengthEntry = (EditText)findViewById(R.id.editWeight);
        mPickDate = (Button) findViewById(R.id.pickDate);
        mSaveButton = (Button) findViewById(R.id.button1);
        ((LinearLayout)findViewById(R.id.buttonLayout)).removeView(findViewById(R.id.button2));
   
        mPickDate.setOnClickListener(getPickDateOnClickListener());
        mSaveButton.setOnClickListener(getSaveOnClickListener()); 
        
        if (selectedDate == null)
        	updateDate(Calendar.getInstance());
        else
        	updateDate(selectedDate);
    }
    
    protected void populateView(){
    	if(lengthSavedInBundle == null){
	    	DBLayer dbLayer = new DBLayer(this);
	    	dbLayer.open();
	    	Cursor lastWeightCursor = dbLayer.fetchLastlength();
	    	//if there is a weight already registered, we present it to the user
	    	if (lastWeightCursor.moveToFirst()) {
	    		Float weight = lastWeightCursor.getFloat( lastWeightCursor.getColumnIndexOrThrow(DBLayer.KEY_LENGTH));
	    		lengthEntry.setText(unitHandler.getValueToDisplay(weight).toString());
	    	} else {
	    		lengthEntry.setText("");
	    	}
	    	lastWeightCursor.close();
	    	dbLayer.close();
    	} else
    		lengthEntry.setText(lengthSavedInBundle);
    }
    
    /**
     * @return the DateSlider needed in the activity
     */
    protected DateSlider getDateSlider(){
		return new DateTimeSlider(this,getOnDateSetListener(),selectedDate);
	}
    
    /**
     * @return true when the email, the default recipient and the weigh has been settled
     */
    private boolean isSetupDone(){
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    	return !preferences.getString("editmail_preference", "").equals("") && 
    				!preferences.getString("defaultRecipientEmail", "").equals("") &&
    					!preferences.getString("lengthUnit_list_preference", "").equals("");
    }
    
    private OnClickListener getSaveOnClickListener(){
    	return new OnClickListener() {
			@Override
			public void onClick(View v) {
				String lengthValue = lengthEntry.getText().toString();
				if (lengthValue.length()>0 && (Float.valueOf(lengthValue).compareTo(Float.valueOf(0)) > 0)){
					DBLayer dbLayer = new DBLayer(NewValueEntryActivity.this);
			    	dbLayer.open();
					long result = dbLayer.insertLength(DateTimeFormatter.getFormattedDate(selectedDate, DateTimeFormatter.YEAR_MONTH_DAY_ORDER), 
							DateTimeFormatter.getFormattedTime(selectedDate),unitHandler.getValueToStore(Float.valueOf(lengthValue)));
					dbLayer.close();
					if (result!=-1){
						Toast.makeText(NewValueEntryActivity.this,
			      				"Values successfully committed.",
			      				Toast.LENGTH_LONG).show();
						lengthSavedInBundle = null;
						NewValueEntryActivity.this.onResume();
					}
				} else {
					Toast.makeText(NewValueEntryActivity.this,
		      				"Insert a valid length value.",
		      				Toast.LENGTH_LONG).show();
				}
			}
		};
    }
}
