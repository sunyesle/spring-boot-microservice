package com.sunyesle.order_service;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderNumber;
    private String skuCode;
    private BigDecimal price;
    private Integer quantity;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public Order(String orderNumber, String skuCode, BigDecimal price, Integer quantity) {
        this.orderNumber = orderNumber;
        this.skuCode = skuCode;
        this.price = price;
        this.quantity = quantity;
        this.status = OrderStatus.PENDING;
    }

    public void confirm() {
        if(this.status != OrderStatus.PENDING){
            throw new IllegalStateException("Order can only be confirmed if it is in PENDING status");
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }
}
