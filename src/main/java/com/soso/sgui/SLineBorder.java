/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import javax.swing.border.LineBorder;


public class SLineBorder extends LineBorder {

    public SLineBorder(Color color, int thickness, boolean roundedCorners) {
        this(color, thickness, roundedCorners, 0);
    }

    public SLineBorder(Color color, int thickness, boolean roundedCorners, int radius) {
        super(color, thickness, roundedCorners);
        this.radius = radius;
        if (radius < 0)
            throw new IllegalArgumentException("radius cannot be negative but " + radius);
    }

    public int getRadius() {
        return radius;
    }

    public Insets getBorderInsets(Component c) {
        return getBorderInsets(c, new Insets(0, 0, 0, 0));
    }

    public Insets getBorderInsets(Component c, Insets insets) {
        int corner = 0;
        double theta = radius * (1.0D - Math.sin(0.78539816339744828));
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
        int r = getRadius();
        int s = rounded ? t + r : thickness;
        int l = ((t + r) - thickness) * 2;
        GeneralPath path = new GeneralPath();
        path.append(new Line2D.Float((x + width) - s, y + thickness, x + s, y + thickness), true);
        if (rounded)
            path.append(new Arc2D.Float(x + thickness, y + thickness, l, l, 90F, 90F, Arc2D.OPEN), true);
        path.append(new Line2D.Float(x + thickness, y + s, x + thickness, (y + height) - s), true);
        if (rounded)
            path.append(new Arc2D.Float(x + thickness, (y + height) - l - thickness - 1, l, l, 180F, 90F, Arc2D.OPEN), true);
        path.append(new Line2D.Float(x + s, (y + height) - thickness - 1, (x + width) - s, (y + height) - thickness - 1), true);
        if (rounded)
            path.append(new Arc2D.Float((x + width) - l - thickness - 1, (y + height) - l - thickness - 1, l, l, 270F, 90F, Arc2D.OPEN), true);
        path.append(new Line2D.Float((x + width) - thickness - 1, (y + height) - s, (x + width) - thickness - 1, y + s), true);
        if (rounded)
            path.append(new Arc2D.Float((x + width) - l - thickness - 1, y + thickness, l, l, 0.0F, 90F, Arc2D.OPEN), true);
        return path;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            Color color = g.getColor();
            g.setColor(getLineColor());
            g2.draw(getBorderOuterShape(x, y, width, height));
            if (getThickness() > 1) {
                g2.fill(getBorderShape(x, y, width, height));
                g2.draw(getBorderInnerShape(x, y, width, height));
            }
            if (color != null)
                g.setColor(color);
        }
    }

    private static final long serialVersionUID = 0xcbbcc96eL;

    protected int radius;
}
