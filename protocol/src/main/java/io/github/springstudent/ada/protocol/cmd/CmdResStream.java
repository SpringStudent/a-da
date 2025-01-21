package io.github.springstudent.ada.protocol.cmd;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author ZhouNing
 * @date 2025/1/21 14:12
 **/
public class CmdResStream extends Cmd {

    private final String playUrl;

    public CmdResStream(String playUrl) {
        this.playUrl = playUrl;
    }

    @Override
    public CmdType getType() {
        return CmdType.ResStream;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    @Override
    public int getWireSize() {
        return 4 + playUrl.length();
    }

    @Override
    public String toString() {
        return String.format("CmdResStream={playUrl:%s}", playUrl);
    }

    @Override
    public void encode(ByteBuf out) throws IOException {
        out.writeInt(playUrl.length());
        out.writeCharSequence(playUrl, StandardCharsets.UTF_8);
    }

    public static CmdResStream decode(ByteBuf in) {
        int playUrlLength = in.readInt();
        String playUrl = in.readCharSequence(playUrlLength, StandardCharsets.UTF_8).toString();
        return new CmdResStream(playUrl);
    }
}
