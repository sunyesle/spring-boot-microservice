package com.sunyesle.inventory_service;

public record InventoryRequest(String skuCode, Integer quantity) {
}
