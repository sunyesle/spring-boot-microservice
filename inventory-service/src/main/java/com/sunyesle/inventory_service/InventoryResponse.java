package com.sunyesle.inventory_service;

public record InventoryResponse(Long id, String skuCode, Integer quantity) {
}
