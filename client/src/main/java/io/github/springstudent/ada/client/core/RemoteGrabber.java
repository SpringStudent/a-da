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
    private FFmpegFrameGrabber grabber;
    private FFmpegFrameRecorder recorder;

    public void start() throws Exception {
        // 确保在重新启动之前，grabber 被正确初始化
        if (grabber == null) {
            grabber = new FFmpegFrameGrabber("desktop");
            grabber.setFormat("gdigrab");
            grabber.setOption("offset_x", "0");
            grabber.setOption("offset_y", "0");
            grabber.setOption("framerate", "25");
        }
        grabber.start();

        // 确保在重新启动之前，recorder 被正确初始化
        if (recorder == null) {
            recorder = new FFmpegFrameRecorder("http://172.16.1.37:11110/receive?id=xxx", grabber.getImageWidth(), grabber.getImageHeight());
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG1VIDEO);
            recorder.setFormat("mpegts");
            recorder.setFrameRate(30);
            recorder.setVideoOption("preset", "ultrafast");
            recorder.setVideoOption("tune", "zerolatency");
            recorder.setVideoQuality(10);
        }
        recorder.start();
        Log.info("remoteGrabber start success");
        // 从 grabber 获取帧并传递给 recorder
        Frame frame;
        while ((frame = grabber.grab()) != null) {
            recorder.record(frame);
        }
    }

    public void stop() throws FFmpegFrameGrabber.Exception, FFmpegFrameRecorder.Exception {
        if (grabber != null) {
            grabber.stop();
            grabber = null;  // 重置 grabber，允许重新初始化
        }
        if (recorder != null) {
            recorder.stop();
            recorder = null;  // 重置 recorder，允许重新初始化
        }
        Log.info("remoteGrabber stop success");
    }
}
