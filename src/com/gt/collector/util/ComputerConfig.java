package com.gt.collector.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class ComputerConfig {
	
	//=======================获取硬盘序列号==========================
	public static String getSerialNumber() {
	    String result = "";
	      try {
	        File file = File.createTempFile("realhowto",".vbs");
	        file.deleteOnExit();
	        FileWriter fw = new java.io.FileWriter(file);
	        String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
	                    +"Set colDrives = objFSO.Drives\n"
	                    +"Set objDrive = colDrives.item(\"" + "C" + "\")\n"
	                    +"Wscript.Echo objDrive.SerialNumber";  // see note
	        fw.write(vbs);
	        fw.close();
	        Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
	        BufferedReader input =
	          new BufferedReader
	            (new InputStreamReader(p.getInputStream()));
	        String line;
	        while ((line = input.readLine()) != null) {
	           result += line;
	        }
	        input.close();
	      }
	      catch(Exception e){
	          e.printStackTrace();
	      }
	      return result.trim();
	}
	
	
	//=============================获取主板序列号====================
	public static String getMisc() {
		  String result = "";
		  try {
		   File file = File.createTempFile("realhowto", ".vbs");
		   file.deleteOnExit();
		   FileWriter fw = new java.io.FileWriter(file);
		   String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
		     + "Set colItems = objWMIService.ExecQuery _ \n"
		     + "   (\"Select * from Win32_BaseBoard\") \n"
		     + "For Each objItem in colItems \n"
		     + "    Wscript.Echo objItem.SerialNumber \n"
		     + "    exit for  ' do the first cpu only! \n" + "Next \n";
		   fw.write(vbs);
		   fw.close();
		   Process p = Runtime.getRuntime().exec(
		     "cscript //NoLogo " + file.getPath());
		   BufferedReader input = new BufferedReader(new InputStreamReader(p
		     .getInputStream()));
		   String line;
		   while ((line = input.readLine()) != null) {
		    result += line;
		   }
		   input.close();
		  } catch (Exception e) {
		   e.printStackTrace();
		  }
		  return result.trim();
	}
	
	public static String getCpuUtil() throws IOException {
		Process process = Runtime.getRuntime().exec(new String[] { "wmic", "cpu", "get", "ProcessorId" });
		process.getOutputStream().close();
		Scanner sc = new Scanner(process.getInputStream());
		String property = sc.next();
		String serial = sc.next();
		return serial;
	}
	
	
	
}
