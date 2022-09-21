/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;


public interface SFontModel {

    String[] getFontFamilyNames();

    Integer[] getSizes();

    String[] getStyleNames();

    int[] getStyleTypes();

    int getFontFamilyNameIndex(String name);

    int getSizeIndex(int size);

    int getSizeRoundIndex(int size);

    int styleNameToType(String name);

    int getStyleTypeIndex(int type);
}
