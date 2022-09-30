/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.letter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.apple.eawt.event.GestureUtilities;
import com.apple.eawt.event.MagnificationEvent;
import com.soso.sgui.SFontChooser;
import org.junit.jupiter.api.Test;
import vavi.util.Debug;


/**
 * many types of cells
 *
 * @see "https://glyphwiki.org/wiki/GlyphWiki"
 */
public class SVTest {

    @Test
    void test() throws Exception {
        BufferedImage image = ImageIO.read(new URL("https://glyphwiki.org/glyph/u2a6d6.svg"));
Debug.println(image);
    }

    static {
        Logger logger = Logger.getLogger("com.soso.sgui.letter.SLetterGlyphCell");
        logger.setLevel(Level.FINE);

        System.setProperty("vavi.imageio.svg.BatikSvgImageReadParam.size", "512x512");
    }

    public static void main(String[] args) throws Exception {
        final SLetterPane letterPane = SLetterPane.newInstance(SLetterConstraint.ORIENTATION.TBRL);
        letterPane.setRowColCountChangable(true);
        letterPane.setFontSizeChangable(false);
        letterPane.setRowRange(80);
        letterPane.setColRange(80);
        letterPane.setColCount(7);
        letterPane.setRowCount(10);
        letterPane.setRowSpace(40);
        letterPane.setColSpace(20);
        letterPane.setBackground(new Color(253, 248, 225));
        letterPane.setForeground(Color.DARK_GRAY);
        letterPane.setSelectionColor(Color.BLUE);
        letterPane.setSelectedTextColor(Color.CYAN);
        letterPane.setFont(new Font("Hiragino Mincho ProN", Font.PLAIN, 12));
        String s = "「てすと」『テストー。』漢字あぁいぃうぅえぇおぉつっっっ。きゃチュぴょ！て、す・と。\nTest, test. It's test!?";
        s += '—';
        for (char c : s.toCharArray()) {
            SLetterCellFactory factory = SLetterCellFactory.getInstance();
            boolean flag = letterPane.addCell(factory.createGlyphCell(c, c != '漢' ? c != '字' ? null : "じ".toCharArray() : "かん".toCharArray()));
            if (!flag) {
Debug.println("OVER at " + c);
                break;
            }
            if (c == '字') {
                letterPane.addCell(factory.createImageCell((new ImageIcon(new URL("https://www.aozora.gr.jp/gaiji/1-85/1-85-25.png"))).getImage(), "PNG".toCharArray()));
                letterPane.addCell(factory.createImageCell(new URL("https://www.aozora.gr.jp/gaiji0213/kigou/1_2_22.gif"), true, true, "GIF".toCharArray(), null, "これは画像"));
                letterPane.addCell(factory.createImageCell(ImageIO.read(new URL("https://www.aozora.gr.jp/gaiji0213/kigou/1_2_22.gif"))));
                letterPane.addCell(factory.createSvgCell(new URL("https://glyphwiki.org/glyph/u2a6d6.svg"), "SVG".toCharArray(), null));
                letterPane.addCell(factory.createSvgCell(new URL("https://glyphwiki.org/glyph/u2b81b.svg"), "SVG".toCharArray(), null));
                letterPane.addCell(factory.createSvgCell(new URL("https://glyphwiki.org/glyph/u2b81c.svg"), "SVG".toCharArray(), null));
            }
        }

        final JFrame frame = new JFrame("SVtest");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new JButton(new AbstractAction("フォント:" + letterPane.getFont()) {
            public void actionPerformed(ActionEvent actionevent) {
                Font font = SFontChooser.showDialog(frame, getValue("Name").toString(), letterPane.getFont());
Debug.println("newFont:" + font);
                putValue("Name", "フォント:" + font);
                letterPane.setFont(font);
            }
        }), BorderLayout.NORTH);
        AffineTransform at = new AffineTransform(); // TODO how to reset?
        at.translate(letterPane.getX(), letterPane.getY());
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintChildren(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
Debug.printf("%3.0f, %3.0f - %5.2f, %5.2f", at.getTranslateX(), at.getTranslateY(), at.getScaleX(), at.getScaleY());
                g2.setTransform(at);
                super.paintChildren(g2);
                g2.dispose();
            }
        };
        GestureUtilities.addGestureListenerTo(panel, new com.apple.eawt.event.GestureAdapter() {
            @Override public void magnify(MagnificationEvent me) {
                double scale = 1.0 + me.getMagnification();
                at.scale(scale, scale);
                Point p = panel.getMousePosition();
                double x = p.x * (1 - scale);
                double y = p.y * (1 - scale);
                at.translate(x, y);
                panel.repaint();
            }
        });
        panel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
Debug.println("here");
                at.scale(1, 1);
                at.translate(0, 0);
            }
        });
//        panel.addMouseWheelListener(e -> {
//            e.
//        });
        panel.add(letterPane, BorderLayout.CENTER);
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
