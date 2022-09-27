/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.letter;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import vavi.util.Debug;


public class SLetterImageCell extends SLetterGlyphCell {

    static Logger logger = Logger.getLogger(SLetterImageCell.class.getName());

    private static class s_ImageFilter extends RGBImageFilter {

        @Override
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
            int v = Math.round(f2);
            v = Math.max(0, v);
            return v = Math.min(255, v);
        }

        final Color color_a;
        final Color color_b;

        public s_ImageFilter(Color color, Color color1) {
            super.canFilterIndexColorModel = true;
            color_a = color;
            color_b = color1;
        }
    }

    private Map<Object, Object> hints;

    {
        hints = new HashMap<>();
        hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    protected SLetterImageCell(Image image, boolean fit, boolean magnifyable, char[] rubys, Font font, String text) {
        super('※', rubys, font);
        if (image == null) {
            throw new IllegalArgumentException("image cannot be null");
        }
        this.image = image;
        this.fit = fit;
        this.magnifyable = magnifyable;
        this.text = text;
    }

    public boolean isFit() {
        return fit;
    }

    public void setFit(boolean fit) {
        this.fit = fit;
        SLetterPane parent = getParent();
        if (parent != null)
            parent.repaint();
    }

    public boolean isMagnifyable() {
        return magnifyable;
    }

    public void setMagnifyable(boolean magnifyable) {
        this.magnifyable = magnifyable;
        SLetterPane parent = getParent();
        if (parent != null)
            parent.repaint();
    }

    @Override
    public String getText() {
        StringBuilder sb = new StringBuilder();
        if (text != null)
            sb.append(text);
        else
            sb.append("??");
        sb.append(super.getText().substring(1));
        return sb.toString();
    }

    @Override
    protected void setParent(SLetterPane parent) {
        SLetterPane oldValue = getParent();
        if (oldValue != null)
            oldValue.removePropertyChangeListener(getPropertyChangeListener());
        super.setParent(parent);
        if (parent != null)
            parent.addPropertyChangeListener(getPropertyChangeListener());
    }

    final PropertyChangeListener getPropertyChangeListener() {
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

    public void paintMagnifiedImage(Graphics2D g, Rectangle bounds) {
        if (!isMagnifyable())
            return;
        SLetterPane parent = getParent();
        if (parent == null)
            return;
        g.setRenderingHints(hints);
        Color color = g.getColor();
//        Composite composite = g.getComposite();
//        g.setComposite(AlphaComposite.getInstance(3, 0.7F));
//        g.setColor(Color.LIGHT_GRAY);
//        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
//        g.setComposite(AlphaComposite.SrcOver);
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
//        if (composite != null)
//            g.setComposite(composite);
        g.setColor(color);
    }

    @Override
    public void paintCell(Graphics2D g, Rectangle cellBounds) {
        if (!isImageDrawable()) {
Debug.println(Level.FINE, "not image ready");
            return;
        }
        SLetterConstraint.ORIENTATION orientation = getOrientation();
        if (orientation == null) {
Debug.println(Level.FINE, "orientation is null");
            return;
        }
        g.setRenderingHints(hints);
        Color color = g.getColor();
        int w = displayImage.getWidth();
        int h = displayImage.getHeight();
        int x = cellBounds.x + (cellBounds.width - w) / 2;
        int y = cellBounds.y + (cellBounds.height - h) / 2;
        double t = getRotateTheta(orientation);
        AffineTransform trans = null;
        if (t != 0.0) {
            trans = g.getTransform();
            AffineTransform affineTransform = new AffineTransform(trans);
            int x1 = cellBounds.x + cellBounds.width / 2;
            int y1 = cellBounds.y + cellBounds.height / 2;
            affineTransform.rotate(t, x1, y1);
            if (isConstraintSet(SLetterConstraint.ROTATE.LR_MIRROR)) {
                affineTransform.translate(0.0, y1);
                affineTransform.scale(1.0, -1.0);
                affineTransform.translate(0.0, -y1);
            }
            g.setTransform(affineTransform);
        }
        Image image = isConstraintSet(SLetterConstraint.SELECTION.SELECTED) ?
                getImageSelected() : getImageUnselected();
        g.drawImage(image, x, y, getParent());
Debug.println(Level.FINE, "image: " + (getRubys() != null ? new String(getRubys()) : "?") + ", " + image);
        if (magnifyable) {
            // draw clickable marker
            g.setColor(defaultColor);
            g.drawRect(cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height);
        }
        if (logger.isLoggable(Level.FINE)) {
            g.setColor(new Color(153, 153, 255));
            g.drawRect(x, y, w, h);
        }
        if (trans != null)
            g.setTransform(trans);
        g.setColor(color);
    }

    private boolean isImageDrawable() {
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
        if (fit) {
            int size = font.getSize();
            if (displayImage != null) {
                int w1 = displayImage.getWidth();
                int h2 = displayImage.getHeight();
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
        Graphics2D g = image.createGraphics();
        g.setRenderingHints(hints);
        g.drawImage(this.image, 0, 0, w, h, parent);
        g.dispose();
        displayImage = image;
        unselectedImage = null;
        selectedImage = null;
        return true;
    }

    private Image getImageUnselected() {
        if (unselectedImage == null) {
            SLetterPane parent = getParent();
            if (parent == null)
                return null;
            if (displayImage == null)
                return null;
            unselectedImage = getFilteredImage(displayImage, parent.getBackground(), parent.getForeground(), parent);
        }
        return unselectedImage;
    }

    private Image getImageSelected() {
        if (selectedImage == null) {
            SLetterPane parent = getParent();
            if (parent == null)
                return null;
            if (displayImage == null)
                return null;
            selectedImage = getFilteredImage(displayImage, parent.getSelectionColor(), parent.getSelectedTextColor(), parent);
        }
        return selectedImage;
    }

    private BufferedImage getFilteredImage(BufferedImage image, Color color, Color color1, Component observer) {
        FilteredImageSource imageSource = new FilteredImageSource(image.getSource(), new s_ImageFilter(color, color1));
        BufferedImage filteredImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = filteredImage.createGraphics();
        g.setRenderingHints(hints);
        g.drawImage(observer.createImage(imageSource), 0, 0, observer);
        return filteredImage;
    }

    private static final Color defaultColor = Color.BLUE;
    private Image image;
    private BufferedImage displayImage;
    private BufferedImage unselectedImage;
    private BufferedImage selectedImage;
    private boolean fit;
    private boolean magnifyable;
    private String text;

    private volatile PropertyChangeListener propertyChangeListener;
}
