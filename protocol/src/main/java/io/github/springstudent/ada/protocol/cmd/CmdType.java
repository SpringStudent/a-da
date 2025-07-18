package io.github.springstudent.ada.protocol.cmd;

/**
 * @author ZhouNing
 * @date 2024/12/11 8:39
 **/
public enum CmdType {
    ReqPing,
    ReqOpen,
    ReqCapture,
    ReqRemoteClipboard,
    ResOpen,
    ResCliInfo,
    ResCapture,
    ResStream,
    ResPong,
    ResRemoteClipboard,
    Capture,
    CompressorConfig,
    CaptureConfig,
    KeyControl,
    MouseControl,
    ClipboardText,
    ClipboardTransfer,
    ReqCliInfo,
    ChangePwd,
}
