package io.github.springstudent.ada.protocol.cmd;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author ZhouNing
 * @date 2025/4/2 9:16
 **/
public class CmdReqCliInfo extends Cmd {

    private int screenNum;

    private String osName;

    public CmdReqCliInfo(int screenNum, String osName) {
        this.screenNum = screenNum;
        this.osName = osName;
    }

    @Override
    public CmdType getType() {
        return CmdType.ReqCliInfo;
    }

    @Override
    public int getWireSize() {
        return 8 + osName.getBytes(StandardCharsets.UTF_8).length;
    }

    public int getScreenNum() {
        return screenNum;
    }

    public String getOsName() {
        return osName;
    }

    @Override
    public String toString() {
        return String.format("CmdClientInfo={screenNum:%d,osName:%s}", screenNum, osName);
    }

    @Override
    public void encode(ByteBuf out) throws IOException {
        out.writeInt(screenNum);
        byte[] osNameBytes = osName.getBytes(StandardCharsets.UTF_8);
        out.writeInt(osNameBytes.length);
        out.writeBytes(osNameBytes);
    }

    public static CmdReqCliInfo decode(ByteBuf in) {
        int screenNum = in.readInt();
        int osNameLength = in.readInt();
        byte[] osNameBytes = new byte[osNameLength];
        in.readBytes(osNameBytes);
        String osName = new String(osNameBytes, StandardCharsets.UTF_8);
        return new CmdReqCliInfo(screenNum, osName);
    }
}
