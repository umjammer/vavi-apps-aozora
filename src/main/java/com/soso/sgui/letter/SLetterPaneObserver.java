/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.letter;

import java.awt.Point;


public interface SLetterPaneObserver {

    void orientationChanged(SLetterConstraint.ORIENTATION oldValue, SLetterConstraint.ORIENTATION newValue);

    void cellAdded(SLetterCell cell);

    void cellRemoved(SLetterCell cell);

    void rowCountChanged(int oldValue, int newValue);

    void colCountChanged(int oldValue, int newValue);

    void rowRangeChanged(int oldValue, int newValue);

    void colRangeChanged(int oldValue, int newValue);

    void fontRangeRatioChanged(float oldValue, float newValue);

    void rowSpaceChanged(int oldValue, int newValue);

    void colSpaceChanged(int oldValue, int newValue);

    void borderRendererChanged(SLetterBorderRenderer oldValue, SLetterBorderRenderer newValue);

    void rowColCountChangeableChanged(boolean value);

    void fontSizeChangeableChanged(boolean value);

    void originDifferenceChanged(Point oldValue, Point newValue);
}
