/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.letter;

import java.awt.Graphics2D;
import java.awt.Rectangle;


public interface SLetterCellDecorator {

    void decorateBeforePaint(Graphics2D g, SLetterCell cell, Rectangle cellBounds, Rectangle rubyBounds);

    void decorateAfterPaint(Graphics2D g, SLetterCell cell, Rectangle cellBounds, Rectangle rubyBounds);

    void removeDecoration(SLetterCell cell);
}
