/*
 * https://github.com/iWumboUWumbo2/AozoraToHTML/blob/main/AozoraBunkoRuby.java
 */

package com.soso.aozora.data;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

import vavi.util.Debug;


/**
 *
 */
public final class AozoraBunkoRuby {

    private String text;

    private List<Integer> liKanjiStart;
    private List<Integer> liFuriganaOpening;
    private List<Integer> liFuriganaClosing;
    private List<Integer> liBoutenOpening;
    private List<Integer> liBoutenClosing;
    private List<Integer> liKanjiBou;

    private HashMap<String, String> tenStyles;
    private HashMap<String, String> senStyles;

    private static final String BOUTEN = "\u508d\u70b9";
    private static final String BOUSEN = "\u7dda";
    private static final String SPACING = "\u5B57\u4E0B\u3052";

    private static final String FW_INTS = "\uFF10\uFF11\uFF12\uFF13\uFF14\uFF15\uFF16\uFF17\uFF18\uFF19";

    private Writer writer;

    /** */
    public AozoraBunkoRuby(Reader reader, Writer writer) {
        this.liKanjiStart = new ArrayList<>();
        this.liFuriganaOpening = new ArrayList<>();
        this.liFuriganaClosing = new ArrayList<>();
        this.liBoutenOpening = new ArrayList<>();
        this.liBoutenClosing = new ArrayList<>();
        this.liKanjiBou = new ArrayList<>();

        this.tenStyles = new HashMap<>();
        this.tenStyles.put("\u306b\u4E38\u508D\u70B9", "\u25CF");
        this.tenStyles.put("\u306b\u767D\u4E38\u508D\u70B9", "\u25CB");
        this.tenStyles.put("\u306b\u9ED2\u4E09\u89D2\u508D\u70B9", "\u25B2");
        this.tenStyles.put("\u306b\u767D\u4E09\u89D2\u508D\u70B9", "\u25B3");
        this.tenStyles.put("\u306b\u4E8C\u91CD\u4E38\u508D\u70B9", "\u25CE");
        this.tenStyles.put("\u306b\u3070\u3064\u508D\u70B9", "\u00D7");

        this.senStyles = new HashMap<>();
        this.senStyles.put("\u306b\u4e8c\u91cd\u508d\u7dda", "text-decoration-style: double;");
        this.senStyles.put("\u306b\u9396\u7dda", "text-decoration-style: dotted;");
        this.senStyles.put("\u306b\u7834\u7dda", "text-decoration-style: dashed;");
        this.senStyles.put("\u306b\u6ce2\u7dda", "text-decoration-style: wavy;");

        readText(reader);
        this.writer = writer;
    }

    /** */
    private void replacements() {
        this.text = this.text.replaceAll("\uFF3B\uFF03\u633F\u7D75\uFF08", "<img src=\"");
        this.text = this.text.replaceAll("\uFF09\u5165\u308B\uFF3D", "\">");

        this.text = this.text.replaceAll("\uFF3B\uFF03\u6539\u9801\uFF3D", "<br>");
    }

    /** */
    private void printDebug(Level level) {
        Debug.printf(Level.FINER, "%d %d %d %d %d %d\n",
                liKanjiStart.size(),
                liFuriganaOpening.size(),
                liFuriganaClosing.size(),
                liBoutenOpening.size(),
                liBoutenClosing.size(),
                liKanjiBou.size());

        int count1 = 0, count2 = 0;
        for (int i = 0; i < this.text.length(); i++) {
            if (this.text.charAt(i) == '\u300a') {
                count1++;
            } else if (this.text.charAt(i) == '\u300b') {
                count2++;
            }
        }
        Debug.printf(Level.FINER, "%d %d", count1, count2);

        Debug.println(Level.FINER, this.text.substring(133130, 133150));
        Debug.println(Level.FINER, "---------------------------------------------------");
        Debug.println(Level.FINER, this.text.substring(133733, 133999));
    }

