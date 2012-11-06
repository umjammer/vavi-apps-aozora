
package com.soso.sgui.img;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.security.AccessControlException;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import com.soso.sgui.SButton;
import com.soso.sgui.SFileChooser;
import com.soso.sgui.SGUIUtil;


final class FileChooserPanel extends JPanel {

    private static final long serialVersionUID = 0x8f32304fL;
    private static final Color background = Color.WHITE;
    private JTextField textField;
    private final SImageResizerPane imageResizePane;
    private int hints;

    FileChooserPanel(SImageResizerPane pane) {
        hints = Image.SCALE_SMOOTH;
        imageResizePane = pane;
        initGUI();
    }

    private void initGUI() {
        setBackground(background);
        setLayout(new BorderLayout(5, 5));
        textField = new JTextField();
        textField.setEditable(false);
        add(textField, BorderLayout.CENTER);
        SButton button = new SButton();
        button.setText("参照...");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                File file = getSelectedFile(hints);
                if (file != null) {
                    if (!file.exists()) {
                        imageResizePane.setImage_a((Image) null);
                        textField.setText("ファイルは存在しません。\t" + file.getAbsolutePath());
                        return;
                    }
                    try {
                        textField.setText(file.getAbsolutePath());
                        Image image = getToolkit().getImage(file.getAbsolutePath());
                        imageResizePane.setImage_a(image);
                        return;
                    } catch (Exception _ex) {
                        imageResizePane.setImage_a((Image) null);
                    }
                    textField.setText("表示できないファイルです。\t" + file.getAbsolutePath());
                }
            }
        });
        add(button, BorderLayout.EAST);
    }

    private File getSelectedFile(int hints) {
        Object readOnly = null;
        try {
            readOnly = UIManager.get("FileChooser.readOnly");
            try {
                UIManager.put("FileChooser.readOnly", true);
                SFileChooser chooser = new SFileChooser();
                chooser.setupMemoryLastDirectoryChooser();
                chooser.setupImageFileChooser(hints);
                JInternalFrame iframe = SGUIUtil.getParentInstanceOf(imageResizePane, JInternalFrame.class);
                boolean headless = iframe == null;
                if ((headless ? chooser.showOpenDialog(imageResizePane) : chooser.showInternalOpenDialog(iframe)) == SFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    return file;
                }
                if (headless) {
                    return null;
                } else {
                    throw new IllegalStateException();
                }
            } finally {
                UIManager.put("FileChooser.readOnly", readOnly);
            }
        } catch (AccessControlException e) {
            throw e;
        }
    }

    final void setHints(int hints) {
        this.hints = hints;
    }
}
