/*
 * https://github.com/iWumboUWumbo2/AozoraToHTML/blob/main/AozoraBunkoRuby.java
 */

package vavi.text.aozora.converter;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * aozora text to html converter
 *
 * TODO ruby start is wrong
 */
public final class AozoraBunkoRuby implements Converter {

    private static final Logger logger = System.getLogger(AozoraBunkoRuby.class.getName());

    private String text;

    private final List<Integer> kanjiStarts;
    private final List<Integer> furiganaOpenings;
    private final List<Integer> furiganaClosings;
    private final List<Integer> emphasisOpenings;
    private final List<Integer> emphasisClosings;
    private final List<Integer> liKanjiBou;

    private final HashMap<String, String> tenStyles;
    private final HashMap<String, String> senStyles;

    private static final String BOUTEN = "傍点";
    private static final String BOUSEN = "線";

    /** the annotations of a page, which are kept as aozora html does, the viewer takes them */
    private static final String[] PAGE_BREAKS = {"［＃改ページ］", "［＃改丁］", "［＃改見開き］", "［＃改段］", "［＃ページの左右中央］"};

    private static final String FW_INTS = "０１２３４５６７８９";
    private static final String KANJI_INTS = "〇一二三四五六七八九";

    /** a number of letters in an annotation */
    private static final String N = "([０-９0-9〇一二三四五六七八九十]+)";

    /** an illustration, ［＃説明（ファイル名、横○×縦○）入る］ */
    private static final Pattern ILLUSTRATION = Pattern.compile("［＃([^［］（]*)（([^（）、］]+?)(?:、横([0-9０-９]+)×縦([0-9０-９]+))?）入る］");

    /** a back reference to a heading, ○○［＃「○○」は中見出し］ */
    private static final Pattern HEADING_BACK = Pattern.compile("［＃「([^」]+)」は(同行|窓)?(大|中|小)見出し］");
    /** a heading, ［＃中見出し］○○［＃中見出し終わり］ and the block of it */
    private static final Pattern HEADING = Pattern.compile("［＃(ここから)?(同行|窓)?(大|中|小)見出し］(<br/>\n)?");
    private static final Pattern HEADING_END = Pattern.compile("［＃(ここで)?(同行|窓)?(大|中|小)見出し終わり］(<br/>)?");

    /** a heading which is not in a line of the text ends its line, the line break after it is not needed */
    private static final Pattern BLOCK_HEADING_BR = Pattern.compile("(<h([3-5]) class=\"(?!dogyo-|mado-)[^\"]*\">(?:(?!</h\\2>).)*</h\\2>)<br/>");

    /** a back reference to bold, italic or the size of letters, ○○［＃「○○」は太字］ */
    private static final Pattern STYLE_BACK = Pattern.compile("［＃「([^」]+)」は(太字|斜体|" + N + "段階(大き|小さ)な文字)］");
    /** bold, italic or the size of letters, ［＃太字］○○［＃太字終わり］ */
    private static final Pattern STYLE = Pattern.compile("［＃(太字|斜体|" + N + "段階(大き|小さ)な文字)］");
    private static final Pattern STYLE_END = Pattern.compile("［＃(太字|斜体|大きな文字|小さな文字)終わり］");
    /** the block of them */
    private static final Pattern BLOCK_STYLE = Pattern.compile("［＃ここから(太字|斜体|" + N + "段階(大き|小さ)な文字)］<br/>\n");
    private static final Pattern BLOCK_STYLE_END = Pattern.compile("［＃ここで(太字|斜体|大きな文字|小さな文字)終わり］<br/>");

    /** a back reference to a caption, ○○［＃「○○」はキャプション］ */
    private static final Pattern CAPTION_BACK = Pattern.compile("［＃「([^」]+)」はキャプション］");

    private static final Pattern BLOCK_JISAGE = Pattern.compile("［＃ここから" + N + "字下げ(?:、折り返して" + N + "字下げ)?(?:、[^］]*)?］");
    private static final Pattern BLOCK_TENTSUKI = Pattern.compile("［＃ここから改行天付き、折り返して" + N + "字下げ］");
    private static final Pattern BLOCK_CHITSUKI = Pattern.compile("［＃ここから地付き］");
    private static final Pattern BLOCK_JIAGE = Pattern.compile("［＃ここから地から" + N + "字上げ］");
    private static final Pattern BLOCK_END = Pattern.compile("［＃ここで(字下げ|地付き|字上げ)終わり］");
    private static final Pattern LINE_JISAGE = Pattern.compile("［＃(?:天から)?" + N + "字下げ］(.*)");
    private static final Pattern LINE_CHITSUKI = Pattern.compile("(.*?)［＃(?:地付き|地から" + N + "字上げ)］(.*)");

