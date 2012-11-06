/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;


public interface SFontModel {

    public abstract String[] getFontFamilyNames();

    public abstract Integer[] getSizes();

    public abstract String[] getStyleNames();

    public abstract int[] getStyleTypes();

    public abstract int getFontFamilyNameIndex(String name);

    public abstract int getSizeIndex(int size);

    public abstract int getSizeRoundIndex(int size);

    public abstract int styleNameToType(String name);

    public abstract int getStyleTypeIndex(int type);
}
