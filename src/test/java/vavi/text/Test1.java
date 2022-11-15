/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.text;


import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.klab.commons.csv.CsvEntity;
import vavi.text.aozora.site.AozoraData;
import vavi.util.Debug;
import vavi.util.archive.Archive;
import vavi.util.archive.Archives;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * OCR suspicious character scanner.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-09-26 nsano initial version <br>
 * @see "https://qiita.com/kaz-utashiro/items/2f199409bdb1e08dc473"
 */
@PropsEntity(url = "file:local.properties")
public class Test1 {

    @Property(name = "aozora.txt")
    String file;

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        Test1 app = new Test1();
        PropsEntity.Util.bind(app);

        Set<String> suspiciousSet = new HashSet<>();
        Scanner scanner = new Scanner(Test1.class.getResourceAsStream("/susupicious-ocr.txt"));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            suspiciousSet.add(line);
        }
        scanner.close();

        scanner = new Scanner(Files.newInputStream(Paths.get(app.file)));
        int l = 1;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            for (String suspicious : suspiciousSet) {
                String pattern = "(.*)(" + suspicious + ")(.*)";
                if (line.matches(pattern)) {
                    System.err.println(l + ": " + line.replaceFirst(pattern, "$1<<<$2>>>$3"));
                }
            }
            l++;
        }
        scanner.close();
        System.err.println(l + " lines");
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
