/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.text.aozora.converter;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * AozoraParserTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-09-22 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
public class AozoraParserTest {

    @Property(name = "aozora.zip")
    String file;

    /**
     * @param args 0: encoding, 1: input, 2: output
     */
    public static void main(String[] args) throws Exception {
        AozoraParserTest app = new AozoraParserTest();
        PropsEntity.Util.bind(app);

        String encoding = "utf-8"; //args[0];
        String input = app.file == null ? args[1] : app.file;
        String output = "/dev/stdout"; //args[2];

        Debug.println(input);

        Path archivePath = Paths.get(input);
        Path textPath = AozoraBunkoRubyTest.getTextPath(archivePath);

        AozoraParser converter = new AozoraParser(
                Files.newBufferedReader(textPath, Charset.forName(encoding)),
                Files.newBufferedWriter(Paths.get(output)));
        converter.printHtml();
        Debug.println("------------------------------------------------------");
        converter.printNoRuby();
        Debug.println("------------------------------------------------------");
        for (AozoraParser.RubyInfo item : converter.getRubyList()) {
            System.err.println(item);
        }
    }
}
