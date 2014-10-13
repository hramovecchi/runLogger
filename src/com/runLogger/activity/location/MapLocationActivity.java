package com.runLogger.activity.location;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.runLogger.R;
import com.runLogger.dao.DBLayer;

public class MapLocationActivity extends android.support.v4.app.FragmentActivity {
	
    private GoogleMap mMap;
	private long runId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
        	runId = extras.getLong(DBLayer.KEY_RUNID);
        }
        
        setUpMapIfNeeded();
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}
	
	private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }
	
	private void setUpMap() {
		DBLayer mDbHelper = new DBLayer(this);
        mDbHelper.open();
		Cursor myCursor = mDbHelper.getLocations(runId);
        
        PolylineOptions plo = new PolylineOptions();
        plo.color(Color.BLUE);
        plo.width(5);
		
		for (boolean hasItem = myCursor.moveToFirst(); hasItem; hasItem = myCursor.moveToNext()) {
			Float lat = myCursor.getFloat( myCursor.getColumnIndexOrThrow(DBLayer.KEY_LATITUDE));
			Float lng = myCursor.getFloat( myCursor.getColumnIndexOrThrow(DBLayer.KEY_LONGITUDE));
			plo.add(new LatLng(lat, lng));
		}
		
		 myCursor.close();
	     mDbHelper.close();
		
		if (plo.getPoints().size() > 0){
			mMap.addPolyline(plo);
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
					new LatLng(plo.getPoints().get(plo.getPoints().size()-1).latitude, plo.getPoints().get(plo.getPoints().size()-1).longitude), Float.valueOf(15)));
			
			//start marker
			mMap.addMarker(new MarkerOptions().position(new LatLng(plo.getPoints().get(0).latitude, plo.getPoints().get(0).longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("Start"));
			
			//end marker
			mMap.addMarker(new MarkerOptions().position(new LatLng(plo.getPoints().get(plo.getPoints().size()-1).latitude, plo.getPoints().get(plo.getPoints().size()-1).longitude)).title("End"));
		}
    }
	

}
