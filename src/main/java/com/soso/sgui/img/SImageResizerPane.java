/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.img;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import com.soso.sgui.SButton;
import com.soso.sgui.SGUIUtil;
import com.soso.sgui.SInternalFrame;


public class SImageResizerPane extends JPanel {

    static enum SizeOption {
        ORIGINAL,
        RATIO_EXTEND,
        FULL_EXTEND
    }

    public BufferedImage showImageResizerInternalDialog(Container container, String title) {
        a(container, title);
        return getResultImage();
    }

    public Image showImageResizerInternalDialog(Container container, String title, boolean flag) {
        a(container, title);
        return getResultImage(flag);
    }

    @SuppressWarnings("deprecation")
    private void a(Container container, String title) {
        final SInternalFrame iframe = new SInternalFrame();
        iframe.setTitle(title);
        iframe.setResizable(false);
        iframe.setMaximizable(false);
        iframe.setClosable(true);
        iframe.getContentPane().add(this);
        iframe.pack();
        SGUIUtil.setCenter(container, iframe);
        JLayeredPane pane = SGUIUtil.getParentInstanceOf(container, JLayeredPane.class);
        if (pane != null) {
            Point p0 = container.getLocation();
            Point p1 = iframe.getLocation();
            iframe.setLocation(p0.x + p1.x, p0.y + p1.y);
            pane.add(iframe, JLayeredPane.MODAL_LAYER);
        } else {
            container.add(iframe, BorderLayout.CENTER);
        }
        iframe.addInternalFrameListener(new InternalFrameAdapter() {
            public final void internalFrameClosing(InternalFrameEvent event) {
                doNotifyAll();
            }
        });
        setDisposer(new Runnable() {
            public final void run() {
                iframe.setModal(false);
                iframe.dispose();
            }
        });
        iframe.setVisible(true);
        while (!waiting) {
            iframe.setModal(true);
        }
        doWait();
    }

    public BufferedImage showImageResizerDialog(Frame owner, String title) {
        JDialog dialog = new JDialog(owner, title, true);
        dialog.setLocationRelativeTo(owner);
        return getResultImage_a(dialog);
    }

    public Image showImageResizerDialog(Frame owner, String title, boolean flag) {
        JDialog dialog = new JDialog(owner, title, true);
        dialog.setLocationRelativeTo(owner);
        return getResultImage_a(dialog, flag);
    }

    public BufferedImage showImageResizerDialog(Dialog owner, String title) {
        JDialog dialog = new JDialog(owner, title, true);
        dialog.setLocationRelativeTo(owner);
        return getResultImage_a(dialog);
    }

    public Image showImageResizerDialog(Dialog owner, String title, boolean flag) {
        JDialog dialog = new JDialog(owner, title, true);
        dialog.setLocationRelativeTo(owner);
        return getResultImage_a(dialog, flag);
    }

    private BufferedImage getResultImage_a(JDialog dialog) {
        showDialog_b(dialog);
        return getResultImage();
    }

