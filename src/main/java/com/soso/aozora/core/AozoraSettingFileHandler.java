/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.core;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;

import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import vavi.util.Debug;


@Deprecated
public class AozoraSettingFileHandler {
    public static class IniFileHandler extends DefaultHandler {

        public AozoraIniFileBean getBean() {
            return bean;
        }

        private Integer putBookmark(String book, Integer page) {
            return getBean().putBookmark(book, page);
        }

        private void setFont(Font font) {
            getBean().setSystemFont(font);
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (ATT_BOOKMARK.equals(qName))
                startBookmark(attributes);
            else if (ATT_FONT.equals(qName))
                startFont(attributes);
            else if (ATT_LETTER.equals(qName))
                startLetter(attributes);
            else if (ATT_LAF.equals(qName))
                startLookAndFeel(attributes);
            else if (ATT_COLOR.equals(qName))
                startColor(attributes);
        }

        public void startColor(Attributes attributes) {
            String foreground = attributes.getValue(KEY_COLOR_FOREGROUND);
            String background = attributes.getValue(KEY_COLOR_BACKGROUND);
            try {
                getBean().setForeground(AozoraSettingFileHandler.decodeColor(foreground));
                getBean().setBackground(AozoraSettingFileHandler.decodeColor(background));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        public void startLookAndFeel(Attributes attributes) {
            String laf = attributes.getValue(KEY_LAF_CLASS);
            try {
                try {
                    Object object = Class.forName(laf).newInstance();
                    if (object instanceof LookAndFeel)
                        getBean().setLookAndFeel((LookAndFeel) object);
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        public void startFont(Attributes attributes) {
            String family = attributes.getValue(KEY_FONT_FAMILY);
            int size = Integer.parseInt(attributes.getValue(KEY_FONT_SIZE));
            String style = attributes.getValue(KEY_FONT_STYLE);
            int intStyle = Font.PLAIN;
            if (!FONT_STYLE_PLAIN.equals(style))
                if (FONT_STYLE_BOLD.equals(style))
                    intStyle = Font.BOLD;
                else if (FONT_STYLE_ITALIC.equals(style))
                    intStyle = Font.ITALIC;
            setFont(new Font(family, intStyle, size));
        }

        public void startLetter(Attributes attributes) {
            String rowSpace = attributes.getValue(KEY_LETTER_ROW_SPACE);
            if (rowSpace != null)
                try {
                    getBean().setRowSpace(Integer.parseInt(rowSpace));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            String fontRatio = attributes.getValue(KEY_LETTER_FONT_RATIO);
            if (fontRatio != null)
                try {
                    getBean().setFontRatio(Float.parseFloat(fontRatio));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
        }

        public void startBookmark(Attributes attributes) {
            String book = attributes.getValue(KEY_BOOK);
            Integer page = Integer.parseInt(attributes.getValue(KEY_PAGE));
            putBookmark(book, page);
        }

        private AozoraIniFileBean bean;

        static final String ATT_BOOKMARK = "bookmark";
        static final String ATT_FONT = "font";
        static final String ATT_LETTER = "letter";
        static final String ATT_LAF = "lookandfeel";
        static final String ATT_COLOR = "color";
        static final String KEY_AOTHOR = "author";
        static final String KEY_WORK = "work";
        @Deprecated
        static final String KEY_BOOK = "name";
        static final String KEY_PAGE = "page";
        static final String KEY_FONT_FAMILY = "name";
        static final String KEY_FONT_SIZE = "size";
        static final String KEY_FONT_STYLE = "style";
        static final String FONT_STYLE_PLAIN = "PLAIN";
        static final String FONT_STYLE_BOLD = "BOLD";
        static final String FONT_STYLE_ITALIC = "ITALIC";
        static final String KEY_LETTER_ROW_SPACE = "rowspace";
        static final String KEY_LETTER_FONT_RATIO = "fontratio";
        static final String KEY_COLOR_FOREGROUND = "foreground";
        static final String KEY_COLOR_BACKGROUND = "background";
        static final String KEY_LAF_CLASS = "class";

        public IniFileHandler() {
            bean = new AozoraIniFileBean();
        }
    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
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

    public void writeAozoraIni(AozoraIniFileBean bean) throws IOException {
        PrintWriter out = null;
        try {
            String xml = createXML(bean);
            File userHome = getUserHome();
            File aozoraIniFile = getAozoraIniFile(userHome);
            out = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(aozoraIniFile.toPath()), ENCODE));
            out.println(xml);
            out.flush();
        } finally {
            out.close();
        }
    }

    private String createXML(AozoraIniFileBean bean) {
        String tab = "\t";
        StringBuilder xml = new StringBuilder(withNewLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        xml.append(withNewLine("<aozora>"));
        xml.append(tab).append(withNewLine("<system>"));
        if (bean.getSystemFont() != null)
            xml.append(tab).append(tab).append(withNewLine(createFontAtt(bean.getSystemFont())));
        xml.append(tab).append(tab).append(withNewLine(createLetterAtt(bean.getRowSpace(), bean.getFontRatio())));
        if (bean.getLookAndFeel() != null)
            xml.append(tab).append(tab).append(withNewLine(createLookAndFeelAtt(bean.getLookAndFeel())));
        xml.append(tab).append(tab).append(withNewLine(createColorAtt(bean.getForeground(), bean.getBackground())));
        xml.append(tab).append(withNewLine("</system>"));
        xml.append(tab).append(withNewLine("<bookmarks>"));
        for (String book : bean.getBookmarks().keySet()) {
            Integer page = bean.getBookmark(book);
            xml.append(tab).append(tab).append(withNewLine(createBookmarkAtt(book, page)));
        }

        xml.append(tab).append(withNewLine("</bookmarks>"));
        xml.append(withNewLine("</aozora>"));
        return xml.toString();
    }

    private String withNewLine(String value) {
        return value + "\n";
    }

    private String createColorAtt(Color foreground, Color background) {
        String foregroundAtt = createAtt("foreground", encodeColor(foreground));
        String backgroundAtt = createAtt("background", encodeColor(background));
        String color = "<color";
        color += " " + foregroundAtt;
        color += " " + backgroundAtt;
        color += " />";
        return color;
    }

    private static String encodeColor(Color color) {
        StringBuilder colorStr = new StringBuilder(Integer.toHexString(color.getRGB()));
        colorStr = new StringBuilder(colorStr.toString().toUpperCase());
        while (colorStr.length() < 6) {
            colorStr.insert(0, "0");
        }
        if (colorStr.length() > 6)
            colorStr = new StringBuilder(colorStr.substring(colorStr.length() - 6));
        colorStr.insert(0, "#");
        return colorStr.toString();
    }

    private static Color decodeColor(String colorStr) {
        return Color.decode(colorStr);
    }

    private String createLookAndFeelAtt(LookAndFeel laf) {
        String lafAtt = createAtt("class", laf.getClass().getCanonicalName());
        String lookAndFeel = "<lookandfeel";
        lookAndFeel += " " + lafAtt;
        lookAndFeel += " />";
        return lookAndFeel;
    }

    private String createBookmarkAtt(String book, Integer page) {
        String bookAtt = createAtt("name", book);
        String pageAtt = createAtt("page", String.valueOf(page));
        String bookmark = "<bookmark";
        bookmark += " " + bookAtt;
        bookmark += " " + pageAtt;
        bookmark += " />";
        return bookmark;
    }

    private String createLetterAtt(int rowSpace, float fontRatio) {
        String letter = "<letter";
        letter += " " + createAtt("rowspace", String.valueOf(rowSpace));
        letter += " " + createAtt("fontratio", String.valueOf(fontRatio));
        letter += " />";
        return letter;
    }

    private String createFontAtt(Font systemFont) {
        String family = createAtt("name", systemFont.getFamily());
        String size = createAtt("size", String.valueOf(systemFont.getSize()));
        String fontStyle = "PLAIN";
        if (Font.PLAIN != systemFont.getStyle())
            if (Font.BOLD == systemFont.getStyle())
                fontStyle = "BOLD";
            else if (Font.ITALIC == systemFont.getStyle())
                fontStyle = "ITALIC";
        String style = createAtt("style", fontStyle);
        String fontAtt = "<font";
        fontAtt += " " + family;
        fontAtt += " " + size;
        fontAtt += " " + style;
        fontAtt += " />";
        return fontAtt;
    }

    private String createAtt(String key, String value) {
        return key + "=\"" + value + "\"";
    }

    public AozoraIniFileBean getIni() throws ParserConfigurationException, SAXException, IOException {
        File userHome = getUserHome();
        File aozoraIniFile = getAozoraIniFile(userHome);
        return readIni(aozoraIniFile);
    }

    private AozoraIniFileBean readIni(File ini) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        IniFileHandler iniFileHandler = new IniFileHandler();
        saxParser.parse(ini, iniFileHandler);
        return iniFileHandler.getBean();
    }

    private File getAozoraIniFile(File home) throws IOException {
        File ini = new File(home, AOZORA_INIT);
        if (!ini.exists())
            ini.createNewFile();
        return ini;
    }

    private File getUserHome() {
        File home = createUserHomePath();
        if (!home.exists())
            home.mkdir();
        return home;
    }

    @SuppressWarnings("unused")
    private File getUserHome(Component parent) throws FileNotFoundException {
        File home = createUserHomePath();
        if (!home.exists()) {
            String crtMsg = createCrtMsg(home.getAbsolutePath());
            int result = JOptionPane.showConfirmDialog(parent, crtMsg, "設定確認", 0);
            if (result == 0)
                home.mkdir();
            else
                throw new FileNotFoundException("It was not approved");
        }
        return home;
    }

    private String createCrtMsg(String where) {
        return where + " " + CRT_MSG;
    }

    private File createUserHomePath() {
        return AozoraEnv.getUserHomeDir();
    }

    private static final String AOZORA_INIT = "aozora.xml";
    private static final String CRT_MSG = "を作成してもよろしいですか？";
    public static final String ENCODE = "UTF-8";
}
