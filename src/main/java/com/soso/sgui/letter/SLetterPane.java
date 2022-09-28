/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.letter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.plaf.FontUIResource;


/**
 * Represents a page.
 */
public class SLetterPane extends JPanel {

    static Logger logger = Logger.getLogger(SLetterPane.class.getName());

    protected static class OverlayHolderCell extends SLetterCell {
        @Override
        public void paintCell(Graphics2D g, Rectangle cellBounds) {
            for (SLetterCell cell : cells) {
                cell.paintCell(g, cellBounds);
            }
        }

        @Override
        public void paintRuby(Graphics2D g, Rectangle rubyBounds) {
            for (SLetterCell cell : cells) {
                cell.paintRuby(g, rubyBounds);
            }
        }

        protected boolean addChildCell(SLetterCell cell) {
            if (cell == null)
                throw new IllegalArgumentException("Cell cannot be null");
            return cells.add(cell);
        }

        protected SLetterCell[] getChildCells() {
            return cells.toArray(new SLetterCell[0]);
        }

        protected boolean removeChildCell(SLetterCell cell) {
            return cells.remove(cell);
        }

        @Override
        public boolean isConstraintSet(SLetterConstraint constraint) {
            for (SLetterCell cell : cells) {
                if (cell.isConstraintSet(constraint))
                    return true;
            }
            return super.isConstraintSet(constraint);
        }

        @Override
        public void addConstraint(SLetterConstraint constraint) {
            for (SLetterCell cell : cells) {
                cell.addConstraint(constraint);
            }
            super.addConstraint(constraint);
        }

        @Override
        protected void setParent(SLetterPane pane) {
            for (SLetterCell cell : cells) {
                cell.setParent(pane);
            }
            super.setParent(pane);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(super.toString());
            sb.append("[");
            if (cells != null) {
                for (SLetterCell cell : cells) {
                    sb.append(cell);
                }
            } else {
                sb.append("null");
            }
            sb.append("]");
            return sb.toString();
        }

        @Override
        public String getText() {
            StringBuilder sb = new StringBuilder();
            for (SLetterCell cell : cells) {
                sb.append(cell.getText());
            }

            return sb.toString();
        }

        private List<SLetterCell> cells;

        protected OverlayHolderCell(SLetterCell... args) {
            cells = new ArrayList<>();
            for (SLetterCell cell : args) {
                if (cell == null)
                    throw new IllegalArgumentException("Cell cannot be null");
                cells.add(cell);
            }
        }
    }

    /** letter location */
    protected static class MatrixIndex implements Comparable<MatrixIndex> {

        protected int row() {
            return row;
        }

        protected int col() {
            return col;
        }

        @Override
        public String toString() {
            return super.toString() + "[row=" + row() + ",col=" + col() + "]";
        }

        @Override
        public int compareTo(MatrixIndex matrix) {
            if (row > matrix.row)
                return 1;
            if (row < matrix.row)
                return -1;
            if (col > matrix.col)
                return 1;
            return col >= matrix.col ? 0 : -1;
        }

        private int row;
        private int col;

