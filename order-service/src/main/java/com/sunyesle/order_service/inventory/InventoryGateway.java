package com.sunyesle.order_service.inventory;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class InventoryGateway {
    private final InventoryClient inventoryClient;

    public Integer getStock(String skuCode) {
        return inventoryClient.getStock(skuCode).quantity();
    }
}
