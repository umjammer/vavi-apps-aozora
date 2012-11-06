/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class SFlowLayoutTest {

    public static void main(String[] args) {
        SFlowLayout flowLayout = new SFlowLayout();
        flowLayout.setOrigin(SFlowLayout.RIGHT_TOP);
        flowLayout.setAxis(SFlowLayout.VERTICAL);
        flowLayout.setFix(SFlowLayout.CENTER);
        flowLayout.setAlign(SFlowLayout.BOTH);
        flowLayout.setValign(SFlowLayout.BOTH);
        flowLayout.setNowrap(false);
        flowLayout.setItemSpace(3);
        flowLayout.setLineSpace(4);
        flowLayout.setTopMargin(50);
        flowLayout.setTopMargin(50);
        flowLayout.setLeftMargin(10);
        flowLayout.setRightMargin(20);
        flowLayout.setBottomMargin(5);
        final JPanel panel = new JPanel(flowLayout);
        int i = 0;
        Component[] components = {
            SFlowLayout.createGlue(),
            SFlowLayout.createWrap(),
            SFlowLayout.createWrap(50),
            SFlowLayout.createSpace(0),
            SFlowLayout.createBand(50)
        };
        for (int j = 0; j <= components.length; j++) {
            for (int k = i + 15; i < k; i++)
                panel.add(new JButton("C" + (i + 1)));

            if (j < components.length)
                panel.add(components[j]);
        }

        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.addComponentListener(new ComponentAdapter() {
            public final void componentResized(ComponentEvent event) {
                panel.doLayout();
                Dimension size = panel.getSize();
                Dimension preferredSize = panel.getPreferredSize();
                frame.setTitle("cSize[width=" + size.width + ";height=" + size.height + "]" + "  " + "pSize[width=" + preferredSize.width + ";height=" + preferredSize.height + "]");
            }
        });
        frame.setSize(300, 300);
        frame.setVisible(true);
    }

    private SFlowLayoutTest() {
    }
}