        protected MatrixIndex(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    /** text selection */
    protected static class SelectionModel {

        protected void init() {
            start = null;
            end = null;
            selectionFinished = false;
        }

        protected boolean isSelected() {
            return start != null && end != null;
        }

        protected boolean isSelectionFinished() {
            return selectionFinished;
        }

        protected boolean isContains(MatrixIndex matrix) {
            if (!isSelected())
                return false;
            int s = start.compareTo(matrix);
            int e = end.compareTo(matrix);
            if (s < 0 && e < 0)
                return false;
            return s <= 0 || e <= 0;
        }

        protected MatrixIndex getSelectionStart() {
            return start;
        }

        protected MatrixIndex getSelectionEnd() {
            return end;
        }

        protected void selectionStart(MatrixIndex index) {
logger.finer("Selection|Start|" + index);
            start = index;
            end = null;
            selectionFinished = false;
        }

        protected void selectionEndUpdate(MatrixIndex index) {
            if (!isSelectionFinished()) {
logger.finer("Selection|EndUpdate|" + index);
                end = index;
            }
        }

        protected void selectionFinish() {
logger.finer("Selection|Finished|" + start + "|" + end);
            selectionFinished = true;
        }

        private MatrixIndex start;
        private MatrixIndex end;
        private boolean selectionFinished;

        protected SelectionModel() {
            init();
        }

        @Override
        public String toString() {
            return "SelectionModel{" +
                    "start=" + start +
                    ", end=" + end +
                    '}';
        }
    }

    public interface MenuItemProducer {

        JMenuItem produceMenuItem(Point point, SLetterCell[] cells, boolean enabled);
    }

    public static SLetterPane newInstance(SLetterConstraint.ORIENTATION orientation) {
        return new SLetterPane(orientation);
    }

    protected SLetterPane(SLetterConstraint.ORIENTATION orientation) {
        if (orientation == null) {
            throw new IllegalArgumentException("ORIENTATION cannot be null");
        }
        orientation_ = orientation;
        init(true);
    }

    protected void init(boolean flag) {
        if (flag || matrix_ == null) {
            synchronized (this) {
                if (flag || matrix_ == null) {
                    support = new SLetterPaneObserverSupport();
                    matrix_ = new SLetterCell[SLetterDefaults.ROW_COUNT][SLetterDefaults.COL_COUNT];
                    setRowRange(SLetterDefaults.ROW_RANGE);
                    setColRange(SLetterDefaults.COL_RANGE);
                    setFontRangeRatio(SLetterDefaults.FONT_RANGE_RASIO);
                    setRowSpace(SLetterDefaults.ROW_SPACE);
                    setColSpace(SLetterDefaults.COL_SPACE);
                    setBackground(SLetterDefaults.BG_COLOR);
                    setForeground(SLetterDefaults.FG_COLOR);
                    setSelectionColor(SLetterDefaults.SELECTED_BG_COLOR);
                    setSelectedTextColor(SLetterDefaults.SELECTED_FG_COLOR);
                    setLetterBorderRendarer(new SLetterDefaults.DefaultLetterBorderRenderer(getOrientation()));
                    point_ = new Point();
                    selectionModel = new SelectionModel();
                    addComponentListener(componentListener);
                    addPropertyChangeListener(propertyChangeListener);
                    addMouseListener(mouseListener);
                    addMouseMotionListener(mouseMotionListener);
                    addKeyListener(keyListener);
                    producers = new ArrayList<>();
                    addMenuItemProducer(getMenuItemProducer());
                }
            }
        }
    }

    private final ComponentListener componentListener = new ComponentAdapter() {
        @Override public void componentResized(ComponentEvent event) {
            SLetterPane.this.componentResized();
        }
    };

    protected void componentResized() {
        if (isRowColCountChangable()) {
logger.finer("SIZE CHANGED EVENT");
            setSize(super.getSize());
        }
        updateLocation();
    }

    private final PropertyChangeListener propertyChangeListener = event -> {
        if (isFontSizeChangable() && "font".equals(event.getPropertyName()))
            fontChanged();
    };

    protected void fontChanged() {
        if (isFontSizeChangable()) {
logger.fine("FONT CHANGED EVENT: getFontRangeRatio(): " + getFontRangeRatio());
            Font font = super.getFont();
            if (font != null) {
                int range = Math.round(Math.max(font.getSize2D(), 16) / getFontRangeRatio());
logger.fine("font: " + font + ", font.getSize2D(): " + font.getSize2D() + ", range: " + range);
                setRowRange(range);
                setColRange(range);
                Dimension size = super.getSize();
                if (size != null && size.width > 0 && size.height > 0)
                    setRowColCountBySize(size.width, size.height);
            }
        }
    }

    private final MouseListener mouseListener = new MouseAdapter() {
        @Override public void mouseClicked(MouseEvent event) {
            if (event.getButton() == MouseEvent.BUTTON1) {
                if (getMaximizedImageCell() != null) {
                    setMaximizedImageCell(null);
                } else {
                    SLetterCell[] cells = getCell(event.getPoint());
                    if (cells.length == 1) {
                        if (cells[0] instanceof SLetterImageCell) {
                            SLetterImageCell imageCell = (SLetterImageCell) cells[0];
                            if (imageCell.isMagnifyable()) {
                                setMaximizedImageCell(imageCell);
                            }
                        }
                    }
                }
            }
        }
        @Override public void mousePressed(MouseEvent event) {
            if (event.getButton() == MouseEvent.BUTTON3) {
                showPopupMenu(event.getPoint());
            } else if (getMaximizedImageCell() == null)
                selectionStart(event.getPoint());
        }
        @Override public void mouseReleased(MouseEvent event) {
            selectionFinish();
        }
    };

    private final MouseMotionListener mouseMotionListener = new MouseMotionAdapter() {
        @Override public void mouseDragged(MouseEvent event) {
            selectionEndUpdate(event.getPoint());
        }
    };

    protected void selectionStart(Point point) {
        getSelectionModel().selectionStart(getRowColAtPoint(point));
        updateSelection();
    }

    protected void selectionEndUpdate(Point point) {
        getSelectionModel().selectionEndUpdate(getRowColAtPoint(point));
        updateSelection();
        requestFocusInWindow();
    }

    private void updateSelection() {
        for (int row = 0; row < matrix_.length; row++) {
            for (int col = 0; col < matrix_[row].length; col++) {
                SLetterCell cell = matrix_[row][col];
                if (cell != null) {
                    MatrixIndex matrix = new MatrixIndex(row, col);
                    if (cell instanceof OverlayHolderCell) {
                        for (SLetterCell child : ((OverlayHolderCell) cell).getChildCells()) {
                            if (child != null) {
                                if (getSelectionModel().isContains(matrix))
                                    child.addConstraint(SLetterConstraint.SELECTION.SELECTED);
                                else
                                    child.removeConstraint(SLetterConstraint.SELECTION.SELECTED);
                            }
                        }
                    }
                    if (getSelectionModel().isContains(matrix))
                        cell.addConstraint(SLetterConstraint.SELECTION.SELECTED);
                    else
                        cell.removeConstraint(SLetterConstraint.SELECTION.SELECTED);
                }
            }
        }

        repaint();
    }

    protected void selectionFinish() {
        getSelectionModel().selectionFinish();
        updateSelection();
    }

    protected void maximizeImage(SLetterImageCell cell) {
    }

    protected SLetterImageCell getMaximizedImageCell() {
        return imageCell;
    }

    protected void setMaximizedImageCell(SLetterImageCell cell) {
        imageCell = cell;
        repaint();
    }

    private final KeyListener keyListener = new KeyAdapter() {
        public void keyPressed(KeyEvent event) {
            if (event.isControlDown() && event.getKeyCode() == KeyEvent.VK_C)
                copyToClipBoard();
        }
    };

    private MenuItemProducer getMenuItemProducer() {
        return new MenuItemProducer() {
            public JMenuItem produceMenuItem(Point point, SLetterCell[] cells, boolean enabled) {
                menuItem.setEnabled(enabled);
                return menuItem;
            }

            JMenuItem menuItem = new JMenuItem(new AbstractAction("選択範囲のコピー") {
                public void actionPerformed(ActionEvent event) {
                    copyToClipBoard();
                }
            });
        };
    }

    protected void showPopupMenu(Point point) {
        JPopupMenu popupMenu = new JPopupMenu("メニュー");
        boolean selected = false;
        SLetterCell[] cells = getCell(point);
        for (SLetterCell cell : cells) {
            if (cell != null && cell.isConstraintSet(SLetterConstraint.SELECTION.SELECTED))
                selected = true;
        }

        for (MenuItemProducer producer : producers) {
            JMenuItem menuItem = producer.produceMenuItem(point, cells, selected);
            if (menuItem != null)
                popupMenu.add(menuItem);
        }
        if (popupMenu.getComponentCount() > 0)
            popupMenu.show(this, point.x, point.y);
    }

    public void addMenuItemProducer(MenuItemProducer producer) {
        producers.add(producer);
    }

    protected void copyToClipBoard() {
logger.fine("COPY|Start");
        StringBuilder sb = new StringBuilder();
        for (SLetterCell cell : getSelectedCells()) {
            sb.append(cell.getText());
        }
        StringSelection selection = new StringSelection(sb.toString());
        getToolkit().getSystemClipboard().setContents(selection, selection);
    }

    public void addObserver(SLetterPaneObserver observer) {
        support.addObserver(observer);
    }

    public void removeObserver(SLetterPaneObserver observer) {
        support.removeObserver(observer);
    }

    public int getRowCount() {
        init(false);
        return matrix_.length;
    }

    public List<SLetterCell> setRowCount(int rowCount) {
        if (rowCount <= 0)
            throw new IllegalArgumentException("must be positive");
        return setRowColCount(rowCount, getColCount());
    }

    public int getColCount() {
        init(false);
        return matrix_[0].length;
    }

    public List<SLetterCell> setColCount(int colCount) {
        if (colCount <= 0)
            throw new IllegalArgumentException("must be positive");
        return setRowColCount(getRowCount(), colCount);
    }

    protected List<SLetterCell> setRowColCount(int row, int col) {
        List<SLetterCell> result = new ArrayList<>();
        int rs = getRowCount();
        int cs = getColCount();
        if (row > 0 && col > 0 && (rs != row || cs != col)) {
            SLetterCell[][] matrix = matrix_;
            matrix_ = new SLetterCell[row][col];
            boolean flag = true;
            for (SLetterCell[] cells : matrix) {
                for (SLetterCell cell : cells) {
                    if (cell != null) {
                        if (cell instanceof OverlayHolderCell) {
                            for (SLetterCell cell1 : ((OverlayHolderCell) cell).getChildCells()) {
                                if (cell1 != null) {
                                    if (flag)
                                        flag = addCell(cell1);
                                    if (!flag) {
                                        fireCellRemoved_b(cell1);
                                        result.add(cell1);
                                    }
                                }
                            }
                        } else {
                            if (flag)
                                flag = addCell(cell);
                            if (!flag) {
                                fireCellRemoved_b(cell);
                                result.add(cell);
                            }
                        }
                    }
                }
            }

            getSelectionModel().init();
            if (rs != row)
                support.rowCountChanged(rs, row);
            if (cs != col)
                support.colCountChanged(cs, col);
            syncSize();
        }
        return result;
    }

    public int getRowRange() {
        return rowRange;
    }

    public void setRowRange(int rowRange) {
        if (rowRange < 0)
            throw new IllegalArgumentException("must be positive");
        if (rowRange == 0) {
new Exception().printStackTrace();
            logger.warning("rowRange is 0");
        }
        int oldValue = this.rowRange;
        if (oldValue != rowRange) {
            this.rowRange = rowRange;
            syncSize();
            support.rowRangeChanged(oldValue, rowRange);
        }
    }

    public int getColRange() {
        return colRange;
    }

    public void setColRange(int colRange) {
        if (colRange < 0)
            throw new IllegalArgumentException("must be positive");
        int oldValue = getColRange();
        if (oldValue != colRange) {
            this.colRange = colRange;
            syncSize();
            support.colRangeChanged(oldValue, colRange);
        }
    }

    public int getRowSpace() {
        return rowSpace;
    }

    public void setFontRangeRatio(float ratio) {
        if (ratio <= 0.0F)
            throw new IllegalArgumentException("must be positive");
        float oldValue = getFontRangeRatio();
        if (oldValue != ratio) {
            fontRangeRatio = ratio;
            fontChanged();
            syncSize();
            support.fontRangeRatioChanged(oldValue, ratio);
        }
    }

    public float getFontRangeRatio() {
        return fontRangeRatio;
    }

    public void setRowSpace(int rowSpace) {
        if (rowSpace < 0)
            throw new IllegalArgumentException("must be positive");
        int oldValue = getRowSpace();
        if (oldValue != rowSpace) {
            this.rowSpace = rowSpace;
            syncSize();
            support.rowSpaceChanged(oldValue, rowSpace);
        }
    }

    public int getColSpace() {
        return colSpace;
    }

    public void setColSpace(int colSpace) {
        if (colSpace < 0)
            throw new IllegalArgumentException("must be positive");
        int oldValue = getColSpace();
        if (oldValue != colSpace) {
            this.colSpace = colSpace;
            syncSize();
            support.colSpaceChanged(oldValue, colSpace);
        }
    }

    public Color getSelectionColor() {
        return selectionColor;
    }

    public void setSelectionColor(Color color) {
        Color oldValue = selectionColor;
        selectionColor = color;
        firePropertyChange("selectionColor", oldValue, color);
    }

    public Color getSelectedTextColor() {
        return selectedTextColor;
    }

    public void setSelectedTextColor(Color color) {
        Color oldValue = selectedTextColor;
        selectedTextColor = color;
        firePropertyChange("selectedTextColor", oldValue, color);
    }

    public SLetterBorderRenderer getLetterBorderRendarer() {
        return letterBorderRendarer;
    }

    public void setLetterBorderRendarer(SLetterBorderRenderer rendarer) {
        SLetterBorderRenderer oldValue = getLetterBorderRendarer();
        if (oldValue != rendarer) {
            letterBorderRendarer = rendarer;
            syncSize();
            support.letterBorderRendererChanged(oldValue, rendarer);
        }
    }

    public SLetterConstraint.ORIENTATION getOrientation() {
        return orientation_;
    }

    public void setOrientation(SLetterConstraint.ORIENTATION orientation) {
        if (orientation == null)
            throw new IllegalArgumentException("ORIENTATION cannot be null");
        SLetterConstraint.ORIENTATION oldValue = orientation_;
        if (oldValue != orientation) {
            orientation_ = orientation;
            syncSize();
            support.orientationChanged(oldValue, orientation);
        }
    }

    public boolean isRowColCountChangable() {
        return rowColCountChangable;
    }

    public void setRowColCountChangable(boolean flag) {
        if (rowColCountChangable != flag) {
            rowColCountChangable = flag;
            syncSize();
            support.rowColCountChangableChanged(flag);
        }
    }

    public boolean isFontSizeChangable() {
        return fontSizeChangable;
    }

    public void setFontSizeChangable(boolean fontSizeChangable) {
        if (this.fontSizeChangable != fontSizeChangable) {
            this.fontSizeChangable = fontSizeChangable;
            syncSize();
            support.fontSizeChangeableChanged(fontSizeChangable);
        }
    }

    public void setSelection(int start, int end) {
        getSelectionModel().init();
        if (start != -1 && end != -1) {
            getSelectionModel().selectionStart(getSelectedMatrixIndex(start));
            getSelectionModel().selectionEndUpdate(getSelectedMatrixIndex(end));
            getSelectionModel().selectionFinish();
        }
        updateSelection();
    }

    protected SelectionModel getSelectionModel() {
        return selectionModel;
    }

    public SLetterCell[] getSelectedCells() {
        List<SLetterCell> result = new ArrayList<>();
        if (getSelectionModel().isSelected()) {
            MatrixIndex start = getSelectionModel().getSelectionStart();
            MatrixIndex end = getSelectionModel().getSelectionEnd();
            if (start.compareTo(end) > 0) {
                MatrixIndex tmp = start;
                start = end;
                end = tmp;
            }
            for (int row = start.row(); row <= end.row(); row++) {
                for (int col = row != start.row() ? 0 : start.col();
                     row != end.row() ? col < matrix_[row].length : col <= end.col();
                     col++) {

                    SLetterCell cell = matrix_[row][col];
                    if (cell != null) {
                        if (cell instanceof OverlayHolderCell) {
                            Collections.addAll(result, ((OverlayHolderCell) cell).getChildCells());
                        } else {
                            result.add(cell);
                        }
                    }
                }
            }
        }
        return result.toArray(new SLetterCell[0]);
    }

    public int getSelectionStart() {
        if (getSelectionModel().isSelected())
            return getLength_a(getSelectionModel().getSelectionStart());
        else
            return -1;
    }

    private int getLength_a(MatrixIndex matrix) {
        int index = 0;
        for (int row = 0; row < matrix_.length; row++) {
            for (int col = 0; col < matrix_[row].length; col++) {
                if (matrix.row() == row && matrix.col() == col)
                    return index;
                if (matrix_[row][col] != null) {
                    if (matrix_[row][col] instanceof OverlayHolderCell)
                        index += ((OverlayHolderCell) matrix_[row][col]).getChildCells().length;
                    else
                        index++;
                }
            }
        }
        throw new IndexOutOfBoundsException("" + matrix + " is out");
    }

    private MatrixIndex getSelectedMatrixIndex(int pos) {
        int index = 0;
        for (int row = 0; row < matrix_.length; row++) {
            for (int col = 0; col < matrix_[row].length; col++) {
                if (matrix_[row][col] != null)
                    if (matrix_[row][col] instanceof OverlayHolderCell)
                        index += ((OverlayHolderCell) matrix_[row][col]).getChildCells().length;
                    else
                        index++;
                if (index >= pos)
                    return new MatrixIndex(row, col);
            }
        }

        throw new IndexOutOfBoundsException("" + pos + " is out");
    }

    public void setSize(int width, int height) {
        setSize(new Dimension(width, height));
    }

    public void setSize(Dimension size) {
logger.fine(String.valueOf(size));
        if (isRowColCountChangable())
            setRowColCountBySize(size.width, size.height);
    }

    public void setPreferredSize(Dimension size) {
logger.fine(String.valueOf(size));
        if (isRowColCountChangable())
            setRowColCountBySize(size.width, size.height);
    }

    protected void setRowColCountBySize(int width, int height) {
        int rows;
        int cols;
        if (getOrientation().isHorizonal()) {
            rows = calcPanelRowCount(height);
            cols = calcPanelColCount(width);
        } else {
            rows = calcPanelRowCount(width);
            cols = calcPanelColCount(height);
        }
        setRowColCount(rows, cols);
        updateLocation();
    }

    private void updateLocation() {
        Dimension size = super.getSize();
        Dimension fixedSize = getFixedSize(null);
        if (size != null && fixedSize != null)
            point_ = new Point(size.width - fixedSize.width, size.height - fixedSize.height);
    }

    public Dimension getMaximumSize() {
logger.fine("");
        return getFixedSize(null);
    }

    public Dimension getMinimumSize() {
logger.fine("getMinimumSize");
        return getFixedSize(null);
    }

    public Dimension getPreferredSize() {
logger.fine("getPreferredSize: " + getFixedSize(null));
        return getFixedSize(null);
    }

    public Dimension getSize() {
logger.fine("getSize");
        return getFixedSize(null);
    }

    public Dimension getSize(Dimension size) {
logger.fine("getSize");
        return getFixedSize(size);
    }

    public Font getFont() {
        Font font = super.getFont();
        int min = Math.min(getRowRange(), getColRange());
        min = Math.round(min * getFontRangeRatio());
        if (font != null && !isFontSizeChangable() && min > 0 && font.getSize() != min)
            font = new FontUIResource(font.getFamily(), font.getStyle(), min);
        return font;
    }

    protected Dimension getFixedSize(Dimension size) {
        Dimension fixedSize = size == null ? new Dimension() : size;
        if (getOrientation().isHorizonal()) {
            fixedSize.width = calcPanelColLength(getColCount());
            fixedSize.height = calcPanelRowLength(getRowCount());
        } else {
            fixedSize.width = calcPanelRowLength(getRowCount());
            fixedSize.height = calcPanelColLength(getColCount());
        }
logger.finer("return:" + fixedSize);
        return fixedSize;
    }

    protected void syncSize() {
        if (isRowColCountChangable()) {
            setSize(super.getSize());
        } else {
            super.setSize(getFixedSize(null));
            super.setPreferredSize(getFixedSize(null));
        }
    }

    protected int calcPanelColLength(int panelColLength) {
        int colRange = getColRange() / 2;
        return (getColSpace() + getColRange()) * panelColLength + getColSpace() + colRange;
    }

    protected int calcPanelColCount(int panelColCount) {
        int colRange = getColRange() / 2;
logger.fine("colRange: " + getColRange() + ", colSpace: " + getColSpace());
        return Math.max(panelColCount - getColSpace() - colRange, 0) / (getColRange() + getColSpace());
    }

    protected int calcPanelRowLength(int panelRowLength) {
        return (getRowSpace() + getRowRange()) * panelRowLength + getRowSpace();
    }

    protected int calcPanelRowCount(int panelRowCount) {
        return Math.max(panelRowCount - getRowSpace(), 0) / (getRowRange() + getRowSpace());
    }

    @Override
    protected void paintBorder(Graphics g) {
        super.paintBorder(g);
        if (getOrientation() == null)
            return;
        Rectangle rectangle = new Rectangle();
        SLetterBorderRenderer renderer = getLetterBorderRendarer();
        for (int r = 0; r < matrix_.length; r++) {
            for (int c = 0; c < matrix_[r].length; c++) {
                SLetterCell cell = matrix_[r][c];
                if (cell != null && cell.isConstraintSet(SLetterConstraint.SELECTION.SELECTED)) {
                    rectangle = getCellBounds(r, c, rectangle).union(getRubyBounds(r, c, null));
                    if (c + 1 < matrix_[r].length) {
                        SLetterCell cell2 = matrix_[r][c + 1];
                        if (cell2 != null && cell2.isConstraintSet(SLetterConstraint.SELECTION.SELECTED))
                            rectangle = rectangle.union(getCellBounds(r, c + 1, null).union(getRubyBounds(r, c + 1, null)));
                    }
                    Color color = g.getColor();
                    g.setColor(getSelectionColor());
                    g.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
                    g.setColor(color);
                }
                rectangle = getCellBounds(r, c, rectangle);
                if (renderer != null)
                    renderer.paintCellBorder((Graphics2D) g, rectangle);
            }

            rectangle = getCellBounds(r, 0, rectangle).union(getCellBounds(r, getColCount() - 1, null));
            if (renderer != null)
                renderer.paintRowBorder((Graphics2D) g, rectangle);
        }
    }

    @Override
    protected void paintChildren(Graphics g) {
        paintCells((Graphics2D) g);
        paintMaximizedImage((Graphics2D) g);
    }

    protected void paintCells(Graphics2D g) {
        if (getOrientation() == null)
            return;
        Color color = g.getColor();
        Font font = g.getFont();
        g.setFont(getFont());
        Object hint = g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Rectangle cellBounds = new Rectangle();
        Rectangle rubyBounds = new Rectangle();

        for (int r = 0; r < matrix_.length; r++) {
            for (int c = 0; c < matrix_[r].length; c++) {
                SLetterCell cell = matrix_[r][c];
                if (cell != null) {
                    cellBounds = getCellBounds(r, c, cellBounds);
                    rubyBounds = getRubyBounds(r, c, rubyBounds);
                    if (cell.isConstraintSet(SLetterConstraint.SELECTION.SELECTED)) {
                        g.setColor(getSelectedTextColor());
                    } else {
                        g.setColor(getForeground());
                    }
                    for (SLetterCellDecorator decorator : cell.getDecorators()) {
                        decorator.decorateBeforePaint(g, cell, cellBounds, rubyBounds);
                    }
    
                    cell.paintCell(g, cellBounds);
                    cell.paintRuby(g, rubyBounds);
                    for (SLetterCellDecorator decorator : cell.getDecorators()) {
                        decorator.decorateAfterPaint(g, cell, cellBounds, rubyBounds);
                    }
                }
            }
        }
        if (hint != null)
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, hint);
        if (font != null)
            g.setFont(font);
        if (color != null)
            g.setColor(color);
    }

