/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraDefaultPane;
import com.soso.sgui.SLineBorder;


public abstract class AozoraBalloonPane extends AozoraDefaultPane {

    private static class FillerPane extends JComponent {

        private void setMargin(int margin) {
            this.margin = margin;
        }

        private int getMargin() {
            return margin;
        }

        private void setOrientation(int orientation) {
            if (orientation != HORIZONTAL && orientation != VERTICAL) {
                throw new IllegalArgumentException("Unknown orientation " + orientation);
            }
            this.orientation = orientation;
        }

        public Dimension getMinimumSize() {
            return orientation != HORIZONTAL ? new Dimension(0, margin) : new Dimension(margin, 0);
        }

        public Dimension getPreferredSize() {
            return orientation != HORIZONTAL ? new Dimension(0, margin) : new Dimension(margin, 0);
        }

        public Dimension getMaximumSize() {
            return orientation != HORIZONTAL ? new Dimension(0x7fffffff, margin)
                                             : new Dimension(margin, 0x7fffffff);
        }

        private static final int HORIZONTAL = 0;
        private static final int VERTICAL = 1;
        private int margin;
        private int orientation;

        private FillerPane(int orientation) {
            setOrientation(orientation);
            setOpaque(false);
        }
    }

    private class HolderPane extends JPanel {

        private void initGUI() {
            setBorder(new SLineBorder(Color.BLACK, 1, true, 8));
            setLayout(new BorderLayout());
        }

        public void setBorder(Border border) {
            if (border instanceof SLineBorder)
                super.setBorder(border);
        }

        public SLineBorder getBorder() {
            return (SLineBorder) super.getBorder();
        }

        protected void paintComponent(Graphics g) {
            Color color0 = g.getColor();
            SLineBorder border = (SLineBorder) super.getBorder();
            if (border != null) {
                Shape borderInnerShape = border.getBorderInnerShape(0, 0, getWidth(), getHeight());
                if (g instanceof Graphics2D) {
                    g.setColor(getContentPane().getBackground());
                    ((Graphics2D) g).fill(borderInnerShape);
                } else {
                    super.paintComponent(g);
                }
            }
            if (color0 != null)
                g.setColor(color0);
        }

        private HolderPane() {
            initGUI();
        }
    }

    protected AozoraBalloonPane(AozoraContext context) {
        super(context);
        initGUI();
    }

    private void initGUI() {
        setOpaque(false);
        setLayout(new BorderLayout());
        add(getTopFiller(), BorderLayout.NORTH);
        add(getBottomFiller(), BorderLayout.SOUTH);
        add(getLeftFiller(), BorderLayout.WEST);
        add(getRightFiller(), BorderLayout.EAST);
        add(getHolderPane(), BorderLayout.CENTER);
        getHolderPane().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                pressedPoint = e.getPoint();
                moveToFront();
            }

