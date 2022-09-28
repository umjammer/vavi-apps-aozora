/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.letter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import javax.swing.UIManager;


public final class SLetterDefaults {

    public static class DefaultLetterBorderRenderer implements SLetterBorderRenderer {

        protected void init() {
            setRowBorderColor(ROW_BORDER_COLOR);
            setCellBorderColor(CELL_BORDER_COLOR);
            setCellCenterLineColor(CELL_CENTER_LINE_COLOR);
            setRowBorderStroke(ROW_BORDER_STROKE);
            setCellBorderStroke(CELL_BORDER_STROKE);
            setCellCenterLineStroke(CELL_CENTER_LINE_STROKE);
        }

        public Color getRowBorderColor() {
            return rowBorderColor;
        }

        public void setRowBorderColor(Color color) {
            rowBorderColor = color;
        }

        public Color getCellBorderColor() {
            return cellBorderColor;
        }

        public void setCellBorderColor(Color color) {
            cellBorderColor = color;
        }

        public Color getCellCenterLineColor() {
            return cellCenterLineColor;
        }

        public void setCellCenterLineColor(Color color) {
            cellCenterLineColor = color;
        }

        public Stroke getRowBorderStroke() {
            return rowBorderStroke;
        }

        public void setRowBorderStroke(Stroke stroke) {
            rowBorderStroke = stroke;
        }

        public Stroke getCellBorderStroke() {
            return cellBorderStroke;
        }

        public void setCellBorderStroke(Stroke stroke) {
            cellBorderStroke = stroke;
        }

        public Stroke getCellCenterLineStroke() {
            return cellCenterLineStroke;
        }

        public void setCellCenterLineStroke(Stroke stroke) {
            cellCenterLineStroke = stroke;
        }

        public SLetterConstraint.ORIENTATION getOrientation() {
            return orientation;
        }

        public void setOrientation(SLetterConstraint.ORIENTATION orientation) {
            if (orientation == null) {
                throw new IllegalArgumentException("ORIENTATION cannot be null");
            }
            this.orientation = orientation;
        }

        public void paintRowBorder(Graphics2D g, Rectangle rowBounds) {
            Color color = g.getColor();
            Stroke stroke = g.getStroke();
            g.setColor(getRowBorderColor());
            g.setStroke(getRowBorderStroke());
            if (getOrientation().isHorizonal()) {
                g.drawLine(rowBounds.x, rowBounds.y, rowBounds.x + rowBounds.width, rowBounds.y);
                g.drawLine(rowBounds.x, rowBounds.y + rowBounds.height, rowBounds.x + rowBounds.width, rowBounds.y + rowBounds.height);
            } else {
                g.drawLine(rowBounds.x, rowBounds.y, rowBounds.x, rowBounds.y + rowBounds.height);
                g.drawLine(rowBounds.x + rowBounds.width, rowBounds.y, rowBounds.x + rowBounds.width, rowBounds.y + rowBounds.height);
            }
            g.setColor(color);
            g.setStroke(stroke);
        }

        public void paintCellBorder(Graphics2D g, Rectangle cellBounds) {
            Color color = g.getColor();
            Stroke stroke = g.getStroke();
            g.setColor(getCellBorderColor());
            g.setStroke(getCellBorderStroke());
            if (getOrientation().isHorizonal()) {
                g.drawLine(cellBounds.x, cellBounds.y, cellBounds.x, cellBounds.y + cellBounds.height);
                g.drawLine(cellBounds.x + cellBounds.width, cellBounds.y, cellBounds.x + cellBounds.width, cellBounds.y + cellBounds.height);
            } else {
                g.drawLine(cellBounds.x, cellBounds.y, cellBounds.x + cellBounds.width, cellBounds.y);
                g.drawLine(cellBounds.x, cellBounds.y + cellBounds.height, cellBounds.x + cellBounds.width, cellBounds.y + cellBounds.height);
            }
            g.setColor(getCellCenterLineColor());
            g.setStroke(getCellCenterLineStroke());
            g.drawLine(cellBounds.x + cellBounds.width / 2, cellBounds.y, cellBounds.x + cellBounds.width / 2, cellBounds.y + cellBounds.height);
            g.drawLine(cellBounds.x, cellBounds.y + cellBounds.height / 2, cellBounds.x + cellBounds.width, cellBounds.y + cellBounds.height / 2);
            g.setColor(color);
            g.setStroke(stroke);
        }

        protected static final Color ROW_BORDER_COLOR = new Color(153, 102, 51);
        protected static final Color CELL_BORDER_COLOR = new Color(170, 102, 51);
        protected static final Color CELL_CENTER_LINE_COLOR = new Color(170, 136, 85);
        protected static final Stroke ROW_BORDER_STROKE = new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10F, null, 0.0F);
        protected static final Stroke CELL_BORDER_STROKE = new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10F, null, 0.0F);
        protected static final Stroke CELL_CENTER_LINE_STROKE = new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10F, new float[] { 1.0F, 1.0F }, 0.0F);

        private SLetterConstraint.ORIENTATION orientation;
        private Color rowBorderColor;
        private Color cellBorderColor;
        private Color cellCenterLineColor;
        private Stroke rowBorderStroke;
        private Stroke cellBorderStroke;
        private Stroke cellCenterLineStroke;

        public DefaultLetterBorderRenderer(SLetterConstraint.ORIENTATION orientation) {
            if (orientation == null) {
                throw new IllegalArgumentException("ORIENTATION cannot be null");
            }
            this.orientation = orientation;
            init();
        }
    }

    private SLetterDefaults() {
    }

    public static final int ROW_SPAN = 1;
    public static final int COL_SPAN = 1;
    public static final int ROW_COUNT = 40;
    public static final int COL_COUNT = 40;
    public static final int ROW_RANGE = 40;
    public static final int COL_RANGE = 40;
    public static final float FONT_RANGE_RASIO = 0.7861513f;
    public static final int ROW_SPACE = 10;
    public static final int COL_SPACE = 0;

    public static final Color BG_COLOR = Color.WHITE;
    public static final Color FG_COLOR = Color.BLACK;
    public static final Color SELECTED_BG_COLOR = UIManager.getColor("TextArea.selectionBackground");
    public static final Color SELECTED_FG_COLOR = UIManager.getColor("TextArea.selectionForeground");
}
