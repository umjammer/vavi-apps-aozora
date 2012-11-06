/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JRadioButton;


public class SRadioButton extends JRadioButton {

    public SRadioButton() {
        attachKeyListener();
    }

    private void attachKeyListener() {
        addKeyListener(new KeyAdapter() {
            public final void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_SPACE)
                    doClick();
            }
        });
    }

    private static final long serialVersionUID = 0x457aef88L;
}
