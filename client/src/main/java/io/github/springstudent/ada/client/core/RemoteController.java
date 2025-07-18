package io.github.springstudent.ada.client.core;


import io.github.springstudent.ada.client.RemoteClient;
import io.github.springstudent.ada.client.monitor.BitCounter;
import io.github.springstudent.ada.client.monitor.Counter;
import io.github.springstudent.ada.client.monitor.FpsCounter;
import io.github.springstudent.ada.client.utils.DialogFactory;
import io.github.springstudent.ada.common.Constants;
import io.github.springstudent.ada.common.log.Log;
import io.github.springstudent.ada.common.utils.EmptyUtils;
import io.github.springstudent.ada.protocol.cmd.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
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

    private FpsCounter fpsCounter;

    private RemoteSubscribe remoteSubscribe;

    private String lastSelectedBitrate;

    private String lastSelectedFrameRate;

    private Integer frameRateGap;

    public RemoteController() {
        receivedBitCounter = new BitCounter("receivedBits", "网络宽带使用量");
        receivedBitCounter.start(1000);
        fpsCounter = new FpsCounter("fpsCounter", "每秒画面帧数");
        fpsCounter.start(1000);
        counters = new ArrayList<>(Arrays.asList(fpsCounter, receivedBitCounter));
        frameRateGap = 1000 / 30;
    }

    public BitCounter getReceivedBitCounter() {
        return receivedBitCounter;
    }

    public FpsCounter getFpsCounter() {
        return fpsCounter;
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

    public void openSession(String deviceCode, String password) {
        this.deviceCode = deviceCode;
        fireCmd(new CmdReqCapture(deviceCode, CmdReqCapture.START_CAPTURE, password));
    }


    public void closeSession() {
        fireCmd(new CmdReqCapture(deviceCode, CmdReqCapture.STOP_CAPTURE));
    }

    @Override
    public void handleCmd(Cmd cmd) {
        if (cmd.getType().equals(CmdType.ResOpen)) {
            CmdResOpen cmdResOpen = (CmdResOpen) cmd;
            if (cmdResOpen.getCode() == CmdResOpen.OFFLINE) {
                showMessageDialog("被控制端不在线", JOptionPane.ERROR_MESSAGE);
            } else if (cmdResOpen.getCode() == CmdResOpen.CONTROL) {
                showMessageDialog("请先断开其他远程控制中的连接", JOptionPane.ERROR_MESSAGE);
            } else if (cmdResOpen.getCode() == CmdResOpen.OK) {
                SwingUtilities.invokeLater(() -> RemoteClient.getRemoteClient().openRemoteScreen());
            }
        } else if (cmd.getType().equals(CmdType.ResCapture)) {
            CmdResCapture cmdResCapture = (CmdResCapture) cmd;
            if (cmdResCapture.getCode() == CmdResCapture.START) {
                RemoteClient.getRemoteClient().getRemoteScreen().launch(cmdResCapture.getScreenNum(),cmdResCapture.getOs());
                start();
            } else if (cmdResCapture.getCode() == CmdResCapture.STOP) {
                RemoteClient.getRemoteClient().getRemoteScreen().close();
                stop();
            } else if (cmdResCapture.getCode() == CmdResCapture.STOP_BYCONTROLLED) {
                RemoteClient.getRemoteClient().getRemoteScreen().close();
                stop();
                showMessageDialog("被控制端断开了连接", JOptionPane.ERROR_MESSAGE);
            } else if (cmdResCapture.getCode() == CmdResCapture.STOP_CHANNELINACTIVE) {
                RemoteClient.getRemoteClient().getRemoteScreen().close();
                stop();
                showMessageDialog("被控制端不在线", JOptionPane.ERROR_MESSAGE);
            } else if (cmdResCapture.getCode() == CmdResCapture.OFFLINE) {
                showMessageDialog("被控制端不在线", JOptionPane.ERROR_MESSAGE);
            } else if (cmdResCapture.getCode() == CmdResCapture.CONTROL) {
                showMessageDialog("请先断开其他远程控制中的连接", JOptionPane.ERROR_MESSAGE);
            } else if (cmdResCapture.getCode() == CmdResCapture.PWDERROR) {
                showMessageDialog("密码错误", JOptionPane.ERROR_MESSAGE);
            }
        } else if (cmd.getType().equals(CmdType.ResStream)) {
            if (remoteSubscribe != null) {
                remoteSubscribe.close();
            }
            try {
                remoteSubscribe = new RemoteSubscribe(((CmdResStream) cmd).getPlayUrl());
            } catch (Exception e) {
                Log.error("remote subscribe error", e);
                showMessageDialog("初始化远程画面失败", JOptionPane.ERROR_MESSAGE);
                this.closeSession();
            }
        } else if (cmd.getType().equals(CmdType.ClipboardText) || cmd.getType().equals(CmdType.ClipboardTransfer)) {
            if (needSetClipboard(cmd)) {
                super.setClipboard(cmd).whenComplete((o, o2) -> RemoteClient.getRemoteClient().getRemoteScreen().transferClipboarButton(true));
            }
        } else if (cmd.getType().equals(CmdType.ResRemoteClipboard)) {
            RemoteClient.getRemoteClient().getRemoteScreen().transferClipboarButton(true);
        }
    }

    @Override
    public String getType() {
        return Constants.CONTROLLER;
    }

    public Action createCaptureConfigurationAction() {
        final Action configure = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                JFrame frame = (JFrame) SwingUtilities.getRoot(RemoteClient.getRemoteClient().getRemoteScreen());
                final JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                // 1. 清晰度行
                JPanel bitrateRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JLabel bitrateLabel = new JLabel("画质：");
                bitrateRow.add(bitrateLabel);
                ButtonGroup bitrateGroup = new ButtonGroup();
                JRadioButton[] bitrateButtons = {
                        new JRadioButton("360"),
                        new JRadioButton("720"),
                        new JRadioButton("1024"),
                        new JRadioButton("2048"),
                        new JRadioButton("3072"),
                        new JRadioButton("4096"),
                        new JRadioButton("6144")
                };
                for (JRadioButton btn : bitrateButtons) {
                    bitrateGroup.add(btn);
                    bitrateRow.add(btn);
                }
                panel.add(bitrateRow);
                setSelectedButton(bitrateGroup, lastSelectedBitrate);
                // 2. 帧率行
                JPanel frameRateRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JLabel frameRateLabel = new JLabel("帧率：");
                frameRateRow.add(frameRateLabel);
                ButtonGroup frameRateGroup = new ButtonGroup();
                JRadioButton[] frameRateButtons = {
                        new JRadioButton("20"),
                        new JRadioButton("25"),
                        new JRadioButton("30"),
                        new JRadioButton("35"),
                        new JRadioButton("40"),
                        new JRadioButton("45")
                };
                for (JRadioButton btn : frameRateButtons) {
                    frameRateGroup.add(btn);
                    frameRateRow.add(btn);
                }
                panel.add(frameRateRow);
                setSelectedButton(frameRateGroup, lastSelectedFrameRate);
                final boolean ok = DialogFactory.showOkCancel(frame, "画面设置", panel, true, () -> {
                    String selectedBitrate = getSelectedButtonText(bitrateGroup);
                    String selectedFrameRate = getSelectedButtonText(frameRateGroup);
                    if (EmptyUtils.isEmpty(selectedBitrate)) {
                        return "请选择清晰度";
                    }
                    if (EmptyUtils.isEmpty(selectedFrameRate)) {
                        return "请选择帧率";
                    }
                    return null;
                });
                if (ok) {
                    lastSelectedFrameRate = getSelectedButtonText(frameRateGroup);
                    lastSelectedBitrate = getSelectedButtonText(bitrateGroup);
                    frameRateGap = 1000 / (lastSelectedFrameRate == null ? 30 : Integer.parseInt(lastSelectedFrameRate));
                    RemoteController.this.fireCmd(new CmdCaptureConfig(Integer.parseInt(lastSelectedFrameRate), Integer.parseInt(lastSelectedBitrate) * 1000));
                }
            }
        };
        configure.putValue(Action.NAME, "画面设置");
        return configure;
    }

    public Integer getFrameRateGap() {
        return frameRateGap;
    }

    private void setSelectedButton(ButtonGroup buttonGroup, String selectedValue) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements(); ) {
            AbstractButton button = buttons.nextElement();
            if (button.getText().equals(selectedValue)) {
                button.setSelected(true);
                break;
            }
        }
    }


    private String getSelectedButtonText(ButtonGroup buttonGroup) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements(); ) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                return button.getText();
            }
        }
        return null;
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
