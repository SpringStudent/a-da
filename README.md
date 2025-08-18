[English](README.md) | [中文](README_zh.md)

#### a-da (ā dà) is translated as "阿大", named after a term my son likes. It is an open-source remote desktop control system.

A distributed remote desktop control system with streaming media support,Based on JavaCV + Netty + Swing technology, the system achieves low-latency and stable frame rates through streaming media transmission. Local tests show a latency of less than 200ms. 
Currently, it supports the Windows/MacOs operating system. If you need support for other operating systems, please refer to my other open-source project:https://github.com/SpringStudent/remote-desktop-control

#### Module Description

* transport: Transmission module, responsible for remote desktop instructions and clipboard transmission
* stream: Streaming media module, responsible for remote desktop screen transmission
* registry: Registry center module, acting as a service registry center
* protocol: Protocol module, responsible for protocol definition
* common: Common module, responsible for the definition of common utility classes
* client: Client module, user-oriented

#### Environment
* Java 8 or higher
* Maven for dependency management

#### Video Demo

[Bilibili Video](https://www.bilibili.com/video/BV1q5ZCYvEJ3/)

#### Q&A

Screen capture parameter configuration RemoteGrabber.java

```java
/**
 * Video capture parameter configuration
 */
FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("desktop");
// Whether capture mouse
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
// Screen size for screen capture
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
// Video bitrate, the higher the value, the clearer the video.
recorder.setVideoBitrate(1000000);
// Set the number of threads used for encoding
recorder.setOption("threads", "auto");
``` 

The command to build the project for Windows x86_64 platform while reducing the JAR size by including only the necessary dependencies is,other platforms see:https://github.com/bytedeco/javacpp-presets/wiki/Reducing-the-Number-of-Dependencies

`mvn clean install -Djavacpp.platform=windows-x86_64`

#### TODO

* Clipboard functionality(completed)
* Distributed streaming media(completed)
* Distributed Clipboard Transfer(completed)
* Configurable video quality(completed)
* Visualized streaming media management
* Transport split to Netty server and File server 

#### gossip

Given Netty's concurrency capabilities, it is perfectly feasible to support thousands to tens of thousands of long connections for the transmission of remote desktop commands. Therefore, the Netty service at the transport layer will not be distributed for the time being. Even if it were to be distributed, it would be for educational purposes rather than practical application.

In the current architectural design, the primary bottleneck in concurrency lies with streaming media. Hence, distributing the streaming media service is essential. Naturally, since the clipboard involves the transfer of large files, distribution is also necessary for this component. Fortunately, 
thanks to the stateless nature of both streaming media and file transfer, implementing distribution is relatively straightforward. This stands in contrast to the Netty service, which is stateful. Distributing the Netty service would require reliance on a central database such as Redis or MySQL, involving extensive modifications and complexities.

2025-03-21 Reflection: If the Netty service in the transport layer needs to be distributed, May be use the MQTT protocol to refactor the transmission and replace Netty, with MySQL or Redis as the best choices for storing connection states.