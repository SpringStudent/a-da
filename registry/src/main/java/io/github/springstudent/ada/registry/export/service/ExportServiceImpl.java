package io.github.springstudent.ada.registry.export.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.netflix.appinfo.InstanceInfo;
import io.github.springstudent.ada.common.Constants;
import io.github.springstudent.ada.registry.export.core.EurekaEventListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;

@Service
public class ExportServiceImpl implements ExportService {

    @Resource
    private EurekaEventListener eurekaEventListener;

    private String nettyServer;

    private Random random = new Random();

    @Override
    public String getServiceInstance(String serviceName) throws Exception {
        List<InstanceInfo> instances = eurekaEventListener.getServiceCache().get(serviceName);
        if (instances == null || instances.isEmpty()) {
            return Constants.buildResponseBody("service" + serviceName + " not found", "", 500);
        } else {
            return Constants.buildResponseBody("success", instances.get(random.nextInt(instances.size())).getHomePageUrl(), 200);
        }
    }

    @Override
    public synchronized String getServiceNettyInstance() throws Exception {
        if (nettyServer == null) {
            List<InstanceInfo> instances = eurekaEventListener.getServiceCache().get(Constants.SERVICE_TRANSPORT);
            if (instances == null || instances.isEmpty()) {
                return Constants.buildResponseBody("service" + Constants.SERVICE_NETTY + " not found", "", 500);
            } else {
                String transportServer = instances.get(0).getHomePageUrl();
                String result = HttpRequest.get(transportServer + "/" + Constants.SERVICE_TRANSPORT + "/netty/server").timeout(10000).execute().body();
                JSONObject jsonObject = JSONUtil.parseObj(result);
                if (jsonObject.getInt("code").intValue() == 200) {
                    this.nettyServer = jsonObject.getStr("result");
                    return Constants.buildResponseBody("success", jsonObject.getStr("result"), 200);
                } else {
                    throw new IllegalStateException(jsonObject.getStr("msg"));
                }
            }
        } else {
            return Constants.buildResponseBody("success", this.nettyServer, 200);
        }
    }

}
