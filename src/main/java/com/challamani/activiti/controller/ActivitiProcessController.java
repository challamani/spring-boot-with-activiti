package com.challamani.activiti.controller;

import com.challamani.activiti.ActivitiAuthnService;
import com.challamani.activiti.model.OrderProcessRequest;
import com.challamani.activiti.model.UserTask;
import com.challamani.activiti.service.UserTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ActivitiProcessController {

    private final ProcessRuntime processRuntime;
    private final ActivitiAuthnService activitiAuthnService;
    private final UserTaskService userTaskService;

    @PostMapping(value = "/start/{processDefinitionKey}")
    public ProcessInstance startProcess(@RequestBody OrderProcessRequest orderProcessRequest,
                                        @PathVariable String processDefinitionKey) {
        activitiAuthnService.authenticate("system");

        log.info("request processDefinitionKey {}, status variable value {} businessKey {}",
                processDefinitionKey,
                orderProcessRequest.getOrder().getStatus(),
                orderProcessRequest.getBusinessKey());

        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(processDefinitionKey)
                .withBusinessKey(orderProcessRequest.getBusinessKey())
                .withName("Process starting for the order# : " + orderProcessRequest.getOrder().getId())
                .withVariable("orderStatus", orderProcessRequest.getOrder().getStatus())
                .withVariable("orderId", orderProcessRequest.getOrder().getId())
                .withVariable("orderItems", orderProcessRequest.getOrder().getOrderItems())
                .build());

        log.info(">>> Created Process Instance: {}", processInstance);
        return processInstance;
    }

    @GetMapping(value = "/tasks")
    public List<UserTask>  getTasks() {
        return userTaskService.getActiveUserTasks();
    }

    @PutMapping(value = "/tasks/{taskId}")
    public UserTask updateTask(@PathVariable String taskId,
                               @RequestBody Map<String, Object> variables) {
        return userTaskService.updateUserTask(taskId,variables);
    }
}
