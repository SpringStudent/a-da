package io.github.springstudent.ada.client.core;


import io.github.springstudent.ada.protocol.cmd.CmdKeyControl;
import io.github.springstudent.ada.protocol.cmd.CmdMouseControl;

/**
 * @author ZhouNing
 * @date 2024/12/13 23:35
 **/
public interface RemoteScreenRobot {
    void handleMessage(CmdMouseControl message);

    void handleMessage(CmdKeyControl message);
}
