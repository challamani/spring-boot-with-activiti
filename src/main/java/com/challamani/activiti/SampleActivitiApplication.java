package com.challamani.activiti;

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

    @Bean
    public Connector processTextConnector() {

        return integrationContext -> {
            Map<String, Object> inBoundVariables = integrationContext.getInBoundVariables();
            String contentValue = (String) inBoundVariables.get("content");

            if (contentValue.contains("activiti")) {
                log.info("Approving content: {}", contentValue);
                integrationContext.addOutBoundVariable("approved",
                        true);
            } else {
                log.info("> Discarding content: {}", contentValue);
                integrationContext.addOutBoundVariable("approved",
                        false);
            }
            return integrationContext;
        };
    }

    @Bean
    public Connector tagTextConnector() {
        return integrationContext -> {
            String contentToTag = (String) integrationContext.getInBoundVariables().get("content");
            contentToTag += " - ACCEPTED";
            integrationContext.addOutBoundVariable("content",
                    contentToTag);
            log.info("Final Content: {}", contentToTag);
            return integrationContext;
        };
    }

    @Bean
    public Connector discardTextConnector() {
        return integrationContext -> {
            String contentToDiscard = (String) integrationContext.getInBoundVariables().get("content");
            contentToDiscard += " - REJECTED";
            integrationContext.addOutBoundVariable("content",
                    contentToDiscard);
            log.info("Final Content: {}", contentToDiscard);
            return integrationContext;
        };
    }

    @Bean
    public UserDetailsService userDetailsService() {
		InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager();
		String[][] usersGroupsAndRoles = {
				{"system", "password", "ROLE_ACTIVITI_USER"},
				{"admin", "password", "ROLE_ACTIVITI_ADMIN"},
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
