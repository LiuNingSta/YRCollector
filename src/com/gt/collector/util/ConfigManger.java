package com.gt.collector.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManger {
	//配置文件
	private static Properties properties;
	
	//构造方法
	public ConfigManger(String fileName){
		properties = new Properties();
		String load = ConfigManger.class.getResource("/").toString().substring(6)+fileName;
		File file = new File(load);
		//InputStream inputStream = ConfigManger.class.getClassLoader().getResourceAsStream("config/jdbc.properties");;
		try {
			InputStream inputStream = new FileInputStream(file);
			properties.load(inputStream);
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//通过KEY拿值
	public String getString (String key){
		return properties.getProperty(key);
	}
}
