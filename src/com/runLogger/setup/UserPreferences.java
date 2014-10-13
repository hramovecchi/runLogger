package com.runLogger.setup;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.runLogger.R;

public class UserPreferences extends PreferenceActivity {
	
	SharedPreferences preferences;
	static final int PICK_DEFAULTRECIPIENTEMAIL_REQUEST = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.user_preferences);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Preference defaultRecipientEmailPreference = findPreference("default_recipient_email");
		defaultRecipientEmailPreference.setSummary("Currently established: "+preferences.getString("defaultRecipientEmail", "N/A"));
		defaultRecipientEmailPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                    //save the default selection to myselfPreferences
            	Intent intent = new Intent(UserPreferences.this, DefaultRecipientEmailSelection.class);
            	startActivityForResult(intent, PICK_DEFAULTRECIPIENTEMAIL_REQUEST);
                return true;
            }
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PICK_DEFAULTRECIPIENTEMAIL_REQUEST) {
			 if (resultCode == RESULT_OK) {
				 String emailAddress = data.getExtras().getString("emailAddress");
				 setDefaultRecipientEmail(emailAddress);
				 findPreference("default_recipient_email").setSummary("Currently established: "+emailAddress);
			 }
		}
	}
	private void setDefaultRecipientEmail(String email){
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("defaultRecipientEmail", email);	
		editor.commit();
	}
}
