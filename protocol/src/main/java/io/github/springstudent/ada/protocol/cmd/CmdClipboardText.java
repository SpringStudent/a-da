package io.github.springstudent.ada.protocol.cmd;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author ZhouNing
 * @date 2024/12/19 19:34
 **/
public class CmdClipboardText extends Cmd {
    private String payload;

    private String controlType;

    public CmdClipboardText(String payload, String controlType) {
        this.payload = payload;
        this.controlType = controlType;
    }

    @Override
    public CmdType getType() {
        return CmdType.ClipboardText;
    }

    @Override
    public int getWireSize() {
        // 修改为根据UTF-8字符集来计算字节数
        return 8 + payload.getBytes(StandardCharsets.UTF_8).length + controlType.length();
    }

    @Override
    public String toString() {
        return CmdClipboardText.class.getSimpleName();
    }

    public String getPayload() {
        return payload;
    }

    public String getControlType() {
        return controlType;
    }

    @Override
    public void encode(ByteBuf out) throws IOException {
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
        out.writeInt(payloadBytes.length);
        out.writeBytes(payloadBytes);
        out.writeInt(controlType.length());
        out.writeCharSequence(controlType, StandardCharsets.UTF_8);
    }

    public static CmdClipboardText decode(ByteBuf in) {
        int payloadLength = in.readInt();
        byte[] payloadBytes = new byte[payloadLength];
        in.readBytes(payloadBytes);
        String payload = new String(payloadBytes, StandardCharsets.UTF_8);
        int controllTypeLength = in.readInt();
        String controlType = in.readCharSequence(controllTypeLength, StandardCharsets.UTF_8).toString();
        return new CmdClipboardText(payload, controlType);
    }
}