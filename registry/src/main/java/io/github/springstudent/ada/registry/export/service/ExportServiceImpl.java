package io.github.springstudent.ada.registry.export.service;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;

@Service
public class ExportServiceImpl implements ExportService {

    @Resource
    private DiscoveryClient discoveryClient;

    private Random random = new Random();

    @Override
    public String getServiceInstance(String serviceName) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        if (instances == null || instances.isEmpty()) {
            return ExportService.buildResponseBody("service" + serviceName + " not found", "", 500);

        } else {
            return ExportService.buildResponseBody("success", instances.get(random.nextInt(instances.size())).getUri().toString(), 200);
        }
    }
}