            public void mouseReleased(MouseEvent e) {
                pressedPoint = null;
            }
        });
        getHolderPane().addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (pressedPoint != null) {
                    Point basePoint = getBasePoint();
                    setBasePoint(new Point((basePoint.x - pressedPoint.x) + e.getX(), (basePoint.y - pressedPoint.y) + e.getY()));
                    moveToFront();
                }
            }
        });
        getHolderPane().add(getContentPane(), BorderLayout.CENTER);
    }

    private HolderPane getHolderPane() {
        if (holderPane == null)
            holderPane = new HolderPane();
        return holderPane;
    }

    private FillerPane getTopFiller() {
        if (topFiller == null)
            topFiller = new FillerPane(FillerPane.VERTICAL);
        return topFiller;
    }

    private FillerPane getBottomFiller() {
        if (bottomFiller == null)
            bottomFiller = new FillerPane(FillerPane.VERTICAL);
        return bottomFiller;
    }

    private FillerPane getLeftFiller() {
        if (leftFiller == null)
            leftFiller = new FillerPane(FillerPane.HORIZONTAL);
        return leftFiller;
    }

    private FillerPane getRightFiller() {
        if (rightFiller == null)
            rightFiller = new FillerPane(FillerPane.HORIZONTAL);
        return rightFiller;
    }

    private JComponent getContentPane() {
        if (contentPane == null)
            contentPane = initContentPane();
        return contentPane;
    }

    protected abstract JComponent initContentPane();

    public void paint(Graphics g) {
        super.paint(g);
        paintLip(g);
    }

    private void paintLip(Graphics g) {
        Point orginPoint = getLocation();
        Point lipPoint = getLipPoint();
        Point basePoint = getBasePoint();
        Point[] footPoints = getLipFootPoints();
        if (lipPoint == null || basePoint == null || footPoints == null || footPoints.length == 0)
            return;
        Color color0 = g.getColor();
        Polygon lipShape = new Polygon();
        lipShape.addPoint(lipPoint.x - orginPoint.x, lipPoint.y - orginPoint.y);
        for (Point footPoint : footPoints) {
            lipShape.addPoint(footPoint.x - orginPoint.x, footPoint.y - orginPoint.y);
        }

        g.setColor(getContentPane().getBackground());
        g.fillPolygon(lipShape);
        g.setColor(getHolderPane().getBorder().getLineColor());
        g.drawLine(lipPoint.x - orginPoint.x, lipPoint.y - orginPoint.y, footPoints[0].x - orginPoint.x, footPoints[0].y - orginPoint.y);
        g.drawLine(lipPoint.x - orginPoint.x, lipPoint.y - orginPoint.y, footPoints[footPoints.length - 1].x - orginPoint.x, footPoints[footPoints.length - 1].y - orginPoint.y);
        if (color0 != null)
            g.setColor(color0);
    }

    public void moveToFront() {
        java.awt.Container parent = getParent();
        if (parent != null && (parent instanceof JLayeredPane)) {
            JLayeredPane layeredPane = (JLayeredPane) parent;
            layeredPane.moveToFront(this);
        }
    }

    public void setLipPoint(Point lipPoint) {
        setBounds(lipPoint, getBasePoint());
        this.lipPoint = lipPoint;
    }

    public Point getLipPoint() {
        return lipPoint;
    }

    public void setBasePoint(Point basePoint) {
        setBounds(getLipPoint(), basePoint);
    }

    public Point getBasePoint() {
        return new Point(getX() + getLeftFiller().getMargin(), getY() + getTopFiller().getMargin());
    }

    private void setBounds(Point lipPoint, Point basePoint) {
        if (basePoint == null)
            throw new IllegalArgumentException("basePoint " + basePoint);
        if (lipPoint == null)
            lipPoint = basePoint;
        Dimension contentSize = getContentSize();
        setFillerMargin(Math.max(0, basePoint.y - lipPoint.y),
                        Math.max(0, basePoint.x - lipPoint.x),
                        Math.max(0, lipPoint.y - basePoint.y - contentSize.height),
                        Math.max(0, lipPoint.x - basePoint.x - contentSize.width));
        setLocation(Math.min(basePoint.x, lipPoint.x), Math.min(basePoint.y, lipPoint.y));
        setSize(getPreferredSize());
    }

    private void setFillerMargin(int top, int left, int bottom, int right) {
        getTopFiller().setMargin(top);
        getLeftFiller().setMargin(left);
        getBottomFiller().setMargin(bottom);
        getRightFiller().setMargin(right);
    }

    private Insets getFillerMargin() {
        return new Insets(getTopFiller().getMargin(),
                          getLeftFiller().getMargin(),
                          getBottomFiller().getMargin(),
                          getRightFiller().getMargin());
    }

    public Dimension getContentSize() {
        return getHolderPane().getPreferredSize();
    }

    private Point[] getLipFootPoints() {
        Point lipPoint = getLipPoint();
        if (lipPoint == null)
            return null;
        Point basePoint = getBasePoint();
        Dimension contentSize = getContentSize();
        Insets margin = getFillerMargin();
        int radius = getHolderPane().getBorder().getRadious();
        int thickness = getHolderPane().getBorder().getThickness();
        Point[] footPoints = new Point[2];
        if (lipPoint.y < basePoint.y && margin.top >= margin.left && margin.top >= margin.right) {
            footPoints[0] = new Point(basePoint.x + contentSize.width / 2, basePoint.y + thickness);
            if (lipPoint.x < footPoints[0].x)
                footPoints[1] = new Point(basePoint.x + radius, basePoint.y + thickness);
            else
                footPoints[1] = new Point((basePoint.x + contentSize.width) - radius, basePoint.y + thickness);
        } else if (lipPoint.x < basePoint.x && margin.left >= margin.top && margin.left >= margin.bottom) {
            footPoints[0] = new Point(basePoint.x + thickness, basePoint.y + contentSize.height / 2);
            if (lipPoint.y < footPoints[0].y)
                footPoints[1] = new Point(basePoint.x + thickness, basePoint.y + radius);
            else
                footPoints[1] = new Point(basePoint.x + thickness, (basePoint.y + contentSize.height) - radius);
        } else if (lipPoint.y > basePoint.y + contentSize.height && margin.bottom >= margin.left && margin.bottom >= margin.right) {
            footPoints[0] = new Point(basePoint.x + contentSize.width / 2, (basePoint.y + contentSize.height) - thickness);
            if (lipPoint.x < footPoints[0].x)
                footPoints[1] = new Point(basePoint.x + radius, (basePoint.y + contentSize.height) - thickness);
            else
                footPoints[1] = new Point((basePoint.x + contentSize.width) - radius, (basePoint.y + contentSize.height) - thickness);
        } else if (lipPoint.x > basePoint.x + contentSize.width && margin.right >= margin.top && margin.right >= margin.bottom) {
            footPoints[0] = new Point((basePoint.x + contentSize.width) - thickness, basePoint.y + contentSize.height / 2);
            if (lipPoint.y < footPoints[0].y)
                footPoints[1] = new Point((basePoint.x + contentSize.width) - thickness, basePoint.y + radius);
            else
                footPoints[1] = new Point((basePoint.x + contentSize.width) - thickness, (basePoint.y + contentSize.height) - radius);
        } else {
            return null;
        }
        return footPoints;
    }

    private Point lipPoint;
    private Point pressedPoint;
    private HolderPane holderPane;
    private FillerPane topFiller;
    private FillerPane bottomFiller;
    private FillerPane leftFiller;
    private FillerPane rightFiller;
    private JComponent contentPane;
}
