package com.runLogger.cursor;

import android.content.Context;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.runLogger.R;
import com.runLogger.dao.DBLayer;
import com.runLogger.utils.DateTimeFormatter;
import com.runLogger.utils.UnitHandler;

/**
 * Cursor to manage the GUI in the Length History Activity
 */
public class HistoryValuesCursorAdapter extends SimpleCursorAdapter {
	
	private int layout;
	private UnitHandler unitHandler;

	public HistoryValuesCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, UnitHandler unitHandler) {
		super(context, layout, c, from, to);
		
		this.layout = layout;
		this.unitHandler = unitHandler;
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		
		Cursor c = getCursor();
		final LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(layout, parent, false);
		int dateRow = c.getColumnIndex(DBLayer.KEY_DATE);
		String date = c.getString(dateRow);
		
		TextView dateTextView = (TextView) v.findViewById(R.id.rowDate);
		if (dateTextView != null) {
			dateTextView.setText(DateTimeFormatter.toDDMMYYYYFormat(date));
		}
		 
		int lengthRow = c.getColumnIndex(DBLayer.KEY_LENGTH);
		Float length = c.getFloat(lengthRow);
		
		TextView lengthTextView = (TextView) v.findViewById(R.id.rowLength);
		if (lengthTextView != null) {
			lengthTextView.setText(unitHandler.getValueToDisplay(length).toString());
		}
		
		TextView lengthUnitTextView = (TextView) v.findViewById(R.id.rowLengthUnit);
		if (lengthUnitTextView != null){
			lengthUnitTextView.setText(PreferenceManager.getDefaultSharedPreferences(context).getString("lengthUnit_list_preference", ""));
		}
		return v;
	}
	
	 @Override
	 public void bindView(View v, Context context, Cursor c) {
		int dateRow = c.getColumnIndex(DBLayer.KEY_DATE);
		String date = c.getString(dateRow);
		
		TextView dateTextView = (TextView) v.findViewById(R.id.rowDate);
		if (dateTextView != null) {
			dateTextView.setText(DateTimeFormatter.toDDMMYYYYFormat(date));
		}
		 
		int lengthRow = c.getColumnIndex(DBLayer.KEY_LENGTH);
		Float length = c.getFloat(lengthRow);
		
		TextView lengthTextView = (TextView) v.findViewById(R.id.rowLength);
		if (lengthTextView != null) {
			lengthTextView.setText(unitHandler.getValueToDisplay(length).toString());
		}
		
		TextView lengthUnitTextView = (TextView) v.findViewById(R.id.rowLengthUnit);
		if (lengthUnitTextView != null){
			lengthUnitTextView.setText(PreferenceManager.getDefaultSharedPreferences(context).getString("lengthUnit_list_preference", ""));
		}
	 }
}
