package io.github.springstudent.ada.client.core;

import io.github.springstudent.ada.client.RemoteClient;
import org.bytedeco.javacv.CanvasFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.springstudent.ada.common.utils.ImageUtilities.getOrCreateIcon;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_WINDOWS;

/**
 * @author ZhouNing
 * @date 2024/12/9 8:42
 */
public class RemoteScreen extends JFrame {

    private transient RemoteScreenListener listeners;

    private int captureWidth;

    private int captureHeight;

    private CanvasFrame canvasFrame;

    private Timer sessionTimer;

    private JToggleButton windowsKeyToggleButton;

    private JToggleButton ctrlKeyToggleButton;

    private final AtomicBoolean controlActivated = new AtomicBoolean(false);

    private final AtomicBoolean isImmutableWindowsSize = new AtomicBoolean(false);

    private final AtomicBoolean windowsKeyActivated = new AtomicBoolean(false);

    private final AtomicBoolean ctrlKeyActivated = new AtomicBoolean(false);

    public RemoteScreen() {
        super("远程桌面");
        this.listeners = RemoteClient.getRemoteClient().getController();
        initFrame();
        initCanvasPanel();
        //allows for seeing the TAB with a regular KEY listener ...
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

    private void initCanvasPanel() {
        this.canvasFrame = new CanvasFrame("ddd");
        canvasFrame.setVisible(false);
        this.add(canvasFrame.getCanvas(), BorderLayout.CENTER);
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
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
        addMinMaximizedListener();
    }


    private void addFocusListener() {
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent ev) {
                if (controlActivated.get()) {
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
            }
        });
    }

    private void addMouseListeners() {
        canvasFrame.getCanvas().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent ev) {
                if (controlActivated.get()) {
                    fireOnMousePressed(ev.getX(), ev.getY(), ev.getButton());
                }
            }

            @Override
            public void mouseReleased(MouseEvent ev) {
                if (controlActivated.get()) {
                    fireOnMouseReleased(ev.getX(), ev.getY(), ev.getButton());
                }
            }
        });

        canvasFrame.getCanvas().addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent ev) {
                if (controlActivated.get()) {
                    fireOnMouseMove(ev.getX(), ev.getY());
                }
            }

            @Override
            public void mouseMoved(MouseEvent ev) {
                if (controlActivated.get()) {
                    fireOnMouseMove(ev.getX(), ev.getY());
                }
            }
        });

        canvasFrame.getCanvas().addMouseWheelListener(ev -> {
            if (controlActivated.get()) {
                fireOnMouseWheeled(ev.getX(), ev.getY(), ev.getWheelRotation());
            }
        });
    }

    private void addKeyListeners() {
        canvasFrame.getCanvas().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent ev) {
                if (controlActivated.get()) {
                    fireOnKeyPressed(ev.getKeyCode(), ev.getKeyChar());
                }
            }

            @Override
            public void keyReleased(KeyEvent ev) {
                if (controlActivated.get()) {
                    fireOnKeyReleased(ev.getKeyCode(), ev.getKeyChar());
                }
            }
        });
    }

    private void addMinMaximizedListener() {
        addWindowStateListener(event -> isImmutableWindowsSize.set((event.getNewState() & Frame.ICONIFIED) == Frame.ICONIFIED || (event.getNewState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH));
    }

    public CanvasFrame getScreenPannel() {
        return canvasFrame;
    }

    public void showImg(BufferedImage img) {
        this.captureWidth = img.getWidth();
        this.captureHeight = img.getHeight();
        this.canvasFrame.showImage(img);
    }

    public void resizeCanvas() {
        if (captureWidth <= 0 || captureHeight <= 0) {
            this.canvasFrame.setCanvasSize(getWidth(),getHeight());
        } else {
            this.canvasFrame.setCanvasSize(captureWidth, captureHeight);
        }
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
        if (canvasFrame != null) {
            canvasFrame.dispose();
        }
        controlActivated.set(false);
        SwingUtilities.invokeLater(() -> {
            this.setVisible(false);
        });

    }

    public AtomicBoolean getControlActivated() {
        return controlActivated;
    }

    private void fireOnMouseMove(int x, int y) {
        listeners.onMouseMove(scaleXPosition(x), scaleYPosition(y));
    }

    private void fireOnMousePressed(int x, int y, int button) {
        listeners.onMousePressed(scaleXPosition(x), scaleYPosition(y), button);
    }

    private void fireOnMouseReleased(int x, int y, int button) {
        listeners.onMouseReleased(scaleXPosition(x), scaleYPosition(y), button);
    }

    private void fireOnMouseWheeled(int x, int y, int rotations) {
        listeners.onMouseWheeled(scaleXPosition(x), scaleYPosition(y), rotations);
    }

    private int scaleYPosition(int y) {
        int canvasHeight = canvasFrame.getCanvas().getHeight();
        return (int) Math.round(y * (captureHeight / (double) canvasHeight));
    }

    private int scaleXPosition(int x) {
        int canvasWidth = canvasFrame.getCanvas().getWidth();
        return (int) Math.round(x * (captureWidth / (double) canvasWidth));
    }

    private void fireOnKeyPressed(int keyCode, char keyChar) {
        listeners.onKeyPressed(keyCode, keyChar);
    }

    private void fireOnKeyReleased(int keyCode, char keyChar) {
        listeners.onKeyReleased(keyCode, keyChar);
    }

}
