/*
 * Copyright (c) 2011 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.text.aozora.viewer;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.UIManager;

import com.soso.aozora.data.AozoraContentsParser;
import com.soso.aozora.data.AozoraContentsParserHandler;
import com.soso.sgui.letter.SLetterCell;
import com.soso.sgui.letter.SLetterCellFactory;
import com.soso.sgui.letter.SLetterConstraint;
import com.soso.sgui.letter.SLetterPane;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import vavi.text.aozora.converter.AozoraBunkoRuby;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * Component -> Application.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2011/09/14 umjammer initial version <br>
 */
@PropsEntity(url = "file:local.properties")
public class SLetterPaneTest {

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

    @Property(name = "aozora.zip")
    String file;

    /**
     * @param args 0: input
     */
    public static void main(String[] args) throws Exception {
        SLetterPaneTest app = new SLetterPaneTest();
        PropsEntity.Util.bind(app);

        String file = app.file != null ? app.file : args[0];

        InputStream text = getTextInputStream(new File(file));

        Reader reader = new BufferedReader(new InputStreamReader(text, StandardCharsets.UTF_8));
        Writer writer = new StringWriter();

        AozoraBunkoRuby converter = new AozoraBunkoRuby(reader, writer);
        converter.printHtml();

//        List<Character> chars = writer.toString().chars().mapToObj(c -> (char) c).collect(Collectors.toList());
//Debug.println("chars: " + chars.size());

        SLetterPane letterPane = SLetterPane.newInstance(SLetterConstraint.ORIENTATION.TBRL);
        letterPane.setRowColCountChangable(true);
        letterPane.setFontSizeChangable(true);
        letterPane.setRowRange(8192);
        letterPane.setColRange(40);
        letterPane.setColCount(40);
        letterPane.setRowCount(40);
        letterPane.setRowSpace(10); // horizontal
        letterPane.setColSpace(10); // vertical
        letterPane.setBackground(new Color(253, 248, 225));
        letterPane.setForeground(Color.black);
        letterPane.setSelectionColor(UIManager.getColor("TextArea.selectionBackground"));
        letterPane.setSelectedTextColor(UIManager.getColor("TextArea.selectionForeground"));
        letterPane.setFont(new Font("Hiragino Mincho ProN", Font.PLAIN, 32));
        letterPane.setLetterBorderRendarer(null);

        new AozoraContentsParser(null, new AozoraContentsParserHandler() {

            boolean reten;
            boolean noted;

            @Override public void ruby(String rb, String rt) {
                Debug.println("ruby|" + rb + "|" + rt);
            }

            @Override public void otherElement(String element) {
                if (element.startsWith("sub class=\"kaeriten\"")) {
                    reten = true;
                    return;
                }
                if (
                        !element.startsWith("script") &&
                                !element.startsWith("/script") &&
                                !element.startsWith("!--") &&
                                !element.startsWith("td") &&
                                !element.startsWith("/td") &&
                                !element.equals("span") &&
                                !element.equals("sub") &&
                                !element.startsWith("/sub") &&
                                !element.startsWith("ui") &&
                                !element.startsWith("li") &&
                                !element.startsWith("/li") &&
                                !element.startsWith("/span") &&
                                !element.startsWith("/table") &&
                                !element.startsWith("/hr") &&
                                !element.startsWith("/a") &&
                                !element.startsWith("tr") &&
                                !element.startsWith("/tr") &&
                                !element.startsWith("td") &&
                                !element.startsWith("/td") &&
                                !element.startsWith("div") &&
                                !element.startsWith("/div") &&
                                !element.startsWith("span") &&
                                !element.startsWith("/span")
                ) {
                    Debug.println("element|" + element);
                }
            }

            final SLetterCell CR = SLetterCellFactory.getInstance().createGlyphCell('\n');

            @Override public void newLine() {
                letterPane.addCell(CR);
            }

            @Override public void img(URL src, String alt, boolean isGaiji) {
                Debug.println("img|" + (isGaiji ? "gaiji|" : "") + src + "|" + alt);
            }

            @Override public void characters(String cdata) {
                if (reten) {
                    if (cdata.equals("レ")) {
                        Debug.println("characters|" + "[レ点]");
                    }
                    reten = false;
                    return;
                }
                if (noted) {
                    if (cdata.startsWith("［＃")) {
                        final String pattern = ".*([Uu]\\+[0-9a-fA-F]{4}).*";
                        if (cdata.matches(pattern)) {
                            String a = cdata.replaceFirst(pattern, "$1");
                            Debug.printf("characters|[noted:%s]", a);
                        }
                        noted = false;
                        return;
                    }
                }

                String s = cdata.replaceAll("[\\s ]", "").trim();
                if (!s.isEmpty()) {
                    if (s.charAt(s.length() - 1) == '※') {
                        noted = true;
                        Debug.println("characters*|" + s);
                    }
                    for (char c : s.toCharArray()) {
                        letterPane.addCell(SLetterCellFactory.getInstance().createGlyphCell(c));
                    }
                }
            }

            @Override public void parseFinished() {
                Debug.println("end|----------------------------------------------------");
            }
        }).parse(new StringReader(writer.toString()), null);

//        JScrollPane sp = new JScrollPane(letterPane);

        final JFrame frame = new JFrame("com.soso.sgui.letter.SLetterPaneTest");
        frame.getContentPane().add(letterPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
//Debug.printf("letterPane: %s, %s", letterPane.getBounds(), sp.getViewport().getView().getSize());
//        letterPane.setSize(sp.getViewport().getSize());
//        letterPane.setPreferredSize(frame.getSize());
        frame.setVisible(true);

        letterPane.repaint();
Debug.printf("letterPane: %s", letterPane.getBounds());
    }
}

/* */
