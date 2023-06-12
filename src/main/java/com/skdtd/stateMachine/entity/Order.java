package com.skdtd.stateMachine.entity;

import com.skdtd.stateMachine.enums.OrderStatus;
import lombok.Data;

@Data
public class Order {
    public final static String ORDER_KEY = Order.class.getName();
    private String id;
    private OrderStatus state;
}
