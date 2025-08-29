package io.github.springstudent.ada.client.core;

import io.github.springstudent.ada.client.RemoteClient;
import io.github.springstudent.ada.common.Constants;
import io.github.springstudent.ada.common.log.Log;
import io.github.springstudent.ada.protocol.cmd.*;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static java.awt.event.KeyEvent.*;

/**
 * 被控制方
 *
 * @author ZhouNing
 * @date 2024/12/9 8:40
 **/
public class RemoteControlled extends RemoteControll implements RemoteScreenRobot {

    private static final char UNIX_SEPARATOR_CHAR = '/';

    private final Set<Integer> pressedKeys = new HashSet<>();

    private RemoteGrabber remoteGrabber;

    private Robot robot;

    public RemoteControlled() {

        this.remoteGrabber = new RemoteGrabber();
        try {
            robot = new Robot();
            robot.setAutoDelay(1);
        } catch (AWTException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void stop() {
        super.stop();
        remoteGrabber.stop();
    }

    @Override
    public void start() {
        super.start();
        remoteGrabber.start();
    }

    public void closeSession(String deviceCode) {
        fireCmd(new CmdReqCapture(deviceCode, CmdReqCapture.STOP_CAPTURE_BY_CONTROLLED));
    }

    @Override
    public void handleCmd(Cmd cmd) {
        if (cmd.getType().equals(CmdType.ResCapture)) {
            CmdResCapture cmdResCapture = (CmdResCapture) cmd;
            if (cmdResCapture.getCode() == CmdResCapture.START_) {
                RemoteClient.getRemoteClient().setControlledAndCloseSessionLabelVisible(true);
                start();
            } else if (cmdResCapture.getCode() == CmdResCapture.STOP_) {
                RemoteClient.getRemoteClient().setControlledAndCloseSessionLabelVisible(false);
                stop();
            }
        } else if (cmd.getType().equals(CmdType.KeyControl)) {
            this.handleMessage((CmdKeyControl) cmd);
        } else if (cmd.getType().equals(CmdType.MouseControl)) {
            this.handleMessage((CmdMouseControl) cmd);
        } else if (cmd.getType().equals(CmdType.ReqRemoteClipboard)) {
            super.sendClipboard().whenComplete((aByte, throwable) -> {
                if (throwable != null || aByte != CmdResRemoteClipboard.OK) {
                    fireCmd(new CmdResRemoteClipboard());
                }
            });
        } else if (cmd.getType().equals(CmdType.ClipboardText) || cmd.getType().equals(CmdType.ClipboardTransfer)) {
            if (needSetClipboard(cmd)) {
                super.setClipboard(cmd).whenComplete((o, o2) -> {
                    fireCmd(new CmdResRemoteClipboard());
                });
            }
        } else if (cmd.getType().equals(CmdType.CaptureConfig)) {
            CmdCaptureConfig cmdCaptureConfig = (CmdCaptureConfig) cmd;
            remoteGrabber.config(cmdCaptureConfig.getFrameRate(), cmdCaptureConfig.getBitRate());
        }
    }

    @Override
    public String getType() {
        return Constants.CONTROLLED;
    }


    @Override
    public void handleMessage(CmdMouseControl message) {
        if (message.isPressed()) {
            if (message.isButton1()) {
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            } else if (message.isButton2()) {
                robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
            } else if (message.isButton3()) {
                robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
            }
        } else if (message.isReleased()) {
            if (message.isButton1()) {
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            } else if (message.isButton2()) {
                robot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);
            } else if (message.isButton3()) {
                robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
            }
        } else if (message.isWheel()) {
            robot.mouseWheel(message.getRotations());
        }
        robot.mouseMove(message.getX(), message.getY());
    }

    @Override
    public void handleMessage(CmdKeyControl message) {
        if (message.isPressed()) {
            try {
                pressKey(message);
            } catch (IllegalArgumentException ex) {
                Log.error("Error while handling " + message);
            }
        } else if (message.isReleased()) {
            try {
                releaseKey(message);
            } catch (IllegalArgumentException ex) {
                Log.error("Error while handling " + message);
            }
        }
    }


    private void pressKey(CmdKeyControl message) {
        int keyCode = escapeByOsId(message.getKeyCode());
        if (keyCode != VK_UNDEFINED) {
            if (keyCode == VK_ALT_GRAPH && File.separatorChar != UNIX_SEPARATOR_CHAR) {
                robot.keyPress(VK_CONTROL);
                pressedKeys.add(VK_CONTROL);
                robot.keyPress(VK_ALT);
                pressedKeys.add(VK_ALT);
                Log.debug("KeyCode ALT_GRAPH %s", () -> String.valueOf(message));
                return;
            }
            Log.debug("KeyCode %s", () -> String.valueOf(message));
            try {
                robot.keyPress(keyCode);
                pressedKeys.add(keyCode);
                return;
            } catch (IllegalArgumentException ie) {
                Log.debug("Proceeding with plan B");
            }
        }
        Log.debug("Undefined KeyCode %s", () -> String.valueOf(message));
        if (message.getKeyChar() != CHAR_UNDEFINED) {
            int dec = message.getKeyChar();
            Log.debug("KeyChar as unicode " + dec + " %s", () -> String.valueOf(message));
            pressedKeys.forEach(robot::keyRelease);
            typeUnicode(dec);
            pressedKeys.forEach(robot::keyPress);
            return;
        }
        Log.warn("Undefined KeyChar " + message);
    }

    private void typeUnicode(int keyCode) {
        if (File.separatorChar == UNIX_SEPARATOR_CHAR) {
            typeLinuxUnicode(keyCode);
            return;
        }
        typeWindowsUnicode(keyCode);
    }

    private void releaseKey(CmdKeyControl message) {
        int keyCode = escapeByOsId(message.getKeyCode());
        if (keyCode != VK_UNDEFINED) {
            if (keyCode == VK_ALT_GRAPH && File.separatorChar != UNIX_SEPARATOR_CHAR) {
                robot.keyRelease(VK_ALT);
                pressedKeys.remove(VK_ALT);
                robot.keyRelease(VK_CONTROL);
                pressedKeys.remove(VK_CONTROL);
                Log.debug("KeyCode ALT_GRAPH %s", () -> String.valueOf(message));
                return;
            }
            Log.debug("KeyCode %s", () -> String.valueOf(message));
            try {
                robot.keyRelease(keyCode);
                pressedKeys.remove(keyCode);
            } catch (IllegalArgumentException ie) {
                Log.warn("Error releasing KeyCode " + message);
            }
        }
    }

    private int escapeByOsId(int keyCode) {
        if (RemoteClient.getRemoteClient().isMac() && keyCode == VK_WINDOWS) {
            return VK_META;
        } else {
            return keyCode;
        }
    }

    /**
     * Unicode characters are typed in decimal on Windows ä => 228
     */
    private void typeWindowsUnicode(int keyCode) {
        robot.keyPress(VK_ALT);
        // simulate a numpad key press for each digit
        for (int i = 3; i >= 0; --i) {
            int code = keyCode / (int) (Math.pow(10, i)) % 10 + VK_NUMPAD0;
            robot.keyPress(code);
            robot.keyRelease(code);
        }
        robot.keyRelease(VK_ALT);
    }

    /**
     * Unicode characters are typed in hex on Linux ä => e4
     */
    private void typeLinuxUnicode(int keyCode) {
        robot.keyPress(VK_CONTROL);
        robot.keyPress(VK_SHIFT);
        robot.keyPress(VK_U);
        robot.keyRelease(VK_U);
        char[] charArray = Integer.toHexString(keyCode).toCharArray();
        // simulate a key press/release for each char
        // char[] { 'e', '4' }  => keyPress(69), keyRelease(69), keyPress(52), keyRelease(52)
        for (char c : charArray) {
            int code = Character.toUpperCase(c);
            robot.keyPress(code);
            robot.keyRelease(code);
        }
        robot.keyRelease(VK_SHIFT);
        robot.keyRelease(VK_CONTROL);
    }
}
