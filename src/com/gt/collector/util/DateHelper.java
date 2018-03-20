package com.gt.collector.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateHelper {
	//根据字符串   和需要指定的转化格式     将字符串转化成日期格式的代码
	public static Date getDateByString(String string,String formatStr) throws Exception{
		if(string!=null){
			return new SimpleDateFormat(formatStr).parse(string);
		}else{
			return new Date();
		}
	}
	//将日期格式转化成字符串
	public static String getStringByDate(Date date,String formatStr){
		return new SimpleDateFormat(formatStr).format(date);
	}
	
	//拿到制定格式的当前时间
	public static Date getNowDateByFormat(String formatStr) throws Exception{
		String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		return DateHelper.getDateByString(date, formatStr);
	}
	
	//时间加多少天
	public static Date dateAddNumDay(Date date,Integer day){
		Calendar calender = Calendar.getInstance();
		if(date!=null){
			calender.setTime(date);
		}
		calender.set(Calendar.DATE, calender.get(Calendar.DATE) + day);
		date = calender.getTime();
		return date;
	}
	//时间加多少个月
	public static Date dateAddNumMonth(Date date,Integer month){
		Calendar calender = Calendar.getInstance();
		if(date!=null){
			calender.setTime(date);
		}
		calender.set(Calendar.MONTH, calender.get(Calendar.MONTH) + month);
		date = calender.getTime();
		return date;
	}
	
	
	//时间加指定小时
	public static Date dateAddNumHours(Date date,Integer hours){
		Calendar calender = Calendar.getInstance();
		if(date!=null){
			calender.setTime(date);
		}
		calender.set(Calendar.HOUR, calender.get(Calendar.HOUR) + hours);
		date = calender.getTime();
		return date;
	}
	
	//时间加指定分钟
	public static Date dateAddNumMinutes(Date date,Integer Minutes){
		Calendar calender = Calendar.getInstance();
		if(date!=null){
			calender.setTime(date);
		}
		calender.set(Calendar.MINUTE, calender.get(Calendar.MINUTE) + Minutes);
		date = calender.getTime();
		return date;
	}
	
	
	//时间加指定秒
	public static Date dateAddNumSecond(Date date,Integer Second){
		Calendar calender = Calendar.getInstance();
		if(date!=null){
			calender.setTime(date);
		}
		calender.set(Calendar.SECOND, calender.get(Calendar.SECOND) + Second);
		date = calender.getTime();
		return date;
	}
	
	//拿到时间的上一个整5分钟时间
	public static Date getLastFriveMinuteDate(Date date){
		Calendar calender = Calendar.getInstance();
		if(date!=null){
			calender.setTime(date);
		}
		Integer minute = calender.get(Calendar.MINUTE);
		minute = minute%5==0?minute:minute-minute%5;
		calender.set(Calendar.MINUTE, minute);
		calender.set(Calendar.SECOND, 0);
		date = calender.getTime();
		return date;
	}
	//拿到时间的上一个整点时间
	public static Date getLastIntegralPointDate(Date date){
		Calendar calender = Calendar.getInstance();
		if(date!=null){
			calender.setTime(date);
		}
		calender.set(Calendar.MINUTE, 0);
		calender.set(Calendar.SECOND, 0);
		date = calender.getTime();
		return date;
	}
	
	//拿到上一个8:00
	public static Date getEightPointForTm(Date date) throws ParseException{
		Calendar calender = Calendar.getInstance();
		if(date!=null){
			calender.setTime(date);
		}
		Integer hours = calender.get(Calendar.HOUR);//拿到小时数
		if(hours<8){//小于8点
			date = DateHelper.dateAddNumDay(date, -1);
		}
		String endTm = new SimpleDateFormat("yyyy-MM-dd").format(date);
		endTm += " 08:00";
		return new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(endTm);
	}
	
	//判断字符串是否是日期格式
	public static boolean isValidDate(String tm){
		if(tm==null){
			return false;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(tm.trim().length()!=dateFormat.toPattern().length()){
			return false;
		}
		//关闭自动计算日期
		dateFormat.setLenient(false);
		try{
			//解析字符串
			dateFormat.parse(tm);
			
		}catch (Exception e) {
			return false;
		}
		return true;
	}
	
}
