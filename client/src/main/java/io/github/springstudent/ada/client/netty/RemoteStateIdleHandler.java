package io.github.springstudent.ada.client.netty;

import io.github.springstudent.ada.common.Constants;
import io.github.springstudent.ada.common.log.Log;
import io.github.springstudent.ada.protocol.cmd.CmdReqPing;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;


/**
 * @author ZhouNing
 * @date 2024/12/11 16:50
 **/
public class RemoteStateIdleHandler extends IdleStateHandler {

    public RemoteStateIdleHandler() {
        super(0, Constants.HEARTBEAT_DURATION_SECONDS, 0);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        ctx.writeAndFlush(new CmdReqPing()).addListeners((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                Log.error("client send ping error,close channel");
            }
        });
        super.userEventTriggered(ctx, evt);
    }
}

