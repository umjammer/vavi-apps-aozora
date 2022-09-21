/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.data;


final class CipherUtil {

    CipherUtil() {
    }

    static byte[] encrypt(byte[] plain) {
        return not(plain);
    }

    static byte[] decrypt(byte[] encrypted) {
        return not(encrypted);
    }

    private static byte[] not(byte[] src) {
        byte[] ret = new byte[src.length];
        int len = src.length;
        for (int i = 0; i < len; i++)
            ret[i] = (byte) ~src[i];

        return ret;
    }
}
