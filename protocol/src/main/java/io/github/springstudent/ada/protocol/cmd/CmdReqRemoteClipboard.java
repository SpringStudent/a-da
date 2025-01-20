package io.github.springstudent.ada.protocol.cmd;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

/**
 * @author ZhouNing
 * @date 2025/1/2 9:06
 **/
public class CmdReqRemoteClipboard extends Cmd {
    @Override
    public CmdType getType() {
        return CmdType.ReqRemoteClipboard;
    }

    @Override
    public int getWireSize() {
        return 0;
    }

    @Override
    public String toString() {
        return CmdReqRemoteClipboard.class.getSimpleName();
    }

    @Override
    public void encode(ByteBuf out) throws IOException {

    }
}
