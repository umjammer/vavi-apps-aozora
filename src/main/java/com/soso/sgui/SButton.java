/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;

import java.awt.Cursor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;


public class SButton extends JButton {

    private static final long serialVersionUID = 0xc473c6d0L;

    public SButton() {
        attachKeyListener();
        attachMouseListener();
    }

    private void attachKeyListener() {
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER)
                    doClick();
            }
        });
    }

    private void attachMouseListener() {
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent event) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            public void mouseExited(MouseEvent event) {
                setCursor(Cursor.getDefaultCursor());
            }
        });
    }
}
