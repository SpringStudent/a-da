import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import java.io.FileOutputStream;

/**
 * @author ZhouNing
 * @date 2025/7/16 9:20
 **/
public class PushRtp {

    public static void main(String[] args) throws Exception {
        // 1. 配置参数（对应FFmpeg命令参数）
        String rtpUrl = "rtp://127.0.0.1:10000"; // 推流地址

        // 编码参数（对应FFmpeg命令的编码选项）
        int frameRate = 15;                   // 对应 -r 15
        int videoBitrate = 2_000_000;         // 对应 -b:v 2M（2Mbps）
        String preset = "ultrafast";          // 对应 -preset ultrafast
        String tune = "zerolatency";          // 对应 -tune zerolatency


        // 2. 初始化桌面采集器（对应输入部分）
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("desktop");
        grabber.setFormat("gdigrab");
        grabber.setFrameRate(15);

        // 采集优化
        grabber.setOption("draw_mouse", "0");        // 不捕获鼠标
        grabber.setOption("show_region", "0");       // 不显示区域
        grabber.setOption("offset_x", "0");          // 偏移优化
        grabber.setOption("offset_y", "0");
        grabber.setOption("hwaccel", "auto");
        grabber.setOption("threads", "auto");
        // 启动采集器
        grabber.start();

        // 3. 初始化RTP推流器（对应输出部分）
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
                rtpUrl,
                grabber.getImageWidth(),
                grabber.getImageHeight()
        );
        // 配置推流器参数（对应FFmpeg编码和输出选项）
        recorder.setFormat("rtp");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFrameRate(frameRate);
        recorder.setVideoBitrate(videoBitrate);
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);

        // 设置x264编码选项（对应-preset和-tune）
        recorder.setVideoOption("preset", preset);
        recorder.setVideoOption("tune", tune);
        // 额外优化：确保实时性
        recorder.setVideoOption("profile:v", "baseline");
        recorder.setVideoOption("level", "3.0");
        recorder.setVideoOption("crf", "23");
        recorder.setGopSize(15*2);

        recorder.start();
        // 5. 循环采集并推送桌面帧
        Frame frame;
        while ((frame = grabber.grab()) != null) {
            recorder.record(frame); // 推送帧
        }


        // 6. 释放资源
        recorder.stop();
        recorder.release();
        grabber.stop();
        grabber.release();
        System.out.println("推流结束");
    }
}
