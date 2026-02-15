package com.sunyesle.inventory_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public InventoryResponse addStock(InventoryRequest request) {
        Inventory inventory = new Inventory(request.skuCode(), request.quantity());
        inventoryRepository.save(inventory);
        log.info("Inventory created successfully");
        return new InventoryResponse(inventory.getId(), inventory.getSkuCode(), inventory.getQuantity());
    }

    @Transactional(readOnly = true)
    public boolean isInStock(String skuCode, Integer quantity) {
        return inventoryRepository.existsBySkuCodeAndQuantityIsGreaterThanEqual(skuCode, quantity);
    }
}
