package com.sunyesle.order_service.stub;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class InventoryClientStub {

    static public void stubInventoryCall(String skuCode, Integer quantity) {
        stubFor(get(urlEqualTo("/api/inventory?skuCode="+skuCode+"&quantity="+quantity))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type","application/json")
                        .withBody("true")));
    }
}
