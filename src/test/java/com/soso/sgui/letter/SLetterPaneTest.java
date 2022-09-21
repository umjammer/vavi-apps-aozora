/*
 * Copyright (c) 2011 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JFrame;
import javax.swing.UIManager;

import com.soso.aozora.data.AozoraBunkoRuby;
import com.soso.sgui.letter.SLetterCellFactory;
import com.soso.sgui.letter.SLetterConstraint;
import com.soso.sgui.letter.SLetterPane;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import vavi.util.Debug;


/**
 * Test01. 
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2011/09/14 umjammer initial version <br>
 */
public class Test01 {

    /** */
    public static InputStream getTextInputStream(File file) throws IOException {
        List<ZipEntry> texts = new ArrayList<>();
        ZipFile zip = new ZipFile(file);
        Enumeration<?> e = zip.getEntries();
        while (e.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) e.nextElement();
//Debug.println(entry);
            if (entry.getName().endsWith(".txt")) {
                texts.add(entry);
            }
        }
        texts.sort((o1, o2) -> (int) (o2.getSize() - o1.getSize()));

        ZipEntry entry = texts.get(0);
Debug.println(entry);
        return zip.getInputStream(entry);
    }

    /**
     * @param args 0: input
     */
    public static void main(String[] args) throws Exception {
        String file = args[0];

        InputStream text = getTextInputStream(new File(file));

        Reader reader = new BufferedReader(new InputStreamReader(text, StandardCharsets.UTF_8));
        Writer writer = new StringWriter();

        AozoraBunkoRuby app = new AozoraBunkoRuby(reader, writer);
        app.printHtml();

        List<Character> chars = writer.toString().chars().mapToObj(c -> (char) c).collect(Collectors.toList());
Debug.println("chars: " + chars.size());

        SLetterPane letterPane = SLetterPane.newInstance(SLetterConstraint.ORIENTATION.TBRL);
        letterPane.setRowColCountChangable(true);
        letterPane.setFontSizeChangable(true);
        letterPane.setRowRange(90);
        letterPane.setColRange(90);
        letterPane.setColCount(40);
        letterPane.setRowCount(40);
        letterPane.setRowSpace(5); // horizontal
        letterPane.setColSpace(2); // vertical
        letterPane.setBackground(new Color(253, 248, 225));
        letterPane.setForeground(Color.black);
        letterPane.setSelectionColor(UIManager.getColor("TextArea.selectionBackground"));
        letterPane.setSelectedTextColor(UIManager.getColor("TextArea.selectionForeground"));
        letterPane.setFont(new Font("Hiragino Mincho ProN", Font.PLAIN, 32));
        letterPane.setLetterBorderRendarer(null);

        for (char c : chars) {
            boolean flag = letterPane.addCell(SLetterCellFactory.getInstance().createGlyphCell(c));
            if (!flag) {
Debug.println("OVER at " + c);
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