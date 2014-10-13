package com.runLogger.utils;

import java.util.Calendar;

import com.runLogger.utils.dateSlider.DateTimeSlider;

/**
 * This class is used to get a formatted date string
 */
public class DateTimeFormatter {
	
	public static final int YEAR_MONTH_DAY_ORDER = 1;
	public static final int DAY_MONTH_YEAR_ORDER = 2;
	public static final int MINUTE_SECOND_ORDER = 3;
	
	/**
	 * Static method that returns a formatted date string
	 * @param   format  value of the selected format (provided by the same class).
	 * @param   calendar  calendar used to get the formatted date string
	 * @return  the formated string date 
	 */
	public static String getFormattedDate(Calendar calendar, int format){
		int m = calendar.get(Calendar.MONTH)+1;
    	int d = calendar.get(Calendar.DATE);
		
		switch (format){
			case YEAR_MONTH_DAY_ORDER:
				return ""+calendar.get(Calendar.YEAR)+
					"-"+(m < 10 ? "0" + Integer.toString(m) : Integer.toString(m))+
					"-"+(d < 10 ? "0" + Integer.toString(d) : Integer.toString(d));
				
			default:
				return ""+(d < 10 ? "0" + Integer.toString(d) : Integer.toString(d))+
					"-"+(m < 10 ? "0" + Integer.toString(m) : Integer.toString(m))+
					"-"+calendar.get(Calendar.YEAR);
		}
	}
	
	/**
	 * Static method that returns a formatted time string
	 * @param   format  value of the selected format (provided by the same class).
	 * @param   calendar  calendar used to get the formatted date string
	 * @return  the formated string time 
	 */
	public static String getFormattedTime(Calendar calendar){
    	int h = calendar.get(Calendar.HOUR_OF_DAY);
    	int min = calendar.get(Calendar.MINUTE) / 
		DateTimeSlider.MINUTEINTERVAL*DateTimeSlider.MINUTEINTERVAL;

		return ""+(h < 10 ? "0" + Integer.toString(h) : Integer.toString(h))+
			":"+(min < 10 ? "0" + Integer.toString(min) : Integer.toString(min));
	}
	
	/**
	 * Static method that returns a formatted time string
	 * @param   YYYYMMDD string date in the format YYYY-MM-DD
	 * @return  the string date in the format DD-MM-YYYY
	 */
	public static String toDDMMYYYYFormat(String YYYYMMDD){
    	String yearMonthDay[] = YYYYMMDD.split("-");
    	return yearMonthDay[2]+"-"+yearMonthDay[1]+"-"+yearMonthDay[0];
	}
}
