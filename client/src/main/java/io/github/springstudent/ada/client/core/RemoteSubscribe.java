package io.github.springstudent.ada.client.core;

import io.github.springstudent.ada.client.RemoteClient;
import io.github.springstudent.ada.common.log.Log;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.awt.image.BufferedImage;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.nio.ByteBuffer;

/**
 * @author ZhouNing
 * @date 2025/1/20 16:04
 **/
public class RemoteSubscribe extends WebSocketClient {
    private FFmpegFrameGrabber grabber;
    private PipedInputStream pipedInputStream;
    private PipedOutputStream pipedOutputStream;
    private Java2DFrameConverter frameConverter;
    private Thread decodeThread;

    public RemoteSubscribe(String serverUri) throws Exception {
        super(new URI(serverUri));
        pipedInputStream = new PipedInputStream();
        pipedOutputStream = new PipedOutputStream();
        pipedInputStream.connect(pipedOutputStream);
        grabber = new FFmpegFrameGrabber(pipedInputStream, 0);
        grabber.setFormat("mpegts");
        grabber.setOption("hwaccel", "auto");
        grabber.setOption("threads", "auto");
        grabber.setOption("analyzeduration", "1000000");
        grabber.setOption("probesize", "1000000");
        frameConverter = new Java2DFrameConverter();
        decodeThread = new Thread(this::decodeFrames);
        decodeThread.start();
        this.connect();
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("Connected to WebSocket server");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received text message: " + message);
    }

    @Override
    public void onMessage(ByteBuffer byteBuffer) {
        // 接收二进制数据并写入 PipedOutputStream
        try {
            byte[] bytes = byteBuffer.array();
            Log.info("RemoteSubscribe.onMessage byte size =" + bytes.length);
            pipedOutputStream.write(bytes);
        } catch (Throwable e) {
            Log.error("RemoteSubscribe.onMessage error", e);
        }
    }

    @Override
    public void close() {
        try {
            super.close();
        } catch (Exception e) {
            Log.error("RemoteSubscribe.close error", e);
        }

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        stop();
    }

    @Override
    public void onError(Exception ex) {
        stop();
    }

    private void stop() {
        try {
            Log.info("RemoteSubscribe.stop start");
            if (this.getConnection() != null && this.getConnection().isOpen()) {
                this.close();
            }
            if (decodeThread != null && decodeThread.isAlive()) {
                decodeThread.interrupt();
            }
            if (pipedInputStream != null) {
                pipedInputStream.close();
            }
            if (pipedOutputStream != null) {
                pipedOutputStream.close();
            }
        } catch (Throwable e) {
            Log.error("RemoteSubscribe.stop error", e);
        }
    }

    private void decodeFrames() {
        try {
            grabber.start();
            RemoteScreen remoteScreen = RemoteClient.getRemoteClient().getRemoteScreen();
            remoteScreen.resizeCanvas();
            while (!Thread.currentThread().isInterrupted()) {
                Frame frame;
                BufferedImage img;
                if ((frame = grabber.grabFrame()) != null && (img = frameConverter.convert(frame)) != null) {
                    remoteScreen.showImg(img);
                }
            }
        } catch (Throwable e) {
            Log.error("RemoteSubscribe.decodeFrames error", e);
        } finally {
            Log.info("RemoteSubscribe.decodeFrames stop");
            try {
                if (grabber != null) {
                    grabber.stop();
                }
            } catch (Exception e) {
            }
        }
    }

}
