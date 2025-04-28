package com.example.UserService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient; // ou @EnableEurekaClient
import org.springframework.cloud.openfeign.EnableFeignClients; // Pour activer les clients Feign
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@SpringBootApplication
@EnableDiscoveryClient 
@EnableFeignClients 
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Bean // Bean pour encoder les mots de passe
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
