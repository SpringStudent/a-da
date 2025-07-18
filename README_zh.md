[English](README.md) | [中文](README_zh.md)

#### a-da(ā dà)中译为"阿大",以我儿子喜欢的词命名的一个开源的远程桌面控制系统

支持流媒体分布式的远程桌面控制系统，基于javacv+netty+swing技术，通过流媒体传输画面从而保证低延迟和稳定的帧率，本地测试延迟低于200ms，支持windows/macos操作系统，
如果有其他操作系统使用需求可移步我另一个开源项目：https://github.com/SpringStudent/remote-desktop-control

#### 模块说明

* transport: 传输模块，负责远程桌面指令和粘贴板传输
* stream: 流媒体模块，负责远程桌面画面传输
* registry: 注册中心模块，作为服务注册中心
* protocol: 协议模块，负责协议的定义
* common: 公共模块，负责公共工具类的定义
* client: 客户端模块，面向用户

#### 运行环境
Java 8 或更高版本
用于依赖管理的 Maven

#### 系统演示

[Bilibili Video](https://www.bilibili.com/video/BV1q5ZCYvEJ3)

#### Q&A

1.抓屏参数配置RemoteGrabber.java

```java
/**
 * 视频抓取参数配置
 */
FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("desktop");
//是否抓取鼠标？
grabber.setOption("draw_mouse", "0");
//抓屏区域
grabber.setOption("offset_x", "0");
grabber.setOption("offset_y", "0");
//抓屏帧率
grabber.setOption("framerate", "45");
//抓屏自动选择硬件加速
grabber.setOption("hwaccel", "auto");
//设置抓屏使用的线程数。
grabber.setOption("threads", "auto");
//抓屏幕大小用
grabber.setOption("video_size", videoSize());

/**
 * 视频编码参数配置
 */
recorder = new FFmpegFrameRecorder(RemoteClient.getRemoteClient().getStreamServer() + "/receive?id=" + streamId, grabber.getImageWidth(), grabber.getImageHeight());
recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG1VIDEO);
//视频编码格式
recorder.setFormat("mpegts");
//视频帧率
recorder.setFrameRate(45);
//视频编码的预设选项，用于控制编码速度和压缩效率之间的平衡
recorder.setVideoOption("preset", "ultrafast");
//视频编码的调优选项，用于控制编码速度和压缩效率之间的平衡
recorder.setVideoOption("tune", "zerolatency");
//视频比特率，值越高视频越清晰
recorder.setVideoBitrate(1000000);
//设置编码时使用的线程数
recorder.setOption("threads", "auto");
```


通过指定javacv运行平台减少RemoteClient.jar打包大小，下面是windows上的打包命令,其他平台参考：https://github.com/bytedeco/javacpp-presets/wiki/Reducing-the-Number-of-Dependencies

`mvn clean install -Djavacpp.platform=windows-x86_64`


#### TODO

* 粘贴板功能 (已完成)
* 流媒体分布式(已完成)
* 粘贴板传输分布式(已完成)
* 清晰度可配置(已完成)
* 流媒体管理可视化

#### 碎碎念

以netty的并发能力来说，支撑个上千到上万的长连接进行远程桌面指令透传是没问题的，所以传输层netty服务暂时不做分布式了，即使是做了也是为了学习，不是为了实际应用。

以当前架构的设计，并发的短板主要还是流媒体，所以流媒体分布式是必须要做的，当然粘贴板涉及到大文件传输，分布式也是必要的，得益于流媒体和文件传输的无状态特性，
实现起来并不麻烦，不同于netty服务是有状态的，如果要对netty服务做分布式需要依赖中心数据库比如redis或者mysql，涉及到的改动范围特别大。

2025-3-21思考：如果传输层netty服务要做分布式，我会用mqtt协议实现传输重构替代netty传输，mysql或者redis做连接状态存储会是最佳选择

