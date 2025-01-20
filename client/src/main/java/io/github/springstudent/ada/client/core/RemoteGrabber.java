package io.github.springstudent.ada.client.core;

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
        // 创建FFmpegFrameGrabber来抓取桌面
        grabber = new FFmpegFrameGrabber("desktop");
        grabber.setFormat("gdigrab");
        grabber.setOption("offset_x", "0");
        grabber.setOption("offset_y", "0");
        grabber.setOption("framerate", "25");
        grabber.start();
        // 创建FFmpegFrameRecorder来推送流
        recorder = new FFmpegFrameRecorder("http://172.16.1.72:11111/receive?id=xxx", grabber.getImageWidth(), grabber.getImageHeight());
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG1VIDEO);
        recorder.setFormat("mpegts");
        recorder.setFrameRate(30);
        recorder.setVideoOption("preset", "ultrafast");
        recorder.setVideoOption("tune", "zerolatency");
        recorder.setVideoQuality(10);
        recorder.start();
        // 从grabber抓取帧并传递给recorder
        Frame frame;
        while ((frame = grabber.grab()) != null) {
            recorder.record(frame);
        }

    }

    public void stop() throws FFmpegFrameGrabber.Exception, FFmpegFrameRecorder.Exception {
        if (grabber != null) {
            grabber.stop();
        }
        if (recorder != null) {
            recorder.stop();
        }
    }

}
