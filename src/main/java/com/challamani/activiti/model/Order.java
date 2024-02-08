package com.challamani.activiti.model;

import lombok.Data;

import java.util.List;

@Data
public class Order {

    private List<Item> orderItems;
    private String status;
    private String deliveryAddress;
    private Double derivedPrice;
    private String id;
}
