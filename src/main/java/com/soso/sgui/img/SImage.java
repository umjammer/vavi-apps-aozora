
package com.soso.sgui.img;

import java.awt.Dimension;
import java.awt.Image;


final class SImage {

    private Image image;
    private Dimension size;

    SImage(Image image, Dimension size) {
        this.image = image;
        this.size = size;
    }

    final Image getImage() {
        return image;
    }

    final Dimension getSize() {
        return size;
    }
}
