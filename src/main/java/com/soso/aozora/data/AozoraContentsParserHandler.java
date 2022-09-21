/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.data;

import java.net.URL;


public interface AozoraContentsParserHandler {

    void characters(String cdata);

    void newLine();

    void ruby(String rb, String rt);

    void img(URL src, String alt, boolean isGaiji);

    void otherElement(String element);

    void parseFinished();
}
