package com.sunyesle.order_service;

import com.sunyesle.order_service.stub.InventoryClientStub;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class OrderServiceApplicationTests {

	@LocalServerPort
	private int port;

	@BeforeEach
	void setUp() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
	}

	@Test
	void 주문을_생성한다() {
		String requestBody = """
				{
				    "skuCode": "AA000000",
				    "price": 89000,
				    "quantity": 1,
				    "userDetails": {
				        "email": "test@gmail.com",
				        "firstName": "John",
				        "lastName": "Doe"
				    }
				}
				""";
		InventoryClientStub.stubInventoryCall("AA000000", 1);

		RestAssured.given()
				.contentType(ContentType.JSON)
				.body(requestBody)
				.when()
				.post("/api/orders")
				.then()
				.statusCode(201)
				.body(Matchers.equalTo("Order Placed Successfully"));
	}
}
