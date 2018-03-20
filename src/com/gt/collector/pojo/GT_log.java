package com.gt.collector.pojo;

import java.util.Date;

public class GT_log {
	private Integer iid;
	private String tableName;
	private String stcd;
	private Date tm;
	private Date modiTime;
	public Integer getIid() {
		return iid;
	}
	public void setIid(Integer iid) {
		this.iid = iid;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getStcd() {
		return stcd;
	}
	public void setStcd(String stcd) {
		this.stcd = stcd;
	}
	public Date getTm() {
		return tm;
	}
	public void setTm(Date tm) {
		this.tm = tm;
	}
	public Date getModiTime() {
		return modiTime;
	}
	public void setModiTime(Date modiTime) {
		this.modiTime = modiTime;
	}
	@Override
	public String toString() {
		return "GT_log [iid=" + iid + ", tableName=" + tableName + ", stcd="
				+ stcd + ", tm=" + tm + ", modiTime=" + modiTime + "]";
	}
	
	
	
	
}
