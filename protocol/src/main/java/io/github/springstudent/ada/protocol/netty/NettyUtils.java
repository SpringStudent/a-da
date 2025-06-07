package io.github.springstudent.ada.protocol.netty;

import io.github.springstudent.ada.protocol.cmd.CmdReqCliInfo;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

/**
 * @author ZhouNing
 * @date 2024/12/11 16:37
 **/
public class NettyUtils {
    private static final AttributeKey<String> ATTR_KEY_READER_TIME = AttributeKey.valueOf("readerTime");
    private static final AttributeKey<String> ATTR_KEY_DEVICE_CODE = AttributeKey.valueOf("deviceCode");
    private static final AttributeKey<String> ATTR_KEY_CONTROLL_FLAG = AttributeKey.valueOf("controllFlag");
    private static final AttributeKey<String> ATTR_KEY_CONTROLL_DEVICECODE = AttributeKey.valueOf("contollDeviceCode");
    private static final AttributeKey<CmdReqCliInfo> ATTR_KEY_CLI_INFO = AttributeKey.valueOf("cliInfo");

    public static void updateReaderTime(Channel channel, Long time) {
        channel.attr(ATTR_KEY_READER_TIME).set(time.toString());
    }

    public static Long getReaderTime(Channel channel) {
        String value = channel.attr(ATTR_KEY_READER_TIME).get();
        if (value != null) {
            return Long.valueOf(value);
        }
        return null;
    }

    public static void updateDeviceCode(Channel channel, String deviceCode) {
        channel.attr(ATTR_KEY_DEVICE_CODE).set(deviceCode);
    }

    public static String getDeviceCode(Channel channel) {
        return channel.attr(ATTR_KEY_DEVICE_CODE).get();
    }

    public static void updateControllFlag(Channel channel, String controllFlag) {
        channel.attr(ATTR_KEY_CONTROLL_FLAG).set(controllFlag);
    }

    public static String getControllFlag(Channel channel) {
        return channel.attr(ATTR_KEY_CONTROLL_FLAG).get();
    }

    public static void updateControllDeviceCode(Channel channel, String deviceCode) {
        channel.attr(ATTR_KEY_CONTROLL_DEVICECODE).set(deviceCode);
    }

    public static String getControllDeviceCode(Channel channel) {
        return channel.attr(ATTR_KEY_CONTROLL_DEVICECODE).get();
    }

    public static void updateCliInfo(Channel channel, CmdReqCliInfo cliInfo) {
        channel.attr(ATTR_KEY_CLI_INFO).set(cliInfo);
    }

    public static CmdReqCliInfo getCliInfo(Channel channel) {
        return channel.attr(ATTR_KEY_CLI_INFO).get();
    }
}

