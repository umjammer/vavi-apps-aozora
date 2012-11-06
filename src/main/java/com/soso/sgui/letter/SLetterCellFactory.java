/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.letter;

import com.soso.sgui.text.CharacterUtil;
import java.awt.Font;
import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;


public class SLetterCellFactory {

    protected SLetterCellFactory() {
    }

    public static SLetterCellFactory getInstance() {
        if (factory == null)
            synchronized (SLetterCellFactory.class) {
                if (factory == null)
                    factory = newInstance();
            }
        return factory;
    }

    public static SLetterCellFactory newInstance() {
        return new SLetterCellFactory();
    }

    public SLetterCell createGlyphCell(char c) {
        return createGlyphCell(c, null, null);
    }

    public SLetterCell createGlyphCell(char c, Font font) {
        return createGlyphCell(c, null, font);
    }

    public SLetterCell createGlyphCell(char c, char[] rubys) {
        return createGlyphCell(c, rubys, null);
    }

    public SLetterCell createGlyphCell(char c, char[] rubys, Font font) {
        SLetterGlyphCell cell = new SLetterGlyphCell(c, rubys, font);
        if (CharacterUtil.isLineTailForbidden(c))
            cell.addConstraint(SLetterConstraint.BREAK.NEW_LINE_IF_LINE_TAIL);
        if (CharacterUtil.isLineHeadForbidden(c))
            cell.addConstraint(SLetterConstraint.BREAK.BACK_IF_LINE_HEAD);
        if (CharacterUtil.isLineSeparator(c))
            cell.addConstraint(SLetterConstraint.BREAK.NEW_LINE);
        if (CharacterUtil.isPageSeparator(c))
            cell.addConstraint(SLetterConstraint.BREAK.NEW_PAGE);
        if (CharacterUtil.isToRotate(c))
            cell.addConstraint(SLetterConstraint.ROTATE.GENERALLY);
        if (CharacterUtil.isToRotateLRMirrorKana(c))
            cell.addConstraint(SLetterConstraint.ROTATE.LR_MIRROR);
        if (CharacterUtil.isHalfWidth(c) || CharacterUtil.isNarrow(c))
            cell.addConstraint(SLetterConstraint.OVERLAY.HALF_OVER);
        if (CharacterUtil.isPunctuateKana(c))
            cell.addConstraint(SLetterConstraint.TRANS.PUNCTURETE);
        if (CharacterUtil.isSmallKana(c))
            cell.addConstraint(SLetterConstraint.TRANS.SMALLCHAR);
        return cell;
    }

    public SLetterCell createImageCell(Image image) {
        return createImageCell(image, true);
    }

    public SLetterCell createImageCell(Image image, String text) {
        return createImageCell(image, true, null, null, text);
    }

    public SLetterCell createImageCell(Image image, boolean resize) {
        return createImageCell(image, resize, null, null);
    }

    public SLetterCell createImageCell(Image image, char[] rubys) {
        return createImageCell(image, true, rubys, null);
    }

    public SLetterCell createImageCell(Image image, boolean resize, char[] rubys, Font font) {
        return createImageCell(image, resize, rubys, font, null);
    }

    public SLetterCell createImageCell(Image image, boolean resize, char[] rubys, Font font, String text) {
        return createImageCell(image, resize, false, rubys, font, text);
    }

    public SLetterCell createImageCell(Image image, boolean resize, boolean maximizable, char[] rubys, Font font, String text) {
        SLetterImageCell imageCell = new SLetterImageCell(image, resize, maximizable, rubys, font, text);
        return imageCell;
    }

    public SLetterCell createImageCell(URL url) {
        return createImageCell(url, true);
    }

    public SLetterCell createImageCell(URL url, String text) {
        return createImageCell(url, true, null, null, text);
    }

    public SLetterCell createImageCell(URL url, boolean flag) {
        return createImageCell(url, flag, null, null, null);
    }

    public SLetterCell createImageCell(URL url, char[] rubys) {
        return createImageCell(url, true, rubys, null, null);
    }

    public SLetterCell createImageCell(URL url, boolean resize, char[] rubys, Font font, String text) {
        return createImageCell(url, resize, false, rubys, font, text);
    }

    public SLetterCell createImageCell(URL url, boolean resize, boolean maximizable, char[] rubys, Font font, String text) {
        Image image = new ImageIcon(url).getImage();
        return createImageCell(image, resize, maximizable, rubys, font, text);
    }

    private static SLetterCellFactory factory;
}
