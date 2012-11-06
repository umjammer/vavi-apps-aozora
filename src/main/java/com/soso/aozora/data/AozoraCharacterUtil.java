/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.data;

import java.util.Arrays;


public class AozoraCharacterUtil {

    public static boolean isGaijiToRotate(String name) {
        if (name.indexOf('.') != -1)
            name = name.substring(0, name.indexOf('.'));
        if (name.indexOf('/') != -1)
            name = name.substring(name.lastIndexOf('/') + 1);
        if (Arrays.binarySearch(GAIJI_ALPHABET_PHONETIC_SIGN, name) >= 0)
            return true;
        return Arrays.binarySearch(GAIJI_ALPHABET_WITH_PHONETIC, name) >= 0;
    }

    public static void main(String[] args) {
        Arrays.sort(GAIJI_ALPHABET_PHONETIC_SIGN);
        System.out.println("Test GAIJI_ALPHABET_PHONETIC_SIGN:");
        for (String test : GAIJI_ALPHABET_PHONETIC_SIGN) {
            if (!isGaijiToRotate(test + ".png"))
                System.err.println("ERROR: " + test);
        }

        System.out.println("Test GAIJI_ALPHABET_WITH_PHONETIC:");
        for (String test : GAIJI_ALPHABET_WITH_PHONETIC) {
            if (!isGaijiToRotate(test + ".jpg"))
                System.err.println("ERROR: " + test);
        }

        System.out.println("Test OTHER:");
        for(Object test[] : new Object[][] {{ "hoge", false },
                                            { "hoge.png", false },
                                            { "1_2_22.gif", false },
                                            { "1-09-81", true },
                                            { "1-09-81.png", true },
                                            { "/gaiji/1-09/1-09-81.png", true }}) {
            if (isGaijiToRotate((String) test[0]) == (Boolean) test[1])
                System.out.println("OK : " + ((Boolean) test[1] ? "ROTATE" : "NOMAL ") + " : " + test[0]);
            else
                System.err.println("ERR : " + test[0]);
        }

        System.out.println("Test FINISH");
    }

    private static final String[] GAIJI_ALPHABET_PHONETIC_SIGN = {
        "1-08-79", "1-08-80", "1-08-81", "1-08-86", "1-08-87", "1-08-88", "1-08-89", "1-08-90", "1-08-91", "1-08-92", 
        "1-09-39", "1-09-52", "1-09-70", "1-09-83", "1-10-01", "1-10-04", "1-10-06", "1-10-08", "1-10-10", "1-10-11", 
        "1-10-12", "1-10-15", "1-10-18", "1-10-20", "1-10-23", "1-10-24", "1-10-26", "1-10-29", "1-10-30", "1-10-31", 
        "1-10-32", "1-10-34", "1-10-35", "1-10-36", "1-10-38", "1-10-41", "1-10-44", "1-10-45", "1-10-46", "1-10-47", 
        "1-10-50", "1-10-51", "1-10-52", "1-10-54", "1-10-62", "1-10-68", "1-10-69", "1-10-70", "1-10-71", "1-10-72", 
        "1-10-73", "1-10-74", "1-10-75", "1-10-76", "1-10-77", "1-10-78", "1-10-79", "1-10-80", "1-10-81", "1-10-82", 
        "1-10-83", "1-10-84", "1-10-85", "1-10-86", "1-10-87", "1-10-88", "1-10-89", "1-10-90", "1-10-91", "1-10-92", 
        "1-10-94", "1-11-01", "1-11-02", "1-11-03", "1-11-04", "1-11-05", "1-11-06", "1-11-07", "1-11-08", "1-11-09", 
        "1-11-13", "1-11-14", "1-11-15", "1-11-16", "1-11-17", "1-11-18", "1-11-19", "1-11-20", "1-11-21", "1-11-22", 
        "1-11-23", "1-11-24", "1-11-25", "1-11-26", "1-11-27", "1-11-28", "1-11-29", "1-11-30", "1-11-31", "1-11-32", 
        "1-11-33", "1-11-34", "1-11-35", "1-11-36", "1-11-37", "1-11-38", "1-11-39", "1-11-40", "1-11-41", "1-11-42", 
        "1-11-43", "1-11-44", "1-11-45", "1-11-46", "1-11-47", "1-11-48", "1-11-49"
    };

    private static final String[] GAIJI_ALPHABET_WITH_PHONETIC = {
        "1-08-82", "1-08-83", "1-08-84", "1-08-85", "1-09-23", "1-09-24", "1-09-25", "1-09-26", "1-09-27", "1-09-28", 
        "1-09-29", "1-09-30", "1-09-31", "1-09-32", "1-09-33", "1-09-34", "1-09-35", "1-09-36", "1-09-37", "1-09-38", 
        "1-09-40", "1-09-41", "1-09-42", "1-09-43", "1-09-44", "1-09-45", "1-09-46", "1-09-47", "1-09-48", "1-09-49", 
        "1-09-50", "1-09-51", "1-09-53", "1-09-54", "1-09-55", "1-09-56", "1-09-57", "1-09-58", "1-09-59", "1-09-60", 
        "1-09-61", "1-09-62", "1-09-63", "1-09-64", "1-09-65", "1-09-66", "1-09-67", "1-09-68", "1-09-69", "1-09-71", 
        "1-09-72", "1-09-73", "1-09-74", "1-09-75", "1-09-76", "1-09-77", "1-09-78", "1-09-79", "1-09-80", "1-09-81", 
        "1-09-82", "1-09-84", "1-09-85", "1-09-86", "1-09-87", "1-09-88", "1-09-89", "1-09-90", "1-09-91", "1-09-92", 
        "1-09-93", "1-09-94", "1-10-03", "1-10-05", "1-10-07", "1-10-09", "1-10-14", "1-10-16", "1-10-19", "1-10-21", 
        "1-10-25", "1-10-27", "1-10-28", "1-10-33", "1-10-37", "1-10-39", "1-10-40", "1-10-42", "1-10-43", "1-10-48", 
        "1-10-49", "1-10-53", "1-10-55", "1-10-57", "1-10-58", "1-10-59", "1-10-60", "1-10-61", "1-10-63", "1-10-64", 
        "1-10-65", "1-10-66", "1-10-67", "1-10-93", "1-11-10", "1-11-11", "1-11-12"
    };
}
