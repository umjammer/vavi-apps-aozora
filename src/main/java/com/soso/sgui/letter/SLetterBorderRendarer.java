/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.letter;

import java.awt.Graphics;
import java.awt.Rectangle;


public interface SLetterBorderRendarer {

    public abstract void paintRowBorder(Graphics g, Rectangle rowBounds);

    public abstract void paintCellBorder(Graphics g, Rectangle cellBounds);
}
