package io.github.springstudent.ada.registry.export.controller;

import io.github.springstudent.ada.common.Constants;
import io.github.springstudent.ada.registry.export.service.ExportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author ZhouNing
 * @date 2025/3/19 13:25
 **/
@RestController
@RequestMapping("/registry")
public class ExportController {

    @Resource
    private ExportService exportService;

    @GetMapping("/streamInstance")
    public String streamInstance() {
        try {
            return exportService.getServiceInstance(Constants.SERVICE_STREAM);
        }catch (Exception e){
            return ExportService.buildResponseBody(e.getMessage(), "", 500);
        }
    }

    @GetMapping("/transportInstance")
    public String transportInstance() {
        try {
            return exportService.getServiceInstance(Constants.SERVICE_TRANSPORT);
        }catch (Exception e){
            return ExportService.buildResponseBody(e.getMessage(), "", 500);
        }
    }

    @GetMapping("/nettyInstance")
    public String nettyInstance(){
        try {
            return exportService.getServiceNettyInstance();
        } catch (Exception e) {
            return ExportService.buildResponseBody(e.getMessage(), "", 500);
        }
    }
}
