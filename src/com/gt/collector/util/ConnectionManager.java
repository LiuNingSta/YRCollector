package com.gt.collector.util;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.pool.DruidPooledConnection;

public class ConnectionManager {
	//配置文件
	private static ConnectionManager instance;
	private DruidDataSource ddsyr;  
	private DruidDataSource ddsst8;
	//无参构造方法
	private ConnectionManager(){
		Properties properties = new Properties();
        try {
        	InputStream ins = ConnectionManager.class.getClassLoader().getResourceAsStream("dbconfig_yr.properties");
        	properties.load(ins);
        	ins.close();
        	this.ddsyr  = (DruidDataSource) DruidDataSourceFactory
                    .createDataSource(properties);
        	ins = ConnectionManager.class.getClassLoader().getResourceAsStream("dbconfig_st8.properties");
        	properties.load(ins);
        	ins.close();
        	this.ddsst8  = (DruidDataSource) DruidDataSourceFactory
                    .createDataSource(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	//单例模式
	 public static final ConnectionManager getInstance() {  
		if(instance==null){
			instance = new ConnectionManager();
		}
		return instance;
	 }
	 //同步
	 public synchronized final DruidPooledConnection getConnection(String dateSource) {  
		 try {
			if(dateSource.equals("yr")){
				if(ddsyr.getActiveCount()>50){
					LogHelper.logErrorMsg("当前yr连接数=" + ddsyr.getActiveCount());
				}
				return ddsyr.getConnection();
			}else{
				if(ddsst8.getActiveCount()>50){
					LogHelper.logErrorMsg("当前st8连接数=" + ddsst8.getActiveCount());
				}
				return ddsst8.getConnection();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}  
		return null;
	 }
	 
	 //关闭连接池
	public synchronized void close(){
		ddsst8.close();   
		ddsyr.close();   
    }
		
}
