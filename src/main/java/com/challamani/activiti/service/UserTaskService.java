package com.challamani.activiti.service;

import com.challamani.activiti.ActivitiAuthnService;
import com.challamani.activiti.model.UserTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserTaskService {

    private final TaskService taskService;
    private final ActivitiAuthnService activitiAuthnService;

    public List<UserTask> getActiveUserTasks() {

        return taskService.createTaskQuery().active().list().stream()
                .map(task -> createUserTaskInfo(task))
                .collect(Collectors.toList());
    }

    public UserTask updateUserTask(String taskId, Map<String, Object> variables){

        activitiAuthnService.authenticate("system");

        String userTaskId = taskService.createTaskQuery()
                .active().taskId(taskId)
                .list().stream().map(task -> task.getId())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("invalid userTask#"+taskId));

        log.info("incoming variables {}", variables);
        //Completing the userTask with incoming variables
        taskService.complete(userTaskId,variables);

        return taskService.createTaskQuery().list().stream()
                .map(task -> createUserTaskInfo(task))
                .findFirst().orElseThrow(()->new RuntimeException("failed at updateUserTask:"+userTaskId));
    }

    private UserTask createUserTaskInfo(Task task) {
        UserTask userTask = new UserTask();
        userTask.setId(task.getId());
        log.info("populating user task details {}", task.getId());
        userTask.setDescription(task.getDescription());
        userTask.setOwner(task.getOwner());
        userTask.setProcessInstanceId(task.getProcessInstanceId());
        userTask.setBusinessKey(task.getBusinessKey());
        userTask.setCreateTime(task.getCreateTime());
        userTask.setClaimTime(task.getClaimTime());
        userTask.setName(task.getName());

        Map<String, Object> localVariables = new HashMap<>();
        task.getTaskLocalVariables().forEach(
                (key, value) -> localVariables.put(key, value)
        );
        userTask.setLocalVariables(localVariables);
        return userTask;
    }
}
