package com.andresbonelli.microservices.inventory;

import com.andresbonelli.microservices.inventory.repository.InventoryRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InventoryServiceApplicationTests {

	@ServiceConnection
	static final MySQLContainer mySqlContainer = new MySQLContainer("mysql:8.0.26")
			.withDatabaseName("inventory_service")
			.withUsername("root")
			.withPassword("root");

	@LocalServerPort
	private Integer port;

	@Autowired
	private InventoryRepository inventoryRepository;

	@BeforeEach
	void setUp() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
	}

	static {
		mySqlContainer.start();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void shouldBeInStock() {
		boolean response = RestAssured.given()
				.when()
				.get("/api/inventory?skuCode=iphone_13&quantity=100")
				.then()
				.log().all()
				.statusCode(200)
				.extract()
				.body()
				.as(Boolean.class);
		assertTrue(response);
	}

	@Test
	void shouldNotBeInStock() {
		boolean response = RestAssured.given()
				.when()
				.get("/api/inventory?skuCode=iphone_13&quantity=101")
				.then()
				.log().all()
				.statusCode(200)
				.extract()
				.body()
				.as(Boolean.class);

		assertFalse(response);

	}
}

