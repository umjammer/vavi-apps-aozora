/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.soso.aozora.data;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * AozoraBunkoRubyTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-09-21 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
public class AozoraBunkoRubyTest {

    @Property(name = "aozora.zip")
    String file;

    /**
     * @param args 0: encoding, 1: input, 2: output
     */
    public static void main(String[] args) throws Exception {
        AozoraBunkoRubyTest app = new AozoraBunkoRubyTest();
        PropsEntity.Util.bind(app);

        String encoding = "utf-8"; //args[0];
        String input = app.file == null ? args[1] : app.file;
        String output = "/dev/stdout"; //args[2];

Debug.println(input);

        Path archivePath = Paths.get(input);
        Path textPath = getTextPath(archivePath);

        AozoraBunkoRuby converter = new AozoraBunkoRuby(
                Files.newBufferedReader(textPath, Charset.forName(encoding)),
                Files.newBufferedWriter(Paths.get(output)));
        converter.printHtml();
    }

    /** */
    public static Path getTextPath(Path archivePath) throws IOException {
        URI uri = URI.create("archive:" + archivePath.toUri());
        FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
        Path virtualRoot = fs.getRootDirectories().iterator().next();
        Debug.println(virtualRoot);
        Path textPath = Files.walk(virtualRoot)
                .filter(p -> p.getFileName() != null && p.getFileName().toString().toLowerCase().endsWith(".txt"))
                .sorted()
                .findFirst().get();
Debug.println("text: " + textPath);
        return textPath;
    }
}
