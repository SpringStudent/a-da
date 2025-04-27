package io.github.springstudent.ada.client.bean;

import io.github.springstudent.ada.client.monitor.BigBrother;
import io.github.springstudent.ada.client.monitor.Counter;
import io.github.springstudent.ada.client.utils.SystemUtilities;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.TimerTask;

import static java.lang.String.format;
import static javax.swing.BoxLayout.LINE_AXIS;
import static javax.swing.SwingConstants.*;
/**
 * @author ZhouNing
 * @date 2025/3/13 14:05
 **/
public class StatusBar extends JPanel {

    private static final int HEIGHT = 20;
    private final JLabel message = new JLabel();
    private final JLabel sessionDuration = new JLabel("00:00:00");
    private final JLabel keyboardLayout = new JLabel();

    private final JLabel fps = new JLabel(String.format("%dfps", 0));

    private final double[] fpsHistory = new double[5];
    private int historyIndex = 0;

    public StatusBar() {
        setLayout(new BoxLayout(this, LINE_AXIS));
        add(Box.createHorizontalStrut(10));
        add(message);
        add(Box.createHorizontalGlue());
        addSeparator();
        addKeyboardLayout();
    }

    public void clearMessage() {
        this.message.setText(null);
    }

    public void setMessage(String message) {
        this.message.setText(message);
    }

    public void setSessionDuration(String sessionDuration) {
        this.sessionDuration.setText(sessionDuration);
    }

    public void setKeyboardLayout(String keyboardLayout) {
        this.keyboardLayout.setText(keyboardLayout);
        this.keyboardLayout.setToolTipText(format("⌨ %s", keyboardLayout));
    }

    public void setFps(double fps) {
        fpsHistory[historyIndex] = fps;
        historyIndex = (historyIndex + 1) % fpsHistory.length;
        Double curFps = Arrays.stream(fpsHistory).average().orElse(0);
        this.fps.setText(String.format("%dfps", curFps.intValue()));
    }

    public String getKeyboardLayout() {
        return keyboardLayout.getText();
    }

    private void addKeyboardLayout() {
        final Dimension dimension = new Dimension(60, HEIGHT);
        keyboardLayout.setHorizontalAlignment(CENTER);
        keyboardLayout.setSize(dimension);
        keyboardLayout.setPreferredSize(dimension);
        add(keyboardLayout);
    }

    public <T> void addCounter(Counter<T> counter, int width) {
        final JLabel lbl = new JLabel(counter.getUid());
        final Dimension dimension = new Dimension(width, HEIGHT);
        lbl.setHorizontalAlignment(CENTER);
        lbl.setSize(dimension);
        lbl.setPreferredSize(dimension);
        lbl.setToolTipText(counter.getShortDescription());
        counter.addListener((counter1, value) -> lbl.setText(counter1.formatInstantValue(value)));
        add(lbl);
    }

    public void addRamInfo() {
        final JLabel lbl = new JLabel();
        final Dimension dimension = new Dimension(110, HEIGHT);
        lbl.setHorizontalAlignment(CENTER);
        lbl.setSize(dimension);
        lbl.setPreferredSize(dimension);
        BigBrother.get().registerRamInfo(new MemoryCounter(lbl));
        lbl.setToolTipText("内存信息");
        add(lbl);
    }

    public void addConnectionDuration() {
        final Dimension dimension = new Dimension(65, HEIGHT);
        sessionDuration.setHorizontalAlignment(RIGHT);
        sessionDuration.setSize(dimension);
        sessionDuration.setPreferredSize(dimension);
        sessionDuration.setToolTipText("会话时长");
        add(sessionDuration);
    }

    public void addFps() {
        final Dimension dimension = new Dimension(45, HEIGHT);
        fps.setHorizontalAlignment(RIGHT);
        fps.setSize(dimension);
        fps.setPreferredSize(dimension);
        fps.setToolTipText("帧率");
        add(fps);
    }

    public void addSeparator() {
        final JToolBar.Separator separator = new JToolBar.Separator();
        separator.setOrientation(VERTICAL);
        add(separator);
    }

    private static class MemoryCounter extends TimerTask {
        private final JLabel lbl;

        private MemoryCounter(JLabel lbl) {
            this.lbl = lbl;
        }

        @Override
        public void run() {
            lbl.setText(SystemUtilities.getRamInfo());
        }
    }

}