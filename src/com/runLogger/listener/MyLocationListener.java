package com.runLogger.listener;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.runLogger.dao.DBLayer;
import com.runLogger.service.LocationService;

public class MyLocationListener implements LocationListener {
	
	private Location lastLocation = null;
	private Float distanceInMeters = Float.valueOf(0);
	private DBLayer dbLayer = null;
	private LocationService locService;
	
	public MyLocationListener(LocationService locService){
		this.dbLayer = new DBLayer(locService);
		this.locService = locService;
	}

	@Override
	public void onLocationChanged(Location location) {
		if (lastLocation == null){
			lastLocation = location;
			
			//save Location in the database if lastLocation != null
			dbLayer.open();
			dbLayer.insertLocation(location.getLatitude(), location.getLongitude());
			dbLayer.close();
		} else {
			distanceInMeters += lastLocation.distanceTo(location);
			lastLocation = location;
			
			//send the message to update the UI
			locService.sendMessageToUI(distanceInMeters);
			
			//save location in the database
			dbLayer.open();
			dbLayer.insertLocation(location.getLatitude(), location.getLongitude());
			dbLayer.close();
		}
		
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

}
