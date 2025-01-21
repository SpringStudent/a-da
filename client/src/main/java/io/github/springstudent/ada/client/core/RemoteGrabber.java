package io.github.springstudent.ada.client.core;

import io.github.springstudent.ada.common.log.Log;
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
                grabber.setOption("offset_x", "0");
                grabber.setOption("offset_y", "0");
                grabber.setOption("framerate", "25");
                grabber.start();

                recorder = new FFmpegFrameRecorder("http://172.16.1.37:11110/receive?id=xxx", grabber.getImageWidth(), grabber.getImageHeight());
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG1VIDEO);
                recorder.setFormat("mpegts");
                recorder.setFrameRate(30);
                recorder.setVideoOption("preset", "ultrafast");
                recorder.setVideoOption("tune", "zerolatency");
                recorder.setVideoQuality(10);
                recorder.start();
                Log.info("remoteGrabber start success");
                Frame frame;
                while ((frame = grabber.grab()) != null) {
                    Thread.sleep(1);
                    recorder.record(frame);
                }
            } catch (Exception e) {

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

    public void stop() {
        Log.info("remoteGrabber to stop");
        if (thread != null) {
            thread.interrupt();
        }
    }
}
