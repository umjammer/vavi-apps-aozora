/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.boot;


public class AozoraPremierMain {

    public static void main(String[] args) {
        AozoraBootLoader.registerPremier();
        AozoraMain.main(args);
    }
}
