/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.data;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.event.AozoraListenerManager;
import com.soso.aozora.viewer.AozoraCommentDecorator;


public class AozoraSettings {

    private static class SettingsXMLBuilder {

        private void build(AozoraSettings settings, String enc) throws IOException {
            builder.startDocument(enc);
            builder.startElement(ELM_SETTINGS);
            builder.startElement(ELM_FONT);
            builder.attribute(ATT_FONT_NAME, settings.getFont().getFamily());
            builder.attribute(ATT_FONT_SIZE, String.valueOf(settings.getFont().getSize()));
            builder.attribute(ATT_FONT_STYLE, getFontStyleVal(settings.getFont()));
            builder.endElement();
            builder.startElement(ELM_LETTER);
            builder.attribute(ATT_LETTER_ROWSPACE, String.valueOf(settings.getRowSpace()));
            builder.attribute(ATT_LETTER_FONTRATIO, String.valueOf(settings.getFontRatio()));
            builder.endElement();
            builder.startElement(ELM_LAF);
            builder.attribute(ATT_LAF_CLASS, settings.getLookAndFeel().getClass().getCanonicalName());
            builder.endElement();
            builder.startElement(ELM_COLOR);
            builder.attribute(ATT_COLOR_FOREGROUND, encodeColor(settings.getForeground()));
            builder.attribute(ATT_COLOR_BACKGROUND, encodeColor(settings.getBackground()));
            builder.endElement();
            builder.startElement(ELM_COMMENT);
            builder.attribute(ATT_COMMENT_TYPE, String.valueOf(settings.getCommentType()));
            builder.endElement();
            builder.endElement();
            builder.endDocument();
        }

        private static String getFontStyleVal(Font font) {
            if (font == null)
                return VAL_FONT_STYLE_PLAIN;
            switch (font.getStyle()) {
            case Font.PLAIN:
                return VAL_FONT_STYLE_PLAIN;
            case Font.BOLD:
                return VAL_FONT_STYLE_BOLD;
            case Font.ITALIC:
                return VAL_FONT_STYLE_ITALIC;
            }
            return VAL_FONT_STYLE_PLAIN;
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

        private final XMLBuilder builder;

        private SettingsXMLBuilder(Appendable out) {
            builder = new XMLBuilder(out);
        }
    }

    private static class SettingsXMLHandler extends DefaultHandler {

        private AozoraSettings getSettings() {
            return settings;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            try {
                if (ELM_FONT.equals(qName))
                    startFont(attributes);
                else if (ELM_LETTER.equals(qName))
                    startLetter(attributes);
                else if (ELM_LAF.equals(qName))
                    startLookAndFeel(attributes);
                else if (ELM_COLOR.equals(qName))
                    startColor(attributes);
                else if (ELM_COMMENT.equals(qName))
                    startComment(attributes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void startFont(Attributes attributes) {
            String family = attributes.getValue(ATT_FONT_NAME);
            int size = Integer.parseInt(attributes.getValue(ATT_FONT_SIZE));
            String style = attributes.getValue(ATT_FONT_STYLE);
            int intStyle = 0;
            if (!VAL_FONT_STYLE_PLAIN.equals(style))
                if (VAL_FONT_STYLE_BOLD.equals(style))
                    intStyle = Font.BOLD;
                else if (VAL_FONT_STYLE_ITALIC.equals(style))
                    intStyle = Font.ITALIC;
            getSettings().setFont(new Font(family, intStyle, size));
        }

        public void startLetter(Attributes attributes) {
            String rowSpace = attributes.getValue(ATT_LETTER_ROWSPACE);
            if (rowSpace != null)
                getSettings().setRowSpace(Integer.parseInt(rowSpace));
            String fontRatio = attributes.getValue(ATT_LETTER_FONTRATIO);
            if (fontRatio != null)
                getSettings().setFontRatio(Float.parseFloat(fontRatio));
        }

        public void startLookAndFeel(Attributes attributes) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
            String laf = attributes.getValue(ATT_LAF_CLASS);
            Object object = Class.forName(laf).newInstance();
            if (object instanceof LookAndFeel)
                getSettings().setLookAndFeel((LookAndFeel) object);
        }

        public void startColor(Attributes attributes) {
            String foreground = attributes.getValue(ATT_COLOR_FOREGROUND);
            String background = attributes.getValue(ATT_COLOR_BACKGROUND);
            getSettings().setForeground(decodeColor(foreground));
            getSettings().setBackground(decodeColor(background));
        }

        private static Color decodeColor(String colorStr) {
            return Color.decode(colorStr);
        }

        public void startComment(Attributes attributes) {
            String type = attributes.getValue(ATT_COMMENT_TYPE);
            getSettings().setCommentType(AozoraCommentDecorator.CommentType.valueOf(type));
        }

        private final AozoraSettings settings;

        private SettingsXMLHandler(AozoraSettings settings) {
            this.settings = settings;
        }
    }

    private AozoraSettings() {
    }

    private AozoraListenerManager getListenerManager() {
        return listenerManager;
    }

    public void setListenerManager(AozoraListenerManager listenerManager) {
        this.listenerManager = listenerManager;
    }

    public int getRowSpace() {
        return rowSpace;
    }

    public void setRowSpace(int rowSpace) {
        this.rowSpace = rowSpace;
    }

    public float getFontRatio() {
        return fontRatio;
    }

    public void setFontRatio(float fontRangeRatio) {
        fontRatio = fontRangeRatio;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public LookAndFeel getLookAndFeel() {
        return lookAndFeel;
    }

    public void setLookAndFeel(LookAndFeel lookAndFeel) {
        this.lookAndFeel = lookAndFeel;
    }

    public Color getForeground() {
        return foreground;
    }

    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    public boolean isCommentVisible() {
        return commentType != null && commentType != AozoraCommentDecorator.CommentType.none;
    }

    public AozoraCommentDecorator.CommentType getCommentType() {
        return commentType;
    }

    public void setCommentType(AozoraCommentDecorator.CommentType commentType) {
        this.commentType = commentType;
        AozoraListenerManager listenerManager = getListenerManager();
        if (listenerManager != null)
            listenerManager.commentTypeChanged(commentType);
    }

    private void setDefaluts() {
        font = new Font("Dialog", Font.PLAIN, 16);
        lookAndFeel = UIManager.getLookAndFeel();
        rowSpace = 10;
        fontRatio = 0.9F;
        foreground = AozoraEnv.DEFAULT_FOREGROUND_COLOR;
        background = AozoraEnv.DEFAULT_BACKGROUND_COLOR;
        commentType = AozoraCommentDecorator.CommentType.ballone;
    }

    public static AozoraSettings create() {
        AozoraSettings settings = new AozoraSettings();
        settings.setDefaluts();
        return settings;
    }

    private static File getSettingsFile() {
        return new File(AozoraEnv.getUserHomeDir(), "aozora/settings.xml");
    }

    public static AozoraSettings load() throws IOException, SAXException {
        File file = getSettingsFile();
        if (file.exists())
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                SettingsXMLHandler handler = new SettingsXMLHandler(create());
                parser.parse(file, handler);
                return handler.getSettings();
            } catch (ParserConfigurationException e) {
                throw new IllegalStateException(e);
            }
        else
            return create();
    }

    public void store() throws IOException {
        OutputStreamWriter writer = null;
        try {
            String enc = "UTF-8";
            File file = getSettingsFile();
            if (!file.exists() && !file.getParentFile().exists())
                file.getParentFile().mkdirs();
            writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), enc);
            SettingsXMLBuilder builder = new SettingsXMLBuilder(writer);
            builder.build(this, enc);
        } finally {
            writer.close();
        }
    }

    private AozoraListenerManager listenerManager;
    private Font font;
    private LookAndFeel lookAndFeel;
    private int rowSpace;
    private float fontRatio;
    private Color foreground;
    private Color background;
    private AozoraCommentDecorator.CommentType commentType;
    private static final String ELM_SETTINGS = "settings";
    private static final String ELM_FONT = "font";
    private static final String ATT_FONT_NAME = "name";
    private static final String ATT_FONT_SIZE = "size";
    private static final String ATT_FONT_STYLE = "style";
    private static final String VAL_FONT_STYLE_PLAIN = "PLAIN";
    private static final String VAL_FONT_STYLE_BOLD = "BOLD";
    private static final String VAL_FONT_STYLE_ITALIC = "ITALIC";
    private static final String ELM_LETTER = "letter";
    private static final String ATT_LETTER_ROWSPACE = "rowspace";
    private static final String ATT_LETTER_FONTRATIO = "fontratio";
    private static final String ELM_LAF = "lookandfeel";
    private static final String ATT_LAF_CLASS = "class";
    private static final String ELM_COLOR = "color";
    private static final String ATT_COLOR_FOREGROUND = "foreground";
    private static final String ATT_COLOR_BACKGROUND = "background";
    private static final String ELM_COMMENT = "comment";
    private static final String ATT_COMMENT_TYPE = "type";
}
