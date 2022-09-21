/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.soso.aozora.core.AozoraEnv;


public class AozoraHistories {

    private static class HistoriesXMLBuilder {

        private void build(AozoraHistories histories, String enc) throws IOException {
            builder.startDocument(enc);
            builder.startElement(ELM_HISTORYS);
            for (AozoraHistoryEntry history : histories.toArray()) {
                builder.startElement(ELM_HISTORY);
                builder.attribute(ATT_HISTORY_BOOK, history.getBook());
                builder.attribute(ATT_HISTORY_POSITION, String.valueOf(history.getPosition()));
                builder.attribute(ATT_HISTORY_TIMESTAMP, String.valueOf(history.getTimestamp()));
                builder.endElement();
            }

            builder.endElement();
            builder.endDocument();
        }

        private final XMLBuilder builder;

        private HistoriesXMLBuilder(Appendable out) {
            builder = new XMLBuilder(out);
        }
    }

    private static class HistoriesXMLHandler extends DefaultHandler {

        AozoraHistories getHistories() {
            return histories;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            try {
                if (ELM_HISTORY.equals(qName))
                    startBoolmark(attributes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void startBoolmark(Attributes attributes) {
            String name = attributes.getValue(ATT_HISTORY_BOOK);
            int position = Integer.parseInt(attributes.getValue(ATT_HISTORY_POSITION));
            long timestamp = Long.parseLong(attributes.getValue(ATT_HISTORY_TIMESTAMP));
            getHistories().addHistory(name, position, timestamp);
        }

        private final AozoraHistories histories;

        private HistoriesXMLHandler() {
            histories = new AozoraHistories();
        }
    }

    public static class AozoraHistoryEntry {

        public String getBook() {
            return book;
        }

        public int getPosition() {
            return position;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String toString() {
            return super.toString() + "[" +
                "book=" + getBook() + ";" +
                "position=" + getPosition() + ";" +
                "timestamp=" + getTimestamp() + "]";
        }

        private String book;
        private int position;
        private long timestamp;

        private AozoraHistoryEntry(String book, int position, long timestamp) {
            this.book = book;
            this.position = position;
            this.timestamp = timestamp;
        }
    }

    private AozoraHistories() {
        histories = new ArrayList<AozoraHistoryEntry>();
    }

    public void addHistory(String book, int position) {
        addHistory(book, position, System.currentTimeMillis());
    }

    private void addHistory(String book, int position, long timestamp) {
        histories.add(new AozoraHistoryEntry(book, position, timestamp));
    }

    public AozoraHistoryEntry[] getHistories(String book) {
        List<AozoraHistoryEntry> hits = new ArrayList<AozoraHistoryEntry>();
        for (AozoraHistoryEntry history : histories) {
            if (history.getBook().equals(book))
                hits.add(history);
        }
        return hits.toArray(new AozoraHistoryEntry[hits.size()]);
    }

    public void removeHistory(AozoraHistoryEntry remove) {
        histories.remove(remove);
    }

    public AozoraHistoryEntry[] toArray() {
        return histories.toArray(new AozoraHistoryEntry[histories.size()]);
    }

    public static AozoraHistories create() {
        AozoraHistories histories = new AozoraHistories();
        return histories;
    }

    private static File getHistoriesFile() {
        return new File(AozoraEnv.getUserHomeDir(), "aozora/histories.xml");
    }

    public static AozoraHistories load() throws IOException, SAXException {
        File file = getHistoriesFile();
        if (file.exists())
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                HistoriesXMLHandler handler = new HistoriesXMLHandler();
                parser.parse(file, handler);
                return handler.getHistories();
            } catch (ParserConfigurationException e) {
                throw new IllegalStateException(e);
            }
        else
            return create();
    }

    public void store() throws IOException {
        OutputStreamWriter writer = null;
        try {
            String enc = "UTF-8";
            File file = getHistoriesFile();
            if (!file.exists() && !file.getParentFile().exists())
                file.getParentFile().mkdirs();
            writer = new OutputStreamWriter(new FileOutputStream(file), enc);
            HistoriesXMLBuilder builder = new HistoriesXMLBuilder(writer);
            builder.build(this, enc);
        } finally {
            writer.close();
        }
    }

    private List<AozoraHistoryEntry> histories;
    private static final String ELM_HISTORYS = "histories";
    private static final String ELM_HISTORY = "history";
    private static final String ATT_HISTORY_BOOK = "book";
    private static final String ATT_HISTORY_POSITION = "position";
    private static final String ATT_HISTORY_TIMESTAMP = "timestamp";
}
