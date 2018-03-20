package com.gt.collector.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gt.collector.dao.BaseDao;

public class DataHelper {
	public static void mergeInto(String tname){
		try {
			BaseDao baseDao = new BaseDao();
			List<Object> list_param = new ArrayList<Object>();
			list_param.add(tname);
			List<Map<String, List<Object>>> table_List = baseDao.execProc("st8","GT_MERGE_TABLE", list_param);//调用存储过程
			baseDao.close();
			if(table_List.size()>0){
				String flag = (String) table_List.get(0).get("Flag").get(0);
				Integer updateRcount = (Integer) table_List.get(0).get("UpdateRcount").get(0);
				Integer insertRcount = (Integer) table_List.get(0).get("InsertRcount").get(0);
				if("success".equals(flag)){
					LogHelper.logInfoMsg("数据处理完毕："+tname+", 插入 " +insertRcount + " 条，更新 " + updateRcount + " 条!");
				}
				if("error".equals(flag)){
					LogHelper.logInfoMsg("表"+tname+"MERGE失败！！");
				}	
			}else{
				LogHelper.logErrorMsg("MERGE没有返回值！！");
			}
		} catch (Exception e) {
			LogHelper.logErrorMsg("DataHelper的mergeInto出现漏洞！errInfo:"+e.toString());
		}
	}
}
