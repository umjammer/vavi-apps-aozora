/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.soso.aozora.data;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import vavi.util.Debug;

import static com.soso.aozora.data.AozoraCharacterUtil.GAIJI_ALPHABET_PHONETIC_SIGN;
import static com.soso.aozora.data.AozoraCharacterUtil.GAIJI_ALPHABET_WITH_PHONETIC;
import static com.soso.aozora.data.AozoraCharacterUtil.isGaijiToRotate;
import static com.soso.aozora.data.CipherUtil.decrypt;
import static com.soso.aozora.data.CipherUtil.encrypt;


/**
 * AozoraCharacterUtilTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-09-21 nsano initial version <br>
 */
class AozoraCharacterUtilTest {

    public static void main(String[] args) {
        Arrays.sort(GAIJI_ALPHABET_PHONETIC_SIGN);
Debug.println("Test GAIJI_ALPHABET_PHONETIC_SIGN:");
        for (String test : GAIJI_ALPHABET_PHONETIC_SIGN) {
            if (!isGaijiToRotate(test + ".png"))
                System.err.println("ERROR: " + test);
        }

Debug.println("Test GAIJI_ALPHABET_WITH_PHONETIC:");
        for (String test : GAIJI_ALPHABET_WITH_PHONETIC) {
            if (!isGaijiToRotate(test + ".jpg"))
                System.err.println("ERROR: " + test);
        }

Debug.println("Test OTHER:");
        for (Object[] test : new Object[][] {{ "hoge", false },
                { "hoge.png", false },
                { "1_2_22.gif", false },
                { "1-09-81", true },
                { "1-09-81.png", true },
                { "/gaiji/1-09/1-09-81.png", true }}) {
            if (isGaijiToRotate((String) test[0]) == (Boolean) test[1])
Debug.println("OK : " + ((Boolean) test[1] ? "ROTATE" : "NOMAL ") + " : " + test[0]);
            else
Debug.println("ERR : " + test[0]);
        }

Debug.println("Test FINISH");
    }

    @Test
    void test2() throws Exception {
Debug.println(new String(decrypt(encrypt("OK".getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8));
    }
}
