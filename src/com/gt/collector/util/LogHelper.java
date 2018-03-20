package com.gt.collector.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogHelper {
	/**
     * 将信息写入到日志
     * @param content
     * @return
     * @throws IOException
     */
    public static boolean logInfoMsg(String data){
        //String content=info(className, ErrorInfo, ErrorContent);
    	data="\r\n["+DateHelper.getStringByDate(new Date(), "yyyy-MM-dd HH:mm:ss")+"]   "+data;
    	String tm = new SimpleDateFormat("yyyyMMdd").format(new Date());
        File file = new File(LogHelper.class.getResource("/").toString().substring(6)+"config/logInfoMsg"+tm+".log");
        if(!file.exists()){
        	create(file);
        }
        RandomAccessFile mm = null;
        boolean flag = false;
        FileOutputStream o = null;
        try {
            o = new FileOutputStream(file,true);
            o.write(data.getBytes("utf-8"));
            o.close();
            flag = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (mm != null) {
                try {
                    mm.close(); 
                } catch (IOException e) {
                    e.printStackTrace();
                }
                 
            }
        }
        return flag;
    }  
    // 输出错误的日志信息
    public static boolean logErrorMsg(String info){
        //String content=info(className, ErrorInfo, ErrorContent);
    	info="\r\n["+DateHelper.getStringByDate(new Date(), "yyyy-MM-dd HH:mm:ss")+"]   "+info;
    	String tm = new SimpleDateFormat("yyyyMMdd").format(new Date());
        File file = new File(LogHelper.class.getResource("/").toString().substring(6)+"config/logErrorMsg"+tm+".log");
        if(!file.exists()){
        	create(file);
        }
        RandomAccessFile mm = null;
        boolean flag = false;
        FileOutputStream o = null;
        try {
            o = new FileOutputStream(file,true);
            o.write(info.getBytes("utf-8"));
            o.close();
            flag = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (mm != null) {
                try {
                    mm.close(); 
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            }
        }
        return flag;
    } 
    
    public static boolean debugInfoMsg(String data){
        //String content=info(className, ErrorInfo, ErrorContent);
    	data="\r\n["+DateHelper.getStringByDate(new Date(), "yyyy-MM-dd HH:mm:ss")+"]   "+data;
    	String tm = new SimpleDateFormat("yyyyMMdd").format(new Date());
        File file = new File(LogHelper.class.getResource("/").toString().substring(6)+"config/debugInfoMsg"+tm+".log");
        if(!file.exists()){
        	create(file);
        }
        RandomAccessFile mm = null;
        boolean flag = false;
        FileOutputStream o = null;
        try {
            o = new FileOutputStream(file,true);
            o.write(data.getBytes("utf-8"));
            o.close();
            flag = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (mm != null) {
                try {
                    mm.close(); 
                } catch (IOException e) {
                    e.printStackTrace();
                }
                 
            }
        }
        return flag;
    }  
    
    
    public static void create(File file) {
        //判断是否存在这个文件的文件夹，如果不存在就创建文件夹，在建文件
         if(!file.getParentFile().exists()){
             //创建文件夹
             file.getParentFile().mkdirs();
             try {
            //创建文件
                 file.createNewFile();
             } catch (IOException e) {
                 e.printStackTrace();
             }
             //如果有文件夹就直接创建文件
         }else{
             try {
                 file.createNewFile();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
}
