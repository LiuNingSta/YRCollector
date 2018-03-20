package com.gt.collector.dao;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.org.objectweb.asm.Type;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.gt.collector.util.ConnectionManager;
import com.gt.collector.util.LogHelper;


public class BaseDao {
	
	private DruidPooledConnection connection;//连接
	
	private PreparedStatement ps;//执行sql的工具
	
	private ResultSet rs;//返回的结果集
	
	private CallableStatement callStatement; //执行存储过程的sql
	
	//拿到连接
	public synchronized Boolean getConnection(String dataSource){
		connection = ConnectionManager.getInstance().getConnection(dataSource);
		if(null!=connection){
			return true;
		}
		return false;
	}
	//查
	public synchronized ResultSet executeQuery(String dataSource,String sql,Object[] objects){
		if(this.getConnection(dataSource)){
			try {
				ps = connection.prepareStatement(sql);
				for(int i=0;i<objects.length;i++){
					ps.setObject(i+1, objects[i]);
				}
				rs = ps.executeQuery();
				connection.commit();
			} catch (SQLException e) {
				e.printStackTrace();
				LogHelper.logErrorMsg("executeQueryError："+e.toString());
			}
		}
		return rs;
	}
	//增删改
	public synchronized Integer executeUpdate(String dataSource,String sql,Object[] objects){
		int num = 0;
		if(this.getConnection(dataSource)){
			try {
				ps = connection.prepareStatement(sql);
				for(int i=0;i<objects.length;i++){
					ps.setObject(i+1, objects[i]);
				}
				num = ps.executeUpdate();
				connection.commit();
				return num; 
			} catch (Exception e) {
				LogHelper.logErrorMsg("executeUpdateError："+e.toString());
				try {
					connection.rollback();//回滚JDBC事务 
				} catch (SQLException e1) {
					
					e1.printStackTrace();
				}
				e.printStackTrace();
				return num;
			}
		}
		return num;
	} 
	//关闭资源
	public synchronized void close(){
		if(rs!=null){
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(callStatement!=null){
			try {
				callStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(ps!=null){
			try {
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(connection!=null){
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	//批量插入
	public synchronized Integer InsertBatch(String dataSource,List<Map<String, Object>> table , String tname){
		Integer insertCount = 0;
		if(this.getConnection(dataSource)){
			try {
				if(table.size()>0){
					connection.setAutoCommit(false);// 更改JDBC事务的默认提交方式 
					//Map<String , Object> table0 = table.get(0);
					String values = "";
					String clomns = "";
					Map<String, Object> map= table.get(0);
					List<String> keyList = new ArrayList<String>();
					for(Entry<String,Object> entry : map.entrySet()){
						String key = entry.getKey();
						keyList.add(key);
						clomns += key+",";
						values += "?,";
					}
					if(!values.equals("")){
						clomns = clomns.substring(0,clomns.length()-1);
						values = values.substring(0,values.length()-1);
						String sql = "insert into "+tname+" ( "+clomns+" ) values ( "+values+" )";
						ps = connection.prepareStatement(sql);
						final int batchSize = 1000;//每次提交1000条
						int bSize = 0;
						for(int i=0;i<table.size();i++){
							Map<String, Object> mapValue = table.get(i);
							for(int j=1;j<keyList.size()+1;j++){
								String key = keyList.get(j-1);
								for(Entry<String,Object> entry : mapValue.entrySet()){
									if(entry.getKey().equals(key)){
										ps.setObject(j,entry.getValue());
									}
								}
							}
							ps.addBatch();
							if(++bSize % batchSize ==0){//能整除1000时  提交语句
								int[] aa = ps.executeBatch();
								insertCount = insertCount+aa.length;
							}
						}
						int[] bb = ps.executeBatch();
						insertCount = insertCount+bb.length;
						connection.commit();
						connection.setAutoCommit(true);// 恢复JDBC事务的默认提交方式	
					}
				}
				return insertCount;
			} catch (Exception e) {
				try {
					connection.rollback();//回滚JDBC事务 
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
				LogHelper.logErrorMsg("批量插入"+tname+"表出错！errInfo："+e.getMessage());
			}
		}
		return insertCount;
	}
	//批量插入

	//调用存储过程
	public synchronized List<Map<String, List<Object>>>  execProc(String dataSource,String pro_name,List<Object> list_parm){
		List<Map<String, List<Object>>> table_List = new ArrayList<Map<String, List<Object>>>();
		if(getConnection(dataSource)){//连接数据库
			try {
				connection.setAutoCommit(false);// 更改JDBC事务的默认提交方式 
				String value = "";
				for(int i=0;i<list_parm.size();i++){
					value += "?,";
				}
				if(value.length()>0){
					value=value.substring(0,value.length()-1);
				}
				String sql = "{call "+pro_name+"("+value+")}";//动态拼接sql
				callStatement = connection.prepareCall(sql);//预编译
				for(int i=0;i<list_parm.size();i++){
					callStatement.setObject(i+1, list_parm.get(i));//跟？号赋值
				}
				boolean hasResultSet = callStatement.execute();
				if(!hasResultSet){//不是结果集
					while(callStatement.getMoreResults()){//还有更多的结果集
						Map<String , List<Object>> tabe = new  HashMap<String, List<Object>>();
						rs = callStatement.getResultSet();
						ResultSetMetaData md = rs.getMetaData(); //获得结果集结构信息,元数据
						int columnCount = md.getColumnCount();   //获得列数 
						for (int j= 1;j <= columnCount; j++) {//循环在map创建k-v  value暂时为空
							List<Object> columnValues = new ArrayList<Object>();
							tabe.put(md.getColumnName(j), columnValues);
						}
						while (rs.next()) {//拿值
							for (int j= 1;j <= columnCount; j++) {//在当前列当前行塞值
								tabe.get(md.getColumnName(j)).add(rs.getObject(j));
							}
						}
						table_List.add(tabe);
					}
				}else{
					rs = callStatement.getResultSet();
					Map<String , List<Object>> tabe = new  HashMap<String, List<Object>>();
					ResultSetMetaData md = rs.getMetaData(); //获得结果集结构信息,元数据
					int columnCount = md.getColumnCount();   //获得列数 
					for (int j= 1;j <= columnCount; j++) {
						List<Object> columnValues = new ArrayList<Object>();
						tabe.put(md.getColumnName(j), columnValues);
					}
					while (rs.next()) {
						for (int j= 1;j <= columnCount; j++) {
							tabe.get(md.getColumnName(j)).add(rs.getObject(j));
						}
					}
					table_List.add(tabe);
				}
				connection.commit();
				connection.setAutoCommit(true);
			} catch (SQLException e) {
				LogHelper.logErrorMsg("execProcError："+e.toString());
				try {
					connection.rollback();//回滚JDBC事务 
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		}
		return table_List;
	}

}
