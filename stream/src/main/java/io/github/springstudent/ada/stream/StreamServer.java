package io.github.springstudent.ada.stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author ZhouNing
 * @date 2025/1/20 16:23
 **/
@SpringBootApplication
@EnableDiscoveryClient
public class StreamServer {

    public static void main(String[] args) {
        SpringApplication.run(StreamServer.class, args);
    }

}
