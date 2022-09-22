/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.core;

import java.awt.Color;
import java.awt.Font;

import vavi.util.Debug;


/**
 * AozoraSettingFileHandlerTest
 */
public class AozoraSettingFileHandlerTest {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        AozoraSettingFileHandler test = new AozoraSettingFileHandler();
        AozoraIniFileBean ini = test.getIni();
        String xml = test.createXML(ini);
Debug.println(xml);
        ini.setSystemFont(new Font("Dialog", Font.PLAIN, 13));
        ini.putBookmark("234", 123);
        ini.putBookmark("7567", 0x34084);
        ini.putBookmark("23464564", 1);
        ini.setRowSpace(2);
        ini.setForeground(new Color(0xaabbccdd, true));
        ini.setBackground(new Color(0x00000000, false));
Debug.println("====================");
        String newXML = test.createXML(ini);
Debug.println(newXML);
        test.writeAozoraIni(ini);
    }
}