    private void showDialog_b(final JDialog dialog) {
        dialog.setResizable(false);
        dialog.setModal(true);
        dialog.getContentPane().removeAll();
        dialog.getContentPane().add(this);
        dialog.pack();
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                doNotifyAll();
            }
        });
        setDisposer(new Runnable() {
            public void run() {
                dialog.dispose();
            }
        });
        dialog.setVisible(true);
    }

    private Image getResultImage_a(JDialog dialog, boolean flag) {
        if (flag) {
            showDialog_b(dialog);
            return getResultImage(flag);
        } else {
            return getResultImage_a(dialog);
        }
    }

    public SImageResizerPane(Dimension size) {
        this(size, null);
    }

    public SImageResizerPane(Dimension size, Image image) {
        this(size, DEFAULT_CANVAS_SIZE, image);
    }

    public SImageResizerPane(Dimension size, Dimension dimension1, Image image) {
        this(size, dimension1, image, -1);
    }

    public SImageResizerPane(Dimension size, Dimension dimension1, final Image image, int hints) {
        animated = false;
        this.hints = hints;
        if (image == null)
            hasChooser = true;
        waiting = false;
        this.size = size;
        imagePanelSize = dimension1;
        initGUI();
        grassPanel.setImagePanel(imagePanel);
        grassPanel.b();
        if (image != null)
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setImage_a(image);
                }
            });
    }

    private void initGUI() {
        setBackground(backgroundColor);
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        setLayout(layout);
        if (hasChooser) {
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridwidth = 0;
            gbc.gridheight = 1;
            FileChooserPanel panel = new FileChooserPanel(this);
            panel.setHints(hints);
            layout.addLayoutComponent(panel, gbc);
            add(panel);
        }
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = -1;
        gbc.gridheight = 3;
        grassPanel = new GrassPanel(imagePanelSize);
        grassPanel.setHints(hints);
        layout.addLayoutComponent(grassPanel, gbc);
        add(grassPanel);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 0;
        gbc.gridheight = 1;
        imagePanel = new ImagePanel(size);
        imagePanel.setSizeOption(SizeOption.ORIGINAL);
        layout.addLayoutComponent(imagePanel, gbc);
        add(imagePanel);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 0;
        gbc.gridheight = 1;
        gbc.weighty = 1.0D;
        JPanel resizerPanel = createResizerPanel();
        layout.addLayoutComponent(resizerPanel, gbc);
        add(resizerPanel);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 0;
        gbc.gridheight = 1;
        gbc.weighty = 0.0D;
        JPanel confermationPanel = createConfirmationPanel();
        layout.addLayoutComponent(confermationPanel, gbc);
        add(confermationPanel);
    }

    private JPanel createResizerPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(backgroundColor);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        ButtonGroup group = new ButtonGroup();
        JRadioButton originalButton = new JRadioButton();
        originalButton.setSelected(true);
        originalButton.setBackground(backgroundColor);
        originalButton.setText("オリジナルサイズを保持");
        originalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                setSizeOption(SizeOption.ORIGINAL);
            }
        });
        group.add(originalButton);
        panel.add(originalButton);
        JRadioButton ratioExtendedButton = new JRadioButton();
        ratioExtendedButton.setBackground(backgroundColor);
        ratioExtendedButton.setText("縦横比を保持して伸縮");
        ratioExtendedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                setSizeOption(SizeOption.RATIO_EXTEND);
            }
        });
        group.add(ratioExtendedButton);
        panel.add(ratioExtendedButton);
        JRadioButton fullExtendedButton = new JRadioButton();
        fullExtendedButton.setBackground(backgroundColor);
        fullExtendedButton.setText("最大サイズに伸縮");
        fullExtendedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                setSizeOption(SizeOption.FULL_EXTEND);
            }
        });
        group.add(fullExtendedButton);
        panel.add(fullExtendedButton);
        panel.add(Box.createGlue());
        return panel;
    }

    private JPanel createConfirmationPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(backgroundColor);
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        cancelButton = new SButton();
        SGUIUtil.setSizeALL(cancelButton, buttonSize);
        cancelButton.setText("取り消し");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                b(isAnimated());
            }
        });
        panel.add(cancelButton);
        approveButton = new SButton();
        SGUIUtil.setSizeALL(approveButton, buttonSize);
        approveButton.setText("完了");
        approveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                a(isAnimated());
            }
        });
        panel.add(approveButton);
        approveButton.setEnabled(false);
        return panel;
    }

    final void setImage_a(Image image) {
        setVisible(false);
        grassPanel.loadImage(image);
        setSizeOption(SizeOption.ORIGINAL);
        approveButton.setEnabled(true);
        setVisible(true);
        repaint();
    }

    final void setSizeOption(SizeOption sizeOption) {
        imagePanel.setSizeOption(sizeOption);
        switch (sizeOption) {
        case ORIGINAL:
            grassPanel.setSize_a(size);
            grassPanel.setOriginalSize(true);
            return;
        case RATIO_EXTEND:
            grassPanel.setSize_a((Dimension) null);
            grassPanel.setOriginalSize(false);
            return;
        case FULL_EXTEND:
            grassPanel.setSize_a((Dimension) null);
            grassPanel.setOriginalSize(false);
            return;
        }
        throw new IllegalStateException("Unknown SizeOption :" + sizeOption);
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
            if (disposer != null)
                disposer.run();
            waiting = true;
            notifyAll();
        }
    }

    final void setDisposer(Runnable disposer) {
        synchronized (this) {
            if (waiting)
                throw new IllegalStateException("Already finished.");
            if (this.disposer != null)
                throw new IllegalStateException("Already having an action.");
            this.disposer = disposer;
        }
    }

    final void d() {
        image_j = imagePanel.getImage_a();
        doNotifyAll();
    }

    final void a(boolean flag) {
        if (flag) {
            image_k = imagePanel.getImage_a(flag);
            doNotifyAll();
        } else {
            d();
        }
    }

    final void e() {
        image_j = null;
        doNotifyAll();
    }

    final void b(boolean flag) {
        if (flag) {
            image_k = null;
            doNotifyAll();
        } else {
            e();
        }
    }

    public BufferedImage getResultImage() {
        return image_j;
    }

    public Image getResultImage(boolean flag) {
        if (flag)
            return image_k;
        else
            return image_j;
    }

    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    public boolean isAnimated() {
        return animated;
    }

    public static void main(String[] args) {
        final JFrame frame = new JFrame();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                System.exit(0);
            }
        });
        SButton button = new SButton();
        button.setText("test");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                SImageResizerPane pane = new SImageResizerPane(new Dimension(300, 200), SImageResizerPane.DEFAULT_CANVAS_SIZE, null, Image.SCALE_REPLICATE);
                Image image = pane.showImageResizerInternalDialog(frame.getLayeredPane(), "イメージリサイズ", false);
                if (image != null) {
                    JOptionPane.showConfirmDialog(frame, "リサイズされました", "test", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, new ImageIcon(image));
                } else {
                    JOptionPane.showConfirmDialog(frame, "キャンセルです", "test", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        frame.getContentPane().add(button);
        frame.pack();
        frame.setVisible(true);
    }

    private static final long serialVersionUID = 0x69dc8253L;
    private static final Color backgroundColor = Color.WHITE;
    public static final Dimension DEFAULT_CANVAS_SIZE = new Dimension(400, 400);
    private static final Dimension buttonSize = new Dimension(100, 24);
    private final Dimension size;
    private final Dimension imagePanelSize;
    private GrassPanel grassPanel;
    private ImagePanel imagePanel;
    private SButton approveButton;
    private SButton cancelButton;
    private boolean hasChooser;
    private BufferedImage image_j;
    private Image image_k;
    private boolean waiting;
    private Runnable disposer;
    private int hints;
    private boolean animated;
}
