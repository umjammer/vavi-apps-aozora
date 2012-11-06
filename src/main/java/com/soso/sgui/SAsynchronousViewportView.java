/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;

import java.awt.LayoutManager;
import java.awt.Rectangle;
import javax.swing.JPanel;


public class SAsynchronousViewportView extends JPanel {

    public SAsynchronousViewportView(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
        asynchronously = true;
    }

    public SAsynchronousViewportView(LayoutManager layout) {
        super(layout);
        asynchronously = true;
    }

    public SAsynchronousViewportView(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
        asynchronously = true;
    }

    public SAsynchronousViewportView() {
        super(true);
        asynchronously = true;
    }

    public void scrollRectToVisible(Rectangle aRect) {
        if (!isAsynchronously())
            super.scrollRectToVisible(aRect);
    }

    public boolean isAsynchronously() {
        return asynchronously;
    }

    public void setAsynchronously(boolean asynchronously) {
        this.asynchronously = asynchronously;
    }

    private static final long serialVersionUID = 1L;

    private boolean asynchronously;
}
