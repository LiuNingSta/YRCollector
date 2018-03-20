package com.gt.collector.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.util.NewBeanInstanceStrategy;

import org.junit.Test;

import com.gt.collector.dao.BaseDao;
import com.gt.collector.pojo.GT_log;

public class Test1 {
	@Test
	public void test1() throws Exception {
		Map<String,Object> map = new HashMap<>();
		map.put("11     ", "");
		System.out.println(map.size());
		String aString = "11";
		System.out.println(aString.indexOf("2"));
		/*//获得结果集结构信息,元数据
		ResultSetMetaData md = rs.getMetaData();
		//列数
		int columnCount = md.getColumnCount();
		List<Map<String, Object>> table_value = new ArrayList<Map<String, Object>>();
		while(rs.next()){//遍历解析结果集
			Map<String, Object> rowMap = new HashMap<String, Object>();
			for(int j=1;j<=columnCount;j++){
				String columnName = md.getColumnName(j).toUpperCase();//列名
				Object value = rs.getObject(j);//数据
				rowMap.put(columnName, value);//加入数据该行
			}
			table_value.add(rowMap);//行数据加到list中
		}
		Map<String,List<Object>>  table_map = new  HashMap<String, List<Object>>();
		
		//将列名放到集合中   遍历集合  拿到列名
		List<String> column_List = new ArrayList<String>();//列名集合
		column_List.add("SourceID");//加入自己的id
		for(int j=1;j<=columnCount;j++){
			column_List.add(md.getColumnName(j).toUpperCase());
		}
		baseDao.close();
		if(null!=table_value && table_value.size()>0){
			for(int m=0;m<column_List.size();m++){//循环列名
				String columnName = column_List.get(m);//拿到列名
				if(columnName.toUpperCase().equals("MODITIME") || columnName.toUpperCase().equals("MOD_TIME")){
					continue;
				}
				List<Object> value_List = new ArrayList<Object>();
				for(int j=0;j<table_value.size();j++){//一行一行循环
					if(column_List.get(m).equals("SourceID")){
						value_List.add("111111111111111");//给SourceID  赋值
					}else{
						Object value = table_value.get(j).get(columnName);//拿到改行该列的值
						value_List.add(value);//加到该列的值的集合中
					}
				}
				table_map.put(columnName, value_List);//把列   加到  表中
			}
		}
		tanmes_map.put("ST_PPTN_R", table_map);//表数据加到map中
		String jsonDate = JsonHelper.Object2Json(tanmes_map);
		//LogHelper.logInfoMsg("  方法名："+method+"  数据解析完毕！！   正在传输数据数据！！稍后");
		//进行数据传输
		//String result = dataHelper.saveCollectorData(id,pcid,jsonDate);
*/	}
}