    private final boolean bookmark = false;
    private final boolean rpTag = false;

    /** */
    public AozoraBunkoRuby() {
        this.kanjiStarts = new ArrayList<>();
        this.furiganaOpenings = new ArrayList<>();
        this.furiganaClosings = new ArrayList<>();
        this.emphasisOpenings = new ArrayList<>();
        this.emphasisClosings = new ArrayList<>();
        this.liKanjiBou = new ArrayList<>();

        this.tenStyles = new HashMap<>();
        //
        this.tenStyles.put("に丸傍点", "●");
        this.tenStyles.put("に白丸傍点", "○");
        this.tenStyles.put("に黒三角傍点", "▲");
        this.tenStyles.put("に白三角傍点", "△");
        this.tenStyles.put("に二重丸傍点", "◎");
        this.tenStyles.put("にばつ傍点", "×");

        this.senStyles = new HashMap<>();
        this.senStyles.put("に二重傍線", "text-decoration-style: double;");
        this.senStyles.put("に鎖線", "text-decoration-style: dotted;");
        this.senStyles.put("に破線", "text-decoration-style: dashed;");
        this.senStyles.put("に波線", "text-decoration-style: wavy;");
    }

    /**
     * The annotations of the layout are marked up as aozora html does.
     *
     * @see "https://www.aozora.gr.jp/aozora-manual/index-input.html"
     */
    private void replacements() {
        this.text = illustrations(this.text);

        this.text = backReferences(this.text, HEADING_BACK, m -> headingTag(m.group(2), m.group(3)));
        this.text = headings(this.text);
        this.text = BLOCK_HEADING_BR.matcher(this.text).replaceAll("$1");
        this.text = backReferences(this.text, CAPTION_BACK, m -> "span class=\"caption\"");
        this.text = this.text.replace("［＃キャプション］", "<span class=\"caption\">");
        this.text = this.text.replace("［＃キャプション終わり］", "</span>");
        this.text = this.text.replace("［＃ここからキャプション］<br/>\n", "<div class=\"caption\">\n");
        this.text = this.text.replace("［＃ここでキャプション終わり］<br/>", "</div>");

        this.text = styles(this.text);

        this.text = this.text.replace("［＃改頁］", "［＃改ページ］");

        // page breaks are marked up as aozora html does, the viewer breaks the page at them
        for (String pageBreak : PAGE_BREAKS) {
            this.text = this.text.replace(pageBreak, "<span class=\"notes\">" + pageBreak + "</span>");
        }

        this.text = layout(this.text);
    }

    /** 4-10. an illustration, the file is at the same place as the text */
    private static String illustrations(String text) {
        Matcher m = ILLUSTRATION.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            StringBuilder img = new StringBuilder("<img class=\"illustration\"");
            if (m.group(3) != null)
                img.append(" width=\"").append(toInt(m.group(3))).append("\" height=\"").append(toInt(m.group(4))).append("\"");
            img.append(" src=\"").append(m.group(2)).append("\" alt=\"").append(m.group(1)).append("\" />");
            m.appendReplacement(sb, Matcher.quoteReplacement(img.toString()));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * 4-8. the tag of bold, italic or the size of letters, the size is given in the steps from
     * the letters of the text
     *
     * @param type 太字, 斜体 or ○段階大きな文字
     * @param steps the steps of the size, null for the others
     * @param larger 大き or 小さ
     */
    private static String styleTag(String tag, String type, String steps, String larger) {
        if (type.equals("太字"))
            return tag + " class=\"futoji\"";
        if (type.equals("斜体"))
            return tag + " class=\"shatai\"";
        int n = toInt(steps);
        String size = (n >= 3 ? "xx-" : n == 2 ? "x-" : "") + (larger.equals("大き") ? "large" : "small");
        return tag + " class=\"" + (larger.equals("大き") ? "dai" : "sho") + n + "\" style=\"font-size: " + size + ";\"";
    }

    /** 4-8. bold, italic and the size of letters */
    private static String styles(String text) {
        text = replace(text, BLOCK_STYLE, m -> "<" + styleTag("div", m.group(1), m.group(2), m.group(3)) + ">\n");
        text = BLOCK_STYLE_END.matcher(text).replaceAll("</div>");
        text = backReferences(text, STYLE_BACK, m -> styleTag("span", m.group(2), m.group(3), m.group(4)));
        text = replace(text, STYLE, m -> "<" + styleTag("span", m.group(1), m.group(2), m.group(3)) + ">");
        return STYLE_END.matcher(text).replaceAll("</span>");
    }

    private static String replace(String text, Pattern pattern, Function<Matcher, String> replacement) {
        Matcher m = pattern.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find())
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement.apply(m)));
        m.appendTail(sb);
        return sb.toString();
    }

    /** 4-2. the tag of a heading */
    private static String headingTag(String type, String level) {
        String prefix = type == null ? "" : type.equals("同行") ? "dogyo-" : "mado-";
        return switch (level) {
            case "大" -> "h3 class=\"" + prefix + "o-midashi\"";
            case "中" -> "h4 class=\"" + prefix + "naka-midashi\"";
            default -> "h5 class=\"" + prefix + "ko-midashi\"";
        };
    }

    /** 4-2. a heading which is given by the annotations before and after it */
    private static String headings(String text) {
        Matcher m = HEADING.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find())
            m.appendReplacement(sb, Matcher.quoteReplacement("<" + headingTag(m.group(2), m.group(3)) + ">"));
        m.appendTail(sb);
        m = HEADING_END.matcher(sb.toString());
        sb = new StringBuilder();
        while (m.find()) {
            String tag = headingTag(m.group(2), m.group(3));
            // the block heading ends its line
            m.appendReplacement(sb, Matcher.quoteReplacement("</" + tag.substring(0, 2) + ">" + (m.group(1) != null || m.group(4) == null ? "" : "<br/>")));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * An annotation which refers to the text before it, as ○○［＃「○○」は中見出し］, is made
     * into the tags around the text.
     *
     * @param tag the start tag without the brackets, the end tag is made from it
     */
    private static String backReferences(String text, Pattern pattern, Function<Matcher, String> tag) {
        Matcher m = pattern.matcher(text);
        int from = 0;
        while (m.find(from)) {
            int start = findTarget(text, m.start(), m.group(1));
            if (start < 0) {
logger.log(Level.DEBUG, "target not found: " + m.group());
                from = m.end();
                continue;
            }
            String open = "<" + tag.apply(m) + ">";
            String close = "</" + open.substring(1).split("[\\s>]", 2)[0] + ">";
            text = text.substring(0, start) + open + text.substring(start, m.start()) + close + text.substring(m.end());
            from = m.start() + open.length() + close.length();
            m = pattern.matcher(text);
        }
        return text;
    }

    /**
     * Where the target of a back reference annotation starts in the line, the ruby, the other
     * annotations and the tags in between are skipped.
     *
     * @param end where the annotation starts
     * @return -1 when the target is not found
     */
    private static int findTarget(String text, int end, String target) {
        int i = end;
        int j = target.length();
        while (j > 0) {
            if (i <= 0)
                return -1;
            char c = text.charAt(i - 1);
            if (c == target.charAt(j - 1)) {
                i--;
                j--;
            } else if (c == '》' || c == '］' || c == '>') {
                int o = text.lastIndexOf(c == '》' ? '《' : c == '］' ? '［' : '<', i - 1);
                if (o < 0)
                    return -1;
                i = o;
            } else if (c == '｜') {
                i--;
            } else {
                return -1;
            }
        }
        // the start of the ruby base is included
        if (i > 0 && text.charAt(i - 1) == '｜')
            i--;
        return i;
    }

    /**
     * 4-3. the indents (字下げ) and the alignments to the line end (地付き, 地寄せ) of a line
     * and of a block of lines. A block ends at the start of another block too.
     */
    private static String layout(String text) {
        StringBuilder sb = new StringBuilder();
        boolean block = false;
        for (String line : text.split("\n", -1)) {
            boolean br = line.endsWith("<br/>");
            String body = br ? line.substring(0, line.length() - "<br/>".length()) : line;
            String open = null;
            Matcher m = BLOCK_END.matcher(body);
            if (m.lookingAt() && m.end() < body.length()) {
                // the rest of the line after the end of the block
                if (block)
                    sb.append("</div>\n");
                block = false;
                body = body.substring(m.end());
            }
            if ((m = BLOCK_JISAGE.matcher(body)).matches()) {
                int wrap = toInt(m.group(2) != null ? m.group(2) : m.group(1));
                open = indentTag(toInt(m.group(1)), wrap);
            } else if ((m = BLOCK_TENTSUKI.matcher(body)).matches()) {
                open = indentTag(0, toInt(m.group(1)));
            } else if (BLOCK_CHITSUKI.matcher(body).matches()) {
                open = chitsukiTag("div", 0);
            } else if ((m = BLOCK_JIAGE.matcher(body)).matches()) {
                open = chitsukiTag("div", toInt(m.group(1)));
            } else if (BLOCK_END.matcher(body).matches()) {
                if (block)
                    sb.append("</div>\n");
                block = false;
                continue;
            }
            if (open != null) {
                if (block)
                    sb.append("</div>\n");
                sb.append(open).append("\n");
                block = true;
                continue;
            }

            if ((m = LINE_JISAGE.matcher(body)).matches()) {
                int n = toInt(m.group(1));
                sb.append(indentTag(n, n)).append(chitsuki(m.group(2), br)).append("</div>\n");
            } else if ((m = LINE_CHITSUKI.matcher(body)).matches() && m.group(1).isEmpty()) {
                int n = m.group(2) != null ? toInt(m.group(2)) : 0;
                sb.append(chitsukiTag("div", n)).append(m.group(3)).append("</div>\n");
            } else {
                sb.append(chitsuki(body, br)).append(br ? "<br/>" : "").append("\n");
            }
        }
        if (block)
            sb.append("</div>\n");
        // the last line has no line break after it
        return sb.substring(0, sb.length() - 1);
    }

    /** the part of the line which is aligned to the line end */
    private static String chitsuki(String body, boolean br) {
        Matcher m = LINE_CHITSUKI.matcher(body);
        if (!m.matches())
            return body;
        int n = m.group(2) != null ? toInt(m.group(2)) : 0;
        return m.group(1) + chitsukiTag("span", n) + m.group(3) + "</span>";
    }

    private static String indentTag(int first, int wrap) {
        if (first == wrap)
            return "<div class=\"jisage_" + first + "\" style=\"margin-left: " + first + "em\">";
        else
            return "<div class=\"burasage\" style=\"margin-left: " + wrap + "em; text-indent: " + (first - wrap) + "em;\">";
    }

    private static String chitsukiTag(String tag, int n) {
        return "<" + tag + " class=\"chitsuki_" + n + "\" style=\"text-align:right; margin-right: " + n + "em\">";
    }

    /** a number in an annotation, which is written in full width digits or in kanji */
    static int toInt(String number) {
        int value = 0;
        int kanji = 0; // for 十
        for (int i = 0; i < number.length(); i++) {
            char c = number.charAt(i);
            int d = FW_INTS.indexOf(c);
            if (d < 0)
                d = KANJI_INTS.indexOf(c);
            if (d < 0 && c >= '0' && c <= '9')
                d = c - '0';
            if (c == '十') {
                kanji += (value == 0 ? 1 : value) * 10;
                value = 0;
            } else if (d >= 0) {
                value = value * 10 + d;
            }
        }
        return kanji + value;
    }

    /** */
    private void printDebug(Level level) {
        logger.log(Level.TRACE, "%d %d %d %d %d %d".formatted(
                kanjiStarts.size(),
                furiganaOpenings.size(),
                furiganaClosings.size(),
                emphasisOpenings.size(),
                emphasisClosings.size(),
                liKanjiBou.size()));

        int count1 = 0, count2 = 0;
        for (int i = 0; i < this.text.length(); i++) {
            if (this.text.charAt(i) == '《') {
                count1++;
            } else if (this.text.charAt(i) == '》') {
                count2++;
            }
        }
        logger.log(Level.TRACE, "%d %d".formatted(count1, count2));

        logger.log(Level.TRACE, this.text.substring(133130, 133150));
        logger.log(Level.TRACE, "---------------------------------------------------");
        logger.log(Level.TRACE, this.text.substring(133733, 133999));
    }

    /** */
    public String parse() {
        this.replacements();
        this.getMarkerIndices();

//        printDebug(Level.DEBUG);

        StringBuilder sb = new StringBuilder();

        int i = 0, j = 0, curr = 0;

        int kssize = this.kanjiStarts.size(), kbsize = this.liKanjiBou.size();
        while (i < kssize && j < kbsize) {
logger.log(Level.TRACE, "%d, %d: %d, [%d, %d], [%d, %d]".formatted(i, j, curr, kanjiStarts.get(i), furiganaClosings.get(i), liKanjiBou.get(j), emphasisClosings.get(j)));
            if (kanjiStarts.get(i) < curr) {
                // overlaps the one already made
                i++;
            } else if (isEmphasisOverlapped(j, curr)) {
                j++;
            } else if (kanjiStarts.get(i) < liKanjiBou.get(j)) {
                sb.append(this.text, curr, kanjiStarts.get(i));
                sb.append(furiganaToRubyTag(kanjiStarts.get(i), furiganaOpenings.get(i), furiganaClosings.get(i)));
                curr = furiganaClosings.get(i) + 1;
                i++;
            } else {
                sb.append(this.text, curr, liKanjiBou.get(j));
                sb.append(emphasisToRubyTag(liKanjiBou.get(j), emphasisOpenings.get(j), emphasisClosings.get(j)));
                curr = emphasisClosings.get(j) + 1;
                j++;
            }
        }

        while (i < this.kanjiStarts.size()) {
            if (kanjiStarts.get(i) < curr) {
                i++;
                continue;
            }
            sb.append(this.text, curr, kanjiStarts.get(i));
            sb.append(furiganaToRubyTag(kanjiStarts.get(i), furiganaOpenings.get(i), furiganaClosings.get(i)));
            curr = furiganaClosings.get(i) + 1;
            i++;
        }


        while (j < this.liKanjiBou.size()) {
logger.log(Level.TRACE, "%d: %d, %d".formatted(j, curr, liKanjiBou.get(j)));
            if (isEmphasisOverlapped(j, curr)) {
                j++;
                continue;
            }
            sb.append(this.text, curr, liKanjiBou.get(j));
            sb.append(emphasisToRubyTag(liKanjiBou.get(j), emphasisOpenings.get(j), emphasisClosings.get(j)));
            curr = emphasisClosings.get(j) + 1;
            j++;
        }

        sb.append(this.text.substring(curr));

        return sb.toString().replace("\uff5c", "");
    }

    /**
     * the target of an emphasis which overlaps the one already made is given up, but its
     * annotation is still taken as a marker so that it is not shown
     *
     * @return true when the whole emphasis is already made
     */
    private boolean isEmphasisOverlapped(int j, int curr) {
        if (liKanjiBou.get(j) >= curr)
            return false;
        if (emphasisOpenings.get(j) < curr)
            return true;
        liKanjiBou.set(j, emphasisOpenings.get(j));
        return false;
    }

    /** */
    public String bookmark(String text) {
        StringBuilder sb = new StringBuilder();
        int curr = 0, count = 1, idx;
        while ((idx = text.indexOf('。', curr)) != -1) {
logger.log(Level.TRACE, "%d %d".formatted(curr, idx));
            sb.append(text, curr, idx);
            sb.append("<a name=\"save_").append(count).append("\" href=\"#save_").append(count).append("\">。</a>");
            curr = idx + 1;
            count++;
        }
        return sb.toString();
    }

    /** */
    private void getMarkerIndices() {
        for (int i = 0; i < text.length(); i++) {
            // <<
            if (this.text.charAt(i) == '《') {
                this.furiganaOpenings.add(i);

                // TODO check is this algorithm can ruby kanji only?
                int idx = i - 1;
                if (this.text.charAt(idx) == '］') {
                    // a gaiji "※［＃...］" is the base letter
                    idx = this.text.lastIndexOf('［', idx);
                    if (idx > 0 && this.text.charAt(idx - 1) == '※') idx--;
                    idx--;
                } else {
                    while (idx >= 0 && isCJKIdeograph(this.text.charAt(idx)) && this.text.charAt(idx) != '｜') idx--;
                    if (idx == i - 1) {
                        // the base is marked by '｜' in the same line, otherwise it is the letter just before
                        while (idx >= 0 && this.text.charAt(idx) != '｜' && this.text.charAt(idx) != '\n') idx--;
                        if (idx < 0 || this.text.charAt(idx) != '｜') idx = i - 2;
                    }
                }

                this.kanjiStarts.add(idx + 1);

                idx = i + 1; while (this.text.charAt(idx) != '》') idx++;
                this.furiganaClosings.add(idx);
                i = idx; // the loop steps over the closing mark
            }
//			// >>
//			else if (this.text.charAt(i) == '\u300b') {
//				this.furiganaClosings.add(i);
//			}
            // [
            else if (this.text.charAt(i) == '［') {
                this.emphasisOpenings.add(i);

                int eidx, ws = -1, we = -1;
                for (eidx = i + 1; this.text.charAt(eidx) != '］'; eidx++) {
                    if (this.text.charAt(eidx) == '「')
                        ws = eidx;
                    else if (this.text.charAt(eidx) == '」')
                        we = eidx;
                }
                String btext = this.text.substring(i + 1, eidx);
                if (btext.endsWith(BOUTEN) || btext.endsWith(BOUSEN))
                    this.liKanjiBou.add(i - (we - ws - 1));
                else
                    this.liKanjiBou.add(i);
                this.emphasisClosings.add(eidx);
                i = eidx; // the loop steps over the closing mark
            }
        }
    }

    /** */
    private String furiganaToRubyTag(int kanjiIndex, int startIndex, int endIndex) {
        StringBuilder ruby = new StringBuilder();

        if (rpTag)
            ruby.append("<rp>").append(this.text.charAt(startIndex)).append("</rp>");
        ruby.append("<rt>");
logger.log(Level.TRACE, "%d %d %d".formatted(kanjiIndex, startIndex, endIndex));
        ruby.append(this.text, startIndex + 1, endIndex);
        ruby.append("</rt>");
        if (rpTag)
            ruby.append("<rp>").append(this.text.charAt(endIndex)).append("</rp>");
        ruby.append("</ruby>");

        ruby.insert(0, "</rb>");
        // the annotation of a gaiji in the base is not shown
        ruby.insert(0, this.text.substring(kanjiIndex, startIndex).replaceAll("［＃[^］]*］", ""));
        ruby.insert(0, "<ruby><rb>");

        return ruby.toString();
    }

    /** */
    private String emphasisToRubyTag(int kanjiIndex, int startIndex, int endIndex) {
        String emphasis = this.text.substring(startIndex + 1, endIndex);
        int wordStart = emphasis.indexOf("「");
        int wordEnd = emphasis.indexOf("」");
        int wordLength = wordEnd - wordStart - 1;

        if (kanjiIndex != startIndex) {
            StringBuilder output = new StringBuilder();
            if (emphasis.endsWith(BOUTEN)) {
                String styleName = emphasis.substring(wordEnd + 1);
                String style = this.tenStyles.getOrDefault(styleName, "﹅");
                output.append("<ruby><rb>").append(this.text, kanjiIndex, startIndex).append("</rb>");
                if (rpTag)
                    output.append("<rp>《</rp>");
                output.append("<rt>");
                output.append(String.valueOf(style).repeat(Math.max(0, wordLength)));
                output.append("</rt>");
                if (rpTag)
                    output.append("<rp>》</rp>");
                output.append("</ruby>");
                return output.toString();
            } else if (emphasis.endsWith(BOUSEN)) {
                String styleName = emphasis.substring(wordEnd + 1);
                String style = this.senStyles.getOrDefault(styleName, "");
                output.append("<u style=\"").append(style).append("\">").append(this.text, kanjiIndex, startIndex).append("</u>");
                return output.toString();
            }
        } else {
            if (Arrays.asList(PAGE_BREAKS).contains(this.text.substring(startIndex, endIndex + 1))) {
                // kept for the viewer, other annotations are dropped
                return this.text.substring(startIndex, endIndex + 1);
            }
        }

        return this.text.substring(kanjiIndex, startIndex);
    }

    /** is kanji */
    private boolean isCJKIdeograph(char c) {
        return Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || c == '々' || c == 'ヶ' || c == 'ノ';
    }

    @Override
    public void readText(Reader reader) {
        Scanner scanner = new Scanner(reader);
        StringBuilder sb = new StringBuilder();
        boolean skip = false;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!skip) {
                if (line.startsWith("-")) {
                    skip = true;
logger.log(Level.INFO, "skip start: " + line);
                } else {
                    sb.append(line).append("<br/>\n");
                }
            } else {
                if (line.startsWith("-")) {
                    skip = false;
logger.log(Level.INFO, "skip end: " + line);
                }
            }
        }
        this.text = sb.toString();
    }

    @Override
    public void printHtml(Writer writer) {
        PrintWriter pr = new PrintWriter(writer);
        pr.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        pr.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        pr.println("<head>");
        pr.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
        pr.println("<link rel='stylesheet' type='text/css' href='jnf_style.css' />");
        pr.println("</head>");
        pr.println("<body>");

        if (bookmark)
            pr.println(bookmark(parse()));
        else
            pr.println(parse());

        pr.println("</body>");
        pr.println("</html>");

        pr.flush();
    }
}