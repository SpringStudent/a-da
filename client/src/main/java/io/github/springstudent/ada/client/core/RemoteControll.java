package io.github.springstudent.ada.client.core;

import cn.hutool.core.io.FileUtil;
import io.github.springstudent.ada.client.RemoteClient;
import io.github.springstudent.ada.common.log.Log;
import io.github.springstudent.ada.protocol.cmd.Cmd;
import io.netty.channel.Channel;

import javax.swing.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.io.File;

import static java.lang.System.getProperty;

/**
 * @author ZhouNing
 * @date 2024/12/10 14:20
 **/
public abstract class RemoteControll implements ClipboardOwner {

    protected Channel channel;

    private String rootDir;

    private String uploadDir;

    private String downloadDir;

    public RemoteControll() {
        this.rootDir = getProperty("java.io.tmpdir") + File.separator + "remoteDeskopControll";
        if (FileUtil.exist(rootDir)) {
            FileUtil.clean(rootDir);
        } else {
            FileUtil.mkdir(rootDir);
        }
        this.uploadDir = rootDir + File.separator + "rmdupload";
        if (!FileUtil.exist(uploadDir)) {
            FileUtil.mkdir(uploadDir);
        }
        this.downloadDir = rootDir + File.separator + "rmddownload";
        if (!FileUtil.exist(downloadDir)) {
            FileUtil.mkdir(downloadDir);
        }
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    protected void fireCmd(Cmd cmd) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(cmd);
        } else {
            Log.error("client fireCmd error,please check network connect");
        }
    }

    protected void showMessageDialog(Object msg, int messageType) {
        SwingUtilities.invokeLater(() -> RemoteClient.getRemoteClient().showMessageDialog(msg, messageType));
    }
    public abstract void handleCmd(Cmd cmd);

    public abstract String getType();

    public void start() {
    }

    public void stop() {
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        Log.info("lostOwnership ....");
    }
}
