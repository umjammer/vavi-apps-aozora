/*
 * Copyright (c) 2011 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.text.aozora;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.soso.aozora.data.AozoraContentsParser;
import com.soso.aozora.data.AozoraContentsParserHandler;
import vavi.text.aozora.converter.AozoraBunkoRuby;
import vavi.text.aozora.converter.AozoraBunkoRubyTest;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * AozoraContentsParserTest
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

            @Override public void newLine() {
//                Debug.println("newLine");
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
                        return;
                    }
                    Debug.println("characters|" + s);
                }
            }

            @Override public void parseFinished() {
                Debug.println("end|----------------------------------------------------");
            }
//        }).parse(forParse, base);
        }).parse(url);
    }
}