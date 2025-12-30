package edu.qts.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

public class ExtractDataFromExcel {

	
	
	
	
	
	 public static String excel(String label,String sheetname) throws Exception {
	        // TODO Auto-generated method stub
	        ArrayList<String> a = new ArrayList<>();
	        FileInputStream inputStream = new FileInputStream(System.getProperty("user.dir") + "/src/test/resources/Files/TestData.XLSX");
	        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
	        int sheets = workbook.getNumberOfSheets();
	        for (int i = 0; i < sheets; i++) {
	            if (workbook.getSheetName(i).equalsIgnoreCase(sheetname)) {
	                XSSFSheet sheet = workbook.getSheetAt(i);
	                Iterator<Row> rows = sheet.rowIterator();
	                Row firstrow = rows.next();
	                Iterator<Cell> ce = firstrow.cellIterator();
	                while (rows.hasNext()) {
	                    Row r = rows.next();
	                    if (r.getCell(0) != null) {
	                        if (r.getCell(0).getStringCellValue().equalsIgnoreCase(label)) {
	                            Iterator<Cell> c = r.cellIterator();
	                            while (c.hasNext()) {
	                                Cell cv= c.next();
	                               // cv.
	                                if(cv.getCellType() == CellType.STRING)
	                                {
	                                    a.add(cv.getStringCellValue());
	                                }
	                                else{
	                                    a.add(NumberToTextConverter.toText(cv.getNumericCellValue()));
	                                }
	                            }
	                        }
	                    }
	                }
	            }
	        }
	        return a.get(1);
	    }

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
