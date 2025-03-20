package io.github.springstudent.ada.registry.export.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.springstudent.ada.common.Constants;
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

    private String nettyServer;

    private Random random = new Random();

    @Override
    public String getServiceInstance(String serviceName) throws Exception {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        if (instances == null || instances.isEmpty()) {
            return ExportService.buildResponseBody("service" + serviceName + " not found", "", 500);
        } else {
            return ExportService.buildResponseBody("success", instances.get(random.nextInt(instances.size())).getUri().toString(), 200);
        }
    }

    @Override
    public synchronized String getServiceNettyInstance() throws Exception {
        if (nettyServer == null) {
            List<ServiceInstance> instances = discoveryClient.getInstances(Constants.SERVICE_TRANSPORT);
            if (instances == null || instances.isEmpty()) {
                return ExportService.buildResponseBody("service" + Constants.SERVICE_NETTY + " not found", "", 500);
            } else {
                String transportServer = instances.get(random.nextInt(instances.size())).getUri().toString();
                String result = HttpRequest.get(transportServer + "/" + Constants.SERVICE_TRANSPORT + "/netty/server").timeout(10000).execute().body();
                JSONObject jsonObject = JSONUtil.parseObj(result);
                if (jsonObject.getInt("code").intValue() == 200) {
                    return ExportService.buildResponseBody("success", jsonObject.getStr("result"), 200);
                } else {
                    throw new IllegalStateException(jsonObject.getStr("msg"));
                }
            }
        } else {
            return nettyServer;
        }
    }
}
