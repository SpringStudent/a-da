package io.github.springstudent.ada.client.core;

import io.github.springstudent.ada.client.RemoteClient;
import io.github.springstudent.ada.client.bean.StatusBar;
import io.github.springstudent.ada.client.monitor.Counter;
import io.github.springstudent.ada.common.utils.EmptyUtils;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.im.InputContext;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.springstudent.ada.common.utils.ImageUtilities.getOrCreateIcon;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_WINDOWS;
import static java.lang.String.format;

/**
 * @author ZhouNing
 * @date 2024/12/9 8:42
 */
public class RemoteScreen extends JFrame {

    private transient RemoteScreenListener listeners;

    private ArrayList<Counter<?>> counters = new ArrayList<>();

    private int captureWidth;

    private int captureHeight;

    private JFXPanel jfxPanel;

    private ImageView imageView;

    private StatusBar statusBar;

    private Timer sessionTimer;

    private JButton reqClipboardButton;

    private JButton sendClipboardButton;

    private JToggleButton windowsKeyToggleButton;

    private JToggleButton ctrlKeyToggleButton;

    private JMenu optionsMenu;

    private int screenNum = 1;

    private char os;

    private final AtomicBoolean controlActivated = new AtomicBoolean(false);

    private final AtomicBoolean isImmutableWindowsSize = new AtomicBoolean(false);

    private final AtomicBoolean windowsKeyActivated = new AtomicBoolean(false);

    private final AtomicBoolean ctrlKeyActivated = new AtomicBoolean(false);

