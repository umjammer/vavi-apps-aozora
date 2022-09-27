/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.text.aozora.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import br.ufmg.dcc.nanocomp.peg.PEG;
import br.ufmg.dcc.nanocomp.peg.Parser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * PegTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-09-25 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
class PegTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "aozora.zip")
    String file;

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    public interface NumberParser extends Parser<Number> {
    }

    @Test
    void test1() throws Exception {
        PEG peg = PEG.getInstance();
        NumberParser parser = peg.generate("start = [0-9]* { return parseInt(text())}", NumberParser.class);
        System.out.println(parser.parse("421"));
    }

    /**
     * @see "https://github.com/aozorahack/aozora-parser.js"
     */
    public interface AozoraParser extends Parser<jdk.nashorn.api.scripting.JSObject> {
    }

    @Test
    void test2() throws Exception {
        PEG peg = PEG.getInstance();
        Path pegPath = Paths.get(PegTest.class.getResource("/aozora-parser.pegjs").toURI());
        String pegString = new String(Files.readAllBytes(pegPath));
        AozoraParser parser = peg.generate(pegString, AozoraParser.class);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Path textPath = AozoraBunkoRubyTest.getTextPath(Paths.get(file));
        String text = new String(Files.readAllBytes(textPath));

        Path outPath = Paths.get("tmp/peg_out.json");
        InputStream is = new ByteArrayInputStream(gson.toJson(parser.parse(text)).getBytes());
        Files.copy(is, outPath);
    }
}
