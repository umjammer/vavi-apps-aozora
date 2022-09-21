/*
 * Copyright (c) 2011 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.soso.aozora.data;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 *
 */
@PropsEntity(url = "file:local.properties")
public class AozoraContentsParserTest {

    @Property(name = "aozora.zip")
    String file;

    @Property(name = "aozora.html")
    String html;

    /** */
    public static void main(String[] args) throws Exception {
        AozoraContentsParserTest app = new AozoraContentsParserTest();
        PropsEntity.Util.bind(app);

        URL url = new URL(app.html);

        Path textPath = AozoraBunkoRubyTest.getTextPath(Paths.get(app.file));
        Reader reader = Files.newBufferedReader(textPath, StandardCharsets.UTF_8);
        Writer writer = new StringWriter();
        AozoraBunkoRuby converter = new AozoraBunkoRuby(reader, writer);
        converter.printHtml();
        URL base = new URL("https://vavi.com");
        Reader forParse = new StringReader(writer.toString());

        new AozoraContentsParser(null, new AozoraContentsParserHandler() {
            public void ruby(String rb, String rt) {
                Debug.println("ruby|" + rb + "|" + rt);
            }

            public void otherElement(String element) {
                Debug.println("element|" + element);
            }

            public void newLine() {
                Debug.println("newLine");
            }

            public void img(URL src, String alt, boolean isGaiji) {
                Debug.println("img|" + (isGaiji ? "gaiji|" : "") + src + "|" + alt);
            }

            public void characters(String cdata) {
                Debug.println("characters|" + cdata);
            }

            public void parseFinished() {
                Debug.println("end|----------------------------------------------------");
            }
        }).parse(forParse, base);
    }
}