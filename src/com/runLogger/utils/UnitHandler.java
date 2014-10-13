package com.runLogger.utils;

import java.text.DecimalFormat;

/**
 * This class is used to manage with the different types of Length Units
 */
public class UnitHandler {
	
	private Float convertionFactor;
	private DecimalFormat df;

	private UnitHandler(float convFactor){
		convertionFactor = Float.valueOf(convFactor);
		df = new DecimalFormat("#.00");
	}
	
	private UnitHandler(float convFactor, DecimalFormat df){
		convertionFactor = Float.valueOf(convFactor);
		this.df = df;
	}
	
	/**
	 * Static method that returns a unit handler object
	 * The Decimal format by default is "#.0"
	 * @param   unit  value of the selected unit (provided by the same class).
	 * @return  the formated string date 
	 */
	public static UnitHandler getInstance(String unit){
		if (unit.equals("Miles"))
			return new UnitHandler(Float.valueOf("0.62137"));
		return new UnitHandler(1);
	}
	
	/**
	 * Static method that returns a unit handler object
	 * @param   unit  value of the selected unit (provided by the same class).
	 * @param   df  decimal format that the unit handler will use
	 * @return  the formated string date 
	 */
	public static UnitHandler getInstance(String unit, DecimalFormat df){
		if (unit.equals("Miles"))
			return new UnitHandler(Float.valueOf("0.62137"), df);
		return new UnitHandler(1, df);
	}
	
	/**
	 * Method that returns a float value in the user interface unit
	 * @param   floatValue the value to be represented in the actual length unit
	 * @return  the float value to display 
	 */
	public Float getValueToDisplay(Float floatValue){
		String value = df.format(floatValue * convertionFactor);
		return (Float.valueOf(value));
	}
	
	/**
	 * Method that returns a float value in the data base unit
	 * @param   userInterfacevalue the value that must be stored in the database unit
	 * @return  the float value to storage 
	 */
	public Float getValueToStore(Float userInterfacevalue){
		return (userInterfacevalue / convertionFactor);
	}
}
