/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.core;

import com.soso.aozora.boot.AozoraContext;
import java.awt.BorderLayout;
import javax.swing.JPanel;


public abstract class AozoraDefaultPane extends JPanel {

    public AozoraDefaultPane(AozoraContext context) {
        super(new BorderLayout(0, 0), true);
        if (context == null) {
            throw new IllegalArgumentException("AozoraContext null");
        }
        this.context = context;
    }

    protected AozoraContext getAzContext() {
        return context;
    }

    protected void log(Object obj) {
        getAzContext().log(obj, getClass());
    }

    protected void log(Throwable t) {
        getAzContext().log(t);
    }

    private final AozoraContext context;
}
