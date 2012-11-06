/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.letter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

import com.soso.sgui.SFontChooser;


public class SVTest {

    public static void main(String[] args) throws Exception {
        System.out.println('—');
        final SLetterPane letterPane = SLetterPane.newInstance(SLetterConstraint.ORIENTATION.TBRL);
        letterPane.setRowColCountChangable(true);
        letterPane.setFontSizeChangable(false);
        letterPane.setRowRange(80);
        letterPane.setColRange(80);
        letterPane.setColCount(7);
        letterPane.setRowCount(10);
        letterPane.setRowSpace(40);
        letterPane.setColSpace(20);
        letterPane.setBackground(Color.LIGHT_GRAY);
        letterPane.setForeground(Color.DARK_GRAY);
        letterPane.setSelectionColor(Color.BLUE);
        letterPane.setSelectedTextColor(Color.CYAN);
        letterPane.setFont(new Font("IPA明朝", Font.PLAIN, 12));
        String s = "「てすと」『テストー。』漢字あぁいぃうぅえぇおぉつっ。きゃチュぴょ！て、す・と。\nTest, test. It's test!?";
        s += '—';
        for (char c : s.toCharArray()) {
            boolean flag = letterPane.addCell(SLetterCellFactory.getInstance().createGlyphCell(c, c != '漢' ? c != '字' ? null : "じ".toCharArray() : "かん".toCharArray()));
            if (!flag) {
                System.out.println("OVER at " + c);
                break;
            }
            if (c == '字') {
                letterPane.addCell(SLetterCellFactory.getInstance().createImageCell((new ImageIcon(new URL("http://www.aozora.gr.jp/gaiji/1-85/1-85-25.png"))).getImage(), "PMG".toCharArray()));
                letterPane.addCell(SLetterCellFactory.getInstance().createImageCell(new URL("http://www.aozora.gr.jp/gaiji0213/kigou/1_2_22.gif"), true, true, "GIF".toCharArray(), null, "これは画像"));
                letterPane.addCell(SLetterCellFactory.getInstance().createImageCell(Toolkit.getDefaultToolkit().createImage(new URL("http://www.aozora.gr.jp/gaiji0213/kigou/1_2_22.gif"))));
            }
        }

        final JFrame frame = new JFrame("SVtest");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new JButton(new AbstractAction("フォント:" + letterPane.getFont()) {
            public final void actionPerformed(ActionEvent actionevent) {
                Font font = SFontChooser.showDialog(frame, getValue("Name").toString(), letterPane.getFont());
                System.out.println("newFont:" + font);
                putValue("Name", "フォント:" + font);
                letterPane.setFont(font);
            }
        }), BorderLayout.NORTH);
        frame.getContentPane().add(letterPane, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            public final void windowClosing(WindowEvent event) {
                System.exit(0);
            }
        });
    }
}
