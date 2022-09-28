/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.boot;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;


public class AozoraMain {

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(Math.min(screen.width, 1024), Math.min(screen.height, 768));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        AozoraContext context = AozoraBootLoader.load(frame, args);
        frame.setTitle(context.getManifest().getSpecificationTitle() + " powered by " + context.getManifest().getSpecificationVendor());
        frame.setVisible(true);
    }
}
