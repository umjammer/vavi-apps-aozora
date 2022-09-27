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
        return map.get(prc);
    }

    public static String toUnicodeChar(int plane, int row, int cell) {
        return map.get(plane + "-" + row + "-" + cell);
    }

    private static Map<String, String> map;

    static {
        try {
            // serialized file of Map<String, Character>
            InputStream is = UnicodeUtil.class.getResourceAsStream("/prc-unicode.ser");
            ObjectInputStream ois = new ObjectInputStream(is);
            map = (Map<String, String>) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
