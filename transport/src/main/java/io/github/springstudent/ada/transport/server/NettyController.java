package io.github.springstudent.ada.transport.server;

import io.github.springstudent.ada.common.Constants;
import io.github.springstudent.ada.protocol.cmd.CmdEurekaServiceChange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ZhouNing
 * @date 2025/3/19 10:47
 **/
@RestController
@RequestMapping("/netty")
public class NettyController {

    @Value("${netty.server.port}")
    private Integer serverPort;

    @Value("${netty.server.ip}")
    private String serverIp;

    @GetMapping("/server")
    public String server() {
        return Constants.buildResponseBody("success", serverIp + ":" + serverPort, 200);
    }

    @GetMapping("/eurekaServiceChange")
    public void eurekaServiceChange(@RequestParam String serviceName, @RequestParam Integer eventType) {
        NettyChannelManager.getAllChannels().forEach(channel -> {
            if (channel.isActive()) {
                channel.writeAndFlush(new CmdEurekaServiceChange(serviceName, eventType));
            }
        });
    }
}
