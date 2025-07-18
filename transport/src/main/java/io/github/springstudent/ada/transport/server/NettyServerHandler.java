package io.github.springstudent.ada.transport.server;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import io.github.springstudent.ada.common.utils.EmptyUtils;
import io.github.springstudent.ada.protocol.cmd.*;
import io.github.springstudent.ada.protocol.netty.NettyUtils;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ZhouNing
 * @date 2024/12/10 20:43
 **/
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<Cmd> {

    /**
     * logger
     */
    private static final Logger log = LoggerFactory.getLogger(NettyServerHandler.class);

    @SuppressWarnings("unchecked")
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Cmd cmd) throws Exception {
        if (cmd == null) {
            return;
        }
        log.info("server recived cmd ={}", cmd);
        NettyUtils.updateReaderTime(ctx.channel(), System.currentTimeMillis());
        if (cmd.getType().equals(CmdType.ChangePwd)) {
            CmdChangePwd cmdChangePwd = (CmdChangePwd) cmd;
            Map<String, Object> map = new HashMap();
            map.put("password", cmdChangePwd.getPassword());
            NettyUtils.updateCliInfo(ctx.channel(), map);
        } else if (cmd.getType().equals(CmdType.ReqCliInfo)) {
            CmdReqCliInfo cmdReqCliInfo = (CmdReqCliInfo) cmd;
            NettyUtils.updateCliInfo(ctx.channel(), BeanUtil.beanToMap(cmdReqCliInfo));
        } else if (cmd.getType().equals(CmdType.ReqPing)) {
            ctx.writeAndFlush(new CmdResPong()).addListeners((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    log.error("send pong error,close Channel");
                    future.channel().close();
                }
            });
        } else if (cmd.getType().equals(CmdType.ReqOpen)) {
            CmdReqOpen cmdReqOpen = (CmdReqOpen) cmd;
            Channel controlledChannel = NettyChannelManager.getChannel(cmdReqOpen.getDeviceCode());
            if (controlledChannel == null) {
                ctx.channel().writeAndFlush(new CmdResOpen(CmdResOpen.OFFLINE));
            } else {
                if (StrUtil.isEmpty(NettyUtils.getControllFlag(ctx.channel())) && StrUtil.isEmpty(NettyUtils.getControllFlag(controlledChannel))) {
                    ctx.channel().writeAndFlush(new CmdResOpen(CmdResOpen.OK));
                } else {
                    ctx.channel().writeAndFlush(new CmdResOpen(CmdResOpen.CONTROL));
                }
            }
        } else if (cmd.getType().equals(CmdType.ReqCapture)) {
            CmdReqCapture cmdReqCapture = (CmdReqCapture) cmd;
            Channel controlledChannel = NettyChannelManager.getChannel(cmdReqCapture.getDeviceCode());
            if (cmdReqCapture.getCaputureOp() == CmdReqCapture.START_CAPTURE) {
                //被控制端没有与服务端建立连接，提示"被控制端不在线"
                if (controlledChannel == null) {
                    ctx.channel().writeAndFlush(new CmdResCapture(CmdResCapture.OFFLINE));
                } else {
                    if (StrUtil.isEmpty(NettyUtils.getControllFlag(ctx.channel())) && StrUtil.isEmpty(NettyUtils.getControllFlag(controlledChannel))) {
                        if (EmptyUtils.isEmpty(cmdReqCapture.getPassword()) || !cmdReqCapture.getPassword().equals(NettyUtils.getCliInfo(controlledChannel).get("password").toString())) {
                            ctx.channel().writeAndFlush(new CmdResCapture(CmdResCapture.PWDERROR));
                        } else {
                            NettyChannelManager.bindChannelBrother(ctx.channel(), controlledChannel);
                        }
                    } else {
                        //控制端正在被控制发起其他远程控制，提示“请先断开其他远程控制中的连接”
                        ctx.channel().writeAndFlush(new CmdResCapture(CmdResCapture.CONTROL));
                    }
                }
            } else {
                if (controlledChannel != null) {
                    if (cmdReqCapture.getCaputureOp() == CmdReqCapture.STOP_CAPTURE) {
                        NettyChannelManager.unbindChannelBrother(CmdReqCapture.STOP_CAPTURE, controlledChannel);
                    } else if (cmdReqCapture.getCaputureOp() == CmdReqCapture.STOP_CAPTURE_BY_CONTROLLED) {
                        NettyChannelManager.unbindChannelBrother(CmdReqCapture.STOP_CAPTURE_BY_CONTROLLED, controlledChannel);
                    }
                } else {
                    ctx.channel().writeAndFlush(new CmdResCapture(CmdResCapture.STOP));
                }
            }
        } else if (cmd.getType().equals(CmdType.Capture) || cmd.getType().equals(CmdType.ResRemoteClipboard) || cmd.getType().equals(CmdType.ResStream)) {
            Channel controllerChannel = NettyChannelManager.getControllerChannel(ctx.channel());
            if (controllerChannel != null) {
                controllerChannel.writeAndFlush(cmd);
            } else {
                ctx.channel().writeAndFlush(new CmdResCapture(CmdResCapture.STOP_));
            }
        } else if (cmd.getType().equals(CmdType.CaptureConfig) || cmd.getType().equals(CmdType.CompressorConfig) || cmd.getType().equals(CmdType.KeyControl) || cmd.getType().equals(CmdType.MouseControl) || cmd.getType().equals(CmdType.ReqRemoteClipboard)) {
            if (StrUtil.isNotEmpty(NettyUtils.getControllFlag(ctx.channel()))) {
                Channel controlledChannel = NettyChannelManager.getControlledChannel(ctx.channel());
                if (controlledChannel != null) {
                    controlledChannel.writeAndFlush(cmd);
                }
            }
        } else if (cmd.getType().equals(CmdType.ClipboardText) || cmd.getType().equals(CmdType.ClipboardTransfer)) {
            String controllDeviceCode = NettyUtils.getControllDeviceCode(ctx.channel());
            if (StrUtil.isNotEmpty(controllDeviceCode)) {
                Channel destChannel = NettyChannelManager.getChannel(controllDeviceCode);
                if (destChannel != null) {
                    destChannel.writeAndFlush(cmd);
                }
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String deviceCode = RandomUtil.randomString(8);
        while (NettyChannelManager.getChannel(deviceCode) != null) {
            deviceCode = RandomUtil.randomString(8);
        }
        String password = RandomUtil.randomString(6);
        NettyUtils.updateDeviceCode(ctx.channel(), deviceCode);
        NettyChannelManager.addChannel(deviceCode, ctx.channel());
        Map<String, Object> map = new HashMap();
        map.put("password", password);
        NettyUtils.updateCliInfo(ctx.channel(), map);
        ctx.channel().writeAndFlush(new CmdResCliInfo(deviceCode, password));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyChannelManager.removeChannelAndBrother(ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info(cause.getMessage());
        ctx.close();
    }

}

