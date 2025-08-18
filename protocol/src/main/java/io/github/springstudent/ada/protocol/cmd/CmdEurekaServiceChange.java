package io.github.springstudent.ada.protocol.cmd;


import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author ZhouNing
 * @date 2025/05/18 19:34
 **/
public class CmdEurekaServiceChange extends Cmd {

    private String serviceName;

    private Integer eventType;

    public CmdEurekaServiceChange(String serviceName, Integer eventType) {
        this.serviceName = serviceName;
        this.eventType = eventType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Integer getEventType() {
        return eventType;
    }

    @Override
    public CmdType getType() {
        return CmdType.EurekaServiceChange;
    }

    @Override
    public int getWireSize() {
        return 4 + serviceName.getBytes(StandardCharsets.UTF_8).length + 4;
    }

    @Override
    public String toString() {
        return String.format("CmdEurekaServiceChange={serviceName:%s,eventType}", serviceName, eventType);
    }

    @Override
    public void encode(ByteBuf out) throws IOException {
        out.writeInt(serviceName.length());
        out.writeCharSequence(serviceName, java.nio.charset.StandardCharsets.UTF_8);
        out.writeInt(eventType);
    }

    public static CmdEurekaServiceChange decode(ByteBuf in) {
        int serviceNameLength = in.readInt();
        String serviceName = in.readCharSequence(serviceNameLength, java.nio.charset.StandardCharsets.UTF_8).toString();
        int eventType = in.readInt();
        return new CmdEurekaServiceChange(serviceName, eventType);
    }

}
