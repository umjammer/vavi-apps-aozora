/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.text;


import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

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
}
