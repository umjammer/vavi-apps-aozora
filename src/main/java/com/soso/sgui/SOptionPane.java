/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.beans.PropertyVetoException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.Icon;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;


public class SOptionPane extends JOptionPane {

    public static String[] safetyMessage(String message) {
        ArrayList<String> messages = new ArrayList<>();
        String[] lines = message.split("[\r\n]");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = (int) (screenSize.width * 0.9F);
        int h = (int) (screenSize.height * 0.6F);
        int y = 0;
        JLabel label = new JLabel();
        for (String line : lines) {
            if (y > h)
                break;
            label.setText(line);
            Dimension size = label.getPreferredSize();
            if (size.width < w) {
                messages.add(line);
                y += size.height;
            } else {
                int s = 0;
                for (int e = 1; e <= line.length() && y <= h; e++) {
                    String text = line.substring(s, e);
                    label.setText(text);
                    Dimension labelSize = label.getPreferredSize();
                    if (e == line.length() || labelSize.width > w) {
                        messages.add(text);
                        y += labelSize.height;
                        e = s = e;
                    }
                }
            }
        }

        if (y > h)
            messages.add("... more");
        return messages.toArray(new String[0]);
    }

    public static void showMessageDialog(Component parent, Object message) throws HeadlessException {
        JOptionPane.showConfirmDialog(parent, (message instanceof String) ? safetyMessage((String) message) : message);
    }

    public static void showMessageDialog(Component parent, Object message, String title, int messageType) throws HeadlessException {
        JOptionPane.showMessageDialog(parent, (message instanceof String) ? safetyMessage((String) message) : message, title, messageType);
    }

    public static void showMessageDialog(Component parent, Object message, String title, int messageType, Icon icon) throws HeadlessException {
        JOptionPane.showMessageDialog(parent, (message instanceof String) ? safetyMessage((String) message) : message, title, messageType, icon);
    }

    public static int showConfirmDialog(Component parent, Object message) throws HeadlessException {
        return JOptionPane.showConfirmDialog(parent, message);
    }

    public static int showConfirmDialog(Component parent, Object message, String title, int optionType) throws HeadlessException {
        return JOptionPane.showConfirmDialog(parent, (message instanceof String) ? (Object) safetyMessage((String) message) : message, title, optionType);
    }

    public static int showConfirmDialog(Component parent, Object message, String title, int optionType, int messageType) throws HeadlessException {
        return JOptionPane.showConfirmDialog(parent, (message instanceof String) ? (Object) safetyMessage((String) message) : message, title, optionType, messageType);
    }

    public static int showConfirmDialog(Component parent, Object message, String title, int optionType, int messageType, Icon icon) throws HeadlessException {
        return JOptionPane.showConfirmDialog(parent, (message instanceof String) ? (Object) safetyMessage((String) message) : message, title, optionType, messageType, icon);
    }

    public static int showOptionDialog(Component parent, Object message, String title, int optionType, int messageType, Icon icon, Object[] options, Object initialValue) throws HeadlessException {
        return JOptionPane.showOptionDialog(parent, (message instanceof String) ? (Object) safetyMessage((String) message) : message, title, optionType, messageType, icon, options, initialValue);
    }

    public static int showCustomeDialog(Component parent, Object message, String title, int optionType, int messageType, Icon icon, Object[] options, Object initialValue, String ok, String yes, String no, String cancel) {
        Object okButtonText = UIManager.get("OptionPane.okButtonText");
        Object yesButtonText = UIManager.get("OptionPane.yesButtonText");
        Object noButtonText = UIManager.get("OptionPane.noButtonText");
        Object cancelButtonText = UIManager.get("OptionPane.cancelButtonText");
        if (ok != null)
            UIManager.put("OptionPane.okButtonText", ok);
        if (yes != null)
            UIManager.put("OptionPane.yesButtonText", yes);
        if (no != null)
            UIManager.put("OptionPane.noButtonText", no);
        if (cancel != null)
            UIManager.put("OptionPane.cancelButtonText", cancel);
        try {
            int r = JOptionPane.showOptionDialog(parent, message, title, optionType, messageType, icon, options, initialValue);
            return r;
        } finally {
            UIManager.put("OptionPane.okButtonText", okButtonText);
            UIManager.put("OptionPane.yesButtonText", yesButtonText);
            UIManager.put("OptionPane.noButtonText", noButtonText);
            UIManager.put("OptionPane.cancelButtonText", cancelButtonText);
        }
    }

