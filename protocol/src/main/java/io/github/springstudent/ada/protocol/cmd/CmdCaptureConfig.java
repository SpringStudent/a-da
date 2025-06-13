package io.github.springstudent.ada.protocol.cmd;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author ZhouNing
 * @date 2025/4/30 15:14
 **/
public class CmdCaptureConfig extends Cmd{

    private int frameRate;
    private int bitRate;
    private String codec;

    public CmdCaptureConfig(int frameRate, int bitRate,String codec) {
        this.frameRate = frameRate;
        this.bitRate = bitRate;
        this.codec = codec;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public int getBitRate() {
        return bitRate;
    }

    public String getCodec() {
        return codec;
    }

    @Override
    public CmdType getType() {
        return CmdType.CaptureConfig;
    }

    @Override
    public int getWireSize() {
        return 4 + 4 + codec.length() +4;
    }
    @Override
    public String toString() {
        return null;
    }

    @Override
    public void encode(ByteBuf out) throws IOException {
        out.writeInt(frameRate);
        out.writeInt(bitRate);
        out.writeInt(codec.length());
        out.writeCharSequence(codec, StandardCharsets.UTF_8);
    }

    public static CmdCaptureConfig decode(ByteBuf in) {
        int frameRate = in.readInt();
        int bitRate = in.readInt();
        int codecLength = in.readInt();
        byte[] codecBytes = new byte[codecLength];
        in.readBytes(codecBytes);
        String codec = new String(codecBytes, StandardCharsets.UTF_8);
        return new CmdCaptureConfig(frameRate, bitRate, codec);
    }
}
