package io.github.springstudent.ada.client.core;


import io.github.springstudent.ada.client.RemoteClient;
import io.github.springstudent.ada.client.monitor.BitCounter;
import io.github.springstudent.ada.client.monitor.Counter;
import io.github.springstudent.ada.common.Constants;
import io.github.springstudent.ada.common.log.Log;
import io.github.springstudent.ada.protocol.cmd.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.springstudent.ada.common.utils.ImageUtilities.getOrCreateIcon;
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

    private ArrayList<Counter<?>> counters;

    private BitCounter receivedBitCounter;

    private RemoteSubscribe remoteSubscribe;

    public RemoteController() {
        receivedBitCounter = new BitCounter("receivedBits", "网络宽带使用量");
        receivedBitCounter.start(1000);
        counters = new ArrayList<>(Arrays.asList(receivedBitCounter));
    }

    public BitCounter getReceivedBitCounter() {
        return receivedBitCounter;
    }

    public ArrayList<Counter<?>> getCounters() {
        return counters;
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
            } else if (cmdResCapture.getCode() == CmdResCapture.STOP) {
                RemoteClient.getRemoteClient().getRemoteScreen().close();
                if (remoteSubscribe != null) {
                    remoteSubscribe.close();
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
        } else if (cmd.getType().equals(CmdType.ResStream)) {
            try {
                remoteSubscribe = new RemoteSubscribe(((CmdResStream) cmd).getPlayUrl());
            } catch (Exception e) {
                Log.error("remote subscribe error", e);
                showMessageDialog("初始化远程画面失败", JOptionPane.ERROR_MESSAGE);
                this.closeSession();
            }
        } else if (cmd.getType().equals(CmdType.ClipboardText) || cmd.getType().equals(CmdType.ClipboardTransfer) && needSetClipboard(cmd)) {
            super.setClipboard(cmd).whenComplete((o, o2) -> RemoteClient.getRemoteClient().getRemoteScreen().transferClipboarButton(true));
        } else if (cmd.getType().equals(CmdType.ResRemoteClipboard)) {
            RemoteClient.getRemoteClient().getRemoteScreen().transferClipboarButton(true);
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

    public Action createRequireRemoteClipboardAction() {
        final Action getRemoteClipboard = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                requireRemoteClipboard();
            }
        };
        getRemoteClipboard.putValue(Action.SHORT_DESCRIPTION, "获取远程粘贴板");
        getRemoteClipboard.putValue(Action.SMALL_ICON, getOrCreateIcon("down.png"));
        return getRemoteClipboard;
    }

    private void requireRemoteClipboard() {
        RemoteClient.getRemoteClient().getRemoteScreen().transferClipboarButton(false);
        fireCmd(new CmdReqRemoteClipboard());
    }

    public Action createSendLoacalClibboardAction() {
        final Action setRemoteClipboard = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                RemoteClient.getRemoteClient().getRemoteScreen().transferClipboarButton(false);
                RemoteController.this.sendClipboard().whenComplete((aByte, throwable) -> {
                    if (throwable != null || aByte != CmdResRemoteClipboard.OK) {
                        RemoteClient.getRemoteClient().getRemoteScreen().transferClipboarButton(true);
                    }
                });
            }
        };
        setRemoteClipboard.putValue(Action.SHORT_DESCRIPTION, "发送本机粘贴板");
        setRemoteClipboard.putValue(Action.SMALL_ICON, getOrCreateIcon("up.png"));
        return setRemoteClipboard;
    }

}
