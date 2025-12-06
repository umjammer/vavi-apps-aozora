package vavi.text.aozora;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;


public class ProportionalVerticalEnglishWithKerning extends JPanel {

    private final Font jaFont = new Font("Noto Serif JP", Font.PLAIN, 36);
    private final Font enFont = new Font("EB Garamond", Font.PLAIN, 36); // Proportional font with kerning

    private final int COLUMN_X = 400;
    private final int LINE_HEIGHT = 52;
    private final int START_Y = 80;

    final String text = "「Magic Missile」が炸裂した！！\n" +
            "ＨＰが０になった……\n" +
            "Level 100 achieved.\n" +
            "This is the real deal with kerning preserved.";

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        g2.setColor(new Color(250, 248, 240));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(Color.BLACK);

        FontRenderContext frc = g2.getFontRenderContext();

        int y = START_Y;
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '\n') {
                y += LINE_HEIGHT / 3;
                i++;
                continue;
            }
            if (isLatin(c)) {
                // Collect entire Latin block including spaces
                StringBuilder block = new StringBuilder();
                while (i < text.length() && (isLatin(text.charAt(i)) || text.charAt(i) == ' ')) {
                    block.append(text.charAt(i));
                    i++;
                }
                // Render the whole block using GlyphVector to preserve kerning and proportional spacing
                y += drawProportionalRotatedBlock(g2, block.toString(), COLUMN_X, y, enFont, frc);
            } else {
                // Normal Japanese character, centered
                g2.setFont(jaFont);
                FontMetrics fm = g2.getFontMetrics();
                int w = fm.charWidth(c);
                g2.drawString(String.valueOf(c), COLUMN_X - w / 2, y + LINE_HEIGHT - 8);
                y += LINE_HEIGHT;
                i++;
            }
            if (y > getHeight() - 100) break;
        }

        g2.dispose();
    }

    private int drawProportionalRotatedBlock(Graphics2D g2, String block, int columnX, int baseY, Font font, FontRenderContext frc) {
        if (block.isEmpty()) return 0;

        GlyphVector gv = font.createGlyphVector(frc, block);
        double totalHeight = gv.getLogicalBounds().getWidth(); // Horizontal width becomes vertical height

        for (int k = 0; k < gv.getNumGlyphs(); k++) {
            Point2D pos = gv.getGlyphPosition(k);
            double glyphY = baseY + pos.getX();

            Shape glyph = gv.getGlyphOutline(k);

            AffineTransform at = new AffineTransform();
            at.translate(columnX, glyphY);
            at.rotate(Math.toRadians(90)); // Clockwise 90 degrees

            // Align by left side bearing (no centering, straight alignment on one side)
            double lsb = gv.getGlyphMetrics(k).getLSB();
            at.translate(0, lsb); // Shift in rotated y to align the left edge

            g2.fill(at.createTransformedShape(glyph));
        }

        return (int) (totalHeight + LINE_HEIGHT - 40); // Add back some spacing adjustment
    }

    private boolean isLatin(char c) {
        return (c >= '!' && c <= '~') || (c >= 'Ａ' && c <= 'Ｚ') || (c >= 'ａ' && c <= 'ｚ') || (c >= '０' && c <= '９');
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Vertical Japanese Novel Renderer");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ProportionalVerticalEnglishWithKerning vjr = new ProportionalVerticalEnglishWithKerning();
            vjr.setPreferredSize(new Dimension(600, 900));
            JScrollPane sp = new JScrollPane(VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
            sp.setViewportView(vjr);
            f.getContentPane().add(sp);
            f.pack();
            f.setVisible(true);
        });
    }
}
