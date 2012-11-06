/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.letter;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import com.soso.sgui.text.CharacterUtil;


public class SLetterGlyphCell extends SLetterCell {

    protected SLetterGlyphCell(char main, char[] rubys, Font font) {
        if (!Character.isDefined(main)) {
            throw new IllegalArgumentException("Character is not defined");
        }
        this.main = main;
        this.rubys = rubys;
        this.font = font;
    }

    public void paintCell(Graphics g, Rectangle cellBounds) {
        SLetterConstraint.ORIENTATION orientation = getOrientation();
        if (orientation == null)
            return;
        boolean is2d = g instanceof Graphics2D;
        Graphics2D g2 = is2d ? (Graphics2D) g : null;
        Font font = g.getFont();
        if (font != null)
            g.setFont(font);
        int asc = g.getFontMetrics().getAscent();
        int desc = g.getFontMetrics().getDescent();
        int width = g.getFontMetrics().charWidth(main);
        int x1 = cellBounds.x + (cellBounds.width - width) / 2;
        int y1 = cellBounds.y + ((cellBounds.height + asc) - desc) / 2;
        int x2 = cellBounds.x + cellBounds.width / 2;
        int y2 = cellBounds.y + cellBounds.height / 2;
        double theta = getRotateTheta(orientation);
        int transX = getTransX(orientation, cellBounds, width);
        int transY = getTransY(orientation, cellBounds, asc, desc);
        x1 += transX;
        y1 += transY;
        AffineTransform transform = null;
        if (is2d && theta != 0.0D) {
            transform = g2.getTransform();
            AffineTransform transform1 = new AffineTransform(transform);
            transform1.rotate(theta, x2, y2);
            if (isConstraintSet(SLetterConstraint.ROTATE.LR_MIRROR)) {
                transform1.translate(0.0D, y2);
                transform1.scale(1.0D, -1D);
                transform1.translate(0.0D, -y2);
            }
            g2.setTransform(transform1);
        }
        g.drawString(String.valueOf(main), x1, y1);
        if (DEBUG.isDebug()) {
            Color color = g.getColor();
            g.setColor(new Color(153, 153, 255));
            g.drawRect(x1, y1 - asc, width, asc);
            g.setColor(new Color(255, 153, 153));
            g.drawRect(x1, y1, width, desc);
            g.setColor(color);
        }
        if (transform != null)
            g2.setTransform(transform);
        g.setFont(font);
    }

    protected double getRotateTheta(SLetterConstraint.ORIENTATION orientation) {
        if (isConstraintSet(SLetterConstraint.ROTATE.GENERALLY))
            switch (orientation) {
            case LRTB:
                return 0.0D;
            case RLTB:
                return !CharacterUtil.isToRotateKana(main) ? 0.0D : Math.PI;
            case TBRL:
            case TBLR:
                return Math.PI / 2;
            }
        if (isConstraintSet(SLetterConstraint.ROTATE.REVERSE))
            switch (orientation) {
            case LRTB:
                return 0.0D;
            case RLTB:
                return !CharacterUtil.isToRotateKana(main) ? 0.0D : -Math.PI;
            case TBRL:
            case TBLR:
                return -Math.PI / 2;
            }
        return 0.0D;
    }

    private int getTransX(SLetterConstraint.ORIENTATION orientation, Rectangle rectangle, int width) {
        int transX = 0;
        if (!isConstraintSet())
            return 0;

        if (isConstraintSet(SLetterConstraint.BREAK.BACK_IF_LINE_HEAD) &&
            isConstraintSet(SLetterConstraint.OVERLAY.LINE_TAIL_OVER))
            if (isConstraintSet(SLetterConstraint.ROTATE.GENERALLY) ||
                isConstraintSet(SLetterConstraint.ROTATE.REVERSE))
                transX = 0 + (orientation.isLeftToRight() ? rectangle.height : rectangle.width);
            else if (isConstraintSet(SLetterConstraint.TRANS.PUNCTURETE))
                transX = 0 + (orientation.isLeftToRight() ? rectangle.width : 0);

        if (isConstraintSet(SLetterConstraint.TRANS.PUNCTURETE)) {
            transX += (orientation.isLeftToRight() ? 0 : rectangle.width / 2);
        } else if (isConstraintSet(SLetterConstraint.TRANS.SMALLCHAR)) {
            int w = (int) ((width / 4) * 0.7861513F);
            transX += (orientation.isLeftToRight() ? -w : w);
        } else if (isConstraintSet(SLetterConstraint.OVERLAY.HALF_OVER_HEAD)) {
            transX += (0 - rectangle.height / 4);
        } else if (isConstraintSet(SLetterConstraint.OVERLAY.HALF_OVER_TAIL)) {
            transX += (rectangle.height / 4);
        }
        if (DEBUG.isDebug())
            DEBUG.log("TransX=" + transX + " ," + main + "|" + rectangle);
        return transX;
    }

