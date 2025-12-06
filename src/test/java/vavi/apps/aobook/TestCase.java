/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.aobook;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.jupiter.api.Test;


/**
 * TestCase.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-12-04 nsano initial version <br>
 */
class TestCase {

    @Test
    void test1() throws Exception {
        StyleWork styleWork = new StyleWork();
        String[] name = new String[1];
        styleWork.StyleWork_readStyle(name);
    }

    @Test
    void test2() throws Exception {
        main(new String[0]);
        Thread.sleep(1000 * 60 * 60);
    }

    public static void main(String[] args) throws Exception {
        Layout layout = new Layout();

        String text = new String(Files.readAllBytes(Paths.get("src/test/resources/sample.txt")));

        layout.layout(text);

        BufferedImage image = layout.getImage(0);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                g.drawImage(image, 0, 0, this);
            }
        };
        panel.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
