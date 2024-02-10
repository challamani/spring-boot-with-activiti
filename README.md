# spring-boot-with-activiti
Activiti is an Apache-licensed BPM engine, which we can use to define the process, a process can consist of a set of human tasks and service tasks.

Activiti provides the following features:
* BPMN2.0 Support 
  * Sub-processing
  * Rest API engine
  * Retry feature for the service tasks 
  * Async service task handling
  * User task handling
  * Activity Auditing 

This repo is to explore some features of Activiti, 

You can find an example BPMN process definition under `/resources/processes/order-process.bpmn20.xml`, the process will get loaded into the system as part of the application startup, 
As this app is using in-memory DB(H2) it doesn't persist runtime and completed process details in disk.

You can access the [H2 database console](http://localhost:9090/activiti/h2-console) by using the database url from logs.

## Start
You can start the app using mvn command `mvn spring-boot:run`

## Start the Order Process Flow using the rest-api

![Image](src/main/resources/img/order-process-bpmn.png)


### Start and complete the process only with service-tasks

*Start the order process flow with item quantity less than 5, the process should complete fully.*
```shell
curl --location 'http://localhost:9090/activiti/start/orderProcess' --header 'Content-Type: application/json' \
--data '{
	"order":{
        "id":"OR89002",
        "status":"initiated",
        "orderItems":[
            {
                "id":89001,
                "name":"Chocolate Box",
                "quantity": 5,
                "price": 100
            }
        ]
    },
    "businessKey":"businessKey001"
}'
```

*Response*
```json
{
    "id": "dc933adb-c6c4-11ee-bf73-964cf6ae4bd3",
    "name": "Process starting for the order# : OR89001",
    "processDefinitionId": "orderProcess:1:ccce0f29-c6c4-11ee-bf73-964cf6ae4bd3",
    "processDefinitionKey": "orderProcess",
    "initiator": "system",
    "startDate": "2024-02-08T20:58:51.164+00:00",
    "businessKey": "businessKey009",
    "status": "COMPLETED",
    "processDefinitionVersion": 1,
    "processDefinitionName": "orderProcess"
}
```

### Start and complete the process instance with a service-task and user-task.

*1. Start the order process flow with item quantity greater than 5, the process should wait at user-task.*
```shell
curl --location 'http://localhost:9090/activiti/start' --header 'Content-Type: application/json' \
--data '{
	"processDefinitionKey":"orderProcess",
    "order":{
        "id":"OR89002",
        "status":"initiated",
        "orderItems":[
            {
                "id":89001,
                "name":"Chocolate Box",
                "quantity": 100,
                "price": 100
            }
        ]
    },
    "businessKey":"businessKey001"
}'
```

*Response*
```json
{
    "id": "f0823a17-c6c4-11ee-bf73-964cf6ae4bd3",
    "name": "Process starting for the order# : OR89005",
    "processDefinitionId": "orderProcess:1:ccce0f29-c6c4-11ee-bf73-964cf6ae4bd3",
    "processDefinitionKey": "orderProcess",
    "initiator": "system",
    "startDate": "2024-02-08T20:59:24.605+00:00",
    "businessKey": "businessKey009",
    "status": "RUNNING",
    "processDefinitionVersion": 1,
    "processDefinitionName": "orderProcess"
}
```

*2. Get active userTasks list*
```curl
curl --location --request GET 'http://localhost:9090/activiti/tasks
```

*Response:*
```json
[
  {
    "id": "8ec20658-c816-11ee-9fea-dac69f7d3eaf",
    "parentProcessId": "8ec0cdcd-c816-11ee-9fea-dac69f7d3eaf",
    "businessKey": "businessKey007",
    "name": "Verify the order details",
    "createTime": "2024-02-10T13:16:10.498+00:00"
  },
  {
    "id": "8f17ee98-c816-11ee-9fea-dac69f7d3eaf",
    "parentProcessId": "8f16dd1d-c816-11ee-9fea-dac69f7d3eaf",
    "businessKey": "businessKey008",
    "name": "Verify the order details",
    "createTime": "2024-02-10T13:16:11.060+00:00"
  }
]
```

*3. Complete the userTask*
```curl
curl --location --request PUT 'http://localhost:9090/activiti/tasks/d4796866-c6c4-11ee-bf73-964cf6ae4bd3'
```

*Response:*
```json
{
    "userTaskId": "d4796866-c6c4-11ee-bf73-964cf6ae4bd3",
    "status": "COMPLETED"
}
```