/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.filechooser.FileFilter;


public class SFileChooser extends JFileChooser {

    static class ChoosableFileFilter extends FileFilter {
        public boolean accept(File file) {
            if (file.isDirectory())
                return true;
            Object name = file.getName();
            int p = ((String) name).lastIndexOf('.');
            if (p == -1)
                return false;
            String extension = ((String) (name)).substring(p + 1).toLowerCase();
            for (int i = 0; i < extensions.length; i++) {
                try {
                    if (extensions[i].equals(extension))
                        return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        public String getDescription() {
            return description;
        }

        private static final String[] extensions = {
            "bmp", "gif", "jpg", "jpeg", "png"
        };

        private static final String description = "画像 " + Arrays.toString(extensions);
    }

    static class ImageFileChooser extends JPanel implements PropertyChangeListener {

        ImageFileChooser() {
            hints = Image.SCALE_SMOOTH;
            applySizeAll();
            image = null;
        }

        public void propertyChange(PropertyChangeEvent event) {
            String name = event.getPropertyName();
            if (name == "SelectedFileChangedProperty" && isShowing()) {
                loadImage((File) event.getNewValue());
                repaint();
            }
        }

        public void paint(Graphics g) {
            super.paint(g);
            Color originalColor = g.getColor();
            paintChecker(g);
            if (image != null) {
                g.drawImage(image, 0, 0, this);
            } else {
                g.setColor(noImageColor);
                g.drawLine(0, 0, getWidth() - 1, getHeight() - 1);
                g.drawLine(0, getHeight() - 1, getWidth() - 1, 0);
                g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }
            g.setColor(originalColor);
        }

        private void paintChecker(Graphics g) {
            g.setColor(oddColor);
            g.fillRect(0, 0, getWidth(), getHeight());
            int w = getWidth() / borderSize.width + 1;
            int h = getHeight() / borderSize.height + 1;
            
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    if (x % 2 + y % 2 == 1) {
                        g.setColor(oddColor);
                    } else {
                        g.setColor(evenColor);
                    }
                    g.fillRect(x * borderSize.width, y * borderSize.height, borderSize.width, borderSize.height);
                }
            }
        }

        private void applySizeAll() {
            SGUIUtil.setSizeALL(this, size);
        }

        private void loadImage(File file) {
            if (file == null || file.isDirectory()) {
                image = null;
            } else {
                try {
                    Image image = getToolkit().getImage(file.getAbsolutePath());
                    Image scaledImage = image.getScaledInstance(getWidth(), getHeight(), hints);
                    this.image = scaledImage;
                } catch (Exception _ex) {
                    image = null;
                }
            }
        }

        void setHints(int hints) {
            this.hints = hints;
        }

        private static final long serialVersionUID = 0x318ae81aL;
        private static final Dimension size = new Dimension(200, 200);
        private static final Color noImageColor = Color.BLACK;
        private static final Dimension borderSize = new Dimension(10, 10);
        private static final Color oddColor = Color.WHITE;
        private static final Color evenColor = new Color(240, 240, 240);
        private Image image;
        private int hints;
    }

    public int showInternalOpenDialog(Container parentComponent) {
        setDialogType(OPEN_DIALOG);
        return showInternalDialog(parentComponent, null);
    }

    public int showInternalSaveDialog(Container parentComponent) {
        setDialogType(SAVE_DIALOG);
        return showInternalDialog(parentComponent, null);
    }

    public int showInternalDialog(Container parent, String text) {
        if (text != null) {
            setApproveButtonText(text);
            setDialogType(CUSTOM_DIALOG);
        }
        String name = "OptionPane.okButtonText";
        Object value = UIManager.get(name);
        try {
            String t2 = getUI().getApproveButtonText(this);
            String locale = UIManager.getString("FileChooser.cancelButtonText", getLocale());
            JButton okButton = null;
            JButton cancelButton = null;
            JButton[] children = SGUIUtil.getChildInstanceOf(this, JButton.class);
            for (JButton child : children) {
                String t = child.getText();
                if (t2.equals(t)) {
                    okButton = child;
                    child.setVisible(false);
                }
                if (locale.equals(t)) {
                    cancelButton = child;
                    child.setVisible(false);
                }
            }

            UIManager.put(name, t2);
            int r = JOptionPane.showInternalConfirmDialog(parent, this, getUI().getDialogTitle(this), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
            if (r == JOptionPane.OK_OPTION) {
                if (okButton != null)
                    okButton.doClick();
                approveSelection();
            } else {
                if (cancelButton != null)
                    cancelButton.doClick();
                cancelSelection();
            }
            if (okButton != null)
                okButton.setVisible(true);
            if (cancelButton != null)
                cancelButton.setVisible(true);
            return result;
        } finally {
            UIManager.put(name, value);
        }
    }

    @Deprecated
    public int showSInternalDialog(Container parent, String text) {
        SGUIUtil.forceHeavyWeightPopupKey(this, true);
        if (text != null) {
            setApproveButtonText(text);
            setDialogType(CUSTOM_DIALOG);
        }
        SInternalFrame iframe = new SInternalFrame();
        iframe.setTitle(getUI().getDialogTitle(this));
        iframe.getContentPane().add(this);
        iframe.pack();
        SGUIUtil.setCenter(parent, iframe);
        JLayeredPane pane = SGUIUtil.getParentInstanceOf(parent, JLayeredPane.class);
        if (pane != null) {
            Point pp = parent.getLocation();
            Point fp = iframe.getLocation();
            iframe.setLocation(pp.x + fp.x, pp.y + fp.y);
            pane.add(iframe, JLayeredPane.MODAL_LAYER);
        } else {
            parent.add(iframe, BorderLayout.CENTER);
        }
        iframe.addInternalFrameListener(new InternalFrameAdapter() {
            public final void internalFrameClosing(InternalFrameEvent event) {
                cancelSelection();
            }
        });
        rescanCurrentDirectory();
        this.iframe = iframe;
        iframe.setVisible(true);
        while (!waiting)
            iframe.setModal(true);
        doWait();
        return result;
    }

    public SFileChooser() {
        result = JOptionPane.DEFAULT_OPTION;
    }

    public void approveSelection() {
        super.approveSelection();
        result = APPROVE_OPTION;
        doNotifyAll();
        dispose();
    }

    public void cancelSelection() {
        super.cancelSelection();
        result = CANCEL_OPTION;
        doNotifyAll();
        dispose();
    }

    @SuppressWarnings("deprecation")
    private void dispose() {
        if (iframe != null) {
            iframe.setModal(false);
            iframe.dispose();
        }
    }

    final void doWait() {
        synchronized (this) {
            while (!waiting) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    final void doNotifyAll() {
        synchronized (this) {
            waiting = true;
            notifyAll();
        }
    }

    public void setupMemoryLastDirectoryChooser() {
        setCurrentDirectory(currentDirectory);
        addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                String name = event.getPropertyName();
                if (name == "directoryChanged")
                    currentDirectory = getCurrentDirectory();
            }
        });
    }

    public void setupImageFileChooser() {
        setMultiSelectionEnabled(false);
        initGUI();
        attachChoosableFileFilter();
    }

    public void setupImageFileChooser(int hints) {
        setupImageFileChooser();
        if (hints != -1)
            imageFileChooser.setHints(hints);
    }

    private void initGUI() {
        imageFileChooser = new ImageFileChooser();
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panel.add(imageFileChooser);
        setAccessory(panel);
        addPropertyChangeListener(imageFileChooser);
    }

    private void attachChoosableFileFilter() {
        addChoosableFileFilter(new ChoosableFileFilter());
    }

    static volatile File currentDirectory;
    private static final long serialVersionUID = 0xced4dcbL;
    private ImageFileChooser imageFileChooser;
    private int result;
    private SInternalFrame iframe;
    private boolean waiting;
}
