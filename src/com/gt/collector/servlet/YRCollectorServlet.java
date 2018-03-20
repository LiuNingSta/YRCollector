package com.gt.collector.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.gt.collector.dao.BaseDao;
import com.gt.collector.thread.ThreadStart;
import com.gt.collector.util.ConnectionManager;
import com.gt.collector.util.DataHelper;
import com.gt.collector.util.LogHelper;

public class YRCollectorServlet extends HttpServlet {
	private static Thread dataThread;  
	private static Boolean threadStop;
	private static Map<String, Object> unstcdMap;
	/**
	 * Constructor of the object.
	 */
	public YRCollectorServlet() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		ConnectionManager.getInstance().close();
		threadStop = true;
		dataThread.interrupt();
		super.destroy();
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		System.out.println("init");
		threadStop = false;
    	System.out.println(dataThread);
    	LogHelper.logInfoMsg("服务正在启动，请稍候 2s ......  ");
		//==========================================  开启线程      =========================================  start
		//暂停10s
		try {
			Thread.sleep(2*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	if(null == dataThread){
			dataThread = new Thread(new ThreadStart());
			dataThread.setDaemon(true);
			dataThread.start();
		}
	}
	public void getYRData(){
		while(!threadStop){
			try {
				LogHelper.logInfoMsg("线程休眠开始抓取数据 ......  ");
				BaseDao baseDao = new BaseDao();
				String sql = "select stcd_m,sttp from V_ST_STBPRP_M_B ";
				Object[] objects2 = {};
				ResultSet rs =  baseDao.executeQuery("st8", sql, objects2);
				Map<String, String> sttpMap = new HashMap<String, String>();
				if(null!=rs){
					while(rs.next()){
						String stcd_m = rs.getString("stcd_m");
						String sttp = rs.getString("sttp");
						if(null!=stcd_m && null!=sttp){
							sttpMap.put(stcd_m.trim(),sttp.trim());
						}
					}
					baseDao.close();
				}else{
					baseDao.close();
					LogHelper.logErrorMsg("请检查V_ST_STBPRP_M_B表！！！！！");
					continue;
				}
				unstcdMap = new HashMap<String,Object>();
				this.getPPTNData(sttpMap);// 雨量
				this.getWATERData(sttpMap);// 水位
				this.getTempData(sttpMap);// 温度
				this.getVoltageData(sttpMap); // 电压
				//this.getSOILData(sttpMap); //墒情
				this.getFlowVData(sttpMap); //流速
				LogHelper.logInfoMsg("开始不匹配的站点处理！！");
				int updateCount = 0;
				int insertCount = 0;
				if(unstcdMap.size()>0){
					LogHelper.logInfoMsg("不匹配的站点有："+unstcdMap);
					Object[] objects = {};
					sql = " select stcd from UNSTCD ";
					rs =  baseDao.executeQuery("st8", sql, objects);
					String updateSql = " update UNSTCD set moditime = getdate() where stcd in (";
					String insertSql = "insert into UNSTCD (stcd,moditime) values ";
					String sqlU = "";
					while(rs.next()){
						String stcd = rs.getString("stcd").trim();
						if(unstcdMap.containsKey(stcd)){
							sqlU= sqlU+"'"+stcd+"',";
							unstcdMap.remove(stcd);
						}
					}
					baseDao.close();
					String sqlI = "";
					for(Entry<String, Object> entry : unstcdMap.entrySet()){
						String unstcd = entry.getKey();
						sqlI = sqlI+"('"+unstcd+"',getDate()),";
					}
					if(!sqlI.equals("")){
						sqlI = sqlI.substring(0, sqlI.length()-1);
						insertSql += sqlI;
						insertCount = baseDao.executeUpdate("st8", insertSql, objects);
						baseDao.close();
					}
					if(!sqlU.equals("")){
						sqlU = sqlU.substring(0, sqlU.length()-1);
						updateSql = updateSql+sqlU+")";
						updateCount = baseDao.executeUpdate("st8", updateSql, objects);
						baseDao.close();
					}
				}
				LogHelper.logInfoMsg("不匹配的站点处理完成！！插入："+insertCount+"条！更新："+updateCount+"条！");
			} catch (Exception e) {
				LogHelper.logErrorMsg("getYRData出问题了!!"+e.toString());
			}finally{
				try {
					LogHelper.logInfoMsg("线程休眠5s ......  ");
					dataThread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	public void getPPTNData(Map<String, String> sttpMap){
		String tname = "ST_PPTN_R";
		try {
			LogHelper.logInfoMsg("开始获取数据！"+tname);
			Long startTime = new Date().getTime();
			//雨量
			String sql = " SELECT top 10000 iid,a.stcd,a.tm,b.R5 drp,a.modiTime FROM GT_LOG a,RAIN b WHERE a.TABLE_NAME = ? and a.stcd = b.stcd and a.tm = b.tm order by a.moditime asc ";
			BaseDao baseDao = new BaseDao();
			Object[] objects = {tname};
			ResultSet rs =  baseDao.executeQuery("yr", sql, objects);
			List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
			List<Integer> iidList = new ArrayList<Integer>();
			while(rs.next()){
				Map<String, Object> map = new HashMap<String, Object>();
				iidList.add(rs.getInt("iid"));
				String stcd =  rs.getString("stcd").trim();
				map.put("stcd", stcd);
				if(!sttpMap.containsKey(stcd)){
					unstcdMap.put(stcd, "");
				}
				map.put("tm", rs.getTimestamp("tm"));
				map.put("modiTime", rs.getTimestamp("modiTime"));
				Double drp = rs.getDouble("drp");
				map.put("drp", drp);
				dataList.add(map);
			}
			baseDao.close();
			LogHelper.logInfoMsg("接收ST_PPTN_R:"+dataList.size()+"条语润数据！");
			if(dataList.size()>0){
				int insertCount = baseDao.InsertBatch("st8", dataList, tname+"_TMP");
				baseDao.close();
				LogHelper.logInfoMsg("批量插入"+tname+"_TMP:"+insertCount+"条！语润数据成功！");
				DataHelper.mergeInto(tname);
				String iidStr = "";
				int deleteCount = 0;
				String sql0 = "delete from GT_LOG where iid in (";
				for(int i=1;i<iidList.size()+1;i++){
					iidStr+=iidList.get(i-1)+",";
					if(i%1000==0){
						sql=sql0+iidStr;
						sql = sql.substring(0,sql.length()-1);
						sql += ")";
						Object[] objects3 = {};
						deleteCount += baseDao.executeUpdate("yr", sql, objects3);
						baseDao.close();
						iidStr = "";
					}
				}
				if(!iidStr.equals("")){
					sql=sql0+iidStr;
					sql = sql.substring(0,sql.length()-1);
					sql += ")";
					Object[] objects3 = {};
					deleteCount += baseDao.executeUpdate("yr", sql, objects3);
					baseDao.close();
				}
				LogHelper.logInfoMsg("deleteLog__"+tname+"："+deleteCount+"条数据成功");
			}
			LogHelper.logInfoMsg("雨量数据处理完成！总共耗时:"+((new Date()).getTime()-startTime)/1000+"s");
		} catch (Exception e) {
			// TODO: handle exception
			LogHelper.logErrorMsg(tname+"："+e.toString());
		}
	}
	public void getTempData(Map<String, String> sttpMap){
		String tname = "ST_TEMPERATURE_R";
		try {
			LogHelper.logInfoMsg("开始获取"+tname+"数据！");
			Long startTime = new Date().getTime();
			//雨量	
			String sql = "SELECT top 10000 iid,a.stcd,a.tm,b.t,a.modiTime FROM GT_LOG a,Temperature b WHERE a.TABLE_NAME = ? and a.stcd = b.stcd and a.tm = b.tm order by a.moditime asc ";
			BaseDao baseDao = new BaseDao();
			Object[] objects = {tname};
			ResultSet rs =  baseDao.executeQuery("yr", sql, objects);
			List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
			List<Integer> iidList = new ArrayList<Integer>();
			while(rs.next()){
				Map<String, Object> map = new HashMap<String, Object>();
				iidList.add(rs.getInt("iid"));
				String stcd = rs.getString("stcd").trim();
				if(!sttpMap.containsKey(stcd)){
					unstcdMap.put(stcd, "");
				}
				map.put("stcd", stcd);
				map.put("tm", rs.getTimestamp("tm"));
				map.put("modiTime", rs.getTimestamp("modiTime"));
				Double t = rs.getDouble("t");
				map.put("t", t);
				dataList.add(map);
			}
			baseDao.close();
			LogHelper.logInfoMsg("开始处理"+tname+": "+dataList.size()+"条！语润数据！");
			if(dataList.size()>0){
				Integer insertCount = baseDao.InsertBatch("st8", dataList, tname+"_TMP");
				baseDao.close();
				LogHelper.logInfoMsg("批量插入"+tname+"_TMP:"+insertCount+"条数据成功！");
				DataHelper.mergeInto(tname);
				String iidStr = "";
				int deleteCount = 0;
				String sql0 = "delete from GT_LOG where iid in (";
				for(int i=1;i<iidList.size()+1;i++){
					iidStr+=iidList.get(i-1)+",";
					if(i%1000==0){
						sql=sql0+iidStr;
						sql = sql.substring(0,sql.length()-1);
						sql += ")";
						Object[] objects3 = {};
						deleteCount += baseDao.executeUpdate("yr", sql, objects3);
						baseDao.close();
						iidStr = "";
					}
				}
				if(!iidStr.equals("")){
					sql=sql0+iidStr;
					sql = sql.substring(0,sql.length()-1);
					sql += ")";
					Object[] objects3 = {};
					deleteCount += baseDao.executeUpdate("yr", sql, objects3);
					baseDao.close();
				}
				LogHelper.logInfoMsg("deleteLog__"+tname+"："+deleteCount+"条数据成功");
			}
			LogHelper.logInfoMsg("温度数据处理完成！总共耗时:"+((new Date()).getTime()-startTime)/1000+"s");
		} catch (Exception e) {
			// TODO: handle exception
			LogHelper.logErrorMsg(tname+"："+e.toString());
		}
	}
	public void getWATERData(Map<String, String> sttpMap){
		try {
			LogHelper.logInfoMsg("开始获取ST_WATER_R数据！");
			Long startTime = new Date().getTime();
			BaseDao baseDao = new BaseDao();
			//水位
			String sql = "SELECT top 10000 iid,a.stcd,a.tm,b.z,a.modiTime FROM GT_LOG a,WATER b WHERE a.TABLE_NAME = 'ST_WATER_R' and a.stcd = b.stcd and a.tm = b.tm order by a.moditime asc ";
			Object[] objects = {};
			ResultSet rs =  baseDao.executeQuery("yr", sql, objects);
			List<Map<String, Object>> riverList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> rsvrList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> tideList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> wasList = new ArrayList<Map<String, Object>>();
			List<Integer> iidList = new ArrayList<Integer>();
			while(rs.next()){
				Map<String, Object> map = new HashMap<String, Object>();
				iidList.add(rs.getInt("iid"));
				String stcd = rs.getString("stcd").trim();
				if(!sttpMap.containsKey(stcd)){
					unstcdMap.put(stcd, "");
				}
				Date tm = rs.getTimestamp("tm");
				map.put("stcd", stcd);
				map.put("tm", tm);
				map.put("modiTime", rs.getTimestamp("modiTime"));
				Double z = rs.getDouble("z");
				if(sttpMap.containsKey(stcd)){
					String sttp = sttpMap.get(stcd);
					switch (sttp.trim().toUpperCase()) {
						case "RR":
							map.put("rz", z);
							rsvrList.add(map);
							break;
						case "DD":
							map.put("upz", z);
							wasList.add(map);
							break;
						case "TT":
							map.put("tdz", z);
							tideList.add(map);
							break;
						default:
							map.put("z", z);
							riverList.add(map);
							break;
					}	
				}else{
					map.put("z", z);
					riverList.add(map);
				}
			}
			baseDao.close();
			LogHelper.logInfoMsg("开始处理ST_WATER_R："+iidList.size()+"条！语润数据！");
			if(riverList.size()>0){
				Integer insertCount = baseDao.InsertBatch("st8", riverList, "ST_RIVER_R_TMP");
				baseDao.close();
				LogHelper.logInfoMsg("批量插入ST_RIVER_R:"+insertCount+"条数据成功！");
				DataHelper.mergeInto("ST_RIVER_R");
			}
			if(rsvrList.size()>0){
				Integer insertCount = baseDao.InsertBatch("st8", rsvrList, "ST_RSVR_R_TMP");
				baseDao.close();
				LogHelper.logInfoMsg("批量插入ST_RSVR_R:"+insertCount+"条数据成功！");
				DataHelper.mergeInto("ST_RSVR_R");
			}
			if(tideList.size()>0){
				Integer insertCount = baseDao.InsertBatch("st8", tideList, "ST_TIDE_R_TMP");
				baseDao.close();
				LogHelper.logInfoMsg("批量插入ST_TIDE_R:"+insertCount+"条数据成功！");
				DataHelper.mergeInto("ST_TIDE_R");
			}
			if(wasList.size()>0){
				Integer insertCount = baseDao.InsertBatch("st8", wasList, "ST_WAS_R_TMP");
				baseDao.close();
				LogHelper.logInfoMsg("批量插入ST_WAS_R:"+insertCount+"条数据成功！");
				DataHelper.mergeInto("ST_WAS_R");
			}
			String iidStr = "";
			int deleteCount = 0;
			String sql0 =  "delete from GT_LOG where iid in (";
			for(int i=1;i<iidList.size()+1;i++){
				iidStr+=iidList.get(i-1)+",";
				if(i%1000==0){
					sql=sql0+iidStr;
					sql = sql.substring(0,sql.length()-1);
					sql += ")";
					Object[] objects3 = {};
					deleteCount += baseDao.executeUpdate("yr", sql, objects3);
					baseDao.close();
					iidStr = "";
				}
			}
			if(!iidStr.equals("")){
				sql=sql0+iidStr;
				sql = sql.substring(0,sql.length()-1);
				sql += ")";
				Object[] objects3 = {};
				deleteCount += baseDao.executeUpdate("yr", sql, objects3);
				baseDao.close();
			}
			LogHelper.logInfoMsg("deleteLog__ST_WATER_R："+deleteCount+"条数据成功");
			LogHelper.logInfoMsg("水位数据处理完成！总共耗时:"+((new Date()).getTime()-startTime)/1000+"s");
		} catch (Exception e) {
			// TODO: handle exception
			LogHelper.logErrorMsg("WATER_____error："+e.toString());
		}
	}

	public void getVoltageData(Map<String, String> sttpMap){
		String tname = "ST_VOLTAGE_R";
		try {
			LogHelper.logInfoMsg("开始获取"+tname+"数据！");
			Long startTime = new Date().getTime();
			//雨量
			String sql = "SELECT top 10000 iid,a.stcd,a.tm,b.voltage,a.modiTime FROM GT_LOG a,voltage b WHERE a.TABLE_NAME = ? and a.stcd = b.stcd and a.tm = b.tm order by a.moditime asc ";
			BaseDao baseDao = new BaseDao();
			Object[] objects = {tname};
			ResultSet rs =  baseDao.executeQuery("yr", sql, objects);
			List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
			List<Integer> iidList = new ArrayList<Integer>();
			while(rs.next()){
				Map<String, Object> map = new HashMap<String, Object>();
				Double v = null;
				iidList.add(rs.getInt("iid"));
				String stcd = rs.getString("stcd").trim();
				map.put("stcd", stcd);
				if(!sttpMap.containsKey(stcd)){
					unstcdMap.put(stcd, "");
				}
				map.put("tm", rs.getTimestamp("tm"));
				map.put("modiTime", rs.getTimestamp("modiTime"));
				String vStr = rs.getString("voltage");
				if(null!=vStr){
					try {
						v = Double.parseDouble(vStr.trim());
					} catch (Exception e) {
						// TODO: handle exception
						continue;
					}
				}else{
					continue;
				}
				map.put("voltage", v);
				dataList.add(map);
			}
			baseDao.close();
			LogHelper.logInfoMsg("开始处理"+tname+": "+dataList.size()+"条语润数据！");
			if(dataList.size()>0){
				Integer insertCount = baseDao.InsertBatch("st8", dataList, tname+"_TMP");
				baseDao.close();
				LogHelper.logInfoMsg("批量插入"+tname+":"+insertCount+"条！语润数据！");
				DataHelper.mergeInto(tname);
				String iidStr = "";
				int deleteCount = 0;
				String sql0 =  "delete from GT_LOG where iid in (";
				for(int i=1;i<iidList.size()+1;i++){
					iidStr+=iidList.get(i-1)+",";
					if(i%1000==0){
						sql=sql0+iidStr;
						sql = sql.substring(0,sql.length()-1);
						sql += ")";
						Object[] objects3 = {};
						deleteCount += baseDao.executeUpdate("yr", sql, objects3);
						baseDao.close();
						iidStr = "";
					}
				}
				if(!iidStr.equals("")){
					sql=sql0+iidStr;
					sql = sql.substring(0,sql.length()-1);
					sql += ")";
					Object[] objects3 = {};
					deleteCount += baseDao.executeUpdate("yr", sql, objects3);
					baseDao.close();
				}
				LogHelper.logInfoMsg("deleteLog__"+tname+"："+deleteCount+"条数据成功");
			}
			LogHelper.logInfoMsg("电压数据处理完成！总共耗时:"+((new Date()).getTime()-startTime)/1000+"s");
		} catch (Exception e) {
			// TODO: handle exception
			LogHelper.logErrorMsg("ST_VOLTAGE_R error："+e.toString());
		}
	}

	//获取流速信息
	public void getFlowVData(Map<String, String> sttpMap){
		String tname = "ST_FLOWV_R";
		try {
			LogHelper.logInfoMsg("开始获取"+tname+"数据！");
			Long startTime = new Date().getTime();
			//雨量
			String sql = "SELECT top 10000 iid,a.stcd,a.tm,b.vx,b.x,b.vy,b.y,a.modiTime FROM GT_LOG a,Q_Vx b WHERE a.TABLE_NAME = ? and a.stcd = b.stcd and a.tm = b.tm and b.mark = 1 order by a.moditime asc ";
			BaseDao baseDao = new BaseDao();
			Object[] objects = {tname};
			ResultSet rs =  baseDao.executeQuery("yr", sql, objects);
			List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
			List<Integer> iidList = new ArrayList<Integer>();
			while(rs.next()){
				Map<String, Object> map = new HashMap<String, Object>();
				Double v = null;
				iidList.add(rs.getInt("iid"));
				String stcd = rs.getString("stcd").trim();
				map.put("stcd", stcd);
				if(!sttpMap.containsKey(stcd)){
					unstcdMap.put(stcd, "");
				}
				map.put("tm", rs.getTimestamp("tm"));
				map.put("modiTime", rs.getTimestamp("modiTime"));
				map.put("vx", rs.getDouble("vx"));
				map.put("x", rs.getDouble("x"));
				map.put("vy", rs.getDouble("vy"));
				map.put("y", rs.getDouble("y"));
				dataList.add(map);
			}
			baseDao.close();
			LogHelper.logInfoMsg("开始处理"+tname+": "+dataList.size()+"条语润数据！");
			if(dataList.size()>0){
				Integer insertCount = baseDao.InsertBatch("st8", dataList, tname+"_TMP");
				baseDao.close();
				LogHelper.logInfoMsg("批量插入"+tname+":"+insertCount+"条！语润数据！");
				DataHelper.mergeInto(tname);
				String iidStr = "";
				int deleteCount = 0;
				String sql0 =  "delete from GT_LOG where iid in (";
				for(int i=1;i<iidList.size()+1;i++){
					iidStr+=iidList.get(i-1)+",";
					if(i%1000==0){
						sql=sql0+iidStr;
						sql = sql.substring(0,sql.length()-1);
						sql += ")";
						Object[] objects3 = {};
						deleteCount += baseDao.executeUpdate("yr", sql, objects3);
						baseDao.close();
						iidStr = "";
					}
				}
				if(!iidStr.equals("")){
					sql=sql0+iidStr;
					sql = sql.substring(0,sql.length()-1);
					sql += ")";
					Object[] objects3 = {};
					deleteCount += baseDao.executeUpdate("yr", sql, objects3);
					baseDao.close();
				}
				LogHelper.logInfoMsg("deleteLog__"+tname+"："+deleteCount+"条数据成功");
			}
			LogHelper.logInfoMsg("电压数据处理完成！总共耗时:"+((new Date()).getTime()-startTime)/1000+"s");
		} catch (Exception e) {
			// TODO: handle exception
			LogHelper.logErrorMsg("ST_FLOWV_R error："+e.toString());
		}
	}

	//获取墒情信息
	public void getSOILData(Map<String, String> sttpMap){
		String tname = "ST_SOIL_R";
		try {
			LogHelper.logInfoMsg("开始获取"+tname+"数据！");
			Long startTime = new Date().getTime();
			//雨量
			String sql = "SELECT top 10000 iid,a.stcd,a.tm,b.exkey,b.vtavslm,b.srlslm,b.slm,a.modiTime FROM GT_LOG a,ST_SOIL_R b WHERE a.TABLE_NAME = ? and a.stcd = b.stcd and a.tm = b.tm order by a.moditime asc ";
			BaseDao baseDao = new BaseDao();
			Object[] objects = {tname};
			ResultSet rs =  baseDao.executeQuery("yr", sql, objects);
			List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
			List<Integer> iidList = new ArrayList<Integer>();
			while(rs.next()){
				Map<String, Object> map = new HashMap<String, Object>();
				Double v = null;
				iidList.add(rs.getInt("iid"));
				String stcd = rs.getString("stcd").trim();
				map.put("stcd", stcd);
				if(!sttpMap.containsKey(stcd)){
					unstcdMap.put(stcd, "");
				}
				map.put("tm", rs.getTimestamp("tm"));
				map.put("modiTime", rs.getTimestamp("modiTime"));
				map.put("exkey", rs.getDouble("exkey"));
				map.put("vtavslm", rs.getDouble("vtavslm"));
				map.put("srlslm", rs.getDouble("srlslm"));
				map.put("slm", rs.getDouble("slm"));
				dataList.add(map);
			}
			baseDao.close();
			LogHelper.logInfoMsg("开始处理"+tname+": "+dataList.size()+"条语润数据！");
			if(dataList.size()>0){
				Integer insertCount = baseDao.InsertBatch("st8", dataList, tname+"_TMP");
				baseDao.close();
				LogHelper.logInfoMsg("批量插入"+tname+":"+insertCount+"条！语润数据！");
				DataHelper.mergeInto(tname);
				String iidStr = "";
				int deleteCount = 0;
				String sql0 =  "delete from GT_LOG where iid in (";
				for(int i=1;i<iidList.size()+1;i++){
					iidStr+=iidList.get(i-1)+",";
					if(i%1000==0){
						sql=sql0+iidStr;
						sql = sql.substring(0,sql.length()-1);
						sql += ")";
						Object[] objects3 = {};
						deleteCount += baseDao.executeUpdate("yr", sql, objects3);
						baseDao.close();
						iidStr = "";
					}
				}
				if(!iidStr.equals("")){
					sql=sql0+iidStr;
					sql = sql.substring(0,sql.length()-1);
					sql += ")";
					Object[] objects3 = {};
					deleteCount += baseDao.executeUpdate("yr", sql, objects3);
					baseDao.close();
				}
				LogHelper.logInfoMsg("deleteLog__"+tname+"："+deleteCount+"条数据成功");
			}
			LogHelper.logInfoMsg("电压数据处理完成！总共耗时:"+((new Date()).getTime()-startTime)/1000+"s");
		} catch (Exception e) {
			// TODO: handle exception
			LogHelper.logErrorMsg("ST_SOIL_R error："+e.toString());
		}
	}
	//获取蒸发信息
	public void getDAYEVData(Map<String, String> sttpMap){
		String tname = "ST_SOIL_R";
		try {
			LogHelper.logInfoMsg("开始获取"+tname+"数据！");
			Long startTime = new Date().getTime();
			//蒸发
			String sql = "SELECT top 10000 iid,a.stcd,a.tm,b.exkey,b.vtavslm,b.srlslm,b.slm,a.modiTime FROM GT_LOG a,ST_SOIL_R b WHERE a.TABLE_NAME = ? and a.stcd = b.stcd and a.tm = b.tm order by a.moditime asc ";
			BaseDao baseDao = new BaseDao();
			Object[] objects = {tname};
			ResultSet rs =  baseDao.executeQuery("yr", sql, objects);
			List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
			List<Integer> iidList = new ArrayList<Integer>();
			while(rs.next()){
				Map<String, Object> map = new HashMap<String, Object>();
				Double v = null;
				iidList.add(rs.getInt("iid"));
				String stcd = rs.getString("stcd").trim();
				map.put("stcd", stcd);
				if(!sttpMap.containsKey(stcd)){
					unstcdMap.put(stcd, "");
				}
				map.put("tm", rs.getTimestamp("tm"));
				map.put("modiTime", rs.getTimestamp("modiTime"));
				map.put("exkey", rs.getDouble("exkey"));
				map.put("vtavslm", rs.getDouble("vtavslm"));
				map.put("srlslm", rs.getDouble("srlslm"));
				map.put("slm", rs.getDouble("slm"));
				dataList.add(map);
			}
			baseDao.close();
			LogHelper.logInfoMsg("开始处理"+tname+": "+dataList.size()+"条语润数据！");
			if(dataList.size()>0){
				Integer insertCount = baseDao.InsertBatch("st8", dataList, tname+"_TMP");
				baseDao.close();
				LogHelper.logInfoMsg("批量插入"+tname+":"+insertCount+"条！语润数据！");
				DataHelper.mergeInto(tname);
				String iidStr = "";
				int deleteCount = 0;
				String sql0 =  "delete from GT_LOG where iid in (";
				for(int i=1;i<iidList.size()+1;i++){
					iidStr+=iidList.get(i-1)+",";
					if(i%1000==0){
						sql=sql0+iidStr;
						sql = sql.substring(0,sql.length()-1);
						sql += ")";
						Object[] objects3 = {};
						deleteCount += baseDao.executeUpdate("yr", sql, objects3);
						baseDao.close();
						iidStr = "";
					}
				}
				if(!iidStr.equals("")){
					sql=sql0+iidStr;
					sql = sql.substring(0,sql.length()-1);
					sql += ")";
					Object[] objects3 = {};
					deleteCount += baseDao.executeUpdate("yr", sql, objects3);
					baseDao.close();
				}
				LogHelper.logInfoMsg("deleteLog__"+tname+"："+deleteCount+"条数据成功");
			}
			LogHelper.logInfoMsg("电压数据处理完成！总共耗时:"+((new Date()).getTime()-startTime)/1000+"s");
		} catch (Exception e) {
			// TODO: handle exception
			LogHelper.logErrorMsg("ST_SOIL_R error："+e.toString());
		}
	}
}
