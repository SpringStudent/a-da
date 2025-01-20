package io.github.springstudent.ada.transport.server;

import io.github.springstudent.ada.common.Constants;
import io.github.springstudent.ada.protocol.netty.NettyUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ZhouNing
 * @date 2024/12/11 16:49
 **/
public class NettyIdleStateHandler extends IdleStateHandler {

    private static final Logger log = LoggerFactory.getLogger(NettyIdleStateHandler.class);

    public NettyIdleStateHandler() {
        super(Constants.HEARTBEAT_DURATION_SECONDS + 1, 0, 0);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        long heartBeatTime = Constants.CLIENT_SESSION_TIMEOUT_MILLS;
        Long lastReadTime = NettyUtils.getReaderTime(ctx.channel());
        if (lastReadTime != null && System.currentTimeMillis() - lastReadTime > heartBeatTime) {
            log.warn("server close channel,client heartbeat timeout");
            ctx.channel().close();
        }
        super.userEventTriggered(ctx, evt);
    }

}

