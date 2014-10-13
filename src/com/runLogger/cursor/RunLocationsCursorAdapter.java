package com.runLogger.cursor;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.runLogger.R;
import com.runLogger.dao.DBLayer;

/**
 * Cursor to manage the GUI in the Run Locations List Activity
 */
public class RunLocationsCursorAdapter extends SimpleCursorAdapter {
	
	private int layout;

	public RunLocationsCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		
		this.layout = layout;
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		
		Cursor c = getCursor();
		final LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(layout, parent, false);
		
		int latitudeRow = c.getColumnIndex(DBLayer.KEY_LATITUDE);
		Float latitude = c.getFloat(latitudeRow);
		
		TextView latitudeTextView = (TextView) v.findViewById(R.id.rowLatitude);
		if (latitudeTextView != null) {
			latitudeTextView.setText(latitude.toString());
		}
		 
		int longitudeRow = c.getColumnIndex(DBLayer.KEY_LONGITUDE);
		Float longitude = c.getFloat(longitudeRow);
		
		TextView longitudeTextView = (TextView) v.findViewById(R.id.rowLongitude);
		if (longitudeTextView != null) {
			longitudeTextView.setText(longitude.toString());
		}
		
		return v;
	}
	
	 @Override
	 public void bindView(View v, Context context, Cursor c) {
		int latitudeRow = c.getColumnIndex(DBLayer.KEY_LATITUDE);
		Float latitude = c.getFloat(latitudeRow);
		
		TextView latitudeTextView = (TextView) v.findViewById(R.id.rowLatitude);
		if (latitudeTextView != null) {
			latitudeTextView.setText(latitude.toString());
		}
		 
		int longitudeRow = c.getColumnIndex(DBLayer.KEY_LONGITUDE);
		Float longitude = c.getFloat(longitudeRow);
		
		TextView longitudeTextView = (TextView) v.findViewById(R.id.rowLongitude);
		if (longitudeTextView != null) {
			longitudeTextView.setText(longitude.toString());
		}
	 }
}
