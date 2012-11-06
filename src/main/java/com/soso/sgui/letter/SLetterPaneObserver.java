/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.letter;

import java.awt.Point;


public interface SLetterPaneObserver {

    public abstract void orientationChanged(SLetterConstraint.ORIENTATION oldValue, SLetterConstraint.ORIENTATION newValue);

    public abstract void cellAdded(SLetterCell cell);

    public abstract void cellRemoved(SLetterCell cell);

    public abstract void rowCountChanged(int oldValue, int newValue);

    public abstract void colCountChanged(int oldValue, int newValue);

    public abstract void rowRangeChanged(int oldValue, int newValue);

    public abstract void colRangeChanged(int oldValue, int newValue);

    public abstract void fontRangeRatioChanged(float oldValue, float newValue);

    public abstract void rowSpaceChanged(int oldValue, int newValue);

    public abstract void colSpaceChanged(int oldValue, int newValue);

    public abstract void borderRendarerChanged(SLetterBorderRendarer oldValue, SLetterBorderRendarer newValue);

    public abstract void rowColCountChangableChanged(boolean value);

    public abstract void fontSizeChangableChanged(boolean value);

    public abstract void originDifferenceChanged(Point oldValue, Point newValue);
}
