package edu.qts.utils;


import com.aventstack.extentreports.ExtentTest;

public class TestDetailsAssigner {

	

	public static void assignDetailsToTest(ExtentTest test ,String testmethodName) {
		test.assignAuthor(System.getProperty("user.name"));
		test.assignDevice(System.getProperty("os.name")+" Laptop");
		
		switch (testmethodName) {
		
		case "getProductById":{
			test.assignCategory(TimeStampAndNamesManager.tag1);
			test.getModel().setDescription("Fetches a single product’s full details using the product ID.");
		}
		
		case "getProductsByLimit":{
			test.assignCategory(TimeStampAndNamesManager.tag1);

			test.getModel().setDescription("Retrieves a paginated list of products based on the specified limit parameter.");
			break;
		}
		case "deleteProductById":{
			test.assignCategory(TimeStampAndNamesManager.tag4);
			test.getModel().setDescription("Deletes a product based on the ID and returns the deleted product details with delete status.");
			break;
		}
		case "getProductCategoriesList":{
			test.assignCategory(TimeStampAndNamesManager.tag1);
			test.getModel().setDescription("Retrieves the complete list of all product categories available in the system.");
			break;
		}
		case "updateProductById":{
			test.assignCategory(TimeStampAndNamesManager.tag3);

			test.getModel().setDescription("Updates product fields (price, title, stock, etc.) for the specified product ID.");
			break;
		}
		case "addProduct":{
			test.assignCategory(TimeStampAndNamesManager.tag2);

			test.getModel().setDescription("Creates a new product with the given payload and returns the newly added product details.");
			break;
		}
		
		case "selectProductTitle":{
			test.assignCategory(TimeStampAndNamesManager.tag1);
			test.getModel().setDescription("Returns the product titles only (subset of product data), useful for filtering and UI validation.");
			break;
		}
		
		case "selectProductPrice":{
			test.assignCategory(TimeStampAndNamesManager.tag1);
			test.getModel().setDescription("Fetches product prices only (subset of product data), used for price comparison or validation.");
			break;
		}
		case "getProductCategories":{
			test.assignCategory(TimeStampAndNamesManager.tag1);
			test.getModel().setDescription("Retrieves products grouped under a specific category.");
			break;
		}
		
	
		
		
		default:
			break;
		}


	}





}