/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.data;

import java.net.URL;


public interface AozoraContentsParserHandler {

    public abstract void characters(String cdata);

    public abstract void newLine();

    public abstract void ruby(String rb, String rt);

    public abstract void img(URL src, String alt, boolean isGaiji);

    public abstract void otherElement(String element);

    public abstract void parseFinished();
}
