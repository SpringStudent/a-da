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
import java.util.concurrent.atomic.AtomicReference;

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
    private Thread uiThread;
    private final AtomicReference<BufferedImage> latestImage = new AtomicReference<>();

    private volatile boolean running = true;

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
        grabber.setOption("framedrop", "1");
        frameConverter = new Java2DFrameConverter();
        decodeThread = new Thread(this::decodeFrames);
        decodeThread.setPriority(Thread.MAX_PRIORITY);
        decodeThread.start();
        this.connect();
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        Log.info("connected to stream server");
    }

    @Override
    public void onMessage(String message) {
        Log.info("received text message: " + message);
    }

    @Override
    public void onMessage(ByteBuffer byteBuffer) {
        // 接收二进制数据并写入 PipedOutputStream
        try {
            byte[] bytes = byteBuffer.array();
            RemoteClient.getRemoteClient().getController().getReceivedBitCounter().add(bytes.length * 8);
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
            running = false;
            if (decodeThread != null && decodeThread.isAlive()) {
                decodeThread.interrupt();
            }
            if (uiThread != null && uiThread.isAlive()) {
                uiThread.interrupt();
            }
            if (pipedInputStream != null) {
                pipedInputStream.close();
            }
            if (pipedOutputStream != null) {
                pipedOutputStream.close();
            }
            Log.info("RemoteSubscribe.stop end");
        } catch (Throwable e) {
            Log.error("RemoteSubscribe.stop error", e);
        }
    }

    private void decodeFrames() {
        try {
            grabber.start();
            RemoteScreen remoteScreen = RemoteClient.getRemoteClient().getRemoteScreen();
            remoteScreen.getControlActivated().set(true);
            // 启动一个单独的线程定时更新UI
            startUIUpdateThread(remoteScreen);
            while (running && !Thread.currentThread().isInterrupted()) {
                Frame frame = grabber.grabFrame();
                if (frame == null) {
                    continue;
                }
                BufferedImage img = frameConverter.convert(frame);
                if (img != null) {
                    latestImage.set(img);
                    RemoteClient.getRemoteClient().getController().getFpsCounter().add(1);
                }
                frame.close();
            }
        } catch (Throwable e) {
            Log.error("RemoteSubscribe.decodeFrames error", e);
        } finally {
            try {
                if (grabber != null) {
                    grabber.stop();
                }
            } catch (Exception e) {
            }
            Log.info("RemoteSubscribe.decodeFrames stop");
        }
    }

    private void startUIUpdateThread(RemoteScreen remoteScreen) {
        uiThread = new Thread(() -> {
            try {
                while (running && !Thread.currentThread().isInterrupted()) {
                    BufferedImage img = latestImage.getAndSet(null);
                    if (img != null) {
                        remoteScreen.showImg(img);
                    }
                    // 控制UI更新频率，例如每16.7ms更新一次（约60fps）
                    Thread.sleep(30);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        uiThread.setDaemon(true);
        uiThread.start();
    }

}
