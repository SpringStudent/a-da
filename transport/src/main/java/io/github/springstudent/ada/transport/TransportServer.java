package io.github.springstudent.ada.transport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TransportServer {
    public static void main(String[] args) {
        SpringApplication.run(TransportServer.class, args);
    }
}
