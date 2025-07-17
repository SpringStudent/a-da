import org.bytedeco.javacv.*;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author ZhouNing
 * @date 2025/7/16 9:20
 **/
public class Subscribe {

    private FFmpegFrameGrabber grabber;
    private CanvasFrame canvas;
    private Thread decodeThread;
    private volatile boolean running = true;
    private Java2DFrameConverter frameConverter;

    public Subscribe(String rtpUrl) throws Exception {
        frameConverter = new Java2DFrameConverter();
        // 创建显示窗口，标题"Remote Desktop Stream"
        canvas = new CanvasFrame("Remote Desktop Stream");
        canvas.setCanvasSize(680, 480);
        canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 用FFmpegFrameGrabber打开RTP流
        grabber = new FFmpegFrameGrabber(new File("D:\\Develop\\wokspace-nb\\a-da\\client\\src\\test\\java\\stream.sdp"));
        grabber.setFormat("sdp");
        grabber.setOption("protocol_whitelist", "file,udp,rtp,crypto");
        grabber.setOption("fflags", "nobuffer");
        grabber.setOption("flags", "low_delay");
        grabber.setOption("framedrop", "1");
        grabber.setOption("probesize", "32");
        grabber.setOption("analyzeduration", "500000");
        grabber.setOption("max_delay", "100000"); // 最大缓冲100ms（超过则丢帧）
        grabber.setOption("sync", "ext"); // 使用外部时间戳同步，避免内部缓冲
        grabber.setOption("nofillin", "1"); // 不填充缺失帧，减少等待

        // 监听窗口关闭，停止拉流线程
        canvas.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stop();
            }
        });

        decodeThread = new Thread(this::decodeFrames);
        decodeThread.setPriority(Thread.MAX_PRIORITY);
        decodeThread.start();
    }

    private void decodeFrames() {
        try {
            grabber.start();
            while (running && !Thread.currentThread().isInterrupted()) {
                Frame frame = grabber.grabImage();  // 只拉视频帧
                if (frame == null) {
                    continue;
                }
                BufferedImage img = frameConverter.convert(frame);
                canvas.showImage(img);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                grabber.stop();
                grabber.release();
            } catch (Exception ignored) {}
            canvas.dispose();
        }
    }

    public void stop() {
        running = false;
        if (decodeThread != null && decodeThread.isAlive()) {
            decodeThread.interrupt();
        }
    }

    public static void main(String[] args) throws Exception {
        String rtpUrl = "rtp://172.16.1.37:10000";
        FFmpegLogCallback.set();
        new Subscribe(rtpUrl);
    }
}
