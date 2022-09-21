/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.letter;

import java.awt.Graphics;
import java.awt.Rectangle;


public interface SLetterCellDecorator {

    void decorateBeforePaint(Graphics g, SLetterCell cell, Rectangle cellBounds, Rectangle rubyBounds);

    void decorateAfterPaint(Graphics g, SLetterCell cell, Rectangle cellBounds, Rectangle rubyBounds);

    void removeDecoration(SLetterCell cell);
}
