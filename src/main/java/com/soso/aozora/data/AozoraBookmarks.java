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


public class AozoraBookmarks {

    private static class BookmarksXMLBuilder {

        private void build(AozoraBookmarks bookmarks, String enc) throws IOException {
            builder.startDocument(enc);
            builder.startElement(ELM_BOOKMARKS);
            for (AozoraBookmarkEntry bookmark : bookmarks.toArray()) {
                builder.startElement(ELM_BOOKMARK);
                builder.attribute(ATT_BOOKMARK_BOOK, bookmark.getBook());
                builder.attribute(ATT_BOOKMARK_POSITION, String.valueOf(bookmark.getPosition()));
                builder.endElement();
            }

            builder.endElement();
            builder.endDocument();
        }

        private final XMLBuilder builder;

        private BookmarksXMLBuilder(Appendable out) {
            builder = new XMLBuilder(out);
        }
    }

    private static class BookmarksXMLHandler extends DefaultHandler {

        AozoraBookmarks getBookmarks() {
            return bookmarks;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            try {
                if (ELM_BOOKMARK.equals(qName))
                    startBoolmark(attributes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void startBoolmark(Attributes attributes) {
            String name = attributes.getValue(ATT_BOOKMARK_BOOK);
            int position = Integer.parseInt(attributes.getValue(ATT_BOOKMARK_POSITION));
            getBookmarks().addBookmark(name, Integer.valueOf(position));
        }

        private final AozoraBookmarks bookmarks;

        private BookmarksXMLHandler() {
            bookmarks = new AozoraBookmarks();
        }
    }

    public static class AozoraBookmarkEntry {

        public String getBook() {
            return book;
        }

        public int getPosition() {
            return position;
        }

        private String book;
        private int position;

        private AozoraBookmarkEntry(String book, int position) {
            this.book = book;
            this.position = position;
        }
    }

    private AozoraBookmarks() {
        bookmarks = new ArrayList<AozoraBookmarkEntry>();
    }

    public void addBookmark(String book, Integer page) {
        removeBookmark(book);
        bookmarks.add(new AozoraBookmarkEntry(book, page.intValue()));
    }

    public AozoraBookmarkEntry getBookmark(String book) {
        for (AozoraBookmarkEntry bookmark : bookmarks) {
            if (bookmark.getBook().equals(book))
                return bookmark;
        }

        return null;
    }

    public void removeBookmark(String book) {
        AozoraBookmarkEntry remove = null;
        for (AozoraBookmarkEntry bookmark : bookmarks) { 
            if (bookmark.getBook().equals(book)) {
                remove = bookmark;
                break;
            }
        }
        if (remove != null)
            bookmarks.remove(remove);
    }

    public AozoraBookmarkEntry[] toArray() {
        return bookmarks.toArray(new AozoraBookmarkEntry[bookmarks.size()]);
    }

    public static AozoraBookmarks create() {
        AozoraBookmarks bookmarks = new AozoraBookmarks();
        return bookmarks;
    }

    private static File getBookmarksFile() {
        return new File(AozoraEnv.getUserHomeDir(), "aozora/bookmarks.xml");
    }

    public static AozoraBookmarks load() throws IOException, SAXException {
        File file = getBookmarksFile();
        if (file.exists())
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                BookmarksXMLHandler handler = new BookmarksXMLHandler();
                parser.parse(file, handler);
                return handler.getBookmarks();
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
            File file = getBookmarksFile();
            if (!file.exists() && !file.getParentFile().exists())
                file.getParentFile().mkdirs();
            writer = new OutputStreamWriter(new FileOutputStream(file), enc);
            BookmarksXMLBuilder builder = new BookmarksXMLBuilder(writer);
            builder.build(this, enc);
        } finally {
            writer.close();
        }
    }

    private List<AozoraBookmarkEntry> bookmarks;

    private static final String ELM_BOOKMARKS = "bookmarks";
    private static final String ELM_BOOKMARK = "bookmark";
    private static final String ATT_BOOKMARK_BOOK = "book";
    private static final String ATT_BOOKMARK_POSITION = "position";
}
