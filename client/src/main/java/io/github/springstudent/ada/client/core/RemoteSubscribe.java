package io.github.springstudent.ada.client.core;

import io.github.springstudent.ada.client.RemoteClient;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.awt.image.BufferedImage;
import java.io.IOException;
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

    public RemoteSubscribe(URI serverUri) throws IOException {
        super(serverUri);
        pipedInputStream = new PipedInputStream();
        pipedOutputStream = new PipedOutputStream();
        pipedInputStream.connect(pipedOutputStream);
        grabber = new FFmpegFrameGrabber(pipedInputStream, 0);
        grabber.setFormat("mpegts");
        frameConverter = new Java2DFrameConverter();
        decodeThread = new Thread(this::decodeFrames);
        decodeThread.start();
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
            System.out.println("###接收流媒体数据");
            pipedOutputStream.write(bytes);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        stop();
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("111123134");
        stop();
    }

    private void stop() {
        try {
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void decodeFrames() {
        try {
            grabber.start();
            while (!Thread.currentThread().isInterrupted()) {
                Frame frame;
                BufferedImage img;
                if ((frame = grabber.grabFrame()) != null && (img = frameConverter.convert(frame)) != null) {
                    RemoteClient.getRemoteClient().getRemoteScreen().showImg(img);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }finally {
            try {
                if (grabber != null) {
                    grabber.stop();
                }
            }catch (Exception e){
            }
        }
    }

}
