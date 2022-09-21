/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraUtil;
import com.soso.aozora.html.TagReader;
import com.soso.aozora.html.TagUtil;
import vavi.util.Debug;


/**
 * HTML formatted Aozora parser.
 */
public class AozoraContentsParser {

    private static class NestedContentsParserHandler implements AozoraContentsParserHandler {

        public void characters(String cdata) {
            parent.characters(cdata);
        }

        public void img(URL src, String alt, boolean isGaiji) {
            parent.img(src, alt, isGaiji);
        }

        public void newLine() {
            parent.newLine();
        }

        public void otherElement(String element) {
            parent.otherElement(element);
        }

        public void parseFinished() {
        }

        public void ruby(String rb, String rt) {
            parent.ruby(rb, rt);
        }

        private final AozoraContentsParserHandler parent;

        private NestedContentsParserHandler(AozoraContentsParserHandler parent) {
            this.parent = parent;
        }
    }

    /**
     *
     * @param context not used in this class.
     * @param handler
     */
    public AozoraContentsParser(AozoraContext context, AozoraContentsParserHandler handler) {
        this.context = context;
        this.handler = handler;
    }

    private AozoraContext getContext() {
        return context;
    }

    public void parse(URL url) throws IOException {
        parse(AozoraUtil.getInputStream(url), url);
    }

    // TODO xml character reference doesn't support
    public void parse(InputStream in, URL base) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) != -1)
                byteOut.write(buf, 0, len);
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        byte[] bytes = byteOut.toByteArray();
        TagReader tin = new TagReader(new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.US_ASCII));
        String encoding = null;
        String tag;
        while ((tag = tin.readNextTag()) != null) {
            String lowerTag = tag.toLowerCase();
            if (lowerTag.contains("/head"))
                break;
            String charset = TagUtil.getHttpEquivContentTypeCharset(tag);
            if (charset != null) {
                encoding = charset;
Debug.println("encoding detected: " + encoding);
                break;
            }
        }
        if (encoding == null)
            encoding = "JISAutoDetect";
Debug.println("encoding set: " + encoding);
        parse((new InputStreamReader(new ByteArrayInputStream(bytes), encoding)), base);
    }

    public void parse(Reader in, URL base) throws IOException {
        TagReader tin = null;
        try {
            tin = new TagReader(in);
            String tag;
            while ((tag = tin.readNextTag()) != null && !tag.toLowerCase().startsWith("body"))
                ;
            StringBuilder commentB = null;
            StringBuilder cdataB = null;
            while (true) {
                if (commentB != null) {
                    tin.readTo("-->", commentB);
                    handler.otherElement(commentB.substring(0, commentB.length() - 1));
                    commentB = null;
                }

                if (cdataB != null) {
                    tin.readTo("]]>", cdataB);
                    handler.characters(cdataB.substring(0, cdataB.length() - 3));
                    cdataB = null;
                    continue;
                }

                StringBuilder pcdataB = new StringBuilder();
                tin.readTo("<", pcdataB);
                String pcdata = pcdataB.substring(0, pcdataB.length() - 1);
                handler.characters(XMLUtil.parsePCDATA(pcdata).toString());
                StringBuilder tagB = new StringBuilder();
                tin.readTo(">", tagB);
                tag = tagB.substring(0, tagB.length() - 1);
                String lowerTag = tag.toLowerCase();
                boolean isTagHandled = false;
                if (tag.startsWith("!--")) {
                    if (tag.length() > 3 && tag.endsWith("--")) {
                        handler.otherElement(tag);
                    } else {
                        commentB = new StringBuilder();
                        commentB.append(tagB);
                    }
                    isTagHandled = true;
                } else if (tag.startsWith("![CDATA[")) {
                    if (tag.length() > 8 && tag.endsWith("]]>")) {
                        handler.characters(tag.substring(8, tag.length() - 3));
                    } else {
                        cdataB = new StringBuilder();
                        cdataB.append(tagB.substring(8));
                    }
                    isTagHandled = true;
                } else if (lowerTag.startsWith("br")) {
                    handler.newLine();
                    isTagHandled = true;
                } else if (lowerTag.startsWith("img")) {
                    boolean isGaiji = false;
                    String classAttr = TagUtil.getAttValue(lowerTag, "class");
                    String gaijiAttr = TagUtil.getAttValue(lowerTag, "gaiji");
                    if ("gaiji".equalsIgnoreCase(classAttr) || "gaiji".equalsIgnoreCase(gaijiAttr))
                        isGaiji = true;
                    String srcAttr = TagUtil.getAttValue(tag, "src");
                    if (srcAttr == null)
                        srcAttr = TagUtil.getAttValue(tag, "SRC");
                    String altAttr = TagUtil.getAttValue(tag, "alt");
                    if (altAttr == null)
                        altAttr = TagUtil.getAttValue(tag, "ALT");
                    if (srcAttr != null) {
                        URL src = new URL(base, srcAttr);
                        handler.img(src, altAttr, isGaiji);
                        isTagHandled = true;
                    }
                } else if (lowerTag.startsWith("ruby")) {
                    StringBuilder rubyB = new StringBuilder();
                    tin.readTo("</ruby>", rubyB);
                    String ruby = rubyB.toString();
                    String lowerRuby = ruby.toLowerCase();
                    int rbO = lowerRuby.indexOf("<rb>");
                    int rbL = lowerRuby.indexOf("</rb>");
                    int rtO = lowerRuby.indexOf("<rt>");
                    int rtL = lowerRuby.indexOf("</rt>");
                    if (rbO != -1 && rbO < rbL && rtO != -1 && rtO < rtL) {
                        String rb = ruby.substring(rbO + 4, rbL);
                        String rt = ruby.substring(rtO + 4, rtL);
                        if (rb.indexOf('<') == -1 && rb.indexOf('>') == -1 && rt.indexOf('<') == -1 && rt.indexOf('>') == -1) {
                            handler.ruby(rb, rt);
                            isTagHandled = true;
                        } else {
                            handler.otherElement("ruby");
                            (new AozoraContentsParser(getContext(), new NestedContentsParserHandler(handler))).parse((new StringReader("<body>" + ruby + "</body>")), base);
                            handler.otherElement("/ruby");
                            isTagHandled = true;
                        }
                    }
                } else if (lowerTag.startsWith("/body")) {
                    handler.parseFinished();
                    break;
                }
                if (!isTagHandled)
                    handler.otherElement(tag);
            }
        } finally {
            try {
                tin.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private final AozoraContext context;
    private final AozoraContentsParserHandler handler;
}
