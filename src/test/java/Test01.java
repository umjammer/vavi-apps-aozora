/*
 * Copyright (c) 2011 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JFrame;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import com.soso.sgui.letter.SLetterCellFactory;
import com.soso.sgui.letter.SLetterConstraint;
import com.soso.sgui.letter.SLetterPane;

/**
 * Test01. 
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2011/09/14 umjammer initial version <br>
 */
public class Test01 {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String file = args[0];

        List<ZipEntry> texts = new ArrayList<ZipEntry>();
        ZipFile zip = new ZipFile(new File(file), "JISAutoDetect");
        Enumeration<?> e = zip.getEntries();
        while (e.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) e.nextElement();
//System.err.println(entry);
            if (entry.getName().endsWith(".txt")) {
                texts.add(entry);
            }
        }
        Collections.sort(texts, new Comparator<ZipEntry>() {
            @Override
            public int compare(ZipEntry o1, ZipEntry o2) {
                return (int) (o2.getSize() - o1.getSize());
            }
        });

        ZipEntry entry = texts.get(0);
System.err.println(entry);

        List<Character> chars = new ArrayList<Character>();
        Reader reader = new InputStreamReader(zip.getInputStream(entry), "JISAutoDetect");
        while (true) {
            int r = reader.read();
            if (r < 0) {
                break;
            }
            chars.add((char) r);
        }
System.err.println("chars: " + chars.size());

        SLetterPane letterPane = SLetterPane.newInstance(SLetterConstraint.ORIENTATION.TBRL);
        letterPane.setRowColCountChangable(true);
        letterPane.setFontSizeChangable(true);
        letterPane.setRowRange(90);
        letterPane.setColRange(90);
        letterPane.setColCount(40);
        letterPane.setRowCount(40);
        letterPane.setRowSpace(5); // horizontal
        letterPane.setColSpace(2); // vertical
        letterPane.setBackground(Color.LIGHT_GRAY.brighter());
        letterPane.setForeground(Color.black);
        letterPane.setSelectionColor(Color.BLUE);
        letterPane.setSelectedTextColor(Color.CYAN);
        letterPane.setFont(new Font("Hiragino Mincho Pro", Font.PLAIN, 16));
//        letterPane.setLetterBorderRendarer(null);

        for (char c : chars) {
            boolean flag = letterPane.addCell(SLetterCellFactory.getInstance().createGlyphCell(c));
            if (!flag) {
                System.out.println("OVER at " + c);
                break;
            }
        }

        final JFrame frame = new JFrame("Test01");
        frame.getContentPane().add(letterPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        letterPane.repaint();
    }
}

/* */
