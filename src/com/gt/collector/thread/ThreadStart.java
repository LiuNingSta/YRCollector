package com.gt.collector.thread;

import com.gt.collector.servlet.YRCollectorServlet;

public class ThreadStart implements Runnable{  
	
    public void run() {
    	YRCollectorServlet yrcollector= new YRCollectorServlet();
    	yrcollector.getYRData();
    }  
}  
