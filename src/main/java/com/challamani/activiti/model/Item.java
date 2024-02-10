package com.challamani.activiti.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Item implements Serializable {
    private static final long serialVersionUID = 10000000L;
    private Integer id;
    private String name;
    private Integer quantity;
    private Double price;
}
