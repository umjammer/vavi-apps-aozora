/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JTextPane;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;

import com.soso.sgui.text.CharacterUtil;
import com.soso.sgui.text.VerticalStyledEditorKit;


public class SVerticalTextPane extends JTextPane {

    public SVerticalTextPane() {
        ratio = 1.0F;
        editable = false;
        editable = true;
        setEditable(false);
        setName("verticalTextPane");
    }

    public void setEditable(boolean b) {
        if (this.editable && b) {
            throw new UnsupportedOperationException("Editable VerticalTextPane is not supported.");
        }
        super.setEditable(b);
    }

    protected EditorKit createDefaultEditorKit() {
        return new VerticalStyledEditorKit();
    }

    public int setAbsoluteSize(Dimension size) {
        String text = getText();
        int length = text.length();

        try {
            setVisible(false);
            SGUIUtil.setSizeALL(this, size);
            int pos = 0;
            TextUI textUi = getUI();
            Insets insets = getInsets();
            for (int x = size.width - insets.right; x >= 0; x--) {
                if ((pos = textUi.viewToModel(this, new Point(x, size.height))) == -1)
                    throw new IllegalStateException("TextPane may not be painted.");
                if (pos > 0) {
                    pos = length - pos;
                    break;
                }
            }
            Rectangle b0 = textUi.modelToView(this, 0);
            Rectangle b1 = textUi.modelToView(this, pos);
            int w = ((b0.x + b0.width) - size.width) + (insets.left + insets.right);
            while (b1.x > w && pos < length)
                pos++;
            while (b1.x < w && pos > 0) {
                pos--;
                b1 = textUi.modelToView(this, pos);
            }

            char c;
            if (++pos <= length) {
                c = text.charAt(pos - 1);
                if (CharacterUtil.isLineSeparator(c)) {
                    setText(text.substring(0, pos - 1));
                    return pos;
                }
                if (CharacterUtil.isLineTailForbidden(c)) {
                    setText(text.substring(0, pos - 1));
                    return pos - 1;
                }
                if (pos == length) {
                    return -1;
                }
                c = text.charAt(pos);
                if (CharacterUtil.isLineSeparator(c)) {
                    setText(text.substring(0, pos));
                    return pos + 1;
                }
                if (CharacterUtil.isLineHeadForbidden(c)) {
                    if (pos + 1 == length) {
                        setText(text.substring(0, pos + 1));
                        return -1;
                    }
                    c = text.charAt(pos + 1);
                    if (CharacterUtil.isLineSeparator(c)) {
                        setText(text.substring(0, pos + 1));
                        return pos + 2;
                    }
                    if (CharacterUtil.isLineHeadForbidden(c)) {
                        setText(text.substring(0, pos + 2));
                        return pos + 2;
                    }
                    setText(text.substring(0, pos + 1));
                    return pos + 1;
                }
                setText(text.substring(0, pos));
                return pos;
            }
            c = '\0';
            return -1;
        } catch (BadLocationException e) {
            return -1;
        } finally {
            setVisible(true);
        }
    }

    public float getLineSpaceRatio() {
        return ratio;
    }

    public void setLineSpaceRatio(float ratio) {
        if (ratio <= 0.0F) {
            throw new IllegalArgumentException("LineSpaceRatio is not positive:" + ratio);
        }
        float oldValue = this.ratio;
        this.ratio = ratio;
        updateUI();
        firePropertyChange("line_space_ratio", oldValue, ratio);
    }

    private static final long serialVersionUID = 0x9f1c3279L;

    public static final String LINE_SPACE_RATIO = "line_space_ratio";
    private float ratio;
    private transient boolean editable;
}
