/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.text;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


/**
 * UnicodeUtil.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-09-24 nsano initial version <br>
 */
public class UnicodeUtil {

    private UnicodeUtil() {}

    /** should not 0 padding */
    public static String toUnicodeChar(String prc) {
        return prcUnicodeMap.get(prc);
    }

    public static String toUnicodeChar(int plane, int row, int cell) {
        return prcUnicodeMap.get(plane + "-" + row + "-" + cell);
    }

    private static Map<String, String> prcUnicodeMap;

    public static String toNew(String old) {
        String new_ = oldNewMap.get(old);
        return new_ == null ? old : new_;
    }

    private static Map<String, String> oldNewMap = new HashMap<>();

    static {
        try {
            // serialized file of Map<String, Character>
            InputStream is = UnicodeUtil.class.getResourceAsStream("/prc-unicode.ser");
            ObjectInputStream ois = new ObjectInputStream(is);
            prcUnicodeMap = (Map<String, String>) ois.readObject();
            ois.close();

            is = UnicodeUtil.class.getResourceAsStream("/old-new.tsv");
            Scanner s = new Scanner(is);
            while (s.hasNextLine()) {
                String l = s.nextLine();
                if (!l.startsWith("#") && !l.trim().isEmpty()) {
                    String[] on = l.split("\t", -1);
                    oldNewMap.put(on[0], on[1]);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
