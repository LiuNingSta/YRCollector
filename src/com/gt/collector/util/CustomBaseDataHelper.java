package com.gt.collector.util;

public class CustomBaseDataHelper {
	public static Boolean isInteger(String value){
		try{
			int val= Integer.parseInt(value);
			return true;
		}catch (Exception e) {
			// TODO: handle exception
			return false;
		}
	}
	
}
