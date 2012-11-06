/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.boot;

import java.text.SimpleDateFormat;
import java.util.Date;


public class AozoraLog {

    public AozoraLog() {
        debugMode = true;
        errorMode = true;
    }

    public static AozoraLog getInstance() {
        return INSTANCE;
    }

    public void log(Object obj) {
        if (debugMode)
            System.out.printf("%s | %s\n", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()), obj);
    }

    public void log(Object obj, Class<?> c) {
        if (debugMode)
            System.out.printf("%s | %s | %s\n", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()), c.getSimpleName(), obj);
    }

    public void log(Class<?> c, String format, Object... args) {
        if (debugMode)
            log(String.format(format, args), c);
    }

    public void log(Throwable t) {
        if (errorMode)
            t.printStackTrace();
        if (debugMode)
            log(t);
    }

    private static final AozoraLog INSTANCE = new AozoraLog();
    private boolean debugMode;
    private boolean errorMode;
}
