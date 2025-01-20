package io.github.springstudent.ada.client.core;


import io.github.springstudent.ada.client.RemoteClient;
import io.github.springstudent.ada.common.log.Log;
import org.bytedeco.javacv.CanvasFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.springstudent.ada.common.utils.ImageUtilities.getOrCreateIcon;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_WINDOWS;
import static java.lang.Math.abs;
import static java.lang.String.format;

/**
 * @author ZhouNing
 * @date 2024/12/9 8:42
 */
public class RemoteScreen extends CanvasFrame {

    private static final int OFFSET = 6;

    private transient RemoteScreenListener listener;

    private static final int DEFAULT_FACTOR = 1;
    private double xFactor = DEFAULT_FACTOR;
    private double yFactor = DEFAULT_FACTOR;

    private Timer sessionTimer;

    private JToggleButton windowsKeyToggleButton;

    private JToggleButton ctrlKeyToggleButton;

    private final AtomicBoolean fitToScreenActivated = new AtomicBoolean(false);

    private final AtomicBoolean keepAspectRatioActivated = new AtomicBoolean(false);

    private final AtomicBoolean isImmutableWindowsSize = new AtomicBoolean(false);

    private final AtomicBoolean windowsKeyActivated = new AtomicBoolean(false);

    private final AtomicBoolean ctrlKeyActivated = new AtomicBoolean(false);

    public RemoteScreen() {
        super("远程桌面");
        this.setVisible(false);
        this.listener = RemoteClient.getRemoteClient().getController();
        initFrame();
        setFocusTraversalKeysEnabled(false);
        initMenuBar();
        initListeners();
    }


