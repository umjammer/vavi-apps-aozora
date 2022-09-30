/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.letter;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.parser.SVGLoader;
import vavi.util.Debug;


public class SLetterSvgCell extends SLetterGlyphCell {

    static Logger logger = Logger.getLogger(SLetterSvgCell.class.getName());

    ImageReader reader;

    protected SLetterSvgCell(byte[] image, char[] rubys, Font font) {
        super('※', rubys, font);
        if (image == null) {
            throw new IllegalArgumentException("image cannot be null");
        }
        this.image = image;
        this.reader = ImageIO.getImageReadersByFormatName("svg").next();
    }

    @Override
    public String getText() {
        return "(svg)";
    }

    @Override
    protected void setParent(SLetterPane parent) {
        super.setParent(parent);
    }

    @Override
    public void paintCell(Graphics2D g, Rectangle cellBounds) {
        SLetterConstraint.ORIENTATION orientation = getOrientation();
        if (orientation == null) {
Debug.println(Level.FINE, "orientation is null");
            return;
        }
        Color color = g.getColor();

        int w = (int) (cellBounds.width * 0.8); // svg is little smaller than font
        int h = (int) (cellBounds.height * 0.8);
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

        // TODO color, selection
        InputStream is = new ByteArrayInputStream(this.image);
        SVGLoader loader = new SVGLoader();
        SVGDocument document = loader.load(is);
        document.render(getParent(), g, new ViewBox(x, y, w, h));

        if (logger.isLoggable(Level.FINE)) {
            g.setColor(new Color(153, 153, 255));
            g.drawRect(x, y, w, h);
        }
        if (trans != null)
            g.setTransform(trans);
        g.setColor(color);
    }

    private static final Color defaultColor = Color.BLUE;
    private byte[] image;
}
