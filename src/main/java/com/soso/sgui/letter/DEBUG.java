/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.letter;

import java.io.PrintStream;


class DEBUG {

    private DEBUG() {
    }

    private static void init() {
        try {
            isDebug = Boolean.valueOf(System.getenv("com.soso.sgui.letter.DEBUG"));
            if (!isDebug)
                isDebug = Boolean.valueOf(System.getProperty("com.soso.sgui.letter.DEBUG"));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            initialized = true;
        }
        if (isDebug)
            log("##### com.soso.sgui.letter.DEBUG #####");
    }

    private static void check() {
        if (!initialized) {
            synchronized (DEBUG.class) {
                if (!initialized)
                    init();
            }
        }
    }

    public static boolean isDebug() {
        check();
        return isDebug;
    }

    public static void log(String message) {
        check();
        out.println(message);
    }

    public static void trace(String message) {
        check();
        out.println(message + "\tat " + new Exception().getStackTrace()[1]);
    }

    private static boolean isDebug = false;
    private static PrintStream out;
    private static volatile boolean initialized = false;

    static {
        out = System.out;
    }
}
