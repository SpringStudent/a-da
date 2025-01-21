package io.github.springstudent.ada.client.core;


import io.github.springstudent.ada.client.RemoteClient;
import io.github.springstudent.ada.common.Constants;
import io.github.springstudent.ada.common.log.Log;
import io.github.springstudent.ada.protocol.cmd.*;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.springstudent.ada.protocol.cmd.CmdKeyControl.KeyState.PRESSED;
import static io.github.springstudent.ada.protocol.cmd.CmdKeyControl.KeyState.RELEASED;
import static java.lang.String.format;

/**
 * 控制方
 *
 * @author ZhouNing
 * @date 2024/12/9 8:39
 **/
public class RemoteController extends RemoteControll implements RemoteScreenListener {
    private String deviceCode;

    private RemoteSubscribe remoteSubscribe;

    public RemoteController() {

    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void start() {
        super.start();
    }

    public void openSession(String deviceCode) {
        this.deviceCode = deviceCode;
        fireCmd(new CmdReqCapture(deviceCode, CmdReqCapture.START_CAPTURE));
    }

    public void closeSession() {
        fireCmd(new CmdReqCapture(deviceCode, CmdReqCapture.STOP_CAPTURE));
    }

    @Override
    public void handleCmd(Cmd cmd) {
        if (cmd.getType().equals(CmdType.ResCapture)) {
            CmdResCapture cmdResCapture = (CmdResCapture) cmd;
            if (cmdResCapture.getCode() == CmdResCapture.START) {
                RemoteClient.getRemoteClient().getRemoteScreen().launch();
                try {
                    remoteSubscribe = new RemoteSubscribe(new URI("ws://172.16.1.37:11110/desktop?id=xxx"));
                    remoteSubscribe.connect();
                } catch (Exception e) {
                }
            } else if (cmdResCapture.getCode() == CmdResCapture.STOP) {
                RemoteClient.getRemoteClient().getRemoteScreen().close();
                if (remoteSubscribe != null) {
                    try {
                        remoteSubscribe.close();
                    } catch (Exception e) {
                    }
                }
                stop();
            } else if (cmdResCapture.getCode() == CmdResCapture.STOP_BYCONTROLLED) {
                RemoteClient.getRemoteClient().getRemoteScreen().close();
                stop();
                if (remoteSubscribe != null) {
                    remoteSubscribe.close();
                }
                showMessageDialog("被控制端断开了连接", JOptionPane.ERROR_MESSAGE);
            } else if (cmdResCapture.getCode() == CmdResCapture.STOP_CHANNELINACTIVE) {
                RemoteClient.getRemoteClient().getRemoteScreen().close();
                stop();
                if (remoteSubscribe != null) {
                    remoteSubscribe.close();
                }
                showMessageDialog("被控制端不在线", JOptionPane.ERROR_MESSAGE);
            } else if (cmdResCapture.getCode() == CmdResCapture.OFFLINE) {
                showMessageDialog("被控制端不在线", JOptionPane.ERROR_MESSAGE);
            } else if (cmdResCapture.getCode() == CmdResCapture.CONTROL) {
                showMessageDialog("请先断开其他远程控制中的连接", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public String getType() {
        return Constants.CONTROLLER;
    }


    @Override
    public void onMouseMove(final int xs, final int ys) {
        fireCmd(new CmdMouseControl(xs, ys));
    }

    @Override
    public void onMousePressed(final int xs, final int ys, final int button) {
        int xbutton = getActingMouseButton(button);
        if (xbutton != CmdMouseControl.UNDEFINED) {
            fireCmd(new CmdMouseControl(xs, ys, CmdMouseControl.ButtonState.PRESSED, xbutton));
        }
    }

    @Override
    public void onMouseReleased(final int x, final int y, final int button) {
        int xbutton = getActingMouseButton(button);
        if (xbutton != CmdMouseControl.UNDEFINED) {
            fireCmd(new CmdMouseControl(x, y, CmdMouseControl.ButtonState.RELEASED, xbutton));
        }
    }

    private int getActingMouseButton(final int button) {
        if (MouseEvent.BUTTON1 == button) {
            return CmdMouseControl.BUTTON1;
        }
        if (MouseEvent.BUTTON2 == button) {
            return CmdMouseControl.BUTTON2;
        }
        if (MouseEvent.BUTTON3 == button) {
            return CmdMouseControl.BUTTON3;
        }
        return CmdMouseControl.UNDEFINED;
    }

    @Override
    public void onMouseWheeled(final int x, final int y, final int rotations) {
        fireCmd(new CmdMouseControl(x, y, rotations));
    }


    private final Map<Integer, Character> pressedKeys = new ConcurrentHashMap<>();

    @Override
    public void onKeyPressed(final int keyCode, final char keyChar) {
        pressedKeys.put(keyCode, keyChar);
        fireCmd(new CmdKeyControl(PRESSED, keyCode, keyChar));
    }

    /**
     * From AWT thread (!)
     */
    @Override
    public void onKeyReleased(final int keyCode, final char keyChar) {
        // -------------------------------------------------------------------------------------------------------------
        // E.g., Windows + R : [Windows.PRESSED] and then the focus is LOST =>
        // missing RELEASED events
        //
        // Currently trying to lease the 'assisted' in a consistent state - not
        // sure I should send the
        // [Windows] key and the like (e.g.,CTRL-ALT-DEL, etc...) at all ...
        // -------------------------------------------------------------------------------------------------------------
        if (keyCode == -1) {
            Log.warn(format("Got keyCode %s keyChar '%s' - releasing all keys", keyCode, keyChar));
            pressedKeys.forEach(this::onKeyReleased);
            return;
        }
        if (!pressedKeys.containsKey(keyCode)) {
            Log.warn(format("Not releasing unpressed keyCode %s keyChar '%s'", keyCode, keyChar));
            return;
        }
        pressedKeys.remove(keyCode);
        fireCmd(new CmdKeyControl(RELEASED, keyCode, keyChar));
    }

}
