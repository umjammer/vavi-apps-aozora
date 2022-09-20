/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.letter;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SLetterImageCell extends SLetterGlyphCell {

    static Logger logger = Logger.getLogger(SLetterImageCell.class.getName());

    private static class s_ImageFilter extends RGBImageFilter {

        public final int filterRGB(int x, int y, int rgb) {
            Color color = new Color(rgb, true);
            Color color1 = new Color(a(color_a.getRed(), color_b.getRed(), color.getRed()),
                                     a(color_a.getGreen(), color_b.getGreen(), color.getBlue()),
                                     a(color_a.getBlue(), color_b.getBlue(), color.getBlue()),
                                     color.getAlpha());
            if (color_a.getRed() == color1.getRed() &&
                color_a.getGreen() == color1.getGreen() &&
                color_a.getBlue() == color1.getBlue())
                color1 = new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0);
            return color1.getRGB();
        }

        private static int a(int c1, int c2, int c3) {
            float f1 = (c1 - c2) / 255F;
            float f2 = c2 + c3 * f1;
            int j1 = Math.round(f2);
            j1 = Math.max(0, j1);
            return j1 = Math.min(255, j1);
        }

        final Color color_a;
        final Color color_b;

        public s_ImageFilter(Color color, Color color1) {
            super.canFilterIndexColorModel = true;
            color_a = color;
            color_b = color1;
        }
    }

    protected SLetterImageCell(Image image, boolean resize, boolean maximizable, char[] rubys, Font font, String text) {
        super('※', rubys, font);
        if (image == null) {
            throw new IllegalArgumentException("image cannot be null");
        }
        this.image = image;
        this.resize = resize;
        this.maximizable = maximizable;
        this.text = text;
    }

    public boolean isResize() {
        return resize;
    }

    public void setResize(boolean resize) {
        this.resize = resize;
        SLetterPane parent = getParent();
        if (parent != null)
            parent.repaint();
    }

    public boolean isMaximizable() {
        return maximizable;
    }

    public void setMaximizable(boolean maximizable) {
        this.maximizable = maximizable;
        SLetterPane parent = getParent();
        if (parent != null)
            parent.repaint();
    }

    public String getText() {
        StringBuilder sb = new StringBuilder();
        if (text != null)
            sb.append(text);
        else
            sb.append("??");
        sb.append(super.getText().substring(1));
        return sb.toString();
    }

    protected void setParent(SLetterPane parent) {
        SLetterPane oldValue = getParent();
        if (oldValue != null)
            oldValue.removePropertyChangeListener(getPropertyChnageListener());
        super.setParent(parent);
        if (parent != null)
            parent.addPropertyChangeListener(getPropertyChnageListener());
    }

    final PropertyChangeListener getPropertyChnageListener() {
        if (propertyChangeListener == null)
            synchronized (this) {
                if (propertyChangeListener == null)
                    propertyChangeListener = event -> {
                        String name = event.getPropertyName();
                        if ("background".equals(name) || "foreground".equals(name))
                            unselectedImage = null;
                        if ("selectionColor".equals(name) || "selectedTextColor".equals(name))
                            selectedImage = null;
                    };
            }
        return propertyChangeListener;
    }

    public void paintMaximizedImage(Graphics g, Rectangle bounds) {
        if (!isMaximizable())
            return;
        SLetterPane parent = getParent();
        if (parent == null)
            return;
        Color color = g.getColor();
        boolean is2d = g instanceof Graphics2D;
        Graphics2D g2 = is2d ? (Graphics2D) g : null;
        Composite composite = null;
        if (is2d) {
            composite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(3, 0.7F));
        }
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        if (is2d)
            g2.setComposite(AlphaComposite.SrcOver);
        int iw = image.getWidth(parent);
        int ih = image.getHeight(parent);
        int w = Math.min(iw, Math.max(0, bounds.width - 20));
        int h = Math.min(ih, Math.max(0, bounds.height - 20));
        if (iw > w || ih > h) {
            double sx = (double) w / (double) iw;
            double sy = (double) h / (double) ih;
            double s = Math.min(sx, sy);
            w = (int) (iw * s);
            h = (int) (ih * s);
        }
        int x = bounds.x + (bounds.width - w) / 2;
        int y = bounds.y + (bounds.height - h) / 2;
        g.clearRect(x, y, w, h);
        g.setColor(Color.BLACK);
        g.drawRect(x - 1, y - 1, w + 1, h + 1);
        g.drawImage(image, x, y, w, h, getParent());
        if (composite != null)
            g2.setComposite(composite);
        g.setColor(color);
    }

    public void paintCell(Graphics g, Rectangle cellBounds) {
        if (!drawImage_b())
            return;
        SLetterConstraint.ORIENTATION orientation = getOrientation();
        if (orientation == null)
            return;
        Color color = g.getColor();
        boolean is2d = g instanceof Graphics2D;
        Graphics2D g2 = is2d ? (Graphics2D) g : null;
        int w = image_c.getWidth();
        int h = image_c.getHeight();
        int x = cellBounds.x + (cellBounds.width - w) / 2;
        int y = cellBounds.y + (cellBounds.height - h) / 2;
        double t = getRotateTheta(orientation);
        AffineTransform trans = null;
        if (is2d && t != 0.0D) {
            trans = g2.getTransform();
            AffineTransform affinetransform1 = new AffineTransform(trans);
            int x1 = cellBounds.x + cellBounds.width / 2;
            int y1 = cellBounds.y + cellBounds.height / 2;
            affinetransform1.rotate(t, x1, y1);
            if (isConstraintSet(SLetterConstraint.ROTATE.LR_MIRROR)) {
                affinetransform1.translate(0.0D, y1);
                affinetransform1.scale(1.0D, -1D);
                affinetransform1.translate(0.0D, -y1);
            }
            g2.setTransform(affinetransform1);
        }
        if (isConstraintSet(SLetterConstraint.SELECTION.SELECTED))
            g.drawImage(getImage_d(), x, y, getParent());
        else
            g.drawImage(getImage_c(), x, y, getParent());
        if (maximizable) {
            g.setColor(defaultColor);
            g.drawRect(cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height);
        }
        if (logger.isLoggable(Level.FINE)) {
            g.setColor(new Color(153, 153, 255));
            g.drawRect(x, y, w, h);
        }
        if (trans != null)
            g2.setTransform(trans);
        g.setColor(color);
    }

    private boolean drawImage_b() {
        SLetterPane parent = getParent();
        if (parent == null)
            return false;
        Font font = parent.getFont();
        if (font == null)
            return false;
        int iw = image.getWidth(parent);
        int ih = image.getHeight(parent);
        int w = iw;
        int h = ih;
        if (resize) {
            int size = font.getSize();
            if (image_c != null) {
                int w1 = image_c.getWidth();
                int h2 = image_c.getHeight();
                if (w1 == size && h2 <= size || w1 <= size && h2 == size)
                    return true;
            }
            float sx = (float) size / (float) iw;
            float sy = (float) size / (float) ih;
            float s = Math.min(sx, sy);
            w = Math.round(iw * s);
            h = Math.round(ih * s);
        }
        BufferedImage image = new BufferedImage(w, h, 2);
        image.getGraphics().drawImage(this.image, 0, 0, w, h, parent);
        image_c = image;
        unselectedImage = null;
        selectedImage = null;
        return true;
    }

    private Image getImage_c() {
        if (unselectedImage == null) {
            SLetterPane parent = getParent();
            if (parent == null)
                return null;
            if (image_c == null)
                return null;
            unselectedImage = getImage_a(image_c, parent.getBackground(), parent.getForeground(), parent);
        }
        return unselectedImage;
    }

    private Image getImage_d() {
        if (selectedImage == null) {
            SLetterPane parent = getParent();
            if (parent == null)
                return null;
            if (image_c == null)
                return null;
            selectedImage = getImage_a(image_c, parent.getSelectionColor(), parent.getSelectedTextColor(), parent);
        }
        return selectedImage;
    }

    private static BufferedImage getImage_a(BufferedImage image, Color color, Color color1, Component component) {
        FilteredImageSource imageSource = new FilteredImageSource(image.getSource(), new s_ImageFilter(color, color1));
        BufferedImage image1 = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = image1.getGraphics();
        g.drawImage(component.createImage(imageSource), 0, 0, component);
        return image1;
    }

    private static final Color defaultColor = Color.BLUE;
    private Image image;
    private BufferedImage image_c;
    private BufferedImage unselectedImage;
    private BufferedImage selectedImage;
    private boolean resize;
    private boolean maximizable;
    private String text;

    private volatile PropertyChangeListener propertyChangeListener;
}