    /** */
    public String parse() {
        this.replacements();
        this.getMarkerIndices();

        printDebug(Level.FINE);

        StringBuilder sb = new StringBuilder();

        int i = 0, j = 0, curr = 0;

        int kssize = this.liKanjiStart.size(), kbsize = this.liKanjiBou.size();
        while (i < kssize && j < kbsize) {
Debug.printf(Level.FINER, "%d, %d: %d, [%d, %d], [%d, %d]\n", i, j, curr, liKanjiStart.get(i), liFuriganaClosing.get(i), liKanjiBou.get(j), liBoutenClosing.get(j));
            if (liKanjiStart.get(i) < liKanjiBou.get(j)) {
                sb.append(this.text.substring(curr, liKanjiStart.get(i)));
                sb.append(furiganaToRubyTag(liKanjiStart.get(i), liFuriganaOpening.get(i), liFuriganaClosing.get(i)));
                curr = liFuriganaClosing.get(i) + 1;
                i++;
            } else {
                sb.append(this.text.substring(curr, liKanjiBou.get(j)));
                sb.append(boutenToRubyTag(liKanjiBou.get(j), liBoutenOpening.get(j), liBoutenClosing.get(j)));
                curr = liBoutenClosing.get(j) + 1;
                j++;
            }
        }

        while (i < this.liKanjiStart.size()) {
            sb.append(this.text.substring(curr, liKanjiStart.get(i)));
            sb.append(furiganaToRubyTag(liKanjiStart.get(i), liFuriganaOpening.get(i), liFuriganaClosing.get(i)));
            curr = liFuriganaClosing.get(i) + 1;
            i++;
        }


        while (j < this.liKanjiBou.size()) {
Debug.printf(Level.FINER, "%d: %d, %d\n", j, curr, liKanjiBou.get(j));
            if (curr >= liKanjiBou.get(j))
                break;
            sb.append(this.text.substring(curr, liKanjiBou.get(j)));
            sb.append(boutenToRubyTag(liKanjiBou.get(j), liBoutenOpening.get(j), liBoutenClosing.get(j)));
            curr = liBoutenClosing.get(j) + 1;
            j++;
        }

        sb.append(this.text.substring(curr));

        return sb.toString().replaceAll("\uff5c", "");
    }

    /** */
    public String bookmark(String text) {
        StringBuilder sb = new StringBuilder();
        int curr = 0, count = 1, idx;
        while ((idx = text.indexOf('\u3002', curr)) != -1) {
Debug.printf(Level.FINER, "%d %d\n", curr, idx);
            sb.append(text.substring(curr, idx));
            sb.append("<a name=\"save_").append(count).append("\" href=\"#save_").append(count).append("\">\u3002</a>");
            curr = idx + 1;
            count++;
        }
        return sb.toString();
    }

    /** */
    private void getMarkerIndices() {
        for (int i = 0; i < text.length(); i++) {
            // <<
            if (this.text.charAt(i) == '\u300a') {
                this.liFuriganaOpening.add(i);

                int idx;
                for (idx = i - 1; isCJKIdeograph(this.text.charAt(idx)) && this.text.charAt(idx) != '\uff5c'; idx--) ;
                if (idx == i - 1) {
                    for (idx = i - 1; this.text.charAt(idx) != '\uff5c'; idx--) ;
                }

                this.liKanjiStart.add(idx + 1);

                for (idx = i + 1; this.text.charAt(idx) != '\u300b'; idx++) ;
                this.liFuriganaClosing.add(idx);
                i = idx + 1;
            }
//			// >>
//			else if (this.text.charAt(i) == '\u300b') {
//				this.liFuriganaClosing.add(i);
//			}
            // [
            else if (this.text.charAt(i) == '\uff3b') {
                this.liBoutenOpening.add(i);

                int eidx, ws = -1, we = -1;
                for (eidx = i + 1; this.text.charAt(eidx) != '\uff3d'; eidx++) {
                    if (this.text.charAt(eidx) == '\u300c')
                        ws = eidx;
                    else if (this.text.charAt(eidx) == '\u300d')
                        we = eidx;
                }
                String btext = this.text.substring(i + 1, eidx);
                if (btext.endsWith(BOUTEN) || btext.endsWith(BOUSEN))
                    this.liKanjiBou.add(i - (we - ws - 1));
                else
                    this.liKanjiBou.add(i);
                this.liBoutenClosing.add(eidx);
                i = eidx + 1;
            }
        }
    }

