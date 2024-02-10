package com.challamani.activiti.model;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class UserTask {
    private String id;
    private String description;
    private String processInstanceId;
    private String owner;
    private String businessKey;
    private String name;
    private Date createTime;
    private Date claimTime;
    private Map<String, Object> localVariables;
}
