package com.challamani.activiti.model;

import lombok.Data;


@Data
public class OrderProcessRequest {

    private static final long serialVersionUID = 10000000L;
    private String businessKey;
    private Order order;
}
