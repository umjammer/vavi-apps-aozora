
package com.soso.sgui.img;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.soso.sgui.SGUIUtil;


final class ImagePanel extends JPanel {

    private static final long serialVersionUID = 0x530116b4L;
    private static final Dimension size_a = new Dimension(16, 16);
    private static final Color oddColor = Color.WHITE;
    private static final Color evenColor = new Color(240, 240, 240);
    private final Dimension size_d;
    private Image image_e;
    private Dimension size_f;
    private Rectangle rect_g;
    private SImageResizerPane.SizeOption sizeOption;

    ImagePanel(Dimension size) {
        size_d = size;
        setSize_b();
    }

    public void update_a(SImage image) {
        image_e = image.getImage();
        size_f = image.getSize();
        repaint();
    }

    public void paint(Graphics g) {
        Color color = g.getColor();
        paintChecker(g);
        if (image_e != null && sizeOption != null)
            paintImage(g);
        g.setColor(color);
    }

    private void paintChecker(Graphics g) {
        g.setColor(oddColor);
        g.fillRect(0, 0, getWidth(), getHeight());
        int w = getWidth() / size_a.width + 1;
        int h = getHeight() / size_a.height + 1;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (x % 2 + y % 2 == 1) {
                    g.setColor(oddColor);
                } else {
                    g.setColor(evenColor);
                }
                g.fillRect(x * size_a.width, y * size_a.height, size_a.width, size_a.height);
            }
        }
    }

    private void paintImage(Graphics g) {
        switch (sizeOption) {
        case ORIGINAL:
            setSize_a(size_f.width, size_f.height);
            break;
        case RATIO_EXTEND:
            double d1 = (double) size_f.width / (double) size_d.width;
            double d2 = (double) size_f.height / (double) size_d.height;
            double d3 = Math.max(d1, d2);
            setSize_a((int) (size_f.width / d3), (int) (size_f.height / d3));
            break;
        case FULL_EXTEND:
            setSize_a(getWidth(), getHeight());
            break;
        default:
            throw new IllegalStateException("Unknown SizeOption :" + sizeOption);
        }
        g.drawImage(image_e, rect_g.x, rect_g.y, rect_g.width, rect_g.height, this);
    }

    BufferedImage getImage_a() {
        if (image_e == null || rect_g == null) {
            return null;
        } else {
            BufferedImage image = new BufferedImage(rect_g.width, rect_g.height, 1);
            image.getGraphics().drawImage(image_e, 0, 0, rect_g.width, rect_g.height, this);
            return image;
        }
    }

    Image getImage_a(boolean flag) {
        if (flag)
            return image_e;
        else
            return getImage_a();
    }

    private void setSize_a(int w, int h) {
        if (getWidth() < w || getHeight() < h) {
            throw new IllegalStateException("Over preview size. Pane size[w=" + getWidth() + ",h=" + getHeight() + "]" + " Image size[w="+ w + ",h=" + h + "]");
        }
        rect_g.x = (getWidth() - w) / 2;
        rect_g.y = (getHeight() - h) / 2;
        rect_g.width = w;
        rect_g.height = h;
    }

    void setSizeOption(SImageResizerPane.SizeOption scale) {
        this.sizeOption = scale;
        repaint();
    }

    private void setSize_b() {
        rect_g = new Rectangle();
        SGUIUtil.setSizeALL(this, size_d);
    }
}
