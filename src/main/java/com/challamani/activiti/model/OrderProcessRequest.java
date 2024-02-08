package com.challamani.activiti.model;

import lombok.Data;


@Data
public class OrderProcessRequest {

    private String businessKey;
    private Order order;
}
