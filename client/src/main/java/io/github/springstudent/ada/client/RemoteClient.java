package io.github.springstudent.ada.client;

import io.github.springstudent.ada.client.core.RemoteControlled;
import io.github.springstudent.ada.client.core.RemoteController;
import io.github.springstudent.ada.client.core.RemoteFrame;
import io.github.springstudent.ada.client.core.RemoteScreen;
import io.github.springstudent.ada.client.netty.RemoteChannelHandler;
import io.github.springstudent.ada.client.netty.RemoteStateIdleHandler;
import io.github.springstudent.ada.client.utils.RemoteUtils;
import io.github.springstudent.ada.common.log.Log;
import io.github.springstudent.ada.common.utils.EmptyUtils;
import io.github.springstudent.ada.protocol.cmd.Cmd;
import io.github.springstudent.ada.protocol.cmd.CmdResCliInfo;
import io.github.springstudent.ada.protocol.cmd.CmdType;
import io.github.springstudent.ada.protocol.netty.NettyDecoder;
import io.github.springstudent.ada.protocol.netty.NettyEncoder;
import io.github.springstudent.ada.protocol.netty.NettyUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import javax.swing.*;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * @author ZhouNing
 * @date 2024/12/6
 */
public class RemoteClient extends RemoteFrame {
    private static RemoteClient remoteClient;

    private String serverIp;

    private Integer serverPort;

    private String clipboardServer;

    private String streamServer;

    private String registryServer;

    private boolean connectStatus;

    private RemoteScreen remoteScreen;

    private RemoteControlled controlled;

    private RemoteController controller;

    private char osId;


    public RemoteClient(String serverIp, Integer serverPort, String clipboardServer, String streamServer) {
        osId = System.getProperty("os.name").toLowerCase().charAt(0);
        remoteClient = this;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.clipboardServer = clipboardServer;
        this.streamServer = streamServer;
        this.controlled = new RemoteControlled();
        this.controller = new RemoteController();
        this.remoteScreen = new RemoteScreen();
        this.connectServer();
    }

    public RemoteClient(String registryServer) {
        osId = System.getProperty("os.name").toLowerCase().charAt(0);
        remoteClient = this;
        this.registryServer = registryServer;
        initFromRegistryServer();
        this.controlled = new RemoteControlled();
        this.controller = new RemoteController();
        this.remoteScreen = new RemoteScreen();
        this.connectServer();
    }


    private void initFromRegistryServer() {
        boolean success = false;
        int retry = 0;
        while (!success) {
            try {
                if (retry > 0) {
                    Thread.sleep(5000);
                }
                Log.info("initFromRegistryServer retry times =" + retry);
                if (EmptyUtils.isEmpty(this.streamServer)) {
                    this.streamServer = RemoteUtils.selectStream(this.registryServer);
                }
                if (EmptyUtils.isEmpty(this.clipboardServer)) {
                    this.clipboardServer = RemoteUtils.selectClipboard(this.registryServer);
                }
                if (EmptyUtils.isEmpty(serverIp) || serverIp == null) {
                    String nettyServer = RemoteUtils.selectNettyServer(this.registryServer);
                    this.serverIp = nettyServer.split(":")[0];
                    this.serverPort = Integer.parseInt(nettyServer.split(":")[1]);
                }
                success = true;
            } catch (Exception e) {
                retry = retry + 1;
                Log.error("initFromRegistryServer error", e);
            }
        }
    }


    @Override
    public void openRemoteScreen(String deviceCode) {
        if (!connectStatus) {
            showMessageDialog("请等待连接连接服务器成功", JOptionPane.ERROR_MESSAGE);
        } else {
            controller.openSession(deviceCode);
        }
    }

    @Override
    public void closeRemoteScreen(String deviceCode) {
        controlled.closeSession(deviceCode);
    }

    @Override
    public void closeRemoteScreen() {
        controller.closeSession();
    }

    public RemoteScreen getRemoteScreen() {
        return remoteScreen;
    }

    /**
     * 连接至server
     */
    public void connectServer() {
        final Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new NettyDecoder());
                        socketChannel.pipeline().addLast(new NettyEncoder());
                        socketChannel.pipeline().addLast(new RemoteStateIdleHandler());
                        socketChannel.pipeline().addLast(new RemoteChannelHandler());
                    }
                });
        //连接至远程客户端
        connect(bootstrap, 0);
    }

    private void connect(Bootstrap bootstrap, int retry) {
        bootstrap.connect(serverIp, serverPort).addListener(future -> {
            if (future.isSuccess()) {
                Log.info("connect to remote server success");
                this.connectStatus = true;
            } else {
                this.connectStatus = false;
                Integer order = retry + 1;
                Log.info(format("reconnect to remote server serverIp=%s ,serverPort=%d,retry times =%d", serverIp, serverPort, order));
                bootstrap.config().group().schedule(() -> connect(bootstrap, order), 5, TimeUnit
                        .SECONDS);
            }
        });
    }

    public RemoteController getController() {
        return controller;
    }

    public RemoteControlled getControlled() {
        return controlled;
    }

    public void handleCmd(ChannelHandlerContext ctx, Cmd cmd) {
        if (cmd.getType().equals(CmdType.ResCliInfo)) {
            CmdResCliInfo clientInfo = (CmdResCliInfo) cmd;
            setDeviceCodeAndPassword(clientInfo.getDeviceCode(), clientInfo.getPassword());
            NettyUtils.updateDeviceCode(ctx.channel(), clientInfo.getDeviceCode());
            updateConnectionStatus(true);
        } else {
            controller.handleCmd(cmd);
            controlled.handleCmd(cmd);
        }
    }

    public void setControllChannel(Channel channel) {
        controller.setChannel(channel);
        controlled.setChannel(channel);
    }

    public void stopClient() {
        showMessageDialog("连接异常", JOptionPane.ERROR_MESSAGE);
        remoteScreen.close();
        controller.stop();
        controlled.stop();
        updateConnectionStatus(false);
        setControlledAndCloseSessionLabelVisible(false);
        setControllChannel(null);
        connectServer();
    }

    public String getClipboardServer() {
        return clipboardServer;
    }

    public String getStreamServer() {
        return streamServer;
    }


    public String getStreamServerWs() {
        if (streamServer.startsWith("http")) {
            return streamServer.replace("http", "ws");
        } else {
            return streamServer.replace("https", "wss");
        }
    }

    public String getRegistryServer() {
        return registryServer;
    }

    public static RemoteClient getRemoteClient() {
        return remoteClient;
    }

    public char getOsId() {
        return osId;
    }

    public static void main(String[] args) throws Exception {
        //不需要注册中心的单机部署
        new RemoteClient("192.168.0.110", 11112, "http://192.168.0.110:11111/transport", "http://192.168.0.110:11110/stream");
        //注册中心部署
//        new RemoteClient("http://192.168.0.110:11113");
    }

}