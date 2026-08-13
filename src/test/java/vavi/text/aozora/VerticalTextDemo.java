package vavi.text.aozora;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class VerticalTextDemo extends JPanel {

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. Cast to Graphics2D for advanced control
        Graphics2D g2d = (Graphics2D) g;

        // 2. Turn on Antialiasing for smooth text
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 3. Define your font and text
        Font font = new Font("SansSerif", Font.PLAIN, 40);
        g2d.setFont(font);
        String text = "Vertical Text Keeping Proportions";

        // 4. Save the current transform so we don't mess up other drawings
        AffineTransform originalTransform = g2d.getTransform();

        // ---------------------------------------------------------
        // LOGIC START: Draw text at x=100, y=50
        // ---------------------------------------------------------
        int x = 100;
        int y = 50;

        // Move the "pen" to the starting position
        g2d.translate(x, y);

        // Rotate 90 degrees clockwise (Math.PI/2)
        g2d.rotate(Math.PI / 2);

        // Draw the string. 
        // Note: We draw at (0,0) because we already translated to (x,y).
        // The text flows along the new "rotated X axis" (which points down).
        g2d.setColor(Color.BLACK);
        g2d.drawString(text, 0, 0);

        // ---------------------------------------------------------
        // LOGIC END: Restore the original transform
        // ---------------------------------------------------------
        g2d.setTransform(originalTransform);

        // Helper visualization (Optional): Draw a red dot at the insertion point
        g2d.setColor(Color.RED);
        g2d.fillOval(x - 3, y - 3, 6, 6);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Vertical Text Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new VerticalTextDemo());
        frame.setSize(400, 600);
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setVisible(true);
    }
}
