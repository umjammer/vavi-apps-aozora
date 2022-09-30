/*
 * Copyright (c) 2011 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.text.aozora.viewer;

import java.awt.image.BufferedImage;
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
import vavi.text.aozora.converter.AozoraBunkoRuby;
import vavi.text.aozora.converter.AozoraBunkoRubyTest;
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
        String title = uri.toString();

        MyTextViewerPaneTest app = new MyTextViewerPaneTest();
        PropsEntity.Util.bind(app);

        Path textPath = AozoraBunkoRubyTest.getTextPath(Paths.get(app.file));
        Reader reader = Files.newBufferedReader(textPath, Charset.forName("ms932")/*StandardCharsets.UTF_8*/);
        Writer writer = new StringWriter();
//        AozoraParser converter = new AozoraParser(reader, writer);
        AozoraBunkoRuby converter = new AozoraBunkoRuby(reader, writer);
        converter.printHtml();
        URL base = new URL("https://vavi.com");
        Reader forParse = new StringReader(writer.toString());
        title = textPath.getFileName().toString();

        BufferedImage icon = ImageIO.read(Files.newInputStream(Paths.get("tmp/Tate.iconset/icon_128x128x2.png")));
        Application application = Application.getApplication();
        application.setDockIconImage(icon);
        application.setDockIconBadge("99");

//        MyTextViewerPane pane = new MyTextViewerPane(uri, 0);
        MyTextViewerPane pane = new MyTextViewerPane(forParse, base, 0);
        JFrame frame = new JFrame(title);
        frame.setLocation(200, 100);
        frame.getContentPane().add(pane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}

/* */
