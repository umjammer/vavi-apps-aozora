/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;


public class SLineBorder extends LineBorder {

    public SLineBorder(Color color, int thickness, boolean roundedCorners) {
        this(color, thickness, roundedCorners, 0);
    }

    public SLineBorder(Color color, int thickness, boolean roundedCorners, int radious) {
        super(color, thickness, roundedCorners);
        this.radious = radious;
        if (radious < 0)
            throw new IllegalArgumentException("radious cannot be negative but " + radious);
    }

    public int getRadious() {
        return radious;
    }

    public Insets getBorderInsets(Component c) {
        return getBorderInsets(c, new Insets(0, 0, 0, 0));
    }

    public Insets getBorderInsets(Component c, Insets insets) {
        int corner = 0;
        double theta = radious * (1.0D - Math.sin(0.78539816339744828D));
        if (getRoundedCorners())
            corner = (int) Math.round(theta) + 1;
        insets.left = insets.top = insets.right = insets.bottom = thickness + corner;
        return insets;
    }

    public Shape getBorderShape(int x, int y, int width, int height) {
        Shape outer = getBorderOuterShape(x, y, width, height);
        Shape inner = getBorderInnerShape(x, y, width, height);
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        path.append(outer, false);
        path.append(inner, false);
        return path;
    }

    public Shape getBorderOuterShape(int x, int y, int width, int height) {
        return getBorderShapeIndexOf(x, y, width, height, 0);
    }

    public Shape getBorderInnerShape(int x, int y, int width, int height) {
        return getBorderShapeIndexOf(x, y, width, height, getThickness() - 1);
    }

    protected Shape getBorderShapeIndexOf(int x, int y, int width, int height, int thickness) {
        boolean rounded = getRoundedCorners();
        int t = getThickness();
        int r = getRadious();
        int s = rounded ? t + r : thickness;
        int l = ((t + r) - thickness) * 2;
        GeneralPath path = new GeneralPath();
        path.append(new Line2D.Float((x + width) - s, y + thickness, x + s, y + thickness), true);
        if (rounded)
            path.append(new Arc2D.Float(x + thickness, y + thickness, l, l, 90F, 90F, 0), true);
        path.append(new Line2D.Float(x + thickness, y + s, x + thickness, (y + height) - s), true);
        if (rounded)
            path.append(new Arc2D.Float(x + thickness, (y + height) - l - thickness - 1, l, l, 180F, 90F, 0), true);
        path.append(new Line2D.Float(x + s, (y + height) - thickness - 1, (x + width) - s, (y + height) - thickness - 1), true);
        if (rounded)
            path.append(new Arc2D.Float((x + width) - l - thickness - 1, (y + height) - l - thickness - 1, l, l, 270F, 90F, 0), true);
        path.append(new Line2D.Float((x + width) - thickness - 1, (y + height) - s, (x + width) - thickness - 1, y + s), true);
        if (rounded)
            path.append(new Arc2D.Float((x + width) - l - thickness - 1, y + thickness, l, l, 0.0F, 90F, 0), true);
        return path;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Color color = g.getColor();
        g.setColor(getLineColor());
        boolean is2d = g instanceof Graphics2D;
        Graphics2D g2 = is2d ? (Graphics2D) g : null;
        if (is2d) {
            g2.draw(getBorderOuterShape(x, y, width, height));
            if (getThickness() > 1) {
                g2.fill(getBorderShape(x, y, width, height));
                g2.draw(getBorderInnerShape(x, y, width, height));
            }
        } else {
            boolean rounded = getRoundedCorners();
            int t = getThickness();
            int r = getRadious();
            int s = rounded ? t + r : 0;
            g.fillRect(x + s, y, width - s * 2, t);
            g.fillRect(x, y + s, t, height - s * 2);
            g.fillRect(x + s, (y + height) - t, width - s * 2, t);
            g.fillRect((x + width) - t, y + s, t, height - s * 2);
            if (rounded) {
                for (int i = 0; i < t; i++) {
                    int l = ((t + r) - i) * 2;
                    int d1 = i;
                    int d2 = l + i + 1;
                    g.drawArc(x + d1, y + d1, l, l, 90, 90);
                    g.drawArc(x + d1, (y + height) - d2, l, l, 180, 90);
                    g.drawArc((x + width) - d2, (y + height) - d2, l, l, 270, 90);
                    g.drawArc((x + width) - d2, y + d1, l, l, 0, 90);
                    if (i != 0) {
                        g.drawArc((x + d1) - 1, y + d1, l, l, 90, 90);
                        g.drawArc(x + d1, (y + d1) - 1, l, l, 90, 90);
                        g.drawArc((x + d1) - 1, (y + height) - d2, l, l, 180, 90);
                        g.drawArc(x + d1, ((y + height) - d2) + 1, l, l, 180, 90);
                        g.drawArc(((x + width) - d2) + 1, (y + height) - d2, l, l, 270, 90);
                        g.drawArc((x + width) - d2, ((y + height) - d2) + 1, l, l, 270, 90);
                        g.drawArc(((x + width) - d2) + 1, y + d1, l, l, 0, 90);
                        g.drawArc((x + width) - d2, (y + d1) - 1, l, l, 0, 90);
                    }
                }
            }
        }
        if (color != null)
            g.setColor(color);
    }

    public static void main(String[] args) {
        SFrame frame = new SFrame();
        frame.setSize(800, 800);
        frame.setDefaultCloseOperation(SFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(0, 0));
        JPanel borderPanel = new JPanel();
        borderPanel.setBackground(Color.BLUE);
        borderPanel.setBorder(new SLineBorder(Color.RED, 100, true, 200));
        frame.getContentPane().add(borderPanel, BorderLayout.CENTER);
        borderPanel.setLayout(new BorderLayout(0, 0));
        JPanel panel = new JPanel();
        panel.setBackground(Color.LIGHT_GRAY);
        borderPanel.add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.WEST);
        frame.getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.EAST);
        frame.getContentPane().add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        frame.getContentPane().add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private static final long serialVersionUID = 0xcbbcc96eL;

    protected int radious;
}
