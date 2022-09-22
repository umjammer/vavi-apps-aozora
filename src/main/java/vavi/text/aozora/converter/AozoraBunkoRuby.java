/*
 * https://github.com/iWumboUWumbo2/AozoraToHTML/blob/main/AozoraBunkoRuby.java
 */

package vavi.text.aozora.converter;

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
 * aozora text to html converter
 *
 * TODO ruby start is wrong
 */
public final class AozoraBunkoRuby {

    private String text;

    private List<Integer> kanjiStarts;
    private List<Integer> furiganaOpenings;
    private List<Integer> furiganaClosings;
    private List<Integer> emphasisOpenings;
    private List<Integer> emphasisClosings;
    private List<Integer> liKanjiBou;

    private HashMap<String, String> tenStyles;
    private HashMap<String, String> senStyles;

    private static final String BOUTEN = "傍点";
    private static final String BOUSEN = "線";
    private static final String SPACING = "字下げ";

    private static final String FW_INTS = "０１２３４５６７８９";

    private Writer writer;

    private boolean bookmark = false;
    private boolean rpTag = false;

    /** */
    public AozoraBunkoRuby(Reader reader, Writer writer) {
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

        readText(reader);
        this.writer = writer;
    }

    /** */
    private void replacements() {
        this.text = this.text.replaceAll("［＃挿絵（", "<img src=\"");
        this.text = this.text.replaceAll("）入る］", "\">");

        this.text = this.text.replaceAll("［＃改頁］", "<br>");
    }

    /** */
    private void printDebug(Level level) {
        Debug.printf(Level.FINER, "%d %d %d %d %d %d\n",
                kanjiStarts.size(),
                furiganaOpenings.size(),
                furiganaClosings.size(),
                emphasisOpenings.size(),
                emphasisClosings.size(),
                liKanjiBou.size());

        int count1 = 0, count2 = 0;
        for (int i = 0; i < this.text.length(); i++) {
            if (this.text.charAt(i) == '《') {
                count1++;
            } else if (this.text.charAt(i) == '》') {
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

        int kssize = this.kanjiStarts.size(), kbsize = this.liKanjiBou.size();
        while (i < kssize && j < kbsize) {
Debug.printf(Level.FINER, "%d, %d: %d, [%d, %d], [%d, %d]\n", i, j, curr, kanjiStarts.get(i), furiganaClosings.get(i), liKanjiBou.get(j), emphasisClosings.get(j));
            if (kanjiStarts.get(i) < liKanjiBou.get(j)) {
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
            sb.append(this.text, curr, kanjiStarts.get(i));
            sb.append(furiganaToRubyTag(kanjiStarts.get(i), furiganaOpenings.get(i), furiganaClosings.get(i)));
            curr = furiganaClosings.get(i) + 1;
            i++;
        }


        while (j < this.liKanjiBou.size()) {
Debug.printf(Level.FINER, "%d: %d, %d\n", j, curr, liKanjiBou.get(j));
            if (curr >= liKanjiBou.get(j))
                break;
            sb.append(this.text, curr, liKanjiBou.get(j));
            sb.append(emphasisToRubyTag(liKanjiBou.get(j), emphasisOpenings.get(j), emphasisClosings.get(j)));
            curr = emphasisClosings.get(j) + 1;
            j++;
        }

        sb.append(this.text.substring(curr));

        return sb.toString().replaceAll("\uff5c", "");
    }

    /** */
    public String bookmark(String text) {
        StringBuilder sb = new StringBuilder();
        int curr = 0, count = 1, idx;
        while ((idx = text.indexOf('。', curr)) != -1) {
Debug.printf(Level.FINER, "%d %d\n", curr, idx);
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
                int idx;
                for (idx = i - 1; isCJKIdeograph(this.text.charAt(idx)) && this.text.charAt(idx) != '｜'; idx--) ;
                if (idx == i - 1) {
                    for (idx = i - 1; this.text.charAt(idx) != '｜'; idx--) ;
                }

                this.kanjiStarts.add(idx + 1);

                for (idx = i + 1; this.text.charAt(idx) != '》'; idx++) ;
                this.furiganaClosings.add(idx);
                i = idx + 1;
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
                i = eidx + 1;
            }
        }
    }

    /** */
    private String furiganaToRubyTag(int kanjiIndex, int startIndex, int endIndex) {
        StringBuilder ruby = new StringBuilder();

        if (rpTag)
            ruby.append("<rp>").append(this.text.charAt(startIndex)).append("</rp>");
        ruby.append("<rt>");
Debug.printf(Level.FINER, "%d %d %d\n", kanjiIndex, startIndex, endIndex);
        ruby.append(this.text, startIndex + 1, endIndex);
        ruby.append("</rt>");
        if (rpTag)
            ruby.append("<rp>").append(this.text.charAt(endIndex)).append("</rp>");
        ruby.append("</ruby>");

        ruby.insert(0, "</rb>");
        ruby.insert(0, this.text.substring(kanjiIndex, startIndex));
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
                output.append("<ruby><rb>").append(this.text.substring(kanjiIndex, startIndex));
                if (rpTag)
                    output.append("<rp>《</rp>");
                output.append("<rt>");
                for (int i = 0; i < wordLength; i++)
                    output.append(style);
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
            StringBuilder output = new StringBuilder();
            if (emphasis.endsWith(SPACING)) {
                int hash = emphasis.indexOf("＃");
                int ji = emphasis.indexOf("字");
                String space = emphasis.substring(hash + 1, ji);
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

    /** is kanji */
    private boolean isCJKIdeograph(char c) {
        return Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || c == '々' || c == 'ヶ';
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

        if (bookmark)
            pr.println(bookmark(parse()));
        else
            pr.println(parse());

        pr.println("</body>");
        pr.println("</html>");

        pr.flush();
    }
}