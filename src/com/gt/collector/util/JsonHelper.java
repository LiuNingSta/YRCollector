package com.gt.collector.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.DefaultDefaultValueProcessor;

/**
 * 
********************************************************************************
模块名       :JSON格式帮助类
文件名       :JsonHelper.java
文件实现功能 :
	json格式的转化
作者         : LiuNing
版本         : 1.0
公司：杭州吉思信息技术
备注         :
--------------------------------------------------------------------------------
修改记录     :
日 期						版本		修改人	 	修改内容
2016-11-18 下午4:25:45		1.0		LiuNing     创建

*******************************************************************************
 */
public class JsonHelper {
	//将java对象转换为json字符串
	public static String Object2Json(Object obj){
		JsonConfig config = new JsonConfig();
		config.registerJsonValueProcessor(Timestamp.class,new DateJsonValueProcessor("yyyy-MM-dd HH:mm:ss"));
		DefaultDefaultValueProcessor(config);
		String str = JSONObject.fromObject(obj,config).toString();
		return str;
	}
	//将LIST集合转换为json字符串
	public static String Array2Json(Object obj){
		JsonConfig config = new JsonConfig();
		config.registerJsonValueProcessor(Timestamp.class,new DateJsonValueProcessor("yyyy-MM-dd HH:mm:ss"));
		DefaultDefaultValueProcessor(config);
		String str = JSONArray.fromObject(obj,config).toString();
		return str;
	}
	
	//将json字符串转化为List<Map<String, Object>>
	public static List<Map<String, Object>> getObjectList(String jsonStr){
		List<Map<String,Object>> mapListJson  = new ArrayList<Map<String,Object>>();
		JSONArray jsonArray = JSONArray.fromObject(jsonStr); 
		mapListJson = (List)jsonArray;
        return mapListJson;
    }
	//将json字符串转化为Map<String,List<Map<String, Object>>>
	public static Map<String,Map<String, List<Object>>> getMapListMap(String jsonStr){
		Map<String,Map<String, List<Object>>> mapMapList = new HashMap<String,Map<String, List<Object>>>();
		mapMapList = JSONObject.fromObject(jsonStr);
		return mapMapList;
	}
	//将json字符串转化为Map<String,List<Object>>
	public static Map<String,List<String>> getMapList(String jsonStr){
		Map<String,List<String>> mapList = new HashMap<String,List<String>>();
		mapList = JSONObject.fromObject(jsonStr);
		return mapList;
	}
	
	public static void  DefaultDefaultValueProcessor (JsonConfig config){
		config.registerDefaultValueProcessor(Double.class, new DefaultDefaultValueProcessor() {
		    public Object getDefaultValue(Class type) {
		        return null;
		    }
		});
		config.registerDefaultValueProcessor(String.class, new DefaultDefaultValueProcessor() {
		    public Object getDefaultValue(Class type) {
		        return null;
		    }
		});
		config.registerDefaultValueProcessor(Integer.class, new DefaultDefaultValueProcessor() {
		    public Object getDefaultValue(Class type) {
		        return null;
		    }
		});
	}
}
