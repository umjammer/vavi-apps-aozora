/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;


public class SMenuItemLabel extends JMenuItem {

    public SMenuItemLabel(String text, char mnemonic, JComponent component) {
        this();
        setText(text);
        setMnemonic(mnemonic);
        setMnemonicTransferComponent(component);
    }

    public SMenuItemLabel() {
        setBorder(null);
        action = new AbstractAction("SMenuItemLabel.mnemonicAction") {
            public void actionPerformed(ActionEvent event) {
                fireFocasTransfer();
            }
        };
    }

    public void setMnemonicTransferComponent(JComponent component) {
        detachKeyStroke();
        detachAction();
        this.component = component;
        attachAction();
        attachKeyStroke();
    }

    public JComponent getMnemonicTransferComponent() {
        return component;
    }

    public void setMnemonic(int mnemonic) {
        detachKeyStroke();
        super.setMnemonic(mnemonic);
        keyStroke = KeyStroke.getKeyStroke("alt " + (char) mnemonic);
        attachKeyStroke();
    }

    private void attachKeyStroke() {
        JComponent component = getMnemonicTransferComponent();
        KeyStroke theKeyStroke = keyStroke;
        if (component != null && theKeyStroke != null)
            component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(theKeyStroke, getValueOfName());
    }

    private void detachKeyStroke() {
        JComponent component = getMnemonicTransferComponent();
        KeyStroke theKeyStroke = keyStroke;
        if (component != null && theKeyStroke != null)
            component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(theKeyStroke);
    }

    private void attachAction() {
        JComponent component = getMnemonicTransferComponent();
        if (component != null)
            component.getActionMap().put(getValueOfName(), action);
    }

    private void detachAction() {
        JComponent component = getMnemonicTransferComponent();
        if (component != null)
            component.getActionMap().remove(getValueOfName());
    }

    private Object getValueOfName() {
        return action.getValue("Name");
    }

    protected void fireFocasTransfer() {
        JComponent component = getMnemonicTransferComponent();
        if (component != null)
            component.requestFocusInWindow();
    }

    private static final long serialVersionUID = 0xb3eafbc3L;

    private JComponent component;
    private final Action action;
    private KeyStroke keyStroke;
}