    public static int showInternalCustomeDialog(Component parent, Object message, String title, int optionType, int messageType, Icon icon, Object[] options, Object initialValue, String ok, String yes, String no, String cancel) {
        Object okButtonText = UIManager.get("OptionPane.okButtonText");
        Object yesButtonText = UIManager.get("OptionPane.yesButtonText");
        Object noButtonText = UIManager.get("OptionPane.noButtonText");
        Object cancelButtonText = UIManager.get("OptionPane.cancelButtonText");
        if (ok != null)
            UIManager.put("OptionPane.okButtonText", ok);
        if (yes != null)
            UIManager.put("OptionPane.yesButtonText", yes);
        if (no != null)
            UIManager.put("OptionPane.noButtonText", no);
        if (cancel != null)
            UIManager.put("OptionPane.cancelButtonText", cancel);
        try {
            int r = JOptionPane.showInternalOptionDialog(parent, message, title, optionType, messageType, icon, options, initialValue);
            return r;
        } finally {
            UIManager.put("OptionPane.okButtonText", okButtonText);
            UIManager.put("OptionPane.yesButtonText", yesButtonText);
            UIManager.put("OptionPane.noButtonText", noButtonText);
            UIManager.put("OptionPane.cancelButtonText", cancelButtonText);
        }
    }

    public static void showSInternalMessageDialog(Component parent, Object message) {
        showSInternalMessageDialog(parent, message, UIManager.getString("OptionPane.messageDialogTitle", parent != null ? parent.getLocale() : Locale.getDefault()), INFORMATION_MESSAGE);
    }

    public static void showSInternalMessageDialog(Component parent, Object message, String title, int messageType) {
        showSInternalMessageDialog(parent, message, title, messageType, null);
    }

    public static void showSInternalMessageDialog(Component parent, Object message, String title, int messageType, Icon icon) {
        showSInternalOptionDialog(parent, message, title, DEFAULT_OPTION, messageType, icon, null, null);
    }

    public static int showSInternalConfirmDialog(Component parent, Object message) {
        return showSInternalConfirmDialog(parent, message, UIManager.getString("OptionPane.titleText"), YES_NO_CANCEL_OPTION);
    }

    public static int showSInternalConfirmDialog(Component parent, Object message, String title, int optionType) {
        return showSInternalConfirmDialog(parent, message, title, optionType, QUESTION_MESSAGE);
    }

    public static int showSInternalConfirmDialog(Component parent, Object message, String title, int optionType, int messageType) {
        return showSInternalConfirmDialog(parent, message, title, optionType, messageType, null);
    }

    public static int showSInternalConfirmDialog(Component parent, Object message, String title, int optionType, int messageType, Icon icon) {
        return showSInternalOptionDialog(parent, message, title, optionType, messageType, icon, null, null);
    }

    @SuppressWarnings("deprecation")
    public static int showSInternalOptionDialog(Component parent, Object message, String title, int optionType, int messageType, Icon icon, Object[] options, Object initialValue) {
        SOptionPane optionPane = new SOptionPane(message, messageType, optionType, icon, options, initialValue);
        optionPane.putClientProperty(new StringBuffer("__force_heavy_weight_popup__"), Boolean.TRUE);
        Component comp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        optionPane.setInitialValue(initialValue);
        SInternalFrame iframe = optionPane.createSInternalFrame(parent, title);
        optionPane.selectInitialValue();
        iframe.setVisible(true);
        if (iframe.isVisible() && !iframe.isShowing()) {
            Container container = iframe.getParent();
            while (container != null) {
                if (!container.isVisible())
                    container.setVisible(true);
                container = container.getParent();
            }
        }
        iframe.setModal(true);
        if (parent instanceof JInternalFrame)
            try {
                ((JInternalFrame) parent).setSelected(true);
            } catch (PropertyVetoException ignored) {
            }
        Object value = optionPane.getValue();
        if (comp != null && comp.isShowing())
            comp.requestFocus();
        if (value == null)
            return -1;
        if (options == null)
            if (value instanceof Integer)
                return (Integer) value;
            else
                return -1;
        int l = options.length;
        for (int i = 0; i < l; i++)
            if (options[i].equals(value))
                return i;

        return -1;
    }

    public SInternalFrame createSInternalFrame(Component parent, String title) {
        Object comp;
        if (parent == null)
            throw new IllegalArgumentException("SOptionPane: parentComponent cannto be null");
        if ((comp = JOptionPane.getDesktopPaneForComponent(parent)) == null &&
            (comp = SGUIUtil.getParentInstanceOf(parent, javax.swing.JLayeredPane.class)) == null)
            comp = parent.getParent();
        final SInternalFrame iframe = new SInternalFrame();
        iframe.setTitle(title);
        iframe.setResizable(false);
        iframe.setClosable(true);
        iframe.setMaximizable(false);
        iframe.setIconifiable(false);
        iframe.putClientProperty("JInternalFrame.frameType", "optionDialog");
        iframe.putClientProperty("JInternalFrame.messageType", getMessageType());
        iframe.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosing(InternalFrameEvent event) {
                if (getValue() == JOptionPane.UNINITIALIZED_VALUE)
                    setValue(null);
            }
        });
        addPropertyChangeListener(event -> {
            if (iframe.isVisible() &&
                event.getSource() == SOptionPane.this &&
                event.getPropertyName().equals("value")) {
                iframe.setModal(false);
                iframe.setVisible(false);
            }
        });
        iframe.getContentPane().add(this, BorderLayout.CENTER);
        if (comp instanceof JLayeredPane) {
            ((JLayeredPane) comp).add(iframe, JLayeredPane.MODAL_LAYER);
        } else {
            if (comp instanceof JRootPane) {
                ((JRootPane) comp).getLayeredPane().add(iframe, JLayeredPane.MODAL_LAYER);
            } else {
                ((JRootPane) comp).add(iframe, BorderLayout.CENTER);
            }
        }
        Dimension preferredSize = iframe.getPreferredSize();
        Dimension size = ((Container) comp).getSize();
        Dimension parentSize = parent.getSize();
        SGUIUtil.setCenter((Component) comp, iframe);
        Point point = SwingUtilities.convertPoint(parent, 0, 0, (Component) comp);
        int w = (parentSize.width - preferredSize.width) / 2 + point.x;
        int h = (parentSize.height - preferredSize.height) / 2 + point.y;
        int w1 = (w + preferredSize.width) - size.width;
        int h1 = (h + preferredSize.height) - size.height;
        w = Math.max(w1 <= 0 ? w : w - w1, 0);
        h = Math.max(h1 <= 0 ? h : h - h1, 0);
        iframe.setBounds(w, h, preferredSize.width, preferredSize.height);
        ((Container) comp).validate();
        try {
            iframe.setSelected(true);
        } catch (PropertyVetoException ignored) {
        }
        return iframe;
    }

    public static void showInternalErrorDialog(Component component, String title, Throwable throwable, boolean flag) {
        StringWriter writer;
        Throwable throwable1 = null;
        writer = new StringWriter();
        if (title != null) {
            writer.write(title);
            writer.write(System.getProperty("line.separator"));
        }
        if (throwable != null) {
            if (flag) {
                throwable.printStackTrace(new PrintWriter(writer));
            } else {
                throwable1 = throwable;
                Throwable throwable2 = throwable;
                while (throwable2 != null) {
                    throwable1 = throwable2;
                    throwable2 = throwable2.getCause();
                }
                writer.write(throwable1.toString());
            }
        }
        showInternalConfirmDialog(component, writer.toString(), "エラー", DEFAULT_OPTION, ERROR_MESSAGE, null);
    }

    public SOptionPane() {
        super("JOptionPane message");
    }

    public SOptionPane(Object message) {
        super(message, PLAIN_MESSAGE);
    }

    public SOptionPane(Object message, int messageType) {
        super(message, messageType, DEFAULT_OPTION);
    }

    public SOptionPane(Object message, int messageType, int optionType) {
        super(message, messageType, optionType, null);
    }

    public SOptionPane(Object message, int messageType, int optionType, Icon icon) {
        super(message, messageType, optionType, icon, null);
    }

    public SOptionPane(Object message, int messageType, int optionType, Icon icon, Object[] options) {
        super(message, messageType, optionType, icon, options, null);
    }

    public SOptionPane(Object message, int messageType, int optionType, Icon icon, Object[] options, Object initialValue) {
        super(message, messageType, optionType, icon, options, initialValue);
    }

    private static final long serialVersionUID = 0x848a2cf7L;

    public static final String key_OptionPane_okButtonText = "OptionPane.okButtonText";
    public static final String key_OptionPane_yesButtonText = "OptionPane.yesButtonText";
    public static final String key_OptionPane_noButtonText = "OptionPane.noButtonText";
    public static final String key_OptionPane_cancelButtonText = "OptionPane.cancelButtonText";
}
