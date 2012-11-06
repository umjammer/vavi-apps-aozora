/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.data;


final class CipherUtil {

    CipherUtil() {
    }

    static byte[] encript(byte[] plain) {
        return not(plain);
    }

    static byte[] decript(byte[] encripted) {
        return not(encripted);
    }

    private static byte[] not(byte[] src) {
        byte[] ret = new byte[src.length];
        int len = src.length;
        for (int i = 0; i < len; i++)
            ret[i] = (byte) ~src[i];

        return ret;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new String(decript(encript("OK".getBytes("UTF-8"))), "UTF-8"));
    }
}
