[English](README.md) | [中文](README_zh.md)

#### a-da(ā dà)中译为"阿大",以我儿子喜欢的词命名的一个开源的远程桌面控制系统

基于javacv+netty+swing技术，通过流媒体传输画面从而保证帧率稳定，目前仅支持windows操作系统，
如果有其他操作系统使用需求可移步我另一个开源项目：https://github.com/SpringStudent/remote-desktop-control

#### 模块说明

* transport: 传输模块，负责远程桌面指令和粘贴板传输
* stream: 流媒体模块，负责远程桌面画面传输
* registry: 注册中心模块，作为服务注册中心（TODO）
* protocol: 协议模块，负责协议的定义
* common: 公共模块，负责公共工具类的定义
* client: 客户端模块，面向用户

#### 系统演示

[Bilibili Video](https://www.bilibili.com/video/BV1roDfYiEjg/)

#### TODO

* 粘贴板功能 (已完成)
* 清晰度可配置
* 流媒体分布式
* 流媒体管理可视化