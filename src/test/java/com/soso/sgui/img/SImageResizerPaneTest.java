
package com.soso.sgui.img;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.soso.sgui.SButton;


/**
 * SImageResizerPaneTest
 */
public class SImageResizerPaneTest {

    /**
     * @param args
     */
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
}
