/*
 * https://github.com/weimingtom/AozoraParser/blob/master/AozoraParser.java
 */

package vavi.text.aozora.converter;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


/**
 * AozoraParser.
 * <li> roby is good
 *
 * TODO controller "［...］" are ignored
 *
 * @author weimingtom
 * @see "http://www.aozora.gr.jp/cards/000035/card1567.html"
 */
public class AozoraParser {

    private enum Type {
        KANJI { boolean matches(char c) { return (c >= '\u4e00' && c <= '\u9fbf') || (c == '\u3005'); }}, // '々'
        HIRAGANA { boolean matches(char c) { return c >= '\u3040' && c <= '\u309f'; }},
        KATAKANA { boolean matches(char c) { return c >= '\u30a0' && c <= '\u30ff'; }},
        /** '《' */
        RUBY_OPEN { boolean matches(char c) { return c == '《'; }},
        /** '》' */
        RUBY_CLOSE { boolean matches(char c) { return c == '》'; }},
        /** '｜' */
        RUBY_START { boolean matches(char c) { return c == '｜'; }},
        OTHER { boolean matches(char c) { return true; }};
        abstract boolean matches(char c);
        /** @see "http://www.chiark.greenend.org.uk/~pmaydell/misc/aozora_ruby.py" */
        static Type lex(char c) {
            return Arrays.stream(values()).filter(e -> e.matches(c)).findFirst().get();
        }
    }

    private int start;
    private int offset;
    private boolean ruby_start;
    private Type current;

    private String text;
    private StringBuffer ostrbuf;

    private String curRB;
    private String curRT;
    private int curRBStartPos;

    public static class RubyInfo {
        public String rb;
        public String rt;
        public int rbStartPos;

        public RubyInfo(String rb, String rt, int rbStartPos) {
            this.rb = rb;
            this.rt = rt;
            this.rbStartPos = rbStartPos;
        }
        @Override public String toString() {
            return "rb = " + rb + ", rt = " + rt + ", rbStartPos = " + rbStartPos;
        }
    }

    private final List<RubyInfo> rubyInfoList = new ArrayList<>();

    private final Writer writer;
    private String breakLine = "<br/>\n"; // "\n"

    /** */
    public void setBreakLine(String breakLine) {
        this.breakLine = breakLine;
    }

    public AozoraParser(Reader reader, Writer writer) {
        readText(reader);
        this.writer = writer;
    }

    private void readText(Reader reader) {
        Scanner scanner = new Scanner(reader);
        StringBuilder sb = new StringBuilder();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            sb.append(line).append(breakLine);
        }
        this.text = sb.toString();
    }

    private void writeSection() {
        if (start < offset) {
            this.ostrbuf.append(this.text, start, offset);
        }
        this.start = this.offset;
    }

    private String getSection() {
        if (start < offset) {
            return this.text.substring(start, offset);
        }
        return null;
    }

    private String parseHtml() {
        start = 0;
        offset = 0;
        ruby_start = false;
        current = Type.OTHER;
        curRB = null;
        curRT = null;
        curRBStartPos = -1;
        this.ostrbuf = new StringBuffer();
        for (this.offset = 0; offset < text.length(); offset++) {
            Type charclass = Type.lex(text.charAt(offset));
            if (charclass == Type.RUBY_START) {
                writeSection();
                start = start + 1;
                ruby_start = true;
                this.ostrbuf.append("<ruby><rb>");
            } else if (charclass == Type.RUBY_OPEN) {
                if (!ruby_start) {
                    // ruby applies to last section of contiguous same-type chars
                    this.ostrbuf.append("<ruby><rb>");
                }
                writeSection();
                start = start + 1;
                this.ostrbuf.append("</rb><rt>");
            } else if (charclass == Type.RUBY_CLOSE) {
                writeSection();
                start = start + 1;
                this.ostrbuf.append("</rt></ruby>");
                ruby_start = false;
            } else if (charclass != current) {
                // start of a different kind of character string
                writeSection();
                current = charclass;
            }
        }
        writeSection();

        return this.ostrbuf.toString();
    }

    private String parseRuby() {
        start = 0;
        offset = 0;
        ruby_start = false;
        current = Type.OTHER;
        curRB = null;
        curRT = null;
        curRBStartPos = -1;
        this.ostrbuf = new StringBuffer();
        for (this.offset = 0; offset < text.length(); offset++) {
            Type charclass = Type.lex(text.charAt(offset));
            if (charclass == Type.RUBY_START) {
                writeSection();
                start = start + 1;
                ruby_start = true;
                //this.ostrbuf.append("<ruby><rb>");
            } else if (charclass == Type.RUBY_OPEN) {
                if (!ruby_start) {
                    // ruby applies to last section of contiguous same-type chars
                    //this.ostrbuf.append("<ruby><rb>");
                }
                String rb = getSection();
                this.curRB = rb;
                // FIXME:
                this.curRBStartPos = this.ostrbuf.length();
                writeSection();
                start = start + 1;
                //this.ostrbuf.append("</rb><rt>");
            } else if (charclass == Type.RUBY_CLOSE) {
                if (false) {
                    writeSection();
                } else {
                    String rt = getSection();
                    this.curRT = rt;
                    if (this.curRT != null) {
                        rubyInfoList.add(new RubyInfo(this.curRB, this.curRT, this.curRBStartPos));
                    }
                    this.curRB = this.curRT = null;
                    this.curRBStartPos = -1;
                    this.start = this.offset;
                }
                start = start + 1;
                //this.ostrbuf.append("</rt></ruby>");
                ruby_start = false;
            } else if (charclass != current) {
                // start of a different kind of character string
                writeSection();
                current = charclass;
            }
        }
        writeSection();

        return this.ostrbuf.toString();
    }

    public String getLoadedText() {
        return this.text;
    }

    public List<AozoraParser.RubyInfo> getRubyList() {
        return this.rubyInfoList;
    }

    public void printHtml() {
        PrintWriter pr = new PrintWriter(writer);

        pr.write("<html>\n");
        pr.write("<head>\n");
        pr.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
        pr.write("</head>\n");
        pr.write("<body>\n");
        pr.write(parseHtml());
        pr.write("</body>\n");
        pr.write("</html>\n");

        pr.flush();
    }

    public void printNoRuby() {
        PrintWriter pr = new PrintWriter(writer);
        pr.write(parseRuby());
        pr.flush();
    }
}
