package io.github.springstudent.ada.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author ZhouNing
 * @date 2025/3/19 10:20
 **/
@SpringBootApplication
@EnableEurekaServer
@EnableDiscoveryClient
public class RegistryApplication {
    public static void main(String[] args) {
        SpringApplication.run(RegistryApplication.class, args);
    }

}
