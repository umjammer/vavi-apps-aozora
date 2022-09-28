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
import java.nio.charset.StandardCharsets;

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
        AozoraAuthor author = null;
        try (TagReader tin = new TagReader(in)) {
            String tag;
label0:     do {
                do {
                    tag = tin.readNextTag();
                    if (tag == null)
                        break label0;
                    switch (tag) {
                    case "author":
                        author = new AozoraAuthor();
                        break;
                    case "id":
                        author.setID(tin.readToEndTag());
                        break;
                    case "name":
                        author.setName(tin.readToEndTag());
                        break;
                    case "kananame":
                        author.setKana(tin.readToEndTag());
                        break;
                    case "romanname":
                        author.setRomanName(tin.readToEndTag());
                        break;
                    case "birthday":
                        author.setBirthDate(tin.readToEndTag());
                        break;
                    case "deadday":
                        author.setDeadDate(tin.readToEndTag());
                        break;
                    case "note":
                        author.setNote(tin.readToEndTag());
                        break;
                    default:
                        if (!"/author".equals(tag))
                            continue label0;
                        handler.author(author);
                        author = null;
                        break;
                    }
                } while (true);
            } while (!"/xml".equals(tag));
        }
    }

    public static byte[] toBytes(AozoraAuthor author) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             Writer out = new OutputStreamWriter(byteOut, StandardCharsets.UTF_8)) {
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
            return byteOut.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static AozoraAuthor loadBytes(byte[] bytes) throws IOException {
        OneAuthorParserHandler handler = new OneAuthorParserHandler();
        parse(new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8), handler);
        return handler.getAuthor();
    }
}
