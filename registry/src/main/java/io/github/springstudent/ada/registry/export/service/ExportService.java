package io.github.springstudent.ada.registry.export.service;

/**
 * @author ZhouNing
 * @date 2025/3/19 13:31
 **/
public interface ExportService {
    String SERVICE_STREAM = "stream";
    String SERVICE_TRANSPORT = "transport";

    static String buildResponseBody(String msg, String result, int code) {
        return "{\"msg\": \""+ msg +"\",\"code\": " + code + ",\"result\": \"" + result + "\"}";
    }

    String getServiceInstance(String serviceName);
}
