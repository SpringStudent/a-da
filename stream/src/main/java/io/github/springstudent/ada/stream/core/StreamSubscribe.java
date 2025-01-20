package io.github.springstudent.ada.stream.core;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author ZhouNing
 * @date 2025/1/20 16:29
 **/
@RestController
public class StreamSubscribe implements ApplicationRunner {
    @Resource
    private WsHandler wsHandler;

    private ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(10);

    private ConcurrentHashMap<String, ControllerInputStream> client = new ConcurrentHashMap<>();


    @PostMapping("/receive")
    @ResponseBody
    public void receive(HttpServletRequest request, String id) throws Exception {
        ServletInputStream inputStream = request.getInputStream();
        ControllerInputStream ct = new ControllerInputStream(inputStream);
        if (client.putIfAbsent(id, ct) != null) {
            throw new Exception("duplicate id");
        }
        int len = -1;
        while ((len = inputStream.available()) != -1) {
            Thread.sleep(1);
            byte[] data = new byte[len];
            inputStream.read(data);
            if (data.length > 0) {
                ct.updateReadLast();
                wsHandler.sendData(data, id);
            }
        }
    }

    @GetMapping("/clients")
    @ResponseBody
    public List<String> clients() {
        return new ArrayList(client.keySet());
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        scheduled.scheduleWithFixedDelay(() -> {
            for (String id : client.keySet()) {
                try {
                    if (client.get(id).timeoutAndClose()) {
                        client.remove(id);
                    }
                } catch (IOException e) {
                }
            }
        }, 10, 3, TimeUnit.SECONDS);
    }

    private static class ControllerInputStream {

        private InputStream inputStream;

        private Long lastread;

        public ControllerInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
            this.lastread = System.currentTimeMillis();
        }

        public void updateReadLast() {
            lastread = System.currentTimeMillis();
        }

        public boolean timeoutAndClose() throws IOException {
            if (System.currentTimeMillis() - lastread >= 5000) {
                inputStream.close();
                return true;
            }
            return false;
        }
    }
}
