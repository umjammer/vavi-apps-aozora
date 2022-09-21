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


public class AozoraWorkParser {

    private static class OneWorkParserHandler implements AozoraWorkParserHandler {

        public void work(AozoraWork work) {
            this.work = work;
        }

        private AozoraWork getWork() {
            return work;
        }

        private AozoraWork work;
    }

    private AozoraWorkParser() {
    }

    public static void parse(Reader in, AozoraWorkParserHandler handler) throws IOException {
        parse(in, handler, true);
    }

    public static void parse(Reader in, AozoraWorkParserHandler handler, boolean skipToPersonEnd) throws IOException {
        TagReader tin = null;
        try {
            tin = new TagReader(in);
            if (skipToPersonEnd)
                tin.skipTo("</person>");
            AozoraWork work = null;
            int _UN_KNOWN = 0;
            int TITLE = 1;
            int ORG_BOOK = 2;
            int CMP_BOOK = 3;
            int cur = 0;
            String tag;
label0:     do
                while ((tag = tin.readNextTag()) != null) {
                    if ("data".equals(tag))
                        work = new AozoraWork();
                    else if ("id".equals(tag))
                        work.setID(tin.readToEndTag());
                    else if ("authorID".equals(tag))
                        work.setAuthorID(tin.readToEndTag());
                    else if ("translatorID".equals(tag))
                        work.setTranslatorID(tin.readToEndTag());
                    else if ("title".equals(tag))
                        cur = TITLE;
                    else if (cur == TITLE && "name".equals(tag))
                        work.setTitleName(tin.readToEndTag());
                    else if (cur == TITLE && "kananame".equals(tag))
                        work.setTitleKana(tin.readToEndTag());
                    else if (cur == TITLE && "original".equals(tag))
                        work.setTitleOriginal(tin.readToEndTag());
                    else if ("/title".equals(tag))
                        cur = _UN_KNOWN;
                    else if ("url".equals(tag))
                        work.setMetaURL(tin.readToEndTag());
                    else if ("contenturl".equals(tag))
                        work.setContentURL(tin.readToEndTag());
                    else if ("kanatype".equals(tag))
                        work.setKanaType(tin.readToEndTag());
                    else if ("note".equals(tag))
                        work.setNote(tin.readToEndTag());
                    else if ("originalbook".equals(tag))
                        cur = ORG_BOOK;
                    else if (cur == ORG_BOOK && "name".equals(tag))
                        work.setOrginalBook(tin.readToEndTag());
                    else if (cur == ORG_BOOK && "publisher".equals(tag))
                        work.setPublisher(tin.readToEndTag());
                    else if (cur == ORG_BOOK && "firstdate".equals(tag))
                        work.setFirstDate(tin.readToEndTag());
                    else if (cur == ORG_BOOK && "inputbase".equals(tag))
                        work.setInputBase(tin.readToEndTag());
                    else if (cur == ORG_BOOK && "proofbase".equals(tag))
                        work.setProofBase(tin.readToEndTag());
                    else if ("/originalbook".equals(tag))
                        cur = _UN_KNOWN;
                    else if ("completebook".equals(tag))
                        cur = CMP_BOOK;
                    else if (cur == CMP_BOOK && "name".equals(tag))
                        work.setCompleteOrginalBook(tin.readToEndTag());
                    else if (cur == CMP_BOOK && "publisher".equals(tag))
                        work.setCompletePublisher(tin.readToEndTag());
                    else if (cur == CMP_BOOK && "firstdate".equals(tag))
                        work.setCompleteFirstDate(tin.readToEndTag());
                    else if ("/completebook".equals(tag)) {
                        cur = _UN_KNOWN;
                    } else {
                        if (!"/data".equals(tag))
                            continue label0;
                        handler.work(work);
                        work = null;
                    }
                }
            while (!"/xml".equals(tag));
        } finally {
            try {
                tin.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] toBytes(AozoraWork work) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try {
            Writer out = null;
            try {
                out = new OutputStreamWriter(byteOut, "utf-8");
                out.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>").append("\n");
                out.append("<xml>").append("\n");
                out.append("\t").append("<data>").append("\n");
                out.append("\t\t").append("<id>").append(work.getID()).append("</id>").append("\n");
                out.append("\t\t").append("<authorID>").append(work.getAuthorID()).append("</authorID>").append("\n");
                out.append("\t\t").append("<translatorID>").append(work.getTranslatorID()).append("</translatorID>").append("\n");
                out.append("\t\t").append("<title>").append("\n");
                out.append("\t\t\t").append("<name>").append(work.getTitleName()).append("</name>").append("\n");
                out.append("\t\t\t").append("<kananame>").append(work.getTitleKana()).append("</kananame>").append("\n");
                out.append("\t\t\t").append("<original>").append(work.getTitleOriginal()).append("</original>").append("\n");
                out.append("\t\t").append("</title>").append("\n");
                out.append("\t\t").append("<url>").append(work.getMetaURL()).append("</url>").append("\n");
                out.append("\t\t").append("<contenturl>").append(work.getContentURL()).append("</contenturl>").append("\n");
                out.append("\t\t").append("<kanatype>").append(work.getKanaType()).append("</kanatype>").append("\n");
                out.append("\t\t").append("<note>").append(work.getNote()).append("</note>").append("\n");
                out.append("\t\t").append("<originalbook>").append("\n");
                out.append("\t\t\t").append("<name>").append(work.getOrginalBook()).append("</name>").append("\n");
                out.append("\t\t\t").append("<publisher>").append(work.getPublisher()).append("</publisher>").append("\n");
                out.append("\t\t\t").append("<firstdate>").append(work.getFirstDate()).append("</firstdate>").append("\n");
                out.append("\t\t\t").append("<inputbase>").append(work.getInputBase()).append("</inputbase>").append("\n");
                out.append("\t\t\t").append("<proofbase>").append(work.getProofBase()).append("</proofbase>").append("\n");
                out.append("\t\t").append("</originalbook>").append("\n");
                out.append("\t\t").append("<completebook>").append("\n");
                out.append("\t\t\t").append("<name>").append(work.getCompleteOrginalBook()).append("</name>").append("\n");
                out.append("\t\t\t").append("<publisher>").append(work.getCompletePublisher()).append("</publisher>").append("\n");
                out.append("\t\t\t").append("<firstdate>").append(work.getCompleteFirstDate()).append("</firstdate>").append("\n");
                out.append("\t\t").append("</completebook>").append("\n");
                out.append("\t").append("</data>").append("\n");
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

    public static AozoraWork loadBytes(byte[] bytes) throws IOException {
        OneWorkParserHandler handler = new OneWorkParserHandler();
        parse(new InputStreamReader(new ByteArrayInputStream(bytes), "utf-8"), handler, false);
        return handler.getWork();
    }
}
