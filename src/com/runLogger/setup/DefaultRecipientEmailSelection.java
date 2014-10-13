package com.runLogger.setup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.widget.Toast;

import com.runLogger.R;

/**
 * This Activity returns the selected email to the activity who calls it
 * the email can be from the user's email setup, from contacts, or from a new user created
 */
public class DefaultRecipientEmailSelection extends PreferenceActivity {
	
	SharedPreferences preferences;
	private static final int CONTACT_PICKER_RESULT = 1001;
	private static final int ADD_CONTACT_RESULT = 1002;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 // Load the preferences from an XML resource
       addPreferencesFromResource(R.xml.default_recipient_email_selection);
       preferences = PreferenceManager.getDefaultSharedPreferences(this);
       setOnPreferenceClicListeners();
	}
	
	private void setOnPreferenceClicListeners(){
		Preference myselfSelection = findPreference("myselfpreference");
	       myselfSelection.setOnPreferenceClickListener(new OnPreferenceClickListener() {
	           public boolean onPreferenceClick(Preference preference) {
	        	   Intent i = new Intent();
	        	   i.putExtra("emailAddress", preferences.getString("editmail_preference", "N/A"));
	        	   setResult(RESULT_OK, i);
	        	   finish();
	           return true;
	           }
	       });
	       Preference fromContactSelection = findPreference("fromcontactspreferences");
	       fromContactSelection.setOnPreferenceClickListener(new OnPreferenceClickListener() {
	           public boolean onPreferenceClick(Preference preference) {
	        	   Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,  
	       	    		ContactsContract.Contacts.CONTENT_URI);
	        	   startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);  
	           return true;
	           }
	       });
	       Preference newContactSelection = findPreference("createnewcontactpreference");
	       newContactSelection.setOnPreferenceClickListener(new OnPreferenceClickListener() {
	           public boolean onPreferenceClick(Preference preference) {
	        	   Intent addContactIntent = new Intent(ContactsContract.Intents.Insert.ACTION, 
	        			   ContactsContract.Contacts.CONTENT_URI);
	        	   startActivityForResult(addContactIntent, ADD_CONTACT_RESULT);
	           return true;
	           }
	       });
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK){
			String emailAddress = getEmailFromContact(data.getData());
			switch (requestCode) {
		    case (CONTACT_PICKER_RESULT):
		    	if (emailAddress.length()>0){
		    		setResult(RESULT_OK, new Intent().putExtra("emailAddress", emailAddress));
		    	} else
		    		 Toast.makeText(DefaultRecipientEmailSelection.this,
		       				"User selected don't have an email",
		       				Toast.LENGTH_LONG).show();
		   	break;
		    case (ADD_CONTACT_RESULT):
		    	if (emailAddress.length()>0){
		    		setResult(RESULT_OK, new Intent().putExtra("emailAddress", emailAddress));
		        } else
		        	Toast.makeText(DefaultRecipientEmailSelection.this,
		       				"User created don't have an email",
		       				Toast.LENGTH_LONG).show();
			}	
			finish();
		}
	}
	
	private String getEmailFromContact(Uri contactData){
		String emailsAddress = "";
        Cursor c =  managedQuery(contactData, null, null, null, null);
        
        if (c.moveToFirst()) {
          String contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
          Cursor emails = getContentResolver().query(
        		  ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, 
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
}