    private int getTransY(SLetterConstraint.ORIENTATION orientation, Rectangle rectangle, int asc, int desc) {
        int transY = 0;
        if (!isConstraintSet())
            return 0;
        if (isConstraintSet(SLetterConstraint.BREAK.BACK_IF_LINE_HEAD) &&
            isConstraintSet(SLetterConstraint.OVERLAY.LINE_TAIL_OVER) &&
            isConstraintSet(SLetterConstraint.TRANS.PUNCTURETE))
            transY = 0 + (orientation.isHorizonal() ? 0 : rectangle.height);
        if (isConstraintSet(SLetterConstraint.TRANS.PUNCTURETE)) {
            int h = rectangle.height / 2;
            transY = transY + (orientation.isHorizonal() ? 0 : -h);
        } else if (isConstraintSet(SLetterConstraint.TRANS.SMALLCHAR)) {
            int h = (asc + desc) / 4;
            transY = transY + (orientation.isHorizonal() ? 0 : -h);
        }
        if (DEBUG.isDebug())
            DEBUG.log("TransY=" + transY + " ," + main + "|" + rectangle);
        return transY;
    }

    public void paintRuby(Graphics g, Rectangle rubyBounds) {
        if (rubys != null && rubys.length != 0) {
            SLetterConstraint.ORIENTATION orientation = getOrientation();
            if (orientation == null)
                return;
            boolean is2d = g instanceof Graphics2D;
            Graphics2D g2 = is2d ? (Graphics2D) g : null;
            Font originalFont = g.getFont();
            int h = orientation.isHorizonal() ? rubyBounds.height : rubyBounds.height / rubys.length;
            int w = orientation.isHorizonal() ? rubyBounds.width / rubys.length : rubyBounds.width;
            int center = (orientation.isHorizonal() ? rubyBounds.width : rubyBounds.height) / 2;
            center = (int) (center * (getParent() == null ? 0.7861513F : getParent().getFontRangeRatio()));
            Font rubyFont = new Font((font != null ? font : originalFont).getName(), (font != null ? font : originalFont).getStyle(), center);
            for (int i = 0; i < rubys.length; i++) {
                char ruby = rubys[i];
                g.setFont(rubyFont);
                int asc = g.getFontMetrics().getAscent();
                int desc = g.getFontMetrics().getDescent();
                int width = g.getFontMetrics().charWidth(rubys[i]);
                Rectangle rectangle1 = orientation.isHorizonal()
                    ? new Rectangle(rubyBounds.x + w * (orientation.isLeftToRight() ? i : rubys.length - i - 1), rubyBounds.y + ((h - rubyFont.getSize()) / 2) * (orientation.isTopToButtom() ? 1 : -1), w, rubyBounds.height)
                    : new Rectangle(rubyBounds.x + ((w - rubyFont.getSize()) / 2) * (orientation.isLeftToRight() ? 1 : -1), rubyBounds.y + h * (orientation.isTopToButtom() ? i : rubys.length - i - 1), rubyBounds.width, h);
                int x1 = rectangle1.x + (rectangle1.width - width) / 2;
                int y1 = rectangle1.y + ((rectangle1.height + asc) - desc) / 2;
                int x2 = rectangle1.x + rectangle1.width / 2;
                int y2 = rectangle1.y + rectangle1.height / 2;
                double theta = 0.0D;
                if (!orientation.isHorizonal() && CharacterUtil.isToRotate(ruby)) {
                    theta = Math.PI / 2;
                    if (!orientation.isTopToButtom())
                        theta = 0.0D - theta;
                }
                if (is2d && theta != 0.0D)
                    g2.rotate(theta, x2, y2);
                g.drawString(String.valueOf(ruby), x1, y1);
                if (DEBUG.isDebug()) {
                    Color color = g.getColor();
                    g.setColor(new Color(153, 153, 255));
                    g.drawRect(x1, y1 - asc, width, asc);
                    g.setColor(new Color(255, 153, 153));
                    g.drawRect(x1, y1, width, desc);
                    g.setColor(color);
                }
                if (is2d && theta != 0.0D)
                    g2.rotate(0.0D - theta, x2, y2);
            }

            g.setFont(originalFont);
        }
    }

    public char getMain() {
        return main;
    }

    public void setMain(char main) {
        this.main = main;
    }

    public char[] getRubys() {
        return rubys;
    }

    public void setRubys(char[] rubys) {
        this.rubys = rubys;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public String toString() {
        return super.toString() +
            "[main=" + main +
            ",ruby=" + (rubys != null ? String.valueOf(rubys) : null) +
            ",font=" + font + "]";
    }

    public String getText() {
        StringBuilder sb = new StringBuilder();
        sb.append(main);
        if (rubys != null && rubys.length > 0)
            sb.append('（').append(rubys).append('）');
        return sb.toString();
    }

    private char main;
    private char[] rubys;
    private Font font;
}
