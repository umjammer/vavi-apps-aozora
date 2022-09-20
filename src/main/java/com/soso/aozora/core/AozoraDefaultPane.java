/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.core;

import com.soso.aozora.boot.AozoraContext;
import java.awt.BorderLayout;
import java.util.logging.Logger;
import javax.swing.JPanel;


public abstract class AozoraDefaultPane extends JPanel {

    static Logger logger = Logger.getLogger(AozoraDefaultPane.class.getName());

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

    private final AozoraContext context;
}
