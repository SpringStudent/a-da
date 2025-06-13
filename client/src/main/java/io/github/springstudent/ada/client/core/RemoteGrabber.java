package io.github.springstudent.ada.client.core;

import cn.hutool.core.util.IdUtil;
import io.github.springstudent.ada.client.RemoteClient;
import io.github.springstudent.ada.common.log.Log;
import io.github.springstudent.ada.protocol.cmd.CmdResStream;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import java.awt.*;

/**
 * @author zhouning
 * @date 2025/01/18 14:30
 */
public class RemoteGrabber {
    private Thread thread;
    private volatile boolean restart;
    private int frameRate;
    private int bitRate;

    public RemoteGrabber() {
        this.frameRate = 30;
        this.bitRate = 1024 * 1000;
        this.restart = false;
    }

    public void config(int cFrameRate, int cBitRate) {
        if (frameRate != cFrameRate || bitRate != cBitRate) {
            this.frameRate = cFrameRate;
            this.bitRate = cBitRate;
            restart = true;
        }
    }

    public void start() {
        thread = new Thread(() -> {
            restart = false;
            FFmpegFrameGrabber grabber = null;
            FFmpegFrameRecorder recorder = null;
            try {
                if (RemoteClient.getRemoteClient().getOsId() == 'm') {
                    grabber = new FFmpegFrameGrabber("1");
                    grabber.setFormat("avfoundation");
                    grabber.setPixelFormat(avutil.AV_PIX_FMT_0RGB);
                } else {
                    grabber = new FFmpegFrameGrabber("desktop");
                    grabber.setFormat("gdigrab");
                }
                grabber.setOption("draw_mouse", "0");
                grabber.setOption("offset_x", "0");
                grabber.setOption("offset_y", "0");
                grabber.setOption("framerate", String.valueOf(frameRate));
                grabber.setOption("hwaccel", "auto");
                grabber.setOption("threads", "auto");
                grabber.setOption("video_size", videoSize());
                grabber.setAudioChannels(0);
                grabber.start();

                String streamId = IdUtil.fastSimpleUUID();
                recorder = new FFmpegFrameRecorder(RemoteClient.getRemoteClient().getStreamServer() + "/receive?id=" + streamId, grabber.getImageWidth(), grabber.getImageHeight());
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG1VIDEO);
//                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setFormat("mpegts");
                recorder.setFrameRate(frameRate);
                recorder.setVideoOption("preset", "ultrafast");
                recorder.setVideoOption("tune", "zerolatency");
                recorder.setVideoBitrate(bitRate);
                recorder.setOption("threads", "auto");
                recorder.start();
                Log.info("remoteGrabber start success");

                publishStream(streamId);
                Frame frame;
                while (!Thread.currentThread().isInterrupted()) {
                    frame = grabber.grab();
                    if (frame != null) {
                        recorder.record(frame);
                    }
                    if (restart) {
                        RemoteGrabber.this.stop();
                        RemoteGrabber.this.start();
                    }
                }
            } catch (Exception e) {
                Log.error("remoteGrabber start exception", e);
            } finally {
                try {
                    if (recorder != null) {
                        recorder.stop();
                    }
                    if (grabber != null) {
                        grabber.stop();
                    }
                } catch (Exception e) {

                }
                Log.info("remoteGrabber stop success");
            }
        });
        thread.start();
    }

    private void publishStream(String streamId) {
        RemoteClient.getRemoteClient().getControlled().fireCmd(new CmdResStream(RemoteClient.getRemoteClient().getStreamServerWs() + "/desktop?id=" + streamId));
    }

    private String videoSize() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        DisplayMode dm = ge.getDefaultScreenDevice().getDisplayMode();
        return String.format("%dx%d", dm.getWidth(), dm.getHeight());
    }

    public void stop() {
        Log.info("remoteGrabber to stop");
        if (thread != null) {
            thread.interrupt();
        }
    }

}
