/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.viewer;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.data.AozoraComment;
import com.soso.sgui.SGUIUtil;
import com.soso.sgui.letter.SLetterCell;
import com.soso.sgui.letter.SLetterCellFactory;
import com.soso.sgui.letter.SLetterConstraint;
import com.soso.sgui.letter.SLetterPane;
import com.soso.sgui.letter.SLetterPaneObserverHelper;


class AozoraCommentBalloonDecorator extends AozoraCommentDecorator {

    private class AozoraCommentBalloonPane extends AozoraBalloonPane {

        @Override
        protected JComponent initContentPane() {
            JPanel contentPane = new JPanel();
            contentPane.setLayout(new BorderLayout());
            contentPane.setBackground(AozoraEnv.COMMENT_BALLONE_BACKGROUND_COLOR);
            JPanel northPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
            northPane.setBorder(new EmptyBorder(1, 0, 3, 0));
            northPane.setOpaque(false);
            String commentator = getComment().getCommentator();
            if (commentator == null || commentator.length() == 0)
                commentator = AozoraComment.NANASHISAN;
            JLabel commentatorLabel = new JLabel(commentator);
            commentatorLabel.setForeground(Color.DARK_GRAY);
            northPane.add(commentatorLabel);
            contentPane.add(northPane, BorderLayout.NORTH);
            SLetterPane letterPane = SLetterPane.newInstance(SLetterConstraint.ORIENTATION.LRTB);
            letterPane.setColCount(1);
            letterPane.setRowCount(1);
            letterPane.setLetterBorderRendarer(null);
            letterPane.setOpaque(false);
            letterPane.setRowColCountChangable(false);
            letterPane.setRowSpace(getTextPane().getFont().getSize() / 3);
            letterPane.setColSpace(0);
            letterPane.setFontRangeRatio(1.0F);
            letterPane.setFontSizeChangable(true);
            letterPane.setFont(getTextPane().getFont());
            for (char c : getComment().getData().toCharArray()) {
                SLetterCell cell = SLetterCellFactory.getInstance().createGlyphCell(c);
                while (!letterPane.addCell(cell)) {
                    if (letterPane.getColCount() < 12)
                        letterPane.setColCount(letterPane.getColCount() + 1);
                    else
                        letterPane.setRowCount(letterPane.getRowCount() + 1);
                }
            }

            contentPane.add(letterPane, BorderLayout.CENTER);
            JPanel southPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 1, 1));
            southPane.setBorder(new EmptyBorder(3, 0, 1, 0));
            southPane.setOpaque(false);
            JLabel timestampLabel = new JLabel(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date(getComment().getTimestamp())));
            timestampLabel.setForeground(Color.GRAY);
            southPane.add(timestampLabel);
            contentPane.add(southPane, BorderLayout.SOUTH);
            return contentPane;
        }

        private AozoraComment getComment() {
            return AozoraCommentBalloonDecorator.this.getComment();
        }

        private AozoraCommentBalloonPane(AozoraContext context) {
            super(context);
        }
    }

    protected AozoraCommentBalloonDecorator(AozoraContext context, AozoraComment comment, SLetterPane textPane, SLetterCell firstCell) {
        super(context, comment, textPane, firstCell);
        getTextPane().addObserver(new SLetterPaneObserverHelper() {
            @Override
            public void cellRemoved(SLetterCell cell) {
                checkCellRemoved(cell);
            }
        });
    }

    private void checkCellRemoved(SLetterCell cell) {
        if (cell == getFirstCell())
            removeBalloonComment();
    }

    @Override
    public void decorateBeforePaint(Graphics2D g, SLetterCell cell, Rectangle cellBounds, Rectangle rubyBounds) {
    }

    @Override
    public void removeDecoration(SLetterCell cell) {
        if (cell == getFirstCell())
            removeBalloonComment();
    }

    @Override
    public void decorateAfterPaint(Graphics2D g, SLetterCell cell, Rectangle cellBounds, Rectangle rubyBounds) {
        Color color0 = g.getColor();
        Composite composite0 = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(3, 0.7F));
        g.setColor(Color.BLUE);

        switch (getTextPane().getOrientation()) {
        case TBRL:
            g.drawLine((cellBounds.x + cellBounds.width) - 1, cellBounds.y, (cellBounds.x + cellBounds.width) - 1, (cellBounds.y + cellBounds.height) - 1);
            break;
        case TBLR:
            g.drawLine(cellBounds.x, cellBounds.y, cellBounds.x, (cellBounds.y + cellBounds.height) - 1);
            break;
        default:
            g.drawLine(cellBounds.x, (cellBounds.y + cellBounds.height) - 1, (cellBounds.x + cellBounds.width) - 1, (cellBounds.y + cellBounds.height) - 1);
            break;
        }
        addBalloonComment(cell, cellBounds);
        if (composite0 != null)
            g.setComposite(composite0);
        if (color0 != null)
            g.setColor(color0);
    }

    private void addBalloonComment(SLetterCell cell, Rectangle cellBounds) {
        if (cell == getFirstCell()) {
            JDesktopPane desktop = getAzContext().getDesktopPane();
            JInternalFrame iframe = SGUIUtil.getParentInstanceOf(getTextPane(), JInternalFrame.class);
            boolean visible = false;
            if (iframe != null && iframe.getParent() == desktop && desktop.getPosition(iframe) == 0)
                visible = true;
            if (!visible) {
                if (commentPane != null)
                    commentPane.setVisible(false);
                return;
            }
            if (commentPane == null) {
                commentPane = new AozoraCommentBalloonPane(getAzContext());
                iframe.addInternalFrameListener(new InternalFrameAdapter() {
                    public void internalFrameIconified(InternalFrameEvent e) {
                        commentPane.setVisible(false);
                    }
                });
            }
            Rectangle textPaneBounds = SGUIUtil.getBoundsRecursive(desktop, getTextPane());
            Point lipPoint = new Point(textPaneBounds.x + cellBounds.x + cellBounds.width, textPaneBounds.y + cellBounds.y + cellBounds.height / 2);
            if (commentPane.getParent() == null) {
                int position = 0;
                for (Component comp : desktop.getComponents()) {
                    if (comp instanceof AozoraCommentBalloonPane) {
                        AozoraCommentBalloonPane anotherCommentPane = (AozoraCommentBalloonPane) comp;
                        if (JLayeredPane.getLayer(anotherCommentPane) == 70) {
                            if (anotherCommentPane.getComment().getTimestamp() <= getComment().getTimestamp())
                                break;
                            position++;
                        }
                    }
                }

                desktop.setLayer(commentPane, 70);
                desktop.add(commentPane);
                desktop.setPosition(commentPane, position);
                commentPane.setBasePoint(new Point(lipPoint.x + 10, lipPoint.y + 15));
            }
            commentPane.setLipPoint(lipPoint);
            commentPane.revalidate();
            commentPane.setVisible(true);
        }
    }

    private void removeBalloonComment() {
        if (commentPane != null) {
            Container parent = commentPane.getParent();
            if (parent != null) {
                parent.remove(commentPane);
                parent.repaint();
            }
        }
    }

    void moveToFront() {
        if (commentPane != null)
            commentPane.moveToFront();
    }

    private AozoraCommentBalloonPane commentPane;
}
