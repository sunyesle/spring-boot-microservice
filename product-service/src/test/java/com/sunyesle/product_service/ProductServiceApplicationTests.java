package com.sunyesle.product_service;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void 상품을_생성한다() {
        String requestBody = """
                {
                    "name": "블루투스 이어폰",
                    "description": "노이즈 캔슬링 기능을 갖춘 무선 이어폰입니다.",
                    "skuCode": "AA000000",
                    "price": 89000
                }
                """;

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("name", Matchers.equalTo("블루투스 이어폰"))
                .body("description", Matchers.equalTo("노이즈 캔슬링 기능을 갖춘 무선 이어폰입니다."))
                .body("skuCode", Matchers.equalTo("AA000000"))
                .body("price", Matchers.equalTo(89000));
    }
}
