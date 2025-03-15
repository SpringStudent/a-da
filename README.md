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

#### TODO

* Clipboard functionality (completed)
* Configurable resolution
* Distributed streaming media
* Visualized streaming media management
