/*
 * Copyright (c) 2011 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.text.aozora.viewer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.apple.eawt.Application;
import org.mozilla.universalchardet.UniversalDetector;
import vavi.text.aozora.converter.AozoraBunkoRuby;
import vavi.text.aozora.converter.AozoraBunkoRubyTest;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * Application -> Component (trimming functions)
 */
//@EnabledIf("localPropertiesExists")
@PropsEntity(url = "file:local.properties")
public class MyTextViewerPaneTest {

//    static boolean localPropertiesExists() {
//        return Files.exists(Paths.get("local.properties"));
//    }

    @Property(name = "aozora.zip")
    String file;

    /**
     * @param args none
     */
    public static void main(String[] args) throws Exception {
        URI uri = URI.create("https://www.aozora.gr.jp/cards/000372/files/42810_23981.html");

        MyTextViewerPaneTest app = new MyTextViewerPaneTest();
        PropsEntity.Util.bind(app);

        Path textPath = AozoraBunkoRubyTest.getTextPath(Paths.get(app.file));
        InputStream is = Files.newInputStream(textPath);
        String charset = getCharset(is);
        is.close();
        Reader reader = Files.newBufferedReader(textPath, Charset.forName(charset));
        Writer writer = new StringWriter();
//        AozoraParser converter = new AozoraParser(reader, writer);
        AozoraBunkoRuby converter = new AozoraBunkoRuby(reader, writer);
        converter.printHtml();
        URL base = new URL("https://vavi.com");
        Reader forParse = new StringReader(writer.toString());
        String title = textPath.getFileName().toString();

        BufferedImage icon = ImageIO.read(Files.newInputStream(Paths.get("tmp/Tate.iconset/icon_128x128x2.png")));
        Application application = Application.getApplication();
        application.setDockIconImage(icon);
        application.setDockIconBadge("99");

        JFrame frame = new JFrame();
        frame.setTitle(title);
        MyTextViewerPane pane = new MyTextViewerPane(uri, 0) {
            @Override protected void title(String title) {
Debug.println("title: " + title);
                frame.setTitle(title);
            }
        };
//        MyTextViewerPane pane = new MyTextViewerPane(forParse, base, 0);
        frame.setLocation(200, 100);
        frame.getContentPane().add(pane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /** @return null not found */
    static String getCharset(InputStream is) throws IOException {
        byte[] buf = new byte[4096];
        // (1)
        UniversalDetector detector = new UniversalDetector();

        // (2)
        int nread;
        while ((nread = is.read(buf)) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nread);
        }
        // (3)
        detector.dataEnd();

        // (4)
        String encoding = detector.getDetectedCharset();
        if ("SHIFT_JIS".equals(encoding)) {
            encoding = "MS932";
        }
        if (encoding != null) {
Debug.println("Detected encoding = " + encoding);
        } else {
Debug.println("No encoding detected.");
        }

        // (5)
        detector.reset();

        return encoding;
    }
}

/* */
