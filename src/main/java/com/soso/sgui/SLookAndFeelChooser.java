/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;


public class SLookAndFeelChooser extends JPanel implements ItemListener {

    private static class LookAndFeelCache {

        final LookAndFeel getLookAndFeel() {
            return lookAndFeel;
        }

        final void setLookAndFeel(LookAndFeel lookAndFeel) {
            this.lookAndFeel = lookAndFeel;
        }

        public final String toString() {
            return getLookAndFeel().getName();
        }

        private LookAndFeel lookAndFeel;

        LookAndFeelCache(LookAndFeel lookAndFeel) {
            setLookAndFeel(lookAndFeel);
        }
    }

    public static class SLookAndFeelChooserEvent extends EventObject {

        public LookAndFeel getLookAndFeel() {
            return lookAndFeel;
        }

        private static final long serialVersionUID = 0x7be3024fL;

        private final LookAndFeel lookAndFeel;

        SLookAndFeelChooserEvent(Object source, LookAndFeel lookAndFeel) {
            super(source);
            this.lookAndFeel = lookAndFeel;
        }
    }

    public interface SLookAndFeelChooserListener extends EventListener {

        void lookAndFeelSelected(SLookAndFeelChooserEvent event);
    }

    public static void showInternalDialog(Container parent, String title, LookAndFeel[] lookAndFeels) throws UnsupportedLookAndFeelException {
        showInternalDialog(parent, title, lookAndFeels, null);
    }

    public static void showInternalDialog(Container parent, String title, String[] names, Component comp) throws UnsupportedLookAndFeelException {
        showInternalDialog(parent, title, SGUIUtil.selectSupportedLookAndFeel(names), comp);
    }

    public static void showInternalDialog(Container parent, String title, LookAndFeel[] lookAndFeels, Component comp) throws UnsupportedLookAndFeelException {
        if (comp == null)
            comp = SGUIUtil.getParentRecursive(parent);
        LookAndFeelCache cache = new LookAndFeelCache(UIManager.getLookAndFeel());
        try {
            SLookAndFeelChooser chooser = new SLookAndFeelChooser(lookAndFeels, comp);
            int result = JOptionPane.showInternalConfirmDialog(parent, chooser, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
            if (result == 0)
                cache.setLookAndFeel(chooser.getSelectedLookAndFeel());
        } finally {
            SGUIUtil.setLookAndFeel(cache.getLookAndFeel(), comp);
        }
    }

    @Deprecated
    public static void showSInternalDialog(Container owner, String title, LookAndFeel[] lookAndFeels, Component comp) throws UnsupportedLookAndFeelException {
        if (comp == null)
            comp = SGUIUtil.getParentRecursive(owner);
        final LookAndFeelCache cache = new LookAndFeelCache(UIManager.getLookAndFeel());
        try {
            final SInternalFrame iframe = new SInternalFrame();
            iframe.setTitle(title);
            iframe.setResizable(false);
            iframe.setMaximizable(false);
            iframe.setClosable(true);
            final SLookAndFeelChooser chooser = new SLookAndFeelChooser(lookAndFeels, iframe);
            chooser.addAction(new AbstractAction("　適用　") {
                public void actionPerformed(ActionEvent event) {
                    iframe.setModal(false);
                    iframe.dispose();
                    cache.setLookAndFeel(chooser.getSelectedLookAndFeel());
                }
            });
            chooser.addAction(new AbstractAction("取り消し") {
                public void actionPerformed(ActionEvent event) {
                    iframe.setModal(false);
                    iframe.dispose();
                }
            });
            iframe.setContentPane(chooser);
            iframe.pack();
            SGUIUtil.setCenter(owner, iframe);
            JLayeredPane pane = SGUIUtil.getParentInstanceOf(owner, JLayeredPane.class);
            if (pane != null) {
                Point p0 = owner.getLocation();
                Point p1 = iframe.getLocation();
                iframe.setLocation(p0.x + p1.x, p0.y + p1.y);
                pane.add(iframe, JLayeredPane.MODAL_LAYER);
            } else {
                owner.add(iframe, BorderLayout.CENTER);
            }
            iframe.addInternalFrameListener(new InternalFrameAdapter() {
                public void internalFrameClosing(InternalFrameEvent event) {
                    iframe.setModal(false);
                    iframe.dispose();
                }
            });
            iframe.setVisible(true);
            iframe.setForceHeavyWeightPopup(true);
            iframe.setModal(true);
        } finally {
            SGUIUtil.setLookAndFeel(cache.getLookAndFeel(), comp);
        }
    }

    public static void showDialog(Frame owner, String title, String[] names, Component comp) throws UnsupportedLookAndFeelException {
        showDialog(owner, title, SGUIUtil.selectSupportedLookAndFeel(names), comp);
    }

    public static void showDialog(Frame owner, String title, LookAndFeel[] lookAndFeels, Component comp) throws UnsupportedLookAndFeelException {
        JDialog dialog = new JDialog(owner, title, true);
        dialog.setLocationRelativeTo(owner);
        a(dialog, lookAndFeels, comp);
    }

    public static void showDialog(Dialog owner, String title, String[] names, Component comp) throws UnsupportedLookAndFeelException {
        showDialog(owner, title, SGUIUtil.selectSupportedLookAndFeel(names), comp);
    }

    public static void showDialog(Dialog owner, String title, LookAndFeel[] lookAndFeels, Component comp) throws UnsupportedLookAndFeelException {
        JDialog dialog = new JDialog(owner, title, true);
        dialog.setLocationRelativeTo(owner);
        a(dialog, lookAndFeels, comp);
    }

    private static void a(final JDialog dialog, LookAndFeel[] lookAndFeels, Component owner) throws UnsupportedLookAndFeelException {
        if (owner == null)
            owner = SGUIUtil.getParentRecursive(dialog);
        final LookAndFeelCache cache = new LookAndFeelCache(UIManager.getLookAndFeel());
        try {
            dialog.setResizable(false);
            dialog.setModal(true);
            dialog.getContentPane().removeAll();
            final SLookAndFeelChooser chooser = new SLookAndFeelChooser(lookAndFeels, dialog);
            chooser.addAction(new AbstractAction("　適用　") {
                public void actionPerformed(ActionEvent event) {
                    dialog.dispose();
                    cache.setLookAndFeel(chooser.getSelectedLookAndFeel());
                }
            });
            chooser.addAction(new AbstractAction("取り消し") {
                public void actionPerformed(ActionEvent event) {
                    dialog.dispose();
                }
            });
            dialog.getContentPane().add(chooser);
            dialog.pack();
            dialog.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent event) {
                    dialog.dispose();
                }
            });
            dialog.setVisible(true);
        } finally {
            SGUIUtil.setLookAndFeel(cache.getLookAndFeel(), owner);
        }
    }

    public SLookAndFeelChooser() {
        initGUI();
        addLaf();
    }

    public SLookAndFeelChooser(LookAndFeel[] lookAndFeels) throws UnsupportedLookAndFeelException {
        this(lookAndFeels, null);
    }

    public SLookAndFeelChooser(LookAndFeel[] lookAndFeels, Component comp) throws UnsupportedLookAndFeelException {
        setUpdateUIRootComponnt(comp);
        initGUI();
        addLaf();
        if (lookAndFeels != null) {
            for (LookAndFeel lookAndFeel : lookAndFeels) {
                addLookAndFeel(lookAndFeel);
            }
        }
    }

    private void initGUI() {
        listeners = new ArrayList<>();
        setLayout(new BorderLayout());
        desktopPane = new JDesktopPane();
        SGUIUtil.setSizeALL(desktopPane, new Dimension(300, 200));
        add(desktopPane, BorderLayout.CENTER);
        JButton button = new JButton();
        button.setText("Button");
        button.setBounds(5, 5, 100, 32);
        button.addActionListener(event -> showInternalFrame());
        desktopPane.add(button);
        showInternalFrame();
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        add(panel, BorderLayout.SOUTH);
        comboBox = new JComboBox();
        comboBox.addItemListener(this);
        panel.add(comboBox);
        this.panel = new JPanel();
        this.panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
        panel.add(this.panel);
    }

    final void addAction(Action action) {
        SButton button = new SButton();
        button.setAction(action);
        panel.add(button);
        repaint();
    }

    final void showInternalFrame() {
        JInternalFrame iframe = new JInternalFrame();
        iframe.setClosable(true);
        iframe.setMaximizable(true);
        iframe.setIconifiable(true);
        iframe.setResizable(true);
        iframe.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        iframe.setTitle("InternalFrame");
        iframe.setBounds((int) (20D + Math.random() * 20D), (int) (40D + Math.random() * 40D), 200, 150);
        desktopPane.add(iframe);
        JScrollPane scrollPane = new JScrollPane();
        iframe.getContentPane().setLayout(new BorderLayout());
        iframe.getContentPane().add(scrollPane, BorderLayout.CENTER);
        JTextPane textPane = new JTextPane();
        textPane.setText("TextPane\n\nYou can enter text here \n\n.\tOne\n..\tTwo\n...\tThree\n....\tFour\n.....\tFive");
        scrollPane.setViewportView(textPane);
        iframe.setVisible(true);
    }

    private void addLaf() {
        try {
            LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
            addLookAndFeel(lookAndFeel);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void itemStateChanged(ItemEvent event) {
        Object item = comboBox.getSelectedItem();
        if (item != null && item instanceof LookAndFeelCache) {
            LookAndFeel lookAndFeel = ((LookAndFeelCache) item).getLookAndFeel();
            fireLookAndFeelSelected(new SLookAndFeelChooserEvent(event.getSource(), lookAndFeel));
        }
    }

    public void setUpdateUIRootComponnt(Component comp) {
        this.rootComponent = comp;
    }

    public void addLookAndFeel(LookAndFeel lookAndFeel) throws UnsupportedLookAndFeelException {
        if (lookAndFeel == null)
            throw new IllegalArgumentException("LookAndFeel null");
        if (!lookAndFeel.isSupportedLookAndFeel()) {
            throw new UnsupportedLookAndFeelException(lookAndFeel.getName());
        }
        comboBox.addItem(new LookAndFeelCache(lookAndFeel));
    }

    public void addSLookAndFeelChooserListener(SLookAndFeelChooserListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("SLookAndFeelChooserListener null");
        }
        listeners.add(listener);
    }

    public SLookAndFeelChooserListener[] getSLookAndFeelChooserListeners() {
        return listeners.toArray(new SLookAndFeelChooserListener[0]);
    }

    public void removeSLookAndFeelChooserListener(SLookAndFeelChooserListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("SLookAndFeelChooserListener null");
        }
        listeners.remove(listener);
    }

    private void fireLookAndFeelSelected(SLookAndFeelChooserEvent event) {
        LookAndFeel lookAndFeel = event.getLookAndFeel();
        if (this.lookAndFeel != null && this.lookAndFeel.getClass() == lookAndFeel.getClass())
            return;
        this.lookAndFeel = lookAndFeel;
        Object comp = rootComponent;
        if (comp == null)
            comp = SGUIUtil.getParentRecursive(this);
        if (comp != null)
            try {
                SGUIUtil.setLookAndFeel(event.getLookAndFeel(), (Component) comp);
            } catch (Exception _ex) {
                SGUIUtil.setLookAndFeel(event.getLookAndFeel(), (Component) comp);
            }
        for (SLookAndFeelChooserListener listener : listeners) {
            listener.lookAndFeelSelected(event);
        }
    }

    public LookAndFeel getSelectedLookAndFeel() {
        Object item = comboBox.getSelectedItem();
        if (item == null)
            return null;
        else
            return ((LookAndFeelCache) item).getLookAndFeel();
    }

    private static final long serialVersionUID = 0xe809a59fL;
    private Component rootComponent;
    private List<SLookAndFeelChooserListener> listeners;
    private JComboBox comboBox;
    private LookAndFeel lookAndFeel;
    private JDesktopPane desktopPane;
    private JPanel panel;
}
