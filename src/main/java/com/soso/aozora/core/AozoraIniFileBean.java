/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.core;

import de.javasoft.plaf.synthetica.SyntheticaSilverMoonLookAndFeel;
import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;


@Deprecated
public class AozoraIniFileBean {

    public AozoraIniFileBean() {
        bookmark = new HashMap<String, Integer>();
        systemFont = new Font("Dialog", Font.PLAIN, 16);
        try {
            lookAndFeel = new SyntheticaSilverMoonLookAndFeel();
        } catch (Exception e) {
            lookAndFeel = UIManager.getLookAndFeel();
        }
        rowSpace = 10;
        fontRatio = 0.9F;
        foreground = AozoraEnv.DEFAULT_FOREGROUND_COLOR;
        background = AozoraEnv.DEFAULT_BACKGROUND_COLOR;
    }

    public int getRowSpace() {
        return rowSpace;
    }

    public void setRowSpace(int rowSpace) {
        this.rowSpace = rowSpace;
    }

    public float getFontRatio() {
        return fontRatio;
    }

    public void setFontRatio(float fontRangeRatio) {
        fontRatio = fontRangeRatio;
    }

    public Font getSystemFont() {
        return systemFont;
    }

    public void setSystemFont(Font systemFont) {
        this.systemFont = systemFont;
    }

    public Integer putBookmark(String book, Integer page) {
        return bookmark.put(book, page);
    }

    public Integer getBookmark(String book) {
        return bookmark.get(book);
    }

    public Integer removeBookmark(String book) {
        return bookmark.remove(book);
    }

    public Map<String, Integer> getBookmarks() {
        return bookmark;
    }

    public LookAndFeel getLookAndFeel() {
        return lookAndFeel;
    }

    public void setLookAndFeel(LookAndFeel lookAndFeel) {
        this.lookAndFeel = lookAndFeel;
    }

    public Color getForeground() {
        return foreground;
    }

    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    private Map<String, Integer> bookmark;
    private Font systemFont;
    private LookAndFeel lookAndFeel;
    private int rowSpace;
    private float fontRatio;
    private Color foreground;
    private Color background;
}