    private void initFrame() {
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(800, 600);
        this.setLayout(new BorderLayout());
        this.setLocationRelativeTo(null);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                RemoteClient.getRemoteClient().closeRemoteScreen();
            }
        });
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        // 适配屏幕菜单项
        JCheckBoxMenuItem fitToScreenItem = new JCheckBoxMenuItem(new AbstractAction("适配屏幕") {
            @Override
            public void actionPerformed(ActionEvent ev) {
                fitToScreenActivated.set(!fitToScreenActivated.get());
                if (fitToScreenActivated.get()) {
                    resetCanvas();
                } else {
                    resetFactors();
                }
                repaint();
            }
        });
        // 保持宽高比菜单项
        JCheckBoxMenuItem keepAspectRatioItem = new JCheckBoxMenuItem(new AbstractAction("保持宽高比") {
            @Override
            public void actionPerformed(ActionEvent ev) {
                keepAspectRatioActivated.set(!keepAspectRatioActivated.get());
                resetCanvas();
                repaint();
            }
        });
        keepAspectRatioItem.setEnabled(false);
        // 根据适配屏幕的状态动态控制保持宽高比的可见性
        fitToScreenItem.addActionListener(e -> keepAspectRatioItem.setEnabled(fitToScreenActivated.get()));
        //发送win键
        this.windowsKeyToggleButton = createToggleButton(createSendWindowsKeyAction());
        menuBar.add(windowsKeyToggleButton);
        menuBar.add(Box.createHorizontalStrut(5));
        this.ctrlKeyToggleButton = createToggleButton(createSendCtrlKeyAction());
        menuBar.add(ctrlKeyToggleButton);
        menuBar.add(Box.createHorizontalStrut(5));
        this.setJMenuBar(menuBar);
    }

    private Action createSendWindowsKeyAction() {
        final Action sendWindowsKey = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                if (windowsKeyActivated.get()) {
                    fireOnKeyReleased(VK_WINDOWS, ' ');
                } else {
                    fireOnKeyPressed(VK_WINDOWS, ' ');
                }
                windowsKeyActivated.set(!windowsKeyActivated.get());
            }
        };
        sendWindowsKey.putValue(Action.SHORT_DESCRIPTION, "发送win键(mac的command)");
        sendWindowsKey.putValue(Action.SMALL_ICON, getOrCreateIcon("win.png"));
        return sendWindowsKey;
    }

    private Action createSendCtrlKeyAction() {
        final Action sendCtrlKey = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                if (ctrlKeyActivated.get()) {
                    fireOnKeyReleased(VK_CONTROL, ' ');
                } else {
                    fireOnKeyPressed(VK_CONTROL, ' ');
                }
                ctrlKeyActivated.set(!ctrlKeyActivated.get());
            }
        };
        sendCtrlKey.putValue(Action.SHORT_DESCRIPTION, "发送ctrl键");
        sendCtrlKey.putValue(Action.SMALL_ICON, getOrCreateIcon("ctrl.png"));
        return sendCtrlKey;
    }

    protected JToggleButton createToggleButton(Action action) {
        final JToggleButton button = new JToggleButton();
        addButtonProperties(action, button);
        return button;
    }

    private JButton createButton(Action action) {
        final JButton button = new JButton();
        addButtonProperties(action, button);
        return button;
    }

    private void addButtonProperties(Action action, AbstractButton button) {
        button.setMargin(new Insets(1, 1, 1, 1));
        button.setHideActionText(true);
        button.setAction(action);
        button.setFocusable(false);
        button.setDisabledIcon(null);
        button.setSelected(false);
        button.setVisible(true);
    }

    private void initListeners() {
        addFocusListener();
        addKeyListeners();
        addMouseListeners();
        addResizeListener();
        addMinMaximizedListener();
    }


    private void addFocusListener() {
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent ev) {
                fireOnKeyReleased(-1, Character.MIN_VALUE);
                if (windowsKeyActivated.get()) {
                    windowsKeyToggleButton.setSelected(false);
                    windowsKeyActivated.set(!windowsKeyActivated.get());
                }
                if (ctrlKeyActivated.get()) {
                    ctrlKeyToggleButton.setSelected(false);
                    ctrlKeyActivated.set(!ctrlKeyActivated.get());
                }
            }
        });
    }

    private void addMouseListeners() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent ev) {
                fireOnMousePressed(ev.getX(), ev.getY(), ev.getButton());
            }

            @Override
            public void mouseReleased(MouseEvent ev) {
                fireOnMouseReleased(ev.getX(), ev.getY(), ev.getButton());
            }
        });

        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent ev) {
                fireOnMouseMove(ev.getX(), ev.getY());
            }

            @Override
            public void mouseMoved(MouseEvent ev) {
                fireOnMouseMove(ev.getX(), ev.getY());
            }
        });

        this.addMouseWheelListener(ev -> {
            fireOnMouseWheeled(ev.getX(), ev.getY(), ev.getWheelRotation());
        });
    }

    private void addKeyListeners() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent ev) {
                fireOnKeyPressed(ev.getKeyCode(), ev.getKeyChar());
            }

            @Override
            public void keyReleased(KeyEvent ev) {
                fireOnKeyReleased(ev.getKeyCode(), ev.getKeyChar());
            }
        });
    }

    private void addResizeListener() {
        addComponentListener(new ComponentAdapter() {
            private Timer resizeTimer;

            @Override
            public void componentResized(ComponentEvent ev) {
                if (resizeTimer != null) {
                    resizeTimer.stop();
                }
                resizeTimer = new Timer(500, e -> resetCanvas());
                resizeTimer.setRepeats(false);
                resizeTimer.start();
            }
        });
    }

    private void addMinMaximizedListener() {
        addWindowStateListener(event -> isImmutableWindowsSize.set((event.getNewState() & Frame.ICONIFIED) == Frame.ICONIFIED || (event.getNewState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH));
    }
    public void launch() {
        long sessionStartTime = Instant.now().getEpochSecond();
        sessionTimer = new Timer(1000, e -> {
            final long seconds = Instant.now().getEpochSecond() - sessionStartTime;
        });
        sessionTimer.start();
        SwingUtilities.invokeLater(() -> this.setVisible(true));

    }

    public void close() {
        if (sessionTimer != null) {
            sessionTimer.stop();
        }
        SwingUtilities.invokeLater(() -> {
            this.setVisible(false);
            this.dispose();
        });
    }


    public void computeScaleFactors(int sourceWidth, int sourceHeight, boolean keepAspectRatio) {
        Log.debug(format("ComputeScaleFactors for w: %d h: %d", sourceWidth, sourceHeight));
        canvas.setSize(canvas.getWidth() - OFFSET, canvas.getHeight() - OFFSET);
        xFactor = canvas.getWidth() / sourceWidth;
        yFactor = canvas.getHeight() / sourceHeight;
        if (keepAspectRatio && abs(xFactor - yFactor) > 0.01) {
            resizeWindow(sourceWidth, sourceHeight);
        }
    }

    private void resizeWindow(int sourceWidth, int sourceHeight) {
        Log.debug("%s", () -> format("Resize  W:H %d:%d x:y %f:%f", this.getWidth(), this.getHeight(), xFactor, yFactor));
        int menuHeight = this.getHeight() - canvas.getHeight();
        final Rectangle maximumWindowBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        if (xFactor < yFactor) {
            if ((sourceWidth * yFactor) + OFFSET < maximumWindowBounds.width) {
                xFactor = yFactor;
                this.setSize((int) (sourceWidth * xFactor) + OFFSET, this.getHeight());
            } else {
                yFactor = xFactor;
                this.setSize(this.getWidth(), (int) (sourceHeight * yFactor) + menuHeight + OFFSET);
            }
        } else {
            if ((sourceHeight * xFactor) + menuHeight + OFFSET < maximumWindowBounds.height) {
                yFactor = xFactor;
                this.setSize(this.getWidth(), (int) (sourceHeight * yFactor) + menuHeight + OFFSET);
            } else {
                xFactor = yFactor;
                this.setSize((int) (sourceWidth * xFactor) + OFFSET, this.getHeight());
            }
        }
        Log.debug("%s", () -> format("Resized W:H %d:%d x:y %f:%f", this.getWidth(), this.getHeight(), xFactor, yFactor));
    }

    private void resetFactors() {
        xFactor = DEFAULT_FACTOR;
        yFactor = DEFAULT_FACTOR;
    }

    void resetCanvas() {
        canvas = null;
    }

    private void fireOnMouseMove(int x, int y) {
        listener.onMouseMove(scaleXPosition(x), scaleYPosition(y));
    }

    private void fireOnMousePressed(int x, int y, int button) {
        listener.onMousePressed(scaleXPosition(x), scaleYPosition(y), button);
    }

    private void fireOnMouseReleased(int x, int y, int button) {
        listener.onMouseReleased(scaleXPosition(x), scaleYPosition(y), button);
    }

    private void fireOnMouseWheeled(int x, int y, int rotations) {
        listener.onMouseWheeled(scaleXPosition(x), scaleYPosition(y), rotations);
    }

    private int scaleYPosition(int y) {
        return (int) Math.round(y / yFactor);
    }

    private int scaleXPosition(int x) {
        return (int) Math.round(x / xFactor);
    }

    private void fireOnKeyPressed(int keyCode, char keyChar) {
        listener.onKeyPressed(keyCode, keyChar);
    }

    private void fireOnKeyReleased(int keyCode, char keyChar) {
        listener.onKeyReleased(keyCode, keyChar);
    }

}
