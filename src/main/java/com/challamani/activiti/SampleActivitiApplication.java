package com.challamani.activiti;

import com.challamani.activiti.model.Item;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.activiti.api.process.runtime.connector.Connector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
@Slf4j
public class SampleActivitiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SampleActivitiApplication.class, args);
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Bean
    public Connector processOrderConnector()  {

        return integrationContext -> {

            String orderId = integrationContext.getInBoundVariable("orderId", String.class);
            String orderStatus = integrationContext.getInBoundVariable("orderStatus", String.class);

            List<Item> orderItems = null;
            try {
                String orderItemsStr = OBJECT_MAPPER.writeValueAsString(integrationContext.getInBoundVariable("orderItems"));
                log.info("order items {}",orderItemsStr);
                orderItems = OBJECT_MAPPER.readValue(orderItemsStr, new TypeReference<ArrayList<Item>>() {
                });
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            log.info("order {} status {}", orderId, orderStatus);
            boolean isItemQuantityExceeding = orderItems.stream().anyMatch(item -> item.getQuantity() > 5);
            boolean isOrderItemsExceeding = orderItems.size() > 10;

            if(isItemQuantityExceeding || isOrderItemsExceeding){
                orderStatus = "PENDING";
            }else {
                orderStatus = "CONFIRMED";
            }

            log.info("order {} status {}", orderId, orderStatus);
            integrationContext.addOutBoundVariable("orderStatus", orderStatus);
            integrationContext.addOutBoundVariable("approved",
                    orderStatus.equalsIgnoreCase("CONFIRMED"));
            return integrationContext;
        };
    }

    @Bean
    public Connector packageServiceConnector() {
        return integrationContext -> {

            Map<String, Object> inBoundVariables = integrationContext.getInBoundVariables();
            String orderId = (String) inBoundVariables.get("orderId");
            String orderStatus = (String) inBoundVariables.get("orderStatus");
            log.info("< packageServiceConnector order {} status {}", orderId, orderStatus);
            if(orderStatus.equalsIgnoreCase("CONFIRMED")){
                log.info("packing is completed for the order {}", orderId);
                orderStatus = "ReadyToShip";
            }else{
                log.info("unexpected order {} has arrived to packing section", orderId);
                orderStatus = "PENDING";
            }
            integrationContext.addOutBoundVariable("orderStatus", orderStatus);
            log.info("< packageServiceConnector order {} status {}", orderId, orderStatus);
            return integrationContext;
        };
    }

    @Bean
    public UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager();
        String[][] usersGroupsAndRoles = {
                {"system", "password", "ROLE_ACTIVITI_USER", "GROUP_activitiTeam"},
                {"admin", "password", "ROLE_ACTIVITI_ADMIN", "GROUP_activitiTeam"},
        };
        for (String[] user : usersGroupsAndRoles) {
            List<String> authoritiesStrings = Arrays.asList(Arrays.copyOfRange(user, 2, user.length));
            log.info("> Registering new user: {} with the following Authorities [{}]", user[0], authoritiesStrings);

            inMemoryUserDetailsManager.createUser(new User(user[0], passwordEncoder().encode(user[1]),
                    authoritiesStrings.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())));
        }
        return inMemoryUserDetailsManager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
