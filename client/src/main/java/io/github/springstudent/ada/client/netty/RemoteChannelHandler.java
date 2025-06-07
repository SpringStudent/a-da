package io.github.springstudent.ada.client.netty;

import io.github.springstudent.ada.client.RemoteClient;
import io.github.springstudent.ada.common.log.Log;
import io.github.springstudent.ada.protocol.cmd.Cmd;
import io.github.springstudent.ada.protocol.cmd.CmdReqCliInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author ZhouNing
 * @date 2024/12/11 13:17
 **/
public class RemoteChannelHandler extends SimpleChannelInboundHandler<Cmd> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Cmd cmd) throws Exception {
        try {
            RemoteClient.getRemoteClient().handleCmd(ctx, cmd);
        } catch (Exception e) {
            Log.error("client channelRead0 error",e);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        RemoteClient.getRemoteClient().stopClient();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        RemoteClient.getRemoteClient().setControllChannel(ctx.channel());
        ctx.channel().writeAndFlush(new CmdReqCliInfo(1, System.getProperty("os.name")));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Log.error("client exceptionCaught error",cause);
        super.exceptionCaught(ctx, cause);
    }
}
