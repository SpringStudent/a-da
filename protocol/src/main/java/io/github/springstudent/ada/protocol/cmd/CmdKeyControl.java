package io.github.springstudent.ada.protocol.cmd;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

/**
 * @author ZhouNing
 * @date 2024/12/13 23:08
 **/
public class CmdKeyControl extends Cmd {
    public enum KeyState {
        PRESSED, RELEASED,
    }

    private static final int PRESSED = 1;

    private static final int RELEASED = 1 << 1;

    private final int info;

    private final int keyCode;

    private final char keyChar;

    public CmdKeyControl(KeyState buttonState, int keyCode, char keyChar) {
        this(buttonState == KeyState.PRESSED ? PRESSED : RELEASED, keyCode, keyChar);
    }

    private CmdKeyControl(int info, int keyCode, char keyChar) {
        this.info = info;
        this.keyCode = keyCode;
        this.keyChar = keyChar;
    }

    @Override
    public CmdType getType() {
        return CmdType.KeyControl;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public char getKeyChar() {
        return keyChar;
    }

    public boolean isPressed() {
        return (info & PRESSED) == PRESSED;
    }

    public boolean isReleased() {
        return (info & RELEASED) == RELEASED;
    }

    @Override
    public int getWireSize() {
        return 10;
    }

    @Override
    public void encode(ByteBuf out) throws IOException {
        out.writeInt(info);
        out.writeInt(keyCode);
        out.writeChar(keyChar);
    }

    public static CmdKeyControl decode(ByteBuf in) throws IOException {
        final int info = in.readInt();
        final int keyCode = in.readInt();
        return new CmdKeyControl(info, keyCode, in.readChar());
    }

    public String toString() {
        return String.format("%s [%d] [%s]", toStringPressed(), keyCode, keyChar);
    }

    private String toStringPressed() {
        if (isPressed()) {
            return "PRESSED";
        }

        if (isReleased()) {
            return "RELEASED";
        }
        return "";
    }

}
