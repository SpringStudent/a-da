package io.github.springstudent.ada.clipboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableDiscoveryClient
public class ClipboardServer {
    public static void main(String[] args) {
        SpringApplication.run(ClipboardServer.class, args);
    }
}