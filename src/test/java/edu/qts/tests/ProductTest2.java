package edu.qts.tests;

import edu.qts.utils.PayloadHelper;
import org.json.JSONObject;
import org.testng.annotations.Test;

import edu.qts.responsevalidation.ProductTest2ResponseValidation;
import edu.qts.routes.Routes;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class ProductTest2 extends BaseApiTest{
  @Test
  public void addProduct() {
	JSONObject o=  new JSONObject();
	  o.put("title", "Red Hatt");
	Response r=  RestAssured.given()
			//.contentType(ContentType.JSON)
			.body(o.toString()).when().post(Routes.addProductsUrl);
	ProductTest2ResponseValidation.validateAddProduct(r);	
  }
  
  
  @Test
  public void deleteProductById() throws Exception {
	  
	  
	Response r=  RestAssured.given().pathParam(PayloadHelper.getIdKey(), PayloadHelper.generateRandomNumber()).when().delete(Routes.deleteProductByIdUrl);
	ProductTest2ResponseValidation.validateDeleteProductById(r);
  }
  
  
  @Test
  public void updateProductById()throws Exception {
	  JSONObject o=new JSONObject();
	  o.put("title","updated product");
	Response r=  RestAssured.given().pathParam(PayloadHelper.getIdKey(), "3").body(o.toString()).when().put(Routes.updateProductByIdUrl);
	ProductTest2ResponseValidation.validateUpdateProductById(r);
	
	
  }
  
  
  
  
  
  
  
  
  
  
  
}
