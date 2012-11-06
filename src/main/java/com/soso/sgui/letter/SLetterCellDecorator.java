/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.letter;

import java.awt.Graphics;
import java.awt.Rectangle;


public interface SLetterCellDecorator {

    public abstract void decorateBeforePaint(Graphics g, SLetterCell cell, Rectangle cellBounds, Rectangle rubyBounds);

    public abstract void decorateAfterPaint(Graphics g, SLetterCell cell, Rectangle cellBounds, Rectangle rubyBounds);

    public abstract void removeDecoration(SLetterCell cell);
}
