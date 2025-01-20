package io.github.springstudent.ada.protocol.cmd;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author ZhouNing
 * @date 2025/1/2 10:48
 **/
public class CmdClipboardTransfer extends Cmd {

    private String deviceCode;

    private String controlType;

    public CmdClipboardTransfer(String deviceCode, String controlType) {
        this.deviceCode = deviceCode;
        this.controlType = controlType;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public String getControlType() {
        return controlType;
    }

    @Override
    public CmdType getType() {
        return CmdType.ClipboardTransfer;
    }

    @Override
    public int getWireSize() {
        return 8 + deviceCode.length() + controlType.length();
    }

    @Override
    public String toString() {
        return CmdClipboardTransfer.class.getSimpleName();
    }

    @Override
    public void encode(ByteBuf out) throws IOException {
        out.writeInt(deviceCode.length());
        out.writeCharSequence(deviceCode, StandardCharsets.UTF_8);
        out.writeInt(controlType.length());
        out.writeCharSequence(controlType, StandardCharsets.UTF_8);
    }

    public static CmdClipboardTransfer decode(ByteBuf in) {
        int deviceCodeLength = in.readInt();
        String deviceCode = in.readCharSequence(deviceCodeLength, StandardCharsets.UTF_8).toString();
        int controllTypeLength = in.readInt();
        String controlType = in.readCharSequence(controllTypeLength, StandardCharsets.UTF_8).toString();
        return new CmdClipboardTransfer(deviceCode, controlType);
    }
}