    protected void paintMaximizedImage(Graphics2D g) {
        SLetterImageCell cell = getMaximizedImageCell();
        if (cell != null)
            cell.paintMagnifiedImage(g, new Rectangle(0, 0, getWidth(), getHeight()));
    }

    public Rectangle getCellBounds(int row, int col, Rectangle bounds) {
        checkRowCount(row);
        checkColCount(col);
        Rectangle cellBounds = bounds == null ? new Rectangle() : bounds;
        SLetterConstraint.ORIENTATION orientation = getOrientation();
        if (!orientation.isLeftToRight())
            getRowCount();
        int r = 0;
        if (!orientation.isTopToButtom())
            getColCount();
        int c = 0;
        switch (orientation) {
        case LRTB:
            r = row;
            c = col;
            break;
        case RLTB:
            r = row;
            c = getColCount() - col - 1;
            break;
        case TBRL:
            r = getRowCount() - row - 1;
            c = col;
            break;
        case TBLR:
            r = row;
            c = col;
            break;
        default:
            throw new IllegalStateException("UnKnown ORIENTATION " + orientation);
        }
        int x = r * getRowRange() + (r + 1) * getRowSpace();
        int y = c * getColRange() + (c + 1) * getColSpace();
        cellBounds.x = orientation.isHorizonal() ? y : x;
        cellBounds.y = orientation.isHorizonal() ? x : y;
        cellBounds.width = orientation.isHorizonal() ? getColRange() : getRowRange();
        cellBounds.height = orientation.isHorizonal() ? getRowRange() : getColRange();
        cellBounds.x += orientation.isLeftToRight() ? 0 : point_.x;
        cellBounds.y += orientation.isTopToButtom() ? 0 : point_.y;
        return cellBounds;
    }

