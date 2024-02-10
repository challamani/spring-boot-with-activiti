package com.challamani.activiti.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Order implements Serializable {

    private static final long serialVersionUID = 10000000L;
    private List<Item> orderItems;
    private String status;
    private String deliveryAddress;
    private Double derivedPrice;
    private String id;
}
