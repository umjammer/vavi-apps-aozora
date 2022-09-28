/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.letter;

import java.awt.Graphics2D;
import java.awt.Rectangle;


public interface SLetterBorderRenderer {

    void paintRowBorder(Graphics2D g, Rectangle rowBounds);

    void paintCellBorder(Graphics2D g, Rectangle cellBounds);
}
