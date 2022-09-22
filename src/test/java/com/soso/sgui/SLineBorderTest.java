/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class SLineBorderTest {

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(800, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(0, 0));
        JPanel borderPanel = new JPanel();
        borderPanel.setBackground(Color.BLUE);
        borderPanel.setBorder(new SLineBorder(Color.RED, 100, true, 200));
        frame.getContentPane().add(borderPanel, BorderLayout.CENTER);
        borderPanel.setLayout(new BorderLayout(0, 0));
        JPanel panel = new JPanel();
        panel.setBackground(Color.LIGHT_GRAY);
        borderPanel.add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.WEST);
        frame.getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.EAST);
        frame.getContentPane().add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        frame.getContentPane().add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        frame.setVisible(true);
    }
}
