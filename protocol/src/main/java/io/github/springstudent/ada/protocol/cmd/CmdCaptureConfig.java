package io.github.springstudent.ada.protocol.cmd;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

/**
 * @author ZhouNing
 * @date 2025/4/30 15:14
 **/
public class CmdCaptureConfig extends Cmd{

    private int frameRate;
    private int bitRate;

    public CmdCaptureConfig(int frameRate, int bitRate) {
        this.frameRate = frameRate;
        this.bitRate = bitRate;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public int getBitRate() {
        return bitRate;
    }

    @Override
    public CmdType getType() {
        return CmdType.CaptureConfig;
    }

    @Override
    public int getWireSize() {
        return 8;
    }
    @Override
    public String toString() {
        return null;
    }

    @Override
    public void encode(ByteBuf out) throws IOException {
        out.writeInt(frameRate);
        out.writeInt(bitRate);
    }

    public static CmdCaptureConfig decode(ByteBuf in) {
        return new CmdCaptureConfig(in.readInt(), in.readInt());
    }
}