    public RemoteScreen() {
        super("远程桌面");
        listeners = RemoteClient.getRemoteClient().getController();
        counters.addAll(RemoteClient.getRemoteClient().getController().getCounters());
        initFrame();
        initCanvasPanel();
        Platform.setImplicitExit(false);
        //allows for seeing the TAB with a regular KEY listener ...
        setFocusTraversalKeysEnabled(false);
        initMenuBar();
        initListeners();
        initStatusBar();
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
        jfxPanel = new JFXPanel();
        add(jfxPanel, BorderLayout.CENTER);
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        imageView.setCacheHint(CacheHint.SPEED);
        imageView.setFocusTraversable(true);
        imageView.setOnMouseEntered(e -> imageView.requestFocus());
        imageView.setPreserveRatio(false);
        StackPane root = new StackPane(imageView);
        Scene scene = new Scene(root);
        jfxPanel.setScene(scene);
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        this.optionsMenu = new JMenu("选项");
        JMenuItem sessionConfigItem = new JMenuItem(RemoteClient.getRemoteClient().getController().createCaptureConfigurationAction());
        this.optionsMenu.add(sessionConfigItem);
        menuBar.add(optionsMenu);
        //发送win键
        this.windowsKeyToggleButton = createToggleButton(createSendWindowsKeyAction());
        menuBar.add(windowsKeyToggleButton);
        menuBar.add(Box.createHorizontalStrut(5));
        this.ctrlKeyToggleButton = createToggleButton(createSendCtrlKeyAction());
        menuBar.add(ctrlKeyToggleButton);
        menuBar.add(Box.createHorizontalStrut(5));
        //粘贴板按钮
        if (EmptyUtils.isNotEmpty(RemoteClient.getRemoteClient().getClipboardServer())) {
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
            this.reqClipboardButton = createButton(RemoteClient.getRemoteClient().getController().createRequireRemoteClipboardAction());
            this.sendClipboardButton = createButton(RemoteClient.getRemoteClient().getController().createSendLoacalClibboardAction());
            buttonPanel.add(reqClipboardButton);
            buttonPanel.add(Box.createHorizontalStrut(5));
            buttonPanel.add(sendClipboardButton);
            menuBar.add(buttonPanel);
        }
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

    public final void transferClipboarButton(boolean enabled) {
        this.sendClipboardButton.setEnabled(enabled);
        this.reqClipboardButton.setEnabled(enabled);
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

    private void initStatusBar() {
        final StatusBar statusBar = new StatusBar();
        final Component horizontalStrut = Box.createHorizontalStrut(20);
        statusBar.add(horizontalStrut);
        for (Counter<?> counter : counters) {
            statusBar.addSeparator();
            statusBar.addCounter(counter, counter.getWidth());
        }
        statusBar.addSeparator();
        statusBar.addRamInfo();
        statusBar.addSeparator();
        statusBar.addConnectionDuration();
        statusBar.addSeparator();
        statusBar.add(horizontalStrut);
        statusBar.add(Box.createHorizontalStrut(10));
        add(statusBar, BorderLayout.SOUTH);
        this.statusBar = statusBar;
        updateInputLocale();
        new Timer(5000, e -> updateInputLocale()).start();
    }

    private void updateInputLocale() {
        String currentKeyboardLayout = InputContext.getInstance().getLocale().toString();
        if (!currentKeyboardLayout.equals(statusBar.getKeyboardLayout())) {
            statusBar.setKeyboardLayout(currentKeyboardLayout);
        }
    }

    private void addFocusListener() {
        jfxPanel.addFocusListener(new FocusAdapter() {
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
        jfxPanel.addMouseListener(new MouseAdapter() {
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

        jfxPanel.addMouseMotionListener(new MouseMotionListener() {
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

        jfxPanel.addMouseWheelListener(ev -> {
            if (controlActivated.get()) {
                fireOnMouseWheeled(ev.getX(), ev.getY(), ev.getWheelRotation());
            }
        });
    }

    private void addKeyListeners() {
        jfxPanel.addKeyListener(new KeyAdapter() {
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

    private WritableImage reusableFxImage = null;

    public void showImg(BufferedImage img) {
        this.captureWidth = img.getWidth();
        this.captureHeight = img.getHeight();
        // 复用WritableImage对象以减少GC压力
        if (reusableFxImage == null ||
                reusableFxImage.getWidth() != img.getWidth() ||
                reusableFxImage.getHeight() != img.getHeight()) {
            reusableFxImage = new WritableImage(img.getWidth(), img.getHeight());
        }
        WritableImage fxImg = SwingFXUtils.toFXImage(img, reusableFxImage);
        // 检查是否需要更新ImageView的尺寸设置
        if (imageView.getFitWidth() != jfxPanel.getWidth() ||
                imageView.getFitHeight() != jfxPanel.getHeight()) {
            imageView.setFitWidth(jfxPanel.getWidth());
            imageView.setFitHeight(jfxPanel.getHeight());
        }
        Platform.runLater(() -> imageView.setImage(fxImg));
    }

    public void launch(int screenNum, char remoteOs) {
        this.screenNum = screenNum;
        this.os = remoteOs;
        long sessionStartTime = Instant.now().getEpochSecond();
        sessionTimer = new Timer(1000, e -> {
            final long seconds = Instant.now().getEpochSecond() - sessionStartTime;
            statusBar.setSessionDuration(format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60));
        });
        sessionTimer.start();
        sendClipboardButton.setEnabled(true);
        reqClipboardButton.setEnabled(true);
        SwingUtilities.invokeLater(() -> this.setVisible(true));

    }

    public void close() {
        if (sessionTimer != null) {
            sessionTimer.stop();
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
        int canvasHeight = jfxPanel.getHeight();
        if (os == 'm' && RemoteClient.getRemoteClient().getOsId() != 'm') {
            return (int) Math.round(y * (captureHeight / (double) canvasHeight)) / 2;
        } else {
            return (int) Math.round(y * (captureHeight / (double) canvasHeight));
        }
    }

    private int scaleXPosition(int x) {
        int canvasWidth = jfxPanel.getWidth();
        if (os == 'm' && RemoteClient.getRemoteClient().getOsId() != 'm') {
            return (int) Math.round(x * (captureWidth / (double) canvasWidth)) / 2;
        } else {
            return (int) Math.round(x * (captureWidth / (double) canvasWidth));
        }
    }

    private void fireOnKeyPressed(int keyCode, char keyChar) {
        listeners.onKeyPressed(keyCode, keyChar);
    }

    private void fireOnKeyReleased(int keyCode, char keyChar) {
        listeners.onKeyReleased(keyCode, keyChar);
    }

}
