package com.andresbonelli.microservices.order;

import com.andresbonelli.microservices.order.repository.OrderRepository;
import com.andresbonelli.microservices.order.stubs.InventoryClientStub;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.testcontainers.containers.MySQLContainer;
import org.hamcrest.Matchers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class OrderServiceApplicationTests {

	@ServiceConnection
	static final MySQLContainer mySqlContainer = new MySQLContainer("mysql:8.0.26")
			.withDatabaseName("order_service")
			.withUsername("root")
			.withPassword("root");

	@LocalServerPort
	private Integer port;

	@Autowired
	private OrderRepository orderRepository;

	private String testOrderId;

	@BeforeEach
	void setUp() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
		orderRepository.deleteAll();
		String requestBody = """
                {
                   "skuCode": "iphone_13",
                     "price": 1500,
                     "quantity": 1
                }
                """;
		InventoryClientStub.stubInventoryCall("iphone_13", 1);
		Response response = RestAssured
				.given()
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post("/api/order");

		testOrderId = response.jsonPath().getString("id");
	}

	static {
		mySqlContainer.start();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void shouldGetAllOrders() {
		RestAssured.given()
				.when()
				.get("/api/order")
				.then()
				.log().all()
				.statusCode(200)
				.body("size()", Matchers.equalTo(1));
	}

	@Test
	void shouldSubmitOrder() {
		String submitOrderJson = """
                {
                     "skuCode": "iphone_13",
                     "price": 1000,
                     "quantity": 1
                }
                """;
		InventoryClientStub.stubInventoryCall("iphone_13", 1);
		var responseBodyString = RestAssured.given()
				.contentType("application/json")
				.body(submitOrderJson)
				.when()
				.post("/api/order")
				.then()
				.log().all()
				.statusCode(201)
				.body("id", Matchers.notNullValue())
				.body("orderCode", Matchers.notNullValue())
				.body("skuCode", Matchers.equalTo("iphone_13"))
				.body("price", Matchers.equalTo(1000))
				.body("quantity", Matchers.equalTo(1));
	}

	@Test
	void shouldFailToSubmitOrder() {
		String submitOrderJson = """
                {
                     "skuCode": "invalid_sku",
                     "price": 1000,
                     "quantity": 1
                }
                """;
		InventoryClientStub.stubFailInventoryCall("invalid_sku",1);
		RestAssured.given()
				.contentType("application/json")
				.body(submitOrderJson)
				.when()
				.post("/api/order")
				.then()
				.log().all()
				.statusCode(400);
	}

	@Test
	void shouldGetOrderById() {
		RestAssured.given()
				.when()
				.get("/api/order/" + testOrderId)
				.then()
				.log().all()
				.statusCode(200)
				.body("id", Matchers.notNullValue())
				.body("orderCode", Matchers.notNullValue())
				.body("skuCode", Matchers.equalTo("iphone_13"))
				.body("price", Matchers.equalTo(1500.0F))
				.body("quantity", Matchers.equalTo(1));
	}

	@Test
	void shouldUpdateOrder() {
		String updateOrderJson = String.format(
				"""
                {
                    "id": "%s",
                    "skuCode": "iphone_13",
                    "price": 2000,
                    "quantity": 2
                }
                """, testOrderId
		);
		RestAssured.given()
				.contentType("application/json")
				.body(updateOrderJson)
				.when()
				.put("/api/order")
				.then()
				.log().all()
				.statusCode(200)
				.body("id", Matchers.notNullValue())
				.body("orderCode", Matchers.notNullValue())
				.body("skuCode", Matchers.equalTo("iphone_13"))
				.body("price", Matchers.equalTo(2000))
				.body("quantity", Matchers.equalTo(2));
	}

	@Test
	void shouldDeleteOrderById() {
		RestAssured.given()
				.when()
				.delete("/api/order/" + testOrderId)
				.then()
				.log().all()
				.statusCode(204);
		RestAssured
				.given()
				.when()
				.get("/api/product/" + testOrderId)
				.then()
				.statusCode(404);
	}
}
