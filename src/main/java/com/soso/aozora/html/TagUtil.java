/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.html;


public class TagUtil {

    public static String getAttValue(String src, String attKey) {
        int attIndex = src.indexOf(attKey);
        if (attIndex != -1) {
            int equalIndex = src.indexOf(EQUAL, attIndex + attKey.length());
            if (equalIndex != -1) {
                int quoteStartIndex = src.indexOf(QUOTE, equalIndex + 1);
                if (quoteStartIndex != -1) {
                    int quoteEndIndex = src.indexOf(QUOTE, quoteStartIndex + 1);
                    if (quoteEndIndex != -1)
                        return src.substring(quoteStartIndex + 1, quoteEndIndex);
                }
            }
        }
        return null;
    }

    public static String getHttpEquivContentTypeCharset(String tag) {
        String lowerTag = tag.toLowerCase();
        if (lowerTag.contains("http-equiv")) {
            String httpEquiv = getAttValue(lowerTag, "http-equiv");
            if ("content-type".equals(httpEquiv)) {
                String contentAttr = getAttValue(lowerTag, "content");
                if (contentAttr != null) {
                    for (String value : contentAttr.split(";")) {
                        value = value.trim();
                        if (value.startsWith("charset")) {
                            int valueIndex = lowerTag.indexOf(value);
                            int beginIndex = lowerTag.indexOf("=", valueIndex) + 1;
                            int endIndex = valueIndex + value.length();
                            String charset = tag.substring(beginIndex, endIndex);
                            return charset.trim();
                        }
                    }
                }
            }
        }
        return null;
    }

    private static final char EQUAL = '='; // 61
    private static final char QUOTE = '"'; // 34
}
