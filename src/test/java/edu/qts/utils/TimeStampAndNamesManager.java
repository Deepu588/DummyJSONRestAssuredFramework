package edu.qts.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeStampAndNamesManager {
	
	

	public static String tag1="GET";
	public static String tag2="POST";
	public static String tag3="PUT";
	public static String tag4="DELETE";
	public static  String getReportName() {
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		String reportName = "Test_Report-" + timeStamp + ".html";
		
		return reportName;
		
	}
	
	
	
	public static String getConfigFilePath() {
		String file= System.getProperty("user.dir")+File.separator+"src"+File.separator+"test"+File.separator+"resources"+File.separator+"config.properties";
		return file;
	}

	

	
	
}
