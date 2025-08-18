package io.github.springstudent.ada.registry.export.core;

import cn.hutool.http.HttpRequest;
import io.github.springstudent.ada.common.Constants;
import io.github.springstudent.ada.common.log.Log;
import io.github.springstudent.ada.common.utils.EmptyUtils;
import io.github.springstudent.ada.registry.export.service.ExportService;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EurekaEventListener {

    @Resource
    private DiscoveryClient discoveryClient;

    private final Map<String, Integer> instanceStatusMap = new ConcurrentHashMap<>();

    @EventListener
    public void onRegistered(EurekaInstanceRegisteredEvent event) {
        if (!event.isReplication()) {
            String appName = event.getInstanceInfo().getAppName().toLowerCase();
            String instanceId = event.getInstanceInfo().getInstanceId();
            String key = appName + ":" + instanceId;
            // 如果上次状态不是 ONLINE，则触发事件
            if (instanceStatusMap.get(key) == null || instanceStatusMap.get(key) != Constants.SERVICE_EVENT_ONLINE) {
                instanceStatusMap.put(key, Constants.SERVICE_EVENT_ONLINE);
                System.out.println("Eureka service online: " + appName + " - " + instanceId);
                eurekaServiceChange(appName, Constants.SERVICE_EVENT_ONLINE);
            }
        }
    }

    @EventListener
    public void onCanceled(EurekaInstanceCanceledEvent event) {
        if (!event.isReplication()) {
            String appName = event.getAppName().toLowerCase();
            String instanceId = event.getServerId();
            String key = appName + ":" + instanceId;
            // 如果上次状态不是 OFFLINE，则触发事件
            if (instanceStatusMap.get(key) == null || instanceStatusMap.get(key) != Constants.SERVICE_EVENT_OFFLINE) {
                instanceStatusMap.put(key, Constants.SERVICE_EVENT_OFFLINE);
                System.out.println("Eureka service offline: " + appName + " - " + instanceId);
                eurekaServiceChange(appName, Constants.SERVICE_EVENT_OFFLINE);
            }
        }
    }

    private void eurekaServiceChange(String appName, Integer eventType) {
        List<ServiceInstance> instances = discoveryClient.getInstances(Constants.SERVICE_TRANSPORT);
        if(EmptyUtils.isNotEmpty(instances)){
            String service = instances.get(0).getUri().toString();
            if (EmptyUtils.isNotEmpty(service)) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("serviceName", appName);
                paramMap.put("eventType", eventType);
                HttpRequest.get(service + "/" + Constants.SERVICE_TRANSPORT + "/netty/eurekaServiceChange").form(paramMap).timeout(10000).execute().body();
            }
        }
    }
}
