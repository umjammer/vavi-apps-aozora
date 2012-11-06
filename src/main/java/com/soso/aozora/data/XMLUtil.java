/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.data;

import java.util.HashMap;
import java.util.Map;


public class XMLUtil {

    private static Map<String, Character> entityMap;

    private static Map<String, Character> getEntityMap() {
        if (entityMap == null) {
            entityMap = new HashMap<String, Character>();
            entityMap.put("amp", '&');
            entityMap.put("lt", '<');
            entityMap.put("gt", '>');
            entityMap.put("quot", '"');
            entityMap.put("apos", '\'');
            entityMap.put("nbsp", '\u0160');
            entityMap.put("iexcl", '\241');
            entityMap.put("cent", '\242');
            entityMap.put("pound", '\243');
            entityMap.put("curren", '\244');
            entityMap.put("yen", '\245');
            entityMap.put("sect", '\247');
            entityMap.put("uml", '\250');
            entityMap.put("copy", '\251');
            entityMap.put("ordf", '\252');
            entityMap.put("laquo", '\253');
            entityMap.put("not", '\254');
            entityMap.put("reg", '\256');
            entityMap.put("macr", '\257');
            entityMap.put("deg", '\260');
            entityMap.put("plusmn", '\261');
            entityMap.put("acute", '\264');
            entityMap.put("micro", '\265');
            entityMap.put("para", '\266');
            entityMap.put("middot", '\267');
            entityMap.put("cedil", '\270');
            entityMap.put("ordm", '\272');
            entityMap.put("raquo", '\273');
            entityMap.put("iquest", '\277');
            entityMap.put("Agrave", '\300');
            entityMap.put("Aacute", '\301');
            entityMap.put("Acirc", '\302');
            entityMap.put("Atilde", '\303');
            entityMap.put("Auml", '\304');
            entityMap.put("Aring", '\305');
            entityMap.put("AElig", '\306');
            entityMap.put("Ccedil", '\307');
            entityMap.put("Egrave", '\310');
            entityMap.put("Eacute", '\311');
            entityMap.put("Ecirc", '\312');
            entityMap.put("Euml", '\313');
            entityMap.put("Igrave", '\314');
            entityMap.put("Iacute", '\315');
            entityMap.put("Icirc", '\316');
            entityMap.put("Iuml", '\317');
            entityMap.put("Ntilde", '\321');
            entityMap.put("Ograve", '\322');
            entityMap.put("Oacute", '\323');
            entityMap.put("Ocirc", '\324');
            entityMap.put("Otilde", '\325');
            entityMap.put("Ouml", '\326');
            entityMap.put("Oslash", '\330');
            entityMap.put("Ugrave", '\331');
            entityMap.put("Uacute", '\332');
            entityMap.put("Ucirc", '\333');
            entityMap.put("Uuml", '\334');
            entityMap.put("szlig", '\337');
            entityMap.put("agrave", '\340');
            entityMap.put("aacute", '\341');
            entityMap.put("acirc", '\342');
            entityMap.put("atilde", '\343');
            entityMap.put("auml", '\344');
            entityMap.put("aring", '\345');
            entityMap.put("aelig", '\346');
            entityMap.put("ccedil", '\347');
            entityMap.put("egrave", '\350');
            entityMap.put("eacute", '\351');
            entityMap.put("ecirc", '\352');
            entityMap.put("euml", '\353');
            entityMap.put("igrave", '\354');
            entityMap.put("iacute", '\355');
            entityMap.put("icirc", '\356');
            entityMap.put("iuml", '\357');
            entityMap.put("ntilde", '\361');
            entityMap.put("ograve", '\362');
            entityMap.put("oacute", '\363');
            entityMap.put("ocirc", '\364');
            entityMap.put("otilde", '\365');
            entityMap.put("ouml", '\366');
            entityMap.put("divide", '\367');
            entityMap.put("oslash", '\370');
            entityMap.put("ugrave", '\371');
            entityMap.put("uacute", '\372');
            entityMap.put("ucirc", '\373');
            entityMap.put("uuml", '\374');
            entityMap.put("yuml", '\377');
        }
        return entityMap;
    }

    public static CharSequence parsePCDATA(String pcdata) {
        if (pcdata.indexOf('&') == -1 || pcdata.indexOf(';') == -1)
            return pcdata;
        StringBuilder cdataB = new StringBuilder();
        int len = pcdata.length();
        for (int i = 0; i < len; i++) {
            char c = pcdata.charAt(i);
            if (c != '&') {
                cdataB.append(c);
                continue;
            }
            int semicolonIndex = pcdata.indexOf(';', i);
            if (semicolonIndex == -1) {
                cdataB.append(pcdata.substring(i));
                break;
            }
            String ent = pcdata.substring(i + 1, semicolonIndex);
            Character ref = getEntityMap().get(ent);
            if (ref != null) {
                cdataB.append(ref);
                i = semicolonIndex;
                continue;
            }
            if (ent.matches("#[0-9]+")) {
                String dec = ent.substring(1);
                try {
                    int code = Integer.valueOf(dec, 10).intValue();
                    if (Character.isDefined(code)) {
                        cdataB.append(Character.toChars(code));
                        continue;
                    }
                } catch (NumberFormatException e) {
                }
            }
            if (ent.matches("#x[0-9a-fA-F]+")) {
                String hex = ent.substring(2);
                try {
                    int code = Integer.valueOf(hex, 16).intValue();
                    if (Character.isDefined(code)) {
                        cdataB.append(Character.toChars(code));
                        continue;
                    }
                } catch (NumberFormatException e) {
                }
            }
            cdataB.append(c);
        }

        return cdataB;
    }
}
