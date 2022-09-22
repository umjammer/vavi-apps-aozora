/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.viewer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.data.AozoraComment;
import com.soso.sgui.SGUIUtil;
import com.soso.sgui.letter.SLetterCell;
import com.soso.sgui.letter.SLetterCellFactory;
import com.soso.sgui.letter.SLetterConstraint;
import com.soso.sgui.letter.SLetterPane;
import com.soso.sgui.letter.SLetterPaneObserverHelper;


class AozoraCommentNoteDecorator extends AozoraCommentDecorator {

    private class AozoraCommentLetterPane extends SLetterPane {

        public void setDelta(int delta) {
            this.delta = delta;
            super.setRowRange(delta);
            super.setColRange(delta);
        }

        public int getDelta() {
            return delta;
        }

        @Override
        public void setRowRange(int rowRange) {
            setDelta(rowRange);
        }

        @Override
        public void setColRange(int colRange) {
            setDelta(colRange);
        }

        @Override
        public void paintCells(Graphics2D g) {
            super.paintCells(g);
        }

        private AozoraComment getComment() {
            return AozoraCommentNoteDecorator.this.getComment();
        }

        private int delta;

        private AozoraCommentLetterPane() {
            super(SLetterConstraint.ORIENTATION.TBRL);
        }
    }

    AozoraCommentNoteDecorator(AozoraContext context, AozoraComment comment, SLetterPane textPane, SLetterCell firstCell) {
        super(context, comment, textPane, firstCell);
        commentPane = null;
        getTextPane().addObserver(new SLetterPaneObserverHelper() {
            @Override
            public void cellRemoved(SLetterCell cell) {
                checkCellRemoved(cell);
            }
        });
    }

    private void checkCellRemoved(SLetterCell cell) {
        if (cell == getFirstCell())
            removeCommentPane();
    }

    @Override
    public void decorateBeforePaint(Graphics2D g, SLetterCell cell, Rectangle cellBounds, Rectangle rubyBounds) {
    }

    @Override
    public void removeDecoration(SLetterCell cell) {
        if (cell == getFirstCell())
            removeCommentPane();
    }

    @Override
    public void decorateAfterPaint(Graphics2D g, SLetterCell cell, Rectangle cellBounds, Rectangle rubyBounds) {
        Color color0 = g.getColor();
        Composite composite0 = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7F));
        if (cell == getFirstCell()) {
            int delta = getTextPane().getRowSpace() / 2;
            delta = Math.min(delta, getTextPane().getRowRange());
            delta = Math.max(delta, 6);
            if (getCommentPane().getDelta() != delta)
                getCommentPane().setDelta(delta);
            JInternalFrame iframe = SGUIUtil.getParentInstanceOf(getTextPane(), JInternalFrame.class);
            if (iframe != null) {
                Rectangle parentBounds = SGUIUtil.getBoundsRecursive(iframe.getContentPane(), getTextPane());
                getCommentPane().setBounds(new Rectangle(
                     new Point((parentBounds.x + cellBounds.x + cellBounds.width + delta) - getCommentPane().getWidth() / 3,
                               parentBounds.y + cellBounds.y + cellBounds.height / 2 + delta + delta / 4),
                     getCommentPane().getPreferredSize()));
                boolean behind = false;
                int position = 0;
                for (Component comp : iframe.getLayeredPane().getComponents()) {
                    if (comp instanceof AozoraCommentLetterPane) {
                        AozoraCommentLetterPane anotherCommentPane = (AozoraCommentLetterPane) comp;
                        if (JLayeredPane.getLayer(anotherCommentPane) == 70) {
                            if (anotherCommentPane.getComment().getTimestamp() <= getComment().getTimestamp())
                                break;
                            position++;
                            if (anotherCommentPane.getBounds().intersects(getCommentPane().getBounds()))
                                behind = true;
                        }
                    }
                }

                if (getCommentPane().getParent() == null) {
                    iframe.getLayeredPane().setLayer(getCommentPane(), 70);
                    iframe.getLayeredPane().add(getCommentPane());
                    iframe.getLayeredPane().setPosition(getCommentPane(), position);
                }
                if (behind)
                    getCommentPane().setForeground(getEarlierForegroundColor());
                else
                    getCommentPane().setForeground(getForegroundColor());
                g.setColor(getCommentPane().getForeground());
                g.drawLine(cellBounds.x + cellBounds.width,
                           cellBounds.y + cellBounds.height / 2,
                           cellBounds.x + cellBounds.width + delta,
                           cellBounds.y + cellBounds.height / 2 + delta / 2);
                g.drawLine(cellBounds.x + cellBounds.width + delta,
                           cellBounds.y + cellBounds.height / 2 + delta / 2,
                           cellBounds.x + cellBounds.width + delta,
                           cellBounds.y + cellBounds.height / 2 + delta);
            }
        }
        g.setColor(getCommentPane().getForeground());
        g.drawLine((cellBounds.x + cellBounds.width) - 1,
                   cellBounds.y,
                   (cellBounds.x + cellBounds.width) - 1,
                   (cellBounds.y + cellBounds.height) - 1);
        if (composite0 != null)
            g.setComposite(composite0);
        if (color0 != null)
            g.setColor(color0);
    }

    private float getFreshnessRatio() {
        long ts = getComment().getTimestamp();
        long now = System.currentTimeMillis();
        long limit = now - COMMENT_DISAPPEAR_LIMIT_MILLIS;
        double ratio = (ts - limit) / 2592000000D;
        return Math.min(1.0F, Math.max(0.0F, (float) ratio));
    }

    private Color getLatestForegroundColor() {
        return new Color(SGUIUtil.compromiseRGB(getTextPane().getForeground().getRGB(), -39424, 0.1F));
    }

    private Color getEarlierForegroundColor() {
        return new Color(SGUIUtil.compromiseRGB(getLatestForegroundColor().getRGB(), getTextPane().getBackground().getRGB(), 0.9F));
    }

    private Color getForegroundColor() {
        return new Color(SGUIUtil.compromiseRGB(getEarlierForegroundColor().getRGB(), getLatestForegroundColor().getRGB(), getFreshnessRatio()));
    }

    private AozoraCommentLetterPane getCommentPane() {
        if (commentPane == null) {
            commentPane = new AozoraCommentLetterPane();
            char[] commentData = getComment().getData().toCharArray();
            commentPane.setBackground(getTextPane().getBackground());
            commentPane.setOpaque(false);
            commentPane.setFont(getTextPane().getFont());
            commentPane.setRowColCountChangable(false);
            commentPane.setRowCount(1);
            commentPane.setColCount(commentData.length);
            commentPane.setRowSpace(0);
            commentPane.setColSpace(0);
            commentPane.setFontRangeRatio(1.0F);
            commentPane.setFontSizeChangable(false);
            commentPane.setLetterBorderRendarer(null);
            for (char commentDatum : commentData)
                commentPane.addCell(SLetterCellFactory.getInstance().createGlyphCell(commentDatum));

        }
        return commentPane;
    }

    private void removeCommentPane() {
        if (commentPane != null) {
            Container parent = commentPane.getParent();
            if (parent != null) {
                parent.remove(commentPane);
                parent.repaint();
            }
        }
    }

    private static final long COMMENT_DISAPPEAR_LIMIT_MILLIS = 0x9a7ec800L;
    private AozoraCommentLetterPane commentPane;
}