    protected Rectangle getRubyBounds(int col, int row, Rectangle bounds) {
        checkRowCount(col);
        checkColCount(row);
        Rectangle rubyBounds = getCellBounds(col, row, bounds);
        SLetterConstraint.ORIENTATION orientation = getOrientation();
        switch (orientation) {
        case LRTB:
        case RLTB:
            rubyBounds.y -= getRowSpace();
            rubyBounds.height = getRowSpace();
            break;
        case TBRL:
            rubyBounds.x += getColRange();
            rubyBounds.width = getRowSpace();
            break;
        case TBLR:
            rubyBounds.x -= getRowSpace();
            rubyBounds.width = getRowSpace();
            break;
        default:
            throw new IllegalStateException("UnKnown ORIENTATION " + orientation);
        }
        return rubyBounds;
    }

    public SLetterCell[] getCell(int row, int col) {
        checkRowCount(row);
        checkColCount(col);
        SLetterCell cell = matrix_[row][col];
        if (cell == null)
            return new SLetterCell[0];
        if (cell instanceof OverlayHolderCell)
            return ((OverlayHolderCell) cell).getChildCells();
        else
            return new SLetterCell[] { cell };
    }

    public SLetterCell[] getCell(Point point) {
        MatrixIndex matrix = getRowColAtPoint(point);
        return getCell(matrix.row(), matrix.col());
    }

