package io.github.springstudent.ada.client.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.security.NoSuchAlgorithmException;

import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;

public abstract class DialogFactory {
    public interface Validator {
        /**
         * @return {@code null} if the validation is fine; otherwise an error
         * message.
         */
        String validate() throws NoSuchAlgorithmException;
    }

    /**
     * Creates and show a modal dialog with ok/cancel buttons.
     *
     * @return {@code true} for {@code OK}, {@code false} for {@code CANCEL}.
     */
    public static boolean showOkCancel(final Component owner, final String title, final JComponent payloadPane, final boolean bordered, final Validator validator) {
        final JButton ok = new JButton("确定");
        final JButton cancel = new JButton("取消");

        final JButton[] buttons = new JButton[]{ok, cancel};

        final JDialog dialog = createDialog(owner, title, payloadPane, bordered, buttons, ok, true);

        final boolean[] result = new boolean[1];

        ok.addActionListener(ev -> {
            final String validationMessage;
            try {
                validationMessage = validator == null ? null : validator.validate();
            } catch (NoSuchAlgorithmException e) {
                throw new UnsupportedOperationException(e);
            }
            if (validationMessage == null) {
                result[0] = true;
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, validationMessage, title + " : Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancel.addActionListener(ev -> {
            result[0] = false;
            dialog.dispose();
        });

        dialog.setVisible(true);

        return result[0];
    }

    /**
     * Creates a modal dialog.
     */
    private static JDialog createDialog(Component owner, String title, JComponent payloadPane, boolean bordered, JButton[] buttons, JButton defaultButton,
                                        boolean hasEscapeButton) {
        final Frame parent = owner instanceof Frame ? (Frame) owner : (Frame) SwingUtilities.getAncestorOfClass(Frame.class, owner);

        payloadPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 15, 10));

        final JPanel payloadPaneEx = new JPanel(new BorderLayout());
        payloadPaneEx.add(payloadPane, BorderLayout.CENTER);

        final JPanel payloadPaneExEx = new JPanel(new BorderLayout());
        if (bordered) {
            payloadPaneExEx.add(payloadPaneEx, BorderLayout.CENTER);
            payloadPaneExEx.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        }

        final JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.add(Box.createHorizontalGlue());

        for (JButton button : buttons) {
            buttonPane.add(Box.createRigidArea(new Dimension(10, 10)));
            buttonPane.add(button);
        }

        buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));

        // --

        final JPanel contentPane = new JPanel(new BorderLayout());
        if (bordered) {
            contentPane.add(payloadPaneExEx, BorderLayout.CENTER);
        } else {
            contentPane.add(payloadPaneEx, BorderLayout.CENTER);
        }
        contentPane.add(buttonPane, BorderLayout.SOUTH);

        // --

        final JDialog dialog = new JDialog(parent, title, true);

        // dialog.setIconImage(ImageFactory.getOrCreateIcon("ladybug.png").getImage());
        dialog.setContentPane(contentPane);
        dialog.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // --

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });

        // Make this dialog CANCELing on ESCAPE key
        if (hasEscapeButton) {
            final KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

            final InputMap inputMap = dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            inputMap.put(escape, "ESCAPE");

            final ActionMap actionMap = dialog.getRootPane().getActionMap();
            actionMap.put("ESCAPE", new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            });
        }

        // --

        SwingUtilities.getRootPane(defaultButton).setDefaultButton(defaultButton);

        // --

        dialog.pack();
        dialog.setLocationRelativeTo(parent);

        return dialog;
    }


}
