/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;

import javax.swing.JInternalFrame;
import javax.swing.JRootPane;


public class SInternalFrame extends JInternalFrame {

    public void setRootPane(JRootPane rootPane) {
        super.setRootPane(rootPane);
    }

    @Deprecated
    public void setForceHeavyWeightPopup(boolean forceHeavyWeightPopup) {
        SGUIUtil.forceHeavyWeightPopupKey(this, forceHeavyWeightPopup);
    }

    @Deprecated
    public void setModal(boolean isModel) {
        if (isModal() != isModel) {
            synchronized (getTreeLock()) {
                if (isModel) {
                    this.isModel = true;
                    if (iframe != null)
                        iframe.setModal(false);
                    iframe = this;
                    SGUIUtil.startLWModal(this);
                } else {
                    this.isModel = false;
                    SGUIUtil.stopLWModal(this);
                    iframe = null;
                }
            }
        }
    }

    public boolean isModal() {
        return isModel;
    }

    public static SInternalFrame currentModalInternalFrame() {
        return iframe;
    }

    private static final long serialVersionUID = 0xdeba4a13L;
    private static volatile SInternalFrame iframe;
    private volatile boolean isModel;
}