    protected MatrixIndex getRowColAtPoint(Point point) {
        SLetterConstraint.ORIENTATION orientation = getOrientation();
        Dimension dimension = getFixedSize(null);
        int cs = getColSpace() / 2;
        int rs = getRowSpace() / 2;
        int x;
        int y;
        switch (orientation) {
        case LRTB:
            x = point.x - cs;
            y = point.y - rs;
            break;
        case RLTB:
            x = (dimension.width + point_.x) - point.x - cs;
            y = point.y - rs;
            break;
        case TBRL:
            x = point.y - cs;
            y = (dimension.width + point_.x) - point.x - rs;
            break;
        case TBLR:
            x = point.y - cs;
            y = point.x - rs;
            break;
        default:
            throw new IllegalStateException("UnKnown ORIENTATION " + orientation);
        }
        int c = x / (getColRange() + getColSpace());
        int r = y / (getRowRange() + getRowSpace());
        c = Math.min(Math.max(0, c), getColCount() - 1);
        r = Math.min(Math.max(0, r), getRowCount() - 1);
        MatrixIndex matrix = new MatrixIndex(r, c);
        logger.fine("RowColAtPoint|" + point + "|" + matrix);
        return matrix;
    }

    public boolean addCell(SLetterCell cell) {
        if (cell == null)
            throw new IllegalArgumentException("cell cannot be null");
        cell.removeConstraint(SLetterConstraint.OVERLAY.LINE_TAIL_OVER);
        if (!cell.isConstraintSet(SLetterConstraint.OVERLAY.FORCE_OVER)) {
            cell.removeConstraint(SLetterConstraint.OVERLAY.HALF_OVER_HEAD);
            cell.removeConstraint(SLetterConstraint.OVERLAY.HALF_OVER_TAIL);
        }
        MatrixIndex matrix = getIndex_b(new MatrixIndex(0, 0));
        if (e(matrix, cell)) {
            if (matrix == null)
                matrix = new MatrixIndex(getRowCount() - 1, getColCount() - 1);
            return f(matrix, cell);
        }
        if (isBreakNewLineIfLineTail(matrix, cell))
            return addCellNewLine_b(matrix, cell);
        if (isBreakBackIfLineHead(matrix, cell) && addCellBreakBack_d(matrix, cell))
            return true;
        if (matrix != null) {
            matrix_[matrix.row()][matrix.col()] = cell;
            fireCellAdded_a(cell);
            return true;
        } else {
            return false;
        }
    }

