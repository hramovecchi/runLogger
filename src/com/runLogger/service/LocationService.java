package com.runLogger.service;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.runLogger.R;
import com.runLogger.activity.location.LocationActivity;
import com.runLogger.listener.MyLocationListener;

public class LocationService extends Service {
	
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_DISTANCE_UPDATED = 3;

	private NotificationManager notificationManager;
	ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
	final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.
	
	private MyLocationListener mlocListener;
	private LocationManager mlocManager;
	
	private static boolean isRunning;

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}
	
	class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_REGISTER_CLIENT:
                mClients.add(msg.replyTo);
                break;
            case MSG_UNREGISTER_CLIENT:
                mClients.remove(msg.replyTo);
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }
	
	public void sendMessageToUI(Float distance) {
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                //Send data as a String
            	Bundle b = new Bundle();
                b.putFloat("distance", distance);
                Message msg = Message.obtain(null, MSG_DISTANCE_UPDATED);
                msg.setData(b);
                mClients.get(i).send(msg);

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }
	
	public void onCreate() {
        super.onCreate();
        Log.i("MyService", "Service Started.");
        showNotification();
        isRunning = true;
        
        /* Use the LocationManager class to obtain GPS locations */
		mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		mlocListener = new MyLocationListener(this);

		mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
    }
	
	private void showNotification() {
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        CharSequence text = getText(R.string.service_started);
        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.icon, text, System.currentTimeMillis());
        
        Intent i = new Intent(this,LocationActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);
        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.service_label), text, contentIntent);
        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        notificationManager.notify(R.string.service_started, notification);
    }
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		 Log.i("MyService", "Received start id " + startId + ": " + intent);
		 return START_STICKY; // run until explicitly stopped.
	}
	
	public static boolean isRunning(){
        return isRunning;
    }
	
	@Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(R.string.service_started); // Cancel the persistent notification.
        Log.i("MyService", "Service Stopped.");
        isRunning = false;
        
		mlocManager.removeUpdates(mlocListener);
    }

}
