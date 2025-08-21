package io.github.springstudent.ada.registry.export.service;

/**
 * @author ZhouNing
 * @date 2025/3/19 13:31
 **/
public interface ExportService {

    String getServiceInstance(String serviceName)throws Exception;

    String getServiceNettyInstance()throws Exception;

}
