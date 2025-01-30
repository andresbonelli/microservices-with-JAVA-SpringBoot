package com.andresbonelli.microservices.product;

import com.andresbonelli.microservices.product.repository.ProductRepository;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests {

	@ServiceConnection
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.5");

	@LocalServerPort
	private Integer port;

	@Autowired
	private ProductRepository productRepository;

	private String testProductId;

	@BeforeEach
	void setUp() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
		productRepository.deleteAll();
		String requestBody = """
                {
                    "name": "testproduct",
                    "description": "this is a preloaded product test",
                    "price": 1000
                }
                """;
		Response response = RestAssured
				.given()
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post("/api/product");

		testProductId = response.jsonPath().getString("id");

	}

	static {
		mongoDBContainer.start();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void shouldCreateProduct() {
		String requestBody = """
				{
				    "name": "testProduct2",
				    "description": "this is a test product",
				    "price": 1200
				}
				""";
				RestAssured
				.given()
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post("/api/product")
				.then()
				.statusCode(201)
						.body("id", Matchers.notNullValue())
						.body("name", Matchers.equalTo("testProduct2"))
						.body("description", Matchers.equalTo("this is a test product"))
						.body("price", Matchers.equalTo(1200));


	}

	@Test
	void shouldReturnAllProducts() {
		RestAssured
				.given()
				.when()
				.get("/api/product")
				.then()
				.statusCode(200)
				.body("$", Matchers.hasSize(1));
	}
	@Test
	void shouldUpdateProduct() {
		String updateRequestBody = String.format("""
            {
                "id": "%s",
                "name": "updatedProduct",
                "description": "updated description",
                "price": 1300
            }
            """, testProductId);
		RestAssured
				.given()
				.contentType("application/json")
				.body(updateRequestBody)
				.when()
				.put("/api/product")
				.then()
				.statusCode(200)
				.body("id", Matchers.equalTo(testProductId))
				.body("name", Matchers.equalTo("updatedProduct"))
				.body("description", Matchers.equalTo("updated description"))
				.body("price", Matchers.equalTo(1300));
	}

	@Test
	void shouldDeleteProduct() {
		RestAssured
				.given()
				.when()
				.delete("/api/product/" + testProductId)
				.then()
				.statusCode(204);

		// Verify product is deleted
		RestAssured
				.given()
				.when()
				.get("/api/product/" + testProductId)
				.then()
				.statusCode(404);
	}


}