    /** */
    private String furiganaToRubyTag(int kanjiIndex, int startIndex, int endIndex) {
        StringBuilder sbFurigana = new StringBuilder();
        int i;

        sbFurigana.append("<rp>").append(this.text.charAt(startIndex)).append("</rp><rt>");
Debug.printf(Level.FINER, "%d %d %d\n", kanjiIndex, startIndex, endIndex);
        sbFurigana.append(this.text.substring(startIndex + 1, endIndex));
        sbFurigana.append("</rt><rp>").append(this.text.charAt(endIndex)).append("</rp></ruby>");

        sbFurigana.insert(0, "</rb>");
        sbFurigana.insert(0, this.text.substring(kanjiIndex, startIndex));
        sbFurigana.insert(0, "<ruby><rb>");

        return sbFurigana.toString();
    }

    /** */
    private String boutenToRubyTag(int kanjiIndex, int startIndex, int endIndex) {
        String btext = this.text.substring(startIndex + 1, endIndex);
        int wordStart = btext.indexOf("\u300c");
        int wordEnd = btext.indexOf("\u300d");
        int wordLength = wordEnd - wordStart - 1;

        if (kanjiIndex != startIndex) {
            StringBuilder output = new StringBuilder();
            if (btext.endsWith(BOUTEN)) {
                String stylename = btext.substring(wordEnd + 1);
                String style = this.tenStyles.getOrDefault(stylename, "\uFE45");
                output.append("<ruby><rb>").append(this.text.substring(kanjiIndex, startIndex));
                output.append("<rp>\u300a</rp><rt>");
                for (int i = 0; i < wordLength; i++)
                    output.append(style);
                output.append("</rt><rp>\u300b</rp></ruby>");
                return output.toString();
            } else if (btext.endsWith(BOUSEN)) {
                String stylename = btext.substring(wordEnd + 1);
                String style = this.senStyles.getOrDefault(stylename, "");
                output.append("<u style=\"").append(style).append("\">").append(this.text, kanjiIndex, startIndex).append("</u>");
                return output.toString();
            }
        } else {
            StringBuilder output = new StringBuilder();
            if (btext.endsWith(SPACING)) {
                int hash = btext.indexOf("\uFF03");
                int ji = btext.indexOf("\u5B57");
                String space = btext.substring(hash + 1, ji);
                int value = 0;
                for (int i = 0; i < space.length(); i++) {
                    value = value * 10 + FW_INTS.indexOf(space.charAt(i));
                }
                for (int i = 0; i < value; i++)
                    output.append(" ");

                return output.toString();
            }
        }

        return this.text.substring(kanjiIndex, startIndex);
    }

    /** */
    private boolean isCJKIdeograph(char c) {
        return Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || c == '\u3005';
    }

    /** */
    private void readText(Reader reader) {
        Scanner scanner = new Scanner(reader);
        StringBuilder sb = new StringBuilder();
        boolean skip = false;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!skip) {
                if (line.startsWith("-")) {
                    skip = true;
Debug.println("skip start: " + line);
                } else {
                    sb.append(line).append("<br/>\n");
                }
            } else {
                if (line.startsWith("-")) {
                    skip = false;
Debug.println("skip end: " + line);
                }
            }
        }
        this.text = sb.toString();
    }

    /** */
    public void printHtml() {
        PrintWriter pr = new PrintWriter(writer);
        pr.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        pr.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        pr.println("<head>");
        pr.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
        pr.println("<link rel='stylesheet' type='text/css' href='jnf_style.css' />");
        pr.println("</head>");
        pr.println("<body>");

        pr.println(bookmark(parse()));

        pr.println("</body>");
        pr.println("</html>");

        pr.flush();
    }
}