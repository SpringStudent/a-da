package io.github.springstudent.ada.protocol.cmd;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

/**
 * @author ZhouNing
 * @date 2024/12/12 9:17
 **/
public class CmdResCapture extends Cmd {
    /**
     * 控制端响应状态码
     */
    public static final byte START = 0x00;
    public static final byte STOP = 0x01;
    public static final byte STOP_BYCONTROLLED = 0x02;
    public static final byte STOP_CHANNELINACTIVE = 0x03;
    public static final byte OFFLINE = 0x04;
    public static final byte CONTROL = 0x05;
    public static final byte PWDERROR = 0x06;
    /**
     * 被控制端响应状态码
     */
    public static final byte START_ = 0x10;

    public static final byte STOP_ = 0x11;

    private byte code;

    private int screenNum = 1;

    private char os;

    public CmdResCapture(byte code) {
        this.code = code;
    }

    public CmdResCapture(byte code, int screenNum, char os) {
        this.code = code;
        this.screenNum = screenNum;
        this.os = os;
    }

    public byte getCode() {
        return code;
    }

    public int getScreenNum() {
        return screenNum;
    }

    public char getOs() {
        return os;
    }

    @Override
    public CmdType getType() {
        return CmdType.ResCapture;
    }

    @Override
    public int getWireSize() {
        return 1 + 4 + 2;
    }

    @Override
    public String toString() {
        return String.format("CmdReqCapture={code:%d,screenNum:%d,os:%s}", code, screenNum, os);
    }

    @Override
    public void encode(ByteBuf out) throws IOException {
        out.writeByte(code);
        out.writeInt(screenNum);
        out.writeChar(os);
    }

    public static CmdResCapture decode(ByteBuf in) {
        return new CmdResCapture(in.readByte(), in.readInt(), in.readChar());
    }
}
