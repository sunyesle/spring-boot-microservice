package com.sunyesle.inventory_service;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apiguardian.api.API;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InventoryServiceApplicationTests {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void 상품_재고_여부를_확인한다() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .param("skuCode", "AA000000")
                .param("quantity", 1)
                .when()
                .get("/api/inventory")
                .then()
                .statusCode(200)
                .body(Matchers.equalTo("true"));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .param("skuCode", "AA000000")
                .param("quantity", 1000)
                .when()
                .get("/api/inventory")
                .then()
                .statusCode(200)
                .body(Matchers.equalTo("false"));
    }
}
