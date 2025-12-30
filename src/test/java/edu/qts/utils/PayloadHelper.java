package edu.qts.utils;

import com.github.javafaker.Faker;


public class PayloadHelper {

public static String generateRandomNumber() {
	
	int randomNumber=	(int)	Math.floor(	Math.random()*10);
	System.out.println(randomNumber);
	System.out.println(new Faker().job().title());
return	String.valueOf(randomNumber);
	
}


//public static void main(String[] args) {
	//generateRandomNumber();
//}
//}
public static String getLimitKey() throws Exception{
   String value= ExtractDataFromExcel.excel("GetProductsByLimitQueryParameter","Products");
return value;
}

public static String getIdKey() throws Exception{


    String value=ExtractDataFromExcel.excel("GetProductByIdPathParameter","Products");
    return value;
}




}




