package com.challamani.activiti.controller;

import com.challamani.activiti.ActivitiAuthnService;
import com.challamani.activiti.model.ProcessRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ActivitiProcessController {

    private final ProcessRuntime processRuntime;
    private final ActivitiAuthnService activitiAuthnService;

    @PostMapping(value = "/start")
    public String startProcess(@RequestBody ProcessRequest processRequest) {
        activitiAuthnService.authenticate("system");
        //categorizeProcess
        log.info("request processDefinitionKey {}, content variable value {} businessKey {}",
                processRequest.getProcessDefinitionKey(),
                processRequest.getContent(),
                processRequest.getBusinessKey());

        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(processRequest.getProcessDefinitionKey())
                .withBusinessKey(processRequest.getBusinessKey())
                .withName("Process starting with variables: " + processRequest.getVariables().size())
                .withVariable("content", processRequest.getContent())
                .build());

        log.info(">>> Created Process Instance: {}", processInstance);
        return processInstance.getBusinessKey();
    }

}
