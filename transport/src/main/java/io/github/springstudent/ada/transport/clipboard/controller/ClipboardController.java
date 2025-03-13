package io.github.springstudent.ada.transport.clipboard.controller;

import io.github.springstudent.ada.transport.clipboard.pojo.Clipboard;
import io.github.springstudent.ada.transport.clipboard.service.ClipboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author ZhouNing
 * @date 2024/12/31 9:33
 **/
@RestController
@RequestMapping("/clipboard")
public class ClipboardController {

    private static final Logger log = LoggerFactory.getLogger(ClipboardController.class);

    @Resource
    private ClipboardService clipboardService;

    @PostMapping("/clear")
    public void clear(@RequestParam(name = "deviceCode") String deviceCode) throws Exception {
        try {
            clipboardService.clear(deviceCode);
        } catch (Exception e) {
            log.error("clear error,deviceCode={}", deviceCode, e);
            throw e;
        }
    }

    @PostMapping("/save")
    public void save(@RequestBody List<Clipboard> clipboards) throws Exception {
        try {
            clipboardService.save(clipboards);
        } catch (Exception e) {
            log.error("save error,clipboards={}", clipboards, e);
            throw e;
        }
    }

    @GetMapping("/get")
    public List<Clipboard> get(@RequestParam(name = "deviceCode") String deviceCode) throws Exception {
        try {
            return clipboardService.get(deviceCode);
        } catch (Exception e) {
            log.error("get error,deviceCode={}", deviceCode, e);
            throw e;
        }
    }

}
