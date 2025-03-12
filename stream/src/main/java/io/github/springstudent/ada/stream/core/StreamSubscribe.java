package io.github.springstudent.ada.stream.core;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZhouNing
 * @date 2025/1/20 16:27
 **/
@RestController
public class StreamSubscribe{
    @Resource
    private WsHandler wsHandler;

    private ConcurrentHashMap<String, InputStream> client = new ConcurrentHashMap<>();


    @PostMapping("/receive")
    @ResponseBody
    public void receive(HttpServletRequest request, String id) throws Exception {
        ServletInputStream inputStream = request.getInputStream();
        if (client.putIfAbsent(id, inputStream) != null) {
            throw new Exception("duplicate id");
        }
        byte[] buffer = new byte[4096];
        int len;
        try {
            while ((len = inputStream.read(buffer)) != -1) {
                wsHandler.sendData(Arrays.copyOf(buffer, len), id);
            }
        } finally {
            client.remove(id);
            inputStream.close();
        }
    }

    @GetMapping("/clients")
    @ResponseBody
    public List<String> clients() {
        return new ArrayList(client.keySet());
    }
}
