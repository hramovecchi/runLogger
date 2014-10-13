package com.runLogger.activity.location;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.runLogger.R;
import com.runLogger.dao.DBLayer;
import com.runLogger.service.LocationService;
import com.runLogger.utils.DateTimeFormatter;
import com.runLogger.utils.UnitHandler;


public class LocationActivity extends Activity{
	
	Messenger mService = null;
	
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	Float distance = null;

	private String lengthUnit;
	private UnitHandler unitHandler;
	private boolean serviceStarted = false;
	
	class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case LocationService.MSG_DISTANCE_UPDATED:
                distance = msg.getData().getFloat("distance")/1000;
                
                ((TextView)findViewById(R.id.distance)).setText(getString(R.string.distance) + unitHandler.getValueToDisplay(distance).toString()+ " "+lengthUnit);
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location);
		
		lengthUnit = PreferenceManager.getDefaultSharedPreferences(LocationActivity.this).getString("lengthUnit_list_preference", "");
    	unitHandler = UnitHandler.getInstance(lengthUnit);
		
		findViewById(R.id.startButton).setOnClickListener(startButtonListener);
		
		findViewById(R.id.stopButton).setOnClickListener(stopButtonListener);
		findViewById(R.id.stopButton).setEnabled(false);
		
		findViewById(R.id.saveButton).setOnClickListener(saveButtonListener);
		findViewById(R.id.saveButton).setEnabled(false);
	}
	
	void doBindService() {
		serviceStarted = true;
        bindService(new Intent(this, LocationService.class), mConnection, Context.BIND_AUTO_CREATE);
    }
	
    void doUnbindService() {
    	serviceStarted = false;
        // If we have received the service, and hence registered with it, then now is the time to unregister.
    	if (mService != null) {
    		try {
    			Message msg = Message.obtain(null, LocationService.MSG_UNREGISTER_CLIENT);
    			msg.replyTo = mMessenger;
    			mService.send(msg);
    		} catch (RemoteException e) {
    			// There is nothing special we need to do if the service has crashed.
    		}
    	}
    	// Detach our existing connection.
    	unbindService(mConnection);
    }
    
    @Override
    protected void onDestroy() {
    	if (serviceStarted){
	    	doUnbindService();
	    	stopService(new Intent(LocationActivity.this, LocationService.class));
    	}
    	super.onDestroy();
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null, LocationService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
        }
    };
	
	private OnClickListener startButtonListener = new OnClickListener() {
		public void onClick(View v) {
			findViewById(R.id.startButton).setEnabled(false);
			findViewById(R.id.saveButton).setEnabled(false);
			findViewById(R.id.stopButton).setEnabled(true);
			
			//Delete unused locations if exists
			DBLayer dbLayer = new DBLayer(LocationActivity.this);
	    	dbLayer.open();
	    	dbLayer.deleteUnUsedLocations();
	    	dbLayer.close();
			
			startService(new Intent(LocationActivity.this, LocationService.class));
			doBindService();
			
			Toast.makeText(LocationActivity.this, 
					"Starting listening to GPS", 
					Toast.LENGTH_SHORT).show();
		}
	};
	
	private OnClickListener stopButtonListener = new OnClickListener() {
		public void onClick(View v) {
			findViewById(R.id.stopButton).setEnabled(false);
			findViewById(R.id.startButton).setEnabled(true);
			
			DBLayer dbLayer = new DBLayer(LocationActivity.this);
	    	dbLayer.open();
			
	    	//enable the save button only if exist locations to be saved
			findViewById(R.id.saveButton).setEnabled(dbLayer.existLocationsToBeSaved());
			
			dbLayer.close();
			
			doUnbindService();
			stopService(new Intent(LocationActivity.this, LocationService.class));
			
			Toast.makeText(LocationActivity.this, 
					"Ending listening to GPS", 
					Toast.LENGTH_SHORT).show();
		}
	};
	
	private OnClickListener saveButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			new AlertDialog.Builder(LocationActivity.this)
			.setTitle(getString(R.string.save_confirm_title))
			.setMessage(getString(R.string.save_confirm_dialog))
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			    public void onClick(DialogInterface dialog, int whichButton) {
			    	findViewById(R.id.saveButton).setEnabled(false);
			    	
			        // save the values in the DATABASE_TABLE_LOGS
			    	Calendar now = Calendar.getInstance();
			    	String date = DateTimeFormatter.getFormattedDate(now, DateTimeFormatter.YEAR_MONTH_DAY_ORDER);
			    	String time = DateTimeFormatter.getFormattedTime(now);
			    	
			    	DBLayer dbLayer = new DBLayer(LocationActivity.this);
			    	dbLayer.open();
					long run_Id = dbLayer.insertLength(date, 
							time,unitHandler.getValueToStore(distance));
					
					// update the last locations with the run_id in the DATABASE_TABLE_LOCATIONS
					if (run_Id!=-1){
						dbLayer.setRunIdToLastSavedLocations(run_Id);
					}
					dbLayer.close();
					
					LocationActivity.this.finish();
			    }})
			 .setNegativeButton(android.R.string.no, null).show();
		}
	};

}
