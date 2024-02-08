package com.challamani.activiti.controller;

import com.challamani.activiti.ActivitiAuthnService;
import com.challamani.activiti.model.OrderProcessRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ActivitiProcessController {

    private final ProcessRuntime processRuntime;
    private final TaskService taskService;
    private final ActivitiAuthnService activitiAuthnService;

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
    public List<String>  getTasks() {
        return taskService.createTaskQuery().active()
                .list().stream()
                .map(task -> task.getId()).collect(Collectors.toList());
    }

    @PutMapping(value = "/tasks/{taskId}")
    public Map<String, String> updateTask(@PathVariable String taskId) {
        activitiAuthnService.authenticate("system");

        String userTaskId = taskService.createTaskQuery().taskId(taskId)
                .list().stream().map(task -> task.getId())
                .collect(Collectors.toList()).get(0);

        taskService.complete(userTaskId,
                Map.of("orderStatus", "COMPLETED",
                        "manualVerification", "COMPLETED",
                        "note", "Nothing suspicious"));

        return Map.of("userTaskId", userTaskId, "status", "COMPLETED");
    }
}
