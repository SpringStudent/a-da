package io.github.springstudent.ada.protocol.cmd;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

/**
 * @author ZhouNing
 * @date 2024/12/11 8:57
 **/
public abstract class Cmd implements Serializable {
    /**
     * 魔数
     */
    private static final byte MAGIC_NUMBER = (byte) 100;

    public abstract CmdType getType();

    public abstract int getWireSize();

    @Override
    public abstract String toString();

    public abstract void encode(ByteBuf out) throws IOException;

    public static void encodeMagicNumber(ByteBuf out) throws IOException {
        out.writeByte(MAGIC_NUMBER);
    }

    public static void encodeWireSize(ByteBuf out,int wireSize)throws IOException{
        out.writeInt(wireSize);
    }

    public static void decodeMagicNumber(ByteBuf in) throws IOException {
        if (Cmd.MAGIC_NUMBER != in.readByte()) {
            throw new IOException("Protocol error!");
        }
    }

    public static <T extends Enum<T>> void encodeEnum(ByteBuf out, Enum<T> value) throws IOException {
        out.writeByte(value.ordinal());
    }

    public static int decodeWireSize(ByteBuf in)throws IOException{
        return in.readInt();
    }

    public static <T extends Enum<T>> T decodeEnum(ByteBuf in, Class<T> enumClass) throws IOException {
        final byte ordinal = in.readByte();
        final T[] xenums = enumClass.getEnumConstants();
        return Arrays.stream(xenums)
                .filter(xenum -> xenum.ordinal() == ordinal)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown " + enumClass.getSimpleName() + " [" + ordinal + "] enum!"));
    }
}
