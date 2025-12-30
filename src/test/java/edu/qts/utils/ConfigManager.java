package edu.qts.utils;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class ConfigManager {

	
	
private static Properties p;
public ConfigManager() {
	
}
	
	public static void ConfigManage()  {

        p = new Properties();
        FileReader f = null;
        String path=TimeStampAndNamesManager.getConfigFilePath();
        		
		try {
			//f = new FileReader("./src//test//resources//config.properties");
			f = new FileReader(path);
	        p.load(f);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String getValue(String key) {
		ConfigManage();
		return p.getProperty(key);
	}
	
	
	public static String getProjectDirectoryForFailureReport() {
        return System.getProperty("user.dir")+File.separator+"test-failure-reports";
    }
	
	
}
