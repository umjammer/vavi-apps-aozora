
package com.soso.sgui.img;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.soso.sgui.SGUIUtil;
import vavi.util.Debug;


final class GrassPanel extends JPanel {

    private static final long serialVersionUID = 0x5baf68d6L;
    private static final Dimension size_a = new Dimension(10, 10);
    private static final Dimension size_b = new Dimension(16, 16);
    private static final Color oddColor = Color.WHITE;
    private static final Color evenColor = new Color(240, 240, 240);
    private static final Color color_e = Color.BLACK;
    private static final Color color_f = Color.ORANGE;
    private static final Color color_g = Color.GRAY;
    private static final Color color_h = Color.RED;

    private final Dimension size_i;
    private final Collection<ImagePanel> imagePanels = new HashSet<>();
    private ImageIcon imageIcon;
    private Image scaledImage;
    private Rectangle imageBounds;
    private double scale;
    private boolean resizerPressed;
    private boolean moverPressed;
    private boolean isResize;
    private Rectangle resizerBounds;
    private Rectangle moverBounds;
    private Rectangle rect_t;
    private Point point_u;
    private Dimension size_v;
    private boolean flag_x;
    private int hints;
    private boolean isOriginalSize;

    ExecutorService ses = Executors.newSingleThreadExecutor();

