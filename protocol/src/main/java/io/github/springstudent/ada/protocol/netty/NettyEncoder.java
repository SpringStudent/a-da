package io.github.springstudent.ada.protocol.netty;

import io.github.springstudent.ada.protocol.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author ZhouNing
 * @date 2024/12/10 20:45
 **/
public class NettyEncoder extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (o instanceof Cmd) {
            Cmd cmd = (Cmd) o;
            Cmd.encodeMagicNumber(byteBuf);
            Cmd.encodeEnum(byteBuf, cmd.getType());
            Cmd.encodeWireSize(byteBuf, cmd.getWireSize());
            cmd.encode(byteBuf);
        }
    }
}
