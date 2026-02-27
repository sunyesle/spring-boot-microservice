package com.sunyesle.order_service.stub;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class InventoryClientStub {

    static public void stubInventoryIsInStockCall(String skuCode, Integer quantity, boolean responseBody) {
        stubFor(get(urlEqualTo("/api/inventory?skuCode=" + skuCode + "&quantity=" + quantity))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(String.valueOf(responseBody))));
    }

    static public void stubInventoryGetStockCall(String skuCode, String responseBody) {
        stubFor(get(urlEqualTo("/api/inventory/" + skuCode))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));
    }
}
