package io.github.springstudent.ada.common;

/**
 * @author ZhouNing
 * @date 2024/12/10 14:57
 **/
public class Constants {

    /**
     * 心跳间隔时长
     */
    public static final int HEARTBEAT_DURATION_SECONDS = 3;

    /**
     * 客户端心跳超时时长
     */
    public static final int CLIENT_SESSION_TIMEOUT_MILLS = Constants.HEARTBEAT_DURATION_SECONDS * 1000 * 10;

    public static final String CONTROLLER = "CONTROLLER";

    public static final String CONTROLLED = "CONTROLLED";

    public static final String SERVICE_STREAM = "stream";
    public static final String SERVICE_TRANSPORT = "transport";
    public static final String SERVICE_NETTY = "netty";

}
