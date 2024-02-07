package com.challamani.activiti.model;

import lombok.Data;

import java.util.Map;

@Data
public class ProcessRequest {

    private String processDefinitionKey;
    private Map<String,Object> variables;
    private String businessKey;
    private String content;

}
