/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.core;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.klab.commons.csv.CsvEntity;
import vavi.text.aozora.site.AozoraData;
import vavi.util.Debug;
import vavi.util.archive.Archive;
import vavi.util.archive.Archives;


/**
 * AozoraSettingFileHandlerTest
 */
public class AozoraSettingFileHandlerTest {

    static {
        System.setProperty("vavi.util.logging.VaviFormatter.extraClassMethod", "sun\\.util\\.logging\\..*?#.*");
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        AozoraSettingFileHandler test = new AozoraSettingFileHandler();
        AozoraIniFileBean ini = test.getIni();
        String xml = test.createXML(ini);
Debug.println(xml);
        ini.setSystemFont(new Font("Dialog", Font.PLAIN, 13));
        ini.putBookmark("234", 123);
        ini.putBookmark("7567", 0x34084);
        ini.putBookmark("23464564", 1);
        ini.setRowSpace(2);
        ini.setForeground(new Color(0xaabbccdd, true));
        ini.setBackground(new Color(0x00000000, false));
Debug.println("====================");
        String newXML = test.createXML(ini);
Debug.println(newXML);
        test.writeAozoraIni(ini);
    }

    @Test
    void test1() throws Exception {
        URL url = new URL("https://www.aozora.gr.jp/index_pages/list_person_all_utf8.zip");
        Archive archive = Archives.getArchive(new BufferedInputStream(url.openStream()));
Debug.println("aarchive: " + archive);
        Arrays.stream(archive.entries()).forEach(e -> System.err.println(e.getName()));
        InputStream is = archive.getInputStream(archive.entries()[0]);
Debug.println("entries: " + archive.entries().length);
Debug.println("is: " + is);
        List<AozoraData> aozoraIndices = CsvEntity.Util.read(AozoraData.class, is);
        aozoraIndices.forEach(System.err::println);
//        Scanner s = new Scanner(is);
//        while (s.hasNextLine()) {
//            System.err.println(s.nextLine());
//        }
    }

    @Test
    void test2() throws Exception {
        Path path = Paths.get("tmp/list_person_all_utf8.zip");
        Archive archive = Archives.getArchive(new BufferedInputStream(Files.newInputStream(path)));
        Arrays.stream(archive.entries()).forEach(e -> System.err.println(e.getName()));
        InputStream is = archive.getInputStream(archive.entries()[0]);
Debug.println("entries: " + archive.entries().length);
Debug.println("is: " + is);
        List<AozoraData> aozoraIndices = CsvEntity.Util.read(AozoraData.class, is);
        aozoraIndices.forEach(System.err::println);
    }
}
