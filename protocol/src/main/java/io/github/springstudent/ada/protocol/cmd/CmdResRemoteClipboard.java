package io.github.springstudent.ada.protocol.cmd;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

/**
 * @author ZhouNing
 * @date 2025/1/2 16:25
 **/
public class CmdResRemoteClipboard extends Cmd {

    public static final byte OK = 0x00;
    public static final byte CLIPBOARD_GETDATA_ERROR = 0x01;
    public static final byte CLIPBOARD_GETDATA_EMPTY = 0x02;
    public static final byte CLIPBOARD_SENDDATA_ERROR = 0x03;
    public static final byte CLIPBOARD_DATA_NOTSUPPORT = 0x04;

    @Override
    public CmdType getType() {
        return CmdType.ResRemoteClipboard;
    }

    @Override
    public int getWireSize() {
        return 0;
    }

    @Override
    public String toString() {
        return CmdResRemoteClipboard.class.getSimpleName();
    }

    @Override
    public void encode(ByteBuf out) throws IOException {

    }
}