    private MatrixIndex getIndex_b(MatrixIndex matrix) {
        for (int row = matrix.row(); row < matrix_.length; row++) {
            boolean f = row != matrix.row();
            for (int col = f ? 0 : matrix.col(); col < matrix_[row].length; col++) {
                if (matrix_[row][col] != null) {
                    if (matrix_[row][col].isConstraintSet(SLetterConstraint.BREAK.NEW_PAGE))
                        break;
                    if (matrix_[row][col].isConstraintSet(SLetterConstraint.BREAK.NEW_LINE))
                        break;
                } else {
                    return new MatrixIndex(row, col);
                }
            }
        }

        return null;
    }

    private MatrixIndex c(MatrixIndex matrix) {
        for (int row = matrix.row(); row != -1; row--) {
            boolean f = row != matrix.row();
            for (int col = f ? matrix_[row].length - 1 : matrix.col(); col != -1; col--)
                if (matrix_[row][col] != null)
                    return new MatrixIndex(row, col);
        }

        return null;
    }

    private boolean isBreakNewLineIfLineTail(MatrixIndex matrix, SLetterCell cell) {
        if (matrix == null || matrix.col() != getColCount() - 1)
            return false;
        return cell != null && cell.isConstraintSet(SLetterConstraint.BREAK.NEW_LINE_IF_LINE_TAIL);
    }

