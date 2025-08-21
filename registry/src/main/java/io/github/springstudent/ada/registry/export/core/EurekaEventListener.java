package io.github.springstudent.ada.registry.export.core;

import cn.hutool.http.HttpRequest;
import com.netflix.appinfo.InstanceInfo;
import io.github.springstudent.ada.common.Constants;
import io.github.springstudent.ada.common.utils.EmptyUtils;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class EurekaEventListener {

    private final Map<String, Integer> instanceStatusMap = new ConcurrentHashMap<>();

    private final Map<String, List<InstanceInfo>> serviceCache = new ConcurrentHashMap<>();

    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    @EventListener
    public void onRegistered(EurekaInstanceRegisteredEvent event) {
        if (!event.isReplication()) {
            String appName = event.getInstanceInfo().getAppName().toLowerCase();
            String instanceId = event.getInstanceInfo().getInstanceId();
            String key = appName + ":" + instanceId;

            // 如果上次状态不是 ONLINE，则触发事件
            if (instanceStatusMap.get(key) == null || instanceStatusMap.get(key) != Constants.SERVICE_EVENT_ONLINE) {
                instanceStatusMap.put(key, Constants.SERVICE_EVENT_ONLINE);
                serviceCache.compute(event.getInstanceInfo().getAppName().toLowerCase(), (k, v) -> {
                    if (v == null) v = new ArrayList<>();
                    v.add(event.getInstanceInfo());
                    return v;
                });
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
                serviceCache.computeIfPresent(appName, (k, v) -> {
                    if (v != null) {
                        v.removeIf(instanceInfo -> instanceInfo.getInstanceId().equals(instanceId));
                        if (v.isEmpty()) {
                            return null;
                        }
                    }
                    return v;
                });
                System.out.println("Eureka service offline: " + appName + " - " + instanceId);
                eurekaServiceChange(appName, Constants.SERVICE_EVENT_OFFLINE);
            }
        }
    }

    private void eurekaServiceChange(String appName, Integer eventType) {
        executor.submit(() -> {
            List<InstanceInfo> instances = serviceCache.get(Constants.SERVICE_TRANSPORT);
            if (EmptyUtils.isNotEmpty(instances)) {
                String service = instances.get(0).getHomePageUrl().toString();
                if (EmptyUtils.isNotEmpty(service)) {
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("serviceName", appName);
                    paramMap.put("eventType", eventType);
                    HttpRequest.get(service + "/" + Constants.SERVICE_TRANSPORT + "/netty/eurekaServiceChange").form(paramMap).timeout(10000).execute().body();
                }
            }
        });
    }

    public Map<String, List<InstanceInfo>> getServiceCache() {
        return serviceCache;
    }
}
