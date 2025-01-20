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

    public RemoteSubscribe(URI serverUri) throws IOException {
        super(serverUri);
        pipedInputStream = new PipedInputStream();
        pipedOutputStream = new PipedOutputStream();
        pipedInputStream.connect(pipedOutputStream);
        grabber = new FFmpegFrameGrabber(pipedInputStream, 0);
        grabber.setFormat("mpegts");
        this.frameConverter = new Java2DFrameConverter();
        new Thread(this::decodeFrames).start();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        try {
            grabber.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    private void decodeFrames() {
        try {
            grabber.start();
            while (true) {
                Frame frame;
                BufferedImage img;
                if ((frame = grabber.grabFrame()) != null && (img = frameConverter.convert(frame)) != null) {
                    RemoteClient.getRemoteClient().getRemoteScreen().showImg(img);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
