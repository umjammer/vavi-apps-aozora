/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;


public class SDialog extends JDialog {

    public SDialog() {
        action = new AbstractAction("dispose") {
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        };
    }

    public SDialog(Frame frame) {
        super(frame);
        action = new AbstractAction("dispose") {
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        };
    }

    public void setDisposeWithEsc(boolean disposeWithEsc) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        Object value = action.getValue("Name");
        if (disposeWithEsc) {
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, value);
            getRootPane().getActionMap().put(value, action);
        } else {
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(keyStroke);
            getRootPane().getActionMap().remove(value);
        }
    }

    private static final long serialVersionUID = 0x61f6acd5L;

    private Action action;
}
