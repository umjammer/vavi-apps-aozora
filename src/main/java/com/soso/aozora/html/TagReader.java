/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.html;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;


public class TagReader extends Reader {

    public TagReader(Reader reader) {
        this.reader = reader;
    }

    public String skipToStartTag(String tagName) throws IOException {
        String startTag = ANGLE_OPEN + tagName;
        skipTo(startTag);
        StringBuilder sb = new StringBuilder();
        readTo(String.valueOf(ANGLE_CLOSE), sb);
        return sb.substring(0, sb.length() - 1);
    }

    public String readNextTag() throws IOException {
        skipTo(String.valueOf(ANGLE_OPEN));
        StringBuilder sb = new StringBuilder();
        readTo(String.valueOf(ANGLE_CLOSE), sb);
        return sb.substring(0, sb.length() - 1);
    }

    public String readToEndTag() throws IOException {
        String endTag = String.valueOf(ANGLE_OPEN) + SLASH;
        StringBuilder sb = new StringBuilder();
        readTo(endTag, sb);
        skipTo(String.valueOf(ANGLE_CLOSE));
        return sb.substring(0, sb.length() - endTag.length());
    }

    public void skipTo(String mark) throws IOException {
        readTo(mark, null);
    }

    public void readTo(String mark, Appendable builder) throws IOException {
        if (mark == null || mark.length() == 0)
            return;
        char[] markChars = mark.toCharArray();
        int i = 0;
        while (i < markChars.length) {
            int c = read();
            if (c == -1)
                throw new EOFException("while seeking " + mark);
            if (markChars[i] == (char) c)
                i++;
            else
                i = 0;
            if (builder != null)
                builder.append((char) c);
        }
    }

    public void close() throws IOException {
        reader.close();
    }

    public boolean equals(Object obj) {
        return reader.equals(obj);
    }

    public int hashCode() {
        return reader.hashCode();
    }

    public void mark(int readAheadLimit) throws IOException {
        reader.mark(readAheadLimit);
    }

    public boolean markSupported() {
        return reader.markSupported();
    }

    public int read() throws IOException {
        return reader.read();
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        return reader.read(cbuf, off, len);
    }

    public int read(char[] cbuf) throws IOException {
        return reader.read(cbuf);
    }

    public int read(CharBuffer target) throws IOException {
        return reader.read(target);
    }

    public boolean ready() throws IOException {
        return reader.ready();
    }

    public void reset() throws IOException {
        reader.reset();
    }

    public long skip(long n) throws IOException {
        return reader.skip(n);
    }

    public String toString() {
        return reader.toString();
    }

    static final char ANGLE_OPEN = '<'; // 60
    static final char ANGLE_CLOSE = '>'; // 62
    static final char SLASH = '/'; // 47
    private final Reader reader;
}