    GrassPanel(Dimension size) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
Debug.println("shutdownHook: " + getClass().getName());
            ses.shutdown();
        }));
        hints = Image.SCALE_SMOOTH;
        isOriginalSize = true;
        this.size_i = size;
        reset();
        initGUI();
    }

    public void paint(Graphics g) {
        super.paint(g);
        Color originalColor = g.getColor();
        paintChecker(g);
        if (scaledImage != null)
            paintImage((Graphics2D) g);
        paint_c(g);
        g.setColor(originalColor);
    }

    private void paintChecker(Graphics g) {

        g.setColor(oddColor);
        g.fillRect(0, 0, getWidth(), getHeight());
        int bw = size_b.width;
        int bh = size_b.height;
        if (scale > 1.0D) {
            bw = Math.max((int) (bw / scale), 1);
            bh = Math.max((int) (bh / scale), 1);
        }
        int w = getWidth() / bw + 1;
        int h = getHeight() / bh + 1;

        for (int y = 0; y < w; y++) {
            for (int x = 0; x < h; x++) {
                if (y % 2 + x % 2 == 1) {
                    g.setColor(oddColor);
                } else {
                    g.setColor(evenColor);
                }
                g.fillRect(y * bw, x * bh, bw, bh);
            }
        }
    }

    private void paintImage(Graphics2D g) {
        Composite composite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(3, 0.5f));
        g.drawImage(scaledImage, imageBounds.x, imageBounds.y, this);
        g.setComposite(composite);
        g.drawImage(scaledImage,
                    moverBounds.x,
                    moverBounds.y,
                    moverBounds.x + moverBounds.width,
                    moverBounds.y + moverBounds.height,
                    moverBounds.x - imageBounds.x,
                    moverBounds.y - imageBounds.y,
                    (moverBounds.x - imageBounds.x) + moverBounds.width,
                    (moverBounds.y - imageBounds.y) + moverBounds.height,
                    this);
    }

    private void paint_c(Graphics g) {
        g.setColor(color_e);
        g.drawRect(moverBounds.x, moverBounds.y, moverBounds.width, moverBounds.height);
        if (isResize) {
            g.setColor(color_h);
        } else {
            if (flag_x) {
                g.setColor(color_f);
            } else {
                g.setColor(color_g);
            }
        }
        g.fillRect(resizerBounds.x, resizerBounds.y, resizerBounds.width, resizerBounds.height);
        g.setColor(color_e);
        g.drawRect(resizerBounds.x, resizerBounds.y, resizerBounds.width, resizerBounds.height);
    }

    void startTask() {
        ses.submit(() -> {
            try {
                Thread.sleep(1000L);
            } catch (Exception e) {
                e.printStackTrace();
                flag_x = !flag_x;
                repaint(resizerBounds);
            }
        });
    }

    void setImagePanel(ImagePanel panel) {
        synchronized (imagePanels) {
            imagePanels.add(panel);
        }
    }

    final void setSize_a(Dimension size) {
        if (size == null) {
            size_v.width = imageBounds.width;
            size_v.height = imageBounds.height;
        } else {
            size_v.width = Math.min((int) (size.width / scale), imageBounds.width);
            size_v.height = Math.min((int) (size.height / scale), imageBounds.height);
            if (size_v.width < moverBounds.width ||
                size_v.height < moverBounds.height ||
                moverBounds.x < imageBounds.x ||
                moverBounds.y < imageBounds.y)
                setSize_b(size_v);
        }
    }

    private void setSize_b(Dimension size) {
        int w = Math.min(size.width, imageBounds.width);
        int h = Math.min(size.height, imageBounds.height);
        int x = Math.min(Math.max(moverBounds.x, imageBounds.x), (imageBounds.x + imageBounds.width) - w);
        int y = Math.min(Math.max(moverBounds.y, imageBounds.y), (imageBounds.y + imageBounds.height) - h);
        resize_a(x, y, w, h);
        update_h();
    }

    private void reset() {
        imageIcon = null;
        scaledImage = null;
        imageBounds = new Rectangle();
        resizerBounds = new Rectangle();
        moverBounds = new Rectangle();
        size_v = new Dimension();
    }

    private void initGUI() {
        setLayout(null);
        SGUIUtil.setSizeALL(this, size_i);
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    void loadImage(Image image) {
        if (image == null) {
            reset();
            return;
        }
        imageIcon = new ImageIcon(image);
        int w1 = imageIcon.getIconWidth();
        int h1 = imageIcon.getIconHeight();
        double sx = (double) w1 / (double) size_i.width;
        double sy = (double) h1 / (double) size_i.height;
        if (sx > 1.0 || sy > 1.0) {
            scale = Math.max(sx, sy);
            if (sx < sy) {
                w1 = -1;
                h1 = size_i.height;
            } else {
                w1 = size_i.width;
                h1 = -1;
            }
        } else {
            scale = 1.0;
        }
        scaledImage = image.getScaledInstance(w1, h1, hints);
        MediaTracker mt = new MediaTracker(this);
        int wait = (int) (System.currentTimeMillis() / 1000L);
        mt.addImage(scaledImage, wait);
        try {
            mt.waitForID(wait);
        } catch (InterruptedException e) {
            throw new IllegalStateException("INTERRUPTED while loading Image");
        }
        mt.statusID(wait, false);
//        boolean flag = false;
        mt.removeImage(scaledImage, wait);
        int w = scaledImage.getWidth(this);
        int h = scaledImage.getHeight(this);
        imageBounds.x = (size_i.width - w) / 2;
        imageBounds.y = (size_i.height - h) / 2;
        imageBounds.width = w;
        imageBounds.height = h;
        setSize_a(imageBounds.getSize());
        setSize_b(imageBounds.getSize());
        update_h();
    }

    private void update_h() {
        int w = (int) (moverBounds.width * scale);
        int h = (int) (moverBounds.height * scale);
        Image image;
        if (isOriginalSize)
            image = imageIcon.getImage();
        else {
            image = createImage(w, h);
            image.getGraphics().drawImage(imageIcon.getImage(),
                                          0, 0, w, h,
                                          (int) ((moverBounds.x - imageBounds.x) * scale),
                                          (int) ((moverBounds.y - imageBounds.y) * scale),
                                          (int) (((moverBounds.x - imageBounds.x) + moverBounds.width) * scale),
                                          (int) (((moverBounds.y - imageBounds.y) + moverBounds.height) * scale),
                                          this);
        }
        SImage image1 = new SImage(image, new Dimension(w, h));
        synchronized (imagePanels) {
            for (ImagePanel panel : imagePanels) {
                panel.update_a(image1);
            }
        }
    }

    private boolean containsMover(int x, int y) {
        return moverBounds.contains(x, y);
    }

    private boolean containsResizer(int x, int y) {
        return resizerBounds.contains(x, y);
    }

    private void resize_a(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0)
            return;
        if (x + width > imageBounds.x + imageBounds.width)
            return;
        if (y + height > imageBounds.y + imageBounds.height) {
            return;
        }
        x = Math.max(x, 0);
        y = Math.max(y, 0);
        moverBounds.x = x;
        moverBounds.y = y;
        moverBounds.width = Math.min(width, size_v.width);
        moverBounds.height = Math.min(height, size_v.height);
        resizerBounds.x = Math.max((moverBounds.x + moverBounds.width) - size_a.width, 0);
        resizerBounds.y = Math.max((moverBounds.y + moverBounds.height) - size_a.height, 0);
        resizerBounds.width = size_a.width;
        resizerBounds.height = size_a.width;
        repaint();
    }

    private void doResize_c(int x, int y) {
        int x1 = x - point_u.x;
        int w = rect_t.width + x1;
        if (w <= 0)
            w = 1;
        if (rect_t.x + w > imageBounds.x + imageBounds.width)
            w = (imageBounds.x + imageBounds.width) - rect_t.x;
        int y1 = y - point_u.y;
        int h = rect_t.height + y1;
        if (h <= 0)
            h = 1;
        if (rect_t.y + h > imageBounds.y + imageBounds.height)
            h = (imageBounds.y + imageBounds.height) - rect_t.y;
        resize_a(rect_t.x, rect_t.y, w, h);
    }

    private void doMove_d(int x, int y) {
        int x1 = x - point_u.x;
        int w = rect_t.x + x1;
        if (w < imageBounds.x)
            w = imageBounds.x;
        if (w + rect_t.width > imageBounds.x + imageBounds.width)
            w = (imageBounds.x + imageBounds.width) - rect_t.width;
        int y1 = y - point_u.y;
        int h = rect_t.y + y1;
        if (h < imageBounds.y)
            h = imageBounds.y;
        if (h + rect_t.height > imageBounds.y + imageBounds.height)
            h = (imageBounds.y + imageBounds.height) - rect_t.height;
        resize_a(w, h, rect_t.width, rect_t.height);
    }

    private final MouseAdapter mouseAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent event) {
            int x = event.getX();
            int y = event.getY();
            if (containsResizer(x, y))
                resizerPressed = true;
            else if (containsMover(x, y))
                moverPressed = true;
            rect_t = moverBounds.getBounds();
            point_u = new Point(x, y);
        }

        @Override
        public void mouseReleased(MouseEvent event) {
            if (resizerPressed) {
                resizerPressed = false;
                update_h();
            }
            if (moverPressed) {
                moverPressed = false;
                update_h();
            }
        }

        @Override
        public void mouseDragged(MouseEvent event) {
            if (resizerPressed)
                doResize_c(event.getX(), event.getY());
            if (moverPressed)
                doMove_d(event.getX(), event.getY());
        }

        @Override
        public void mouseMoved(MouseEvent event) {
            if (containsResizer(event.getX(), event.getY())) {
                isResize = true;
                setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
            } else if (containsMover(event.getX(), event.getY())) {
                isResize = false;
                setCursor(new Cursor(Cursor.MOVE_CURSOR));
            } else {
                isResize = false;
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            repaint();
        }
    };

    void setHints(int hints) {
        this.hints = hints;
    }

    void setOriginalSize(boolean isOriginalSize) {
        this.isOriginalSize = isOriginalSize;
    }
}
