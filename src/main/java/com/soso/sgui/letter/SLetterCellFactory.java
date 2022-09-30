/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.letter;

import java.awt.Font;
import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import javax.swing.ImageIcon;

import com.soso.sgui.text.CharacterUtil;


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

    public SLetterCell createKaeritenGlyphCell(char c) {
        SLetterGlyphCell cell = new SLetterGlyphCell(c, null, null);
        cell.addConstraint(SLetterConstraint.TRANS.SMALLCHAR);
        return cell;
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

    /** for surrogate pair */
    public SLetterCell createGlyphCell(int cp, char[] rubys, Font font) {
        return new SLetterGlyphCell(cp, rubys, font);
    }

    /** for svg */
    public SLetterCell createSvgCell(URL url, char[] rubys, Font font) {
        try (InputStream is = new BufferedInputStream(url.openStream());
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] b = new byte[8192];
            int l;
            while ((l = is.read(b)) > 0) {
                baos.write(b, 0, l);
            }
            return new SLetterSvgCell(baos.toByteArray(), rubys, font);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public SLetterCell createImageCell(Image image) {
        return createImageCell(image, true);
    }

    public SLetterCell createImageCell(Image image, String text) {
        return createImageCell(image, true, null, null, text);
    }

    public SLetterCell createImageCell(Image image, boolean fit) {
        return createImageCell(image, fit, null, null);
    }

    public SLetterCell createImageCell(Image image, char[] rubys) {
        return createImageCell(image, true, rubys, null);
    }

    public SLetterCell createImageCell(Image image, boolean fit, char[] rubys, Font font) {
        return createImageCell(image, fit, rubys, font, null);
    }

    public SLetterCell createImageCell(Image image, boolean fit, char[] rubys, Font font, String text) {
        return createImageCell(image, fit, false, rubys, font, text);
    }

    public SLetterCell createImageCell(Image image, boolean fit, boolean magnifyable, char[] rubys, Font font, String text) {
        return new SLetterImageCell(image, fit, magnifyable, rubys, font, text);
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

    public SLetterCell createImageCell(URL url, boolean fit, char[] rubys, Font font, String text) {
        return createImageCell(url, fit, false, rubys, font, text);
    }

    public SLetterCell createImageCell(URL url, boolean fit, boolean magnifyable, char[] rubys, Font font, String text) {
        Image image = new ImageIcon(url).getImage();
        return createImageCell(image, fit, magnifyable, rubys, font, text);
    }

    private static SLetterCellFactory factory;
}
