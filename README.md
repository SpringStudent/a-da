[English](README.md) | [中文](README_zh.md)

#### a-da (ā dà) is translated as "阿大", named after a term my son likes. It is an open-source remote desktop control system.

Built on javacv + netty + swing technology, it ensures stable frame rates through streaming media transmission. Currently,
it only supports the Windows operating system. If you need support for other operating systems, you can check out another 
open-source project of mine:https://github.com/SpringStudent/remote-desktop-control

#### Module Description

* transport: Transmission module, responsible for remote desktop instructions and clipboard transmission
* stream: Streaming media module, responsible for remote desktop screen transmission
* registry: Registry center module, acting as a service registry center (TODO)
* protocol: Protocol module, responsible for protocol definition
* common: Common module, responsible for the definition of common utility classes
* client: Client module, user-oriented

#### Video Demo

[Bilibili Video](https://www.bilibili.com/video/BV1roDfYiEjg/)

#### Q&A

Screen capture parameter configuration RemoteGrabber.java

```java
/**
 * Video capture parameter configuration
 */
FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("desktop");
// Capture mouse
grabber.setOption("draw_mouse", "0");
// Screen capture area
grabber.setOption("offset_x", "0");
grabber.setOption("offset_y", "0");
// Screen capture frame rate
grabber.setOption("framerate", "45");
// Automatically select hardware acceleration for screen capture
grabber.setOption("hwaccel", "auto");
// Set the number of threads used for screen capture.
grabber.setOption("threads", "auto");
// Screen size for screen capture to switch screens
grabber.setOption("video_size", videoSize());

/**
 * Video encoding parameter configuration
 */
recorder = new FFmpegFrameRecorder(RemoteClient.getRemoteClient().getStreamServer() + "/receive?id=" + streamId, grabber.getImageWidth(), grabber.getImageHeight());
recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG1VIDEO);
// Video encoding format
recorder.setFormat("mpegts");
// Video frame rate
recorder.setFrameRate(45);
// Video encoding preset options, used to balance encoding speed and compression efficiency
recorder.setVideoOption("preset", "ultrafast");
// Video encoding tuning options, used to balance encoding speed and compression efficiency
recorder.setVideoOption("tune", "zerolatency");
// The quality of video encoding usually ranges from 0 to 21 or higher, with 0 being the highest quality
recorder.setVideoQuality(6);
// Set the number of threads used for encoding
recorder.setOption("threads", "auto");
``` 

#### TODO

* Clipboard functionality (completed)
* Configurable resolution
* Distributed streaming media
* Visualized streaming media management
