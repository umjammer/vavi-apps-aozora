/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JComponent;


public class SImageIcon extends ImageIcon {

    private void init(URL url, String desc) {
        final URL theUrl = url;
        final String theDesc = desc;
        Thread thread = new Thread(new Runnable() {
            public final void run() {
                image = Toolkit.getDefaultToolkit().getImage(theUrl);
                if (image != null) {
                    description = theDesc;
                    loadImage(image);
                }
            }
        });
        thread.start();
    }

    public SImageIcon(URL url, JComponent comp) {
        status = 0;
        description = null;
        width = -1;
        height = -1;
        loaded = false;
        comp.repaint();
        init(url, url.toExternalForm());
    }

    protected void loadImage(Image image) {
        synchronized (tracker) {
            int id = incrementCount();
            tracker.addImage(image, id);
            int theId = id;
            Image theImage = image;
            try {
                tracker.waitForID(theId, 0L);
            } catch (InterruptedException _ex) {
                System.out.println("INTERRUPTED while loading Image");
            }
            status = tracker.statusID(theId, false);
            tracker.removeImage(theImage, theId);
            width = theImage.getWidth(observer);
            height = theImage.getHeight(observer);
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            loaded = true;
        }
    }

    private static int incrementCount() {
        synchronized (tracker) {
            return ++count;
        }
    }

    public int getImageLoadStatus() {
        return status;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
        loadImage(image);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public synchronized void paintIcon(Component owner, Graphics g, int x, int y) {
        if (loaded) {
            if (observer == null) {
                g.drawImage(image, x, y, owner);
            } else {
                g.drawImage(image, x, y, observer);
            }
        } else {
            final Component theOwner = owner;
            final Graphics theG = g;
            final int theX = x;
            final int theY = y;
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(500L);
                        paintImage(theOwner, theG, theX, theY);
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.err);
                    }
                }
            });
            thread.start();
        }
    }

    private void paintImage(Component owner, Graphics g, int x, int y) {
        if (loaded)
            if (observer == null) {
                g.drawImage(image, x, y, owner);
                return;
            } else {
                g.drawImage(image, x, y, observer);
                return;
            }
        try {
            Thread.sleep(250L);
            paintImage(owner, g, x, y);
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }

    public int getIconWidth() {
        return width;
    }

    public int getIconHeight() {
        return height;
    }

    public void setImageObserver(ImageObserver observer) {
        this.observer = observer;
    }

    public ImageObserver getImageObserver() {
        return observer;
    }

    public String toString() {
        if (description != null)
            return description;
        else
            return super.toString();
    }

    transient Image image;
    transient int status;
    ImageObserver observer;
    String description;
    private static int count;
    int width;
    int height;
    boolean loaded;
}
