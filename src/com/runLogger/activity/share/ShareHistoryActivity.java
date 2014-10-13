package com.runLogger.activity.share;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.runLogger.R;
import com.runLogger.cursor.HistoryValuesCursorAdapter;
import com.runLogger.dao.DBLayer;
import com.runLogger.utils.DateTimeFormatter;
import com.runLogger.utils.UnitHandler;
import com.runLogger.utils.dateSlider.DateSlider;
import com.runLogger.utils.dateSlider.DateSlider.OnDateSetListener;
import com.runLogger.utils.dateSlider.DefaultDateSlider;

/**
 * Manages the sending of email's
 * */
public class ShareHistoryActivity extends Activity {
	
	private Button pickDateButton;
	private Spinner contactSpinner;
	private Button sendButton;
	private final int DATE_DIALOG_ID = 101;
	private final int SEND_EMAIL = 102;
	private DateSlider mDateSlider;
	private Calendar selectedDate;
	private ListView list;
	private SharedPreferences preferences;
	private String emailAddress;
	private OnItemSelectedListener contactSpinnerOnItemSelectedListener;
	private ArrayList<String> spinnerArray;
	
	private static final int CONTACT_PICKER_RESULT = 1001;
	private static final int ADD_CONTACT_RESULT = 1002;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_lenght);
		
		if (savedInstanceState != null){
			selectedDate = (Calendar)savedInstanceState.getSerializable("selectedDate");
			spinnerArray = savedInstanceState.getStringArrayList("spinnerArray");
		}
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		pickDateButton = (Button) findViewById(R.id.pickDate);
		pickDateButton.setOnClickListener(getPickDateOnClickListener());
		contactSpinner = (Spinner) findViewById(R.id.emailSpinner);
		list = (ListView)findViewById(R.id.shareLengthlist);
		sendButton = (Button) findViewById(R.id.sendButton);
		sendButton.setOnClickListener(getSendButtonOnClickListener());
		populateDateButton();
		populateContactsSpinner(getSpinnerArray());
	}
	
	private OnClickListener getPickDateOnClickListener(){
    	return new OnClickListener() {
			public void onClick(View arg0) {
				// call the internal showDialog method using the predefined ID
				showDialog(DATE_DIALOG_ID);
			}        	
        };
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
	
	protected DateSlider getDateSlider(){
		return new DefaultDateSlider(this,getOnDateSetListener(),selectedDate);
	}
	
	private OnDateSetListener getOnDateSetListener(){
		return new DateSlider.OnDateSetListener() {
            public void onDateSet(DateSlider view, Calendar selectedDate) {
            	// update the dateText view with the corresponding date
            	ShareHistoryActivity.this.updateDateDisplay(selectedDate);
            };
		};
	}
	
	private void updateDateDisplay(Calendar calendar){
	    selectedDate = calendar;
	    pickDateButton.setText(getDateStringValue(calendar));
	    populateList();
	}
	
	private String getDateStringValue(Calendar c){
    	return DateTimeFormatter.getFormattedDate(c, DateTimeFormatter.DAY_MONTH_YEAR_ORDER);
	}
	
	private OnClickListener getSendButtonOnClickListener(){
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				String body = getEmailBody();
    			
    			final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
    			emailIntent.setType("plain/text");
    			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{emailAddress}); //address
    			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, ShareHistoryActivity.this.getString(R.string.mailSubject)); 	//subject
    			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);							//body
    			ShareHistoryActivity.this.startActivityForResult(Intent.createChooser(emailIntent, "Send mail..."),SEND_EMAIL);
			}
		};
	}
	
	/**
	 * @return String containing the body of the email to send
	 */
	private String getEmailBody() {
		StringBuilder body = new StringBuilder();
		DBLayer dbLayer = new DBLayer(ShareHistoryActivity.super.getApplication().getBaseContext());
    	dbLayer.open();
    	Cursor cursor = dbLayer.fetchLengthAfterDate(DateTimeFormatter.getFormattedDate(selectedDate, DateTimeFormatter.YEAR_MONTH_DAY_ORDER), DBLayer.DESC_ORDER);
		if (cursor.moveToFirst()) {
			String unit_selection = preferences.getString("lengthUnit_list_preference", "");
			UnitHandler unitHandler = UnitHandler.getInstance(unit_selection);
			do {
				String length = cursor.getString(cursor.getColumnIndexOrThrow(DBLayer.KEY_LENGTH));
				body.append("Date: " + DateTimeFormatter.toDDMMYYYYFormat(cursor.getString(cursor.getColumnIndexOrThrow(DBLayer.KEY_DATE)))
						+" | Time: " + cursor.getString(cursor.getColumnIndexOrThrow(DBLayer.KEY_TIME))
						+" | Length: " + (unitHandler.getValueToDisplay(Float.valueOf(length))).toString() + " " + unit_selection
						+ "\n");
			} while (cursor.moveToNext());
		}
		cursor.close();
		dbLayer.close();
		return body.toString();
	}
	
	/**
	 * Set up the date with the oldest date at the length history
	 */
	private void populateDateButton(){
		if (selectedDate == null){
			DBLayer dbLayer = new DBLayer(this);
	    	dbLayer.open();
	    	Cursor c = dbLayer.fetchOldestLength();
	    	selectedDate = Calendar.getInstance();
	    	if (c.moveToFirst()) {
		    	String yearMonthDay[] = c.getString(c.getColumnIndexOrThrow(DBLayer.KEY_DATE)).split("-");
		    	selectedDate.set(Integer.valueOf(yearMonthDay[0]), Integer.valueOf(yearMonthDay[1])-1, 
		    			Integer.valueOf(yearMonthDay[2]));
	    	}
	    	c.close();
	    	dbLayer.close();
		}
		updateDateDisplay(selectedDate);
	}
	
	/**
	 * Set up the contacts spinner with the available options
	 */
	private void populateContactsSpinner(ArrayList<String> spinnerArray){
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
		contactSpinner.setAdapter(spinnerArrayAdapter);
		contactSpinner.setOnItemSelectedListener(getContactSpinnerOnItemSelectedListener());
	}
	
	/**
	 * Gets the OnItemSelectedListener for the contact spinner
	 * @return OnItemSelectedListener
	 */
	private OnItemSelectedListener getContactSpinnerOnItemSelectedListener(){
		if (contactSpinnerOnItemSelectedListener == null){
			contactSpinnerOnItemSelectedListener =  new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
			    	String pickedEmail = spinnerArray.get(position);
			        if (pickedEmail.equals(getString(R.string.new_contact))){
			        	Intent addContactIntent = new Intent(ContactsContract.Intents.Insert.ACTION, 
			        			   ContactsContract.Contacts.CONTENT_URI);
			        	   startActivityForResult(addContactIntent, ADD_CONTACT_RESULT);
			        } else if(pickedEmail.equals(getString(R.string.from_contact))){
			        	Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,  
			       	    		ContactsContract.Contacts.CONTENT_URI);
			        	   startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
			        }else {
			        	emailAddress = pickedEmail;
			        }
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		    }
		};
		}
		return contactSpinnerOnItemSelectedListener;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK){
			final String userEmail = getEmailFromContact(data.getData());
			
			if (requestCode == ADD_CONTACT_RESULT ||  requestCode == CONTACT_PICKER_RESULT){
		    	if (userEmail.length()>0){
		    		AlertDialog.Builder builder = new AlertDialog.Builder(ShareHistoryActivity.this);
	            	builder.setMessage("Do you want to save this user's email as the default recipient email?")
	            	       .setCancelable(false)
	            	       .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
	            	           public void onClick(DialogInterface dialog, int id) {
	            	        	   spinnerArray.remove(preferences.getString("defaultRecipientEmail", ""));
	            	        	   
	            	        	   SharedPreferences.Editor editor = preferences.edit();
	            	        	   editor.putString("defaultRecipientEmail", userEmail);	
	            	        	   editor.commit();
	            	        	   
	            	        	   spinnerArray.add(0, userEmail);
            	        		   populateContactsSpinner(spinnerArray);
	            	           }
	            	       })
	            	       .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
	            	           public void onClick(DialogInterface dialog, int id) {
	            	        	   if (!spinnerArray.contains(userEmail)){
	            	        		   spinnerArray.add(0, userEmail);
	            	        		   populateContactsSpinner(spinnerArray);
	            	        	   }
	            	        	   dialog.cancel();
	            	           }
	            	       });
	            	AlertDialog alert = builder.create();
	            	alert.show();
		        } else
		        	Toast.makeText(ShareHistoryActivity.this,
		        			getString(R.string.user_without_email),
		       				Toast.LENGTH_LONG).show();
			}	
		}
		populateContactsSpinner(getSpinnerArray());
	}
	
	private String getEmailFromContact(Uri contactData){
		String emailsAddress = "";
        Cursor c =  managedQuery(contactData, null, null, null, null);
        
        if (c.moveToFirst()) {
          String contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
          Cursor emails = getContentResolver().query(
        		  ContactsContract.CommonDataKinds.Email.CONTENT_URI,
        		  null, 
        		  ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId, null, null); 
          while (emails.moveToNext()) { 
             emailsAddress += emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))+", ";
          }
          emails.close();
        }
        c.close();
        if (emailsAddress.length()==0)
        	return emailsAddress;
        return emailsAddress.substring(0, emailsAddress.length()-2);
	}
	
	private ArrayList<String> getSpinnerArray(){
		if (spinnerArray == null){
			spinnerArray = new ArrayList<String>();
			spinnerArray.add(preferences.getString("editmail_preference", ""));
			if (!preferences.getString("editmail_preference", "").equals(preferences.getString("defaultRecipientEmail", "")))
				spinnerArray.add(preferences.getString("defaultRecipientEmail", ""));
			
			spinnerArray.add(getString(R.string.from_contact));
			spinnerArray.add(getString(R.string.new_contact));
		}
		return spinnerArray;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("selectedDate", selectedDate);
		outState.putStringArrayList("spinnerArray", spinnerArray);
	}
	
	private void populateList(){
		String[] from = new String[] { DBLayer.KEY_DATE, DBLayer.KEY_LENGTH };
	    int[] to = new int[] { R.id.rowDate, R.id.rowLength };
	        
		DBLayer dbLayer = new DBLayer(ShareHistoryActivity.super.getApplication().getBaseContext());
    	dbLayer.open();
    	Cursor cursor = dbLayer.fetchLengthAfterDate(DateTimeFormatter.getFormattedDate(selectedDate, DateTimeFormatter.YEAR_MONTH_DAY_ORDER), DBLayer.DESC_ORDER);
    	
    	UnitHandler unitHandler = UnitHandler.getInstance(PreferenceManager.getDefaultSharedPreferences(this).getString("lengthUnit_list_preference", ""));
         
        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter values = new HistoryValuesCursorAdapter(this, R.layout.length_history_entry, cursor, from, to, unitHandler);
        list.setAdapter(values);
        
        dbLayer.close();
	}
}
