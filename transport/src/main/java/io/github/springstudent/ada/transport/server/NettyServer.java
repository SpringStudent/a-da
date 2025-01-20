package io.github.springstudent.ada.transport.server;

import cn.hutool.core.thread.NamedThreadFactory;
import io.github.springstudent.ada.protocol.netty.NettyDecoder;
import io.github.springstudent.ada.protocol.netty.NettyEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * @author ZhouNing
 * @date 2024/12/10 14:07
 **/
@Component
public class NettyServer implements InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    @Value("${netty.server.port}")
    private Integer serverPort;

    @Value("${netty.server.ip}")
    private String serverIp;

    /**
     * 连接通道
     */
    private Channel channel;

    @Override
    public void afterPropertiesSet() throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1, new NamedThreadFactory("netty-boss-", true));
        EventLoopGroup workerGroup = new NioEventLoopGroup(10, new NamedThreadFactory("netty-worker-", true));
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new NettyDecoder());
                        socketChannel.pipeline().addLast(new NettyEncoder());
                        socketChannel.pipeline().addLast(new NettyIdleStateHandler());
                        socketChannel.pipeline().addLast(new NettyServerHandler());
                    }
                });
        ChannelFuture channelFuture = bootstrap.bind(new InetSocketAddress(serverIp, serverPort));
        channelFuture.syncUninterruptibly();
        this.channel = channelFuture.channel();
        logger.info("server start success,host={},port={}", serverIp, serverPort);
    }

    @Override
    public void destroy() throws Exception {
        if (channel != null && channel.isActive()) {
            channel.close();
            logger.info("server stop success");
        }
    }

}
