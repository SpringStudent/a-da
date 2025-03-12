package io.github.springstudent.ada.client.core;

import cn.hutool.core.util.IdUtil;
import io.github.springstudent.ada.client.RemoteClient;
import io.github.springstudent.ada.common.log.Log;
import io.github.springstudent.ada.protocol.cmd.CmdResStream;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

/**
 * @author zhouning
 * @date 2025/01/18 14:30
 */
public class RemoteGrabber {
    private Thread thread;

    public void start() {
        thread = new Thread(() -> {
            FFmpegFrameGrabber grabber = null;
            FFmpegFrameRecorder recorder = null;
            try {
                grabber = new FFmpegFrameGrabber("desktop");
                grabber.setFormat("gdigrab");
                grabber.setOption("draw_mouse", "0");
                grabber.setOption("offset_x", "0");
                grabber.setOption("offset_y", "0");
                grabber.setOption("framerate", "45");
                grabber.setOption("hwaccel", "auto");
                grabber.setOption("threads", "auto");
                grabber.start();

                String streamId = IdUtil.fastSimpleUUID();
                recorder = new FFmpegFrameRecorder(RemoteClient.getRemoteClient().getStreamServer() + "/receive?id=" + streamId, grabber.getImageWidth(), grabber.getImageHeight());
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG1VIDEO);
//                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setFormat("mpegts");
                recorder.setFrameRate(45);
                recorder.setVideoOption("preset", "ultrafast");
                recorder.setVideoOption("tune", "zerolatency");
                recorder.setVideoQuality(8);
                recorder.setOption("threads", "auto");
                recorder.start();
                Log.info("remoteGrabber start success");

                publishStream(streamId);
                Frame frame;
                while ((frame = grabber.grab()) != null && !Thread.currentThread().isInterrupted()) {
                    recorder.record(frame);
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

    public void stop() {
        Log.info("remoteGrabber to stop");
        if (thread != null) {
            thread.interrupt();
        }
    }
}
