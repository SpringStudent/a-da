package io.github.springstudent.ada.registry.export.controller;

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
        return exportService.getServiceInstance(ExportService.SERVICE_STREAM);
    }

    @GetMapping("/transportInstance")
    public String transportInstance() {
        return exportService.getServiceInstance(ExportService.SERVICE_TRANSPORT);
    }
}