    private boolean addCellNewLine_b(MatrixIndex matrix, SLetterCell cell) {
        MatrixIndex newLine = getIndex_b(new MatrixIndex(matrix.row() + 1, 0));
        if (newLine != null) {
            matrix_[newLine.row()][newLine.col()] = cell;
            return true;
        } else {
            return false;
        }
    }

    private static boolean isBreakBackIfLineHead(MatrixIndex matrix, SLetterCell cell) {
        if (matrix != null && matrix.col() != 0)
            return false;
        return cell.isConstraintSet(SLetterConstraint.BREAK.BACK_IF_LINE_HEAD);
    }

    private boolean addCellBreakBack_d(MatrixIndex matrix, SLetterCell cell) {
        MatrixIndex matrix1 = c(new MatrixIndex(matrix != null ? matrix.row() : getRowCount() - 1, matrix != null ? matrix.col() : getColCount() - 1));
        if (matrix1 == null)
            return false;
        SLetterCell cell1 = matrix_[matrix1.row()][matrix1.col()];
        if (cell1.isConstraintSet(SLetterConstraint.BREAK.NEW_LINE))
            return false;
        if (cell1 instanceof OverlayHolderCell) {
            ((OverlayHolderCell) cell1).addChildCell(cell);
            fireCellAdded_a(cell);
            cell.addConstraint(SLetterConstraint.OVERLAY.LINE_TAIL_OVER);
            return true;
        } else {
            OverlayHolderCell cell2 = new OverlayHolderCell(cell1, cell);
            fireCellAdded_a(cell);
            fireCellAdded_a(cell2);
            matrix_[matrix1.row()][matrix1.col()] = cell2;
            cell.addConstraint(SLetterConstraint.OVERLAY.LINE_TAIL_OVER);
            return true;
        }
    }

    private boolean e(MatrixIndex matrix, SLetterCell cell) {
        if (cell.isConstraintSet(SLetterConstraint.OVERLAY.FORCE_OVER) ||
            cell.isConstraintSet(SLetterConstraint.OVERLAY.HALF_OVER)) {
            if (matrix != null)
                return true;
            MatrixIndex matrix1 = c(new MatrixIndex(getRowCount() - 1, getColCount() - 1));
            if (matrix1 != null) {
                if (cell.isConstraintSet(SLetterConstraint.OVERLAY.FORCE_OVER))
                    return true;
                if (cell.isConstraintSet(SLetterConstraint.OVERLAY.HALF_OVER)) {
                    SLetterCell cell1 = matrix_[matrix1.row()][matrix1.col()];
                    if (cell1 instanceof OverlayHolderCell) {
                        OverlayHolderCell cell3 = (OverlayHolderCell) cell1;
                        for (SLetterCell cell2 : cell3.getChildCells()) {
                            if (!cell2.isConstraintSet(SLetterConstraint.OVERLAY.HALF_OVER_HEAD))
                                return false;
                        }

                        return true;
                    }
                    if (cell1.isConstraintSet(SLetterConstraint.OVERLAY.HALF_OVER_HEAD))
                        return true;
                }
            }
        }
        return false;
    }

