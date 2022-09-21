/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import com.soso.aozora.html.TagReader;


public class AozoraAuthorParser {

    private static class OneAuthorParserHandler implements AozoraAuthorParserHandler {

        public void author(AozoraAuthor author) {
            this.author = author;
        }

        private AozoraAuthor getAuthor() {
            return author;
        }

        private AozoraAuthor author;
    }

    private AozoraAuthorParser() {
    }

    public static void parse(Reader in, AozoraAuthorParserHandler handler) throws IOException {
        TagReader tin = new TagReader(in);
        AozoraAuthor author = null;
        try {
            String tag;
label0:     do {
                do {
                    tag = tin.readNextTag();
                    if (tag == null)
                        break label0;
                    if ("author".equals(tag))
                        author = new AozoraAuthor();
                    else if ("id".equals(tag))
                        author.setID(tin.readToEndTag());
                    else if ("name".equals(tag))
                        author.setName(tin.readToEndTag());
                    else if ("kananame".equals(tag))
                        author.setKana(tin.readToEndTag());
                    else if ("romanname".equals(tag))
                        author.setRomanName(tin.readToEndTag());
                    else if ("birthday".equals(tag))
                        author.setBirthDate(tin.readToEndTag());
                    else if ("deadday".equals(tag))
                        author.setDeadDate(tin.readToEndTag());
                    else if ("note".equals(tag)) {
                        author.setNote(tin.readToEndTag());
                    } else {
                        if (!"/author".equals(tag))
                            continue label0;
                        handler.author(author);
                        author = null;
                    }
                } while (true);
            } while (!"/xml".equals(tag));
        } finally {
            try {
                tin.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] toBytes(AozoraAuthor author) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        Writer out = null;
        try {
            try {
                out = new OutputStreamWriter(byteOut, "utf-8");
                out.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>").append("\n");
                out.append("<xml>").append("\n");
                out.append("\t").append("<author>").append("\n");
                out.append("\t\t").append("<id>").append(author.getID()).append("</id>").append("\n");
                out.append("\t\t").append("<name>").append(author.getName()).append("</name>").append("\n");
                out.append("\t\t").append("<kananame>").append(author.getKana()).append("</kananame>").append("\n");
                out.append("\t\t").append("<romanname>").append(author.getRomanName()).append("</romanname>").append("\n");
                out.append("\t\t").append("<birthday>").append(author.getBirthDate()).append("</birthday>").append("\n");
                out.append("\t\t").append("<deadday>").append(author.getDeadDate()).append("</deadday>").append("\n");
                out.append("\t\t").append("<note>").append(author.getNote()).append("</note>").append("\n");
                out.append("\t").append("</author>").append("\n");
                out.append("</xml>").append("\n");
                out.flush();
            } finally {
                out.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return byteOut.toByteArray();
    }

    public static AozoraAuthor loadBytes(byte[] bytes) throws IOException {
        OneAuthorParserHandler handler = new OneAuthorParserHandler();
        parse(new InputStreamReader(new ByteArrayInputStream(bytes), "utf-8"), handler);
        return handler.getAuthor();
    }
}
