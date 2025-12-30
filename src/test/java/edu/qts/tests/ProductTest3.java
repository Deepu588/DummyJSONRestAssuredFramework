package edu.qts.tests;

import org.testng.annotations.Test;

import edu.qts.responsevalidation.ProductTest3ResponseValidation;
import edu.qts.routes.Routes;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class ProductTest3 {
  @Test
  public void getProductCategoriesList() {
	  
	 Response r= RestAssured.given().when().get(Routes.getProductCategoriesListUrl);
	  ProductTest3ResponseValidation.validateProductCategoriesList(r);
	  
  }
  
  @Test
  public void selectProductPrice() {
	Response r=  RestAssured.given().queryParams("limit", "4","select","price").when().get(Routes.selectProductPriceUrl);
	
	ProductTest3ResponseValidation.validateSelectProductPrice(r);
	
  }
  
  @Test
  public void selectProductTitle() {
	Response r=  RestAssured.given().queryParams("limit", "4","select","tile").when().get(Routes.selectProductTitleUrl);
	
	ProductTest3ResponseValidation.validateSelectproductTitle(r);
	
  }
  
  
  
}