    private boolean f(MatrixIndex matrix, SLetterCell cell) {
label0: {
            MatrixIndex matrix1 = c(new MatrixIndex(matrix.row(), matrix.col()));
            if (matrix1 != null) {
                SLetterCell cell1 = matrix_[matrix1.row()][matrix1.col()];
                if (!cell1.isConstraintSet(SLetterConstraint.BREAK.NEW_LINE) &&
                    !cell1.isConstraintSet(SLetterConstraint.BREAK.NEW_PAGE)) {
                    if (cell.isConstraintSet(SLetterConstraint.OVERLAY.FORCE_OVER)) {
                        if (cell1 instanceof OverlayHolderCell) {
                            ((OverlayHolderCell) cell1).addChildCell(cell);
                        } else {
                            OverlayHolderCell cell0 = new OverlayHolderCell(cell1, cell);
                            matrix_[matrix1.row()][matrix1.col()] = cell0;
                        }
                        fireCellAdded_a(cell);
                        return true;
                    }
                    if (cell.isConstraintSet(SLetterConstraint.OVERLAY.HALF_OVER)) {
                        if (cell1 instanceof OverlayHolderCell) {
                            OverlayHolderCell cell3 = (OverlayHolderCell) cell1;
                            boolean flag = false;
                            for (SLetterCell cell2 : cell3.getChildCells()) {
                                if (!cell2.isConstraintSet(SLetterConstraint.OVERLAY.HALF_OVER_HEAD))
                                    break label0;
                                flag = true;
                            }

                            cell.addConstraint(flag ? (SLetterConstraint) (SLetterConstraint.OVERLAY.HALF_OVER_TAIL) :
                                    (SLetterConstraint) (SLetterConstraint.OVERLAY.HALF_OVER_HEAD));
                            cell3.addChildCell(cell);
                            fireCellAdded_a(cell);
                            return true;
                        }
                        if (cell1.isConstraintSet(SLetterConstraint.OVERLAY.HALF_OVER_HEAD)) {
                            cell.addConstraint(SLetterConstraint.OVERLAY.HALF_OVER_TAIL);
                            OverlayHolderCell overlayholdercell2 = new OverlayHolderCell(cell1, cell);
                            matrix_[matrix1.row()][matrix1.col()] = overlayholdercell2;
                            fireCellAdded_a(cell);
                            return true;
                        }
                    }
                }
            }
        }
        if (cell.isConstraintSet(SLetterConstraint.OVERLAY.HALF_OVER))
            cell.addConstraint(SLetterConstraint.OVERLAY.HALF_OVER_HEAD);
        fireCellAdded_a(cell);
        matrix_[matrix.row()][matrix.col()] = cell;
        return true;
    }

    public SLetterCell setCell(SLetterCell cell, int row, int col) {
        if (cell == null) {
            throw new IllegalArgumentException("cell cannot be null");
        } else {
            checkRowCount(row);
            checkColCount(col);
            SLetterCell oldCell = matrix_[row][col];
            matrix_[row][col] = cell;
            fireCellAdded_a(cell);
            return oldCell;
        }
    }

//    @SuppressWarnings("cast")
    private void fireCellAdded_a(SLetterCell cell) {
        cell.setParent(this);
//         if (cell instanceof Component)
//             add((Component) cell);
        support.cellAdded(cell);
    }

    public boolean removeCell(SLetterCell cell) {
        if (cell == null)
            throw new IllegalArgumentException("Cell cannot be null");
        for (int row = 0; row < matrix_.length; row++) {
            for (int col = 0; col < matrix_[row].length; col++)
                if (matrix_[row][col] == cell) {
                    matrix_[row][col] = null;
                    fireCellRemoved_b(cell);
                    return true;
                }
        }

        return false;
    }

    public SLetterCell[] removeCellAll() {
        List<SLetterCell> result = new ArrayList<>();
        for (int row = 0; row < matrix_.length; row++) {
            for (int col = 0; col < matrix_[row].length; col++) {
                SLetterCell cell = matrix_[row][col];
                matrix_[row][col] = null;
                if (cell instanceof OverlayHolderCell) {
                    for (SLetterCell cell1 : ((OverlayHolderCell) cell).getChildCells()) { 
                        if (cell1 != null) {
                            fireCellRemoved_b(cell1);
                            result.add(cell1);
                        }
                    }
                } else if (cell != null) {
                    fireCellRemoved_b(cell);
                    result.add(cell);
                }
            }
        }
        return result.toArray(new SLetterCell[0]);
    }

//    @SuppressWarnings("cast")
    private void fireCellRemoved_b(SLetterCell cell) {
        cell.setParent(null);
//        if (cell instanceof Component)
//             remove((Component) cell);
        support.cellRemoved(cell);
    }

    private void checkRowCount(int rowCount) {
        if (rowCount < 0 || getRowCount() <= rowCount)
            throw new IndexOutOfBoundsException("row [" + rowCount + "/" + getRowCount() + "]");
    }

    private void checkColCount(int colCount) {
        if (colCount < 0 || getColCount() <= colCount)
            throw new IndexOutOfBoundsException("col [" + colCount + "/" + getColCount() + "]");
    }

    private static final long serialVersionUID = 0x137ebb5dL;
    private SLetterConstraint.ORIENTATION orientation_;
    private SLetterCell[][] matrix_;
    private int rowRange;
    private int colRange;
    private float fontRangeRatio;
    private int rowSpace;
    private int colSpace;
    private SLetterBorderRenderer letterBorderRendarer;
    private Color selectionColor;
    private Color selectedTextColor;
    private boolean rowColCountChangable;
    private boolean fontSizeChangable;
    private Point point_;
    private SelectionModel selectionModel;
    private SLetterImageCell imageCell;
    private SLetterPaneObserverSupport support;
    private List<MenuItemProducer> producers;
}
