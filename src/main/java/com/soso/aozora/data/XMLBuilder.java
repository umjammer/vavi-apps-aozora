/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.data;

import java.io.IOException;
import java.util.Stack;


class XMLBuilder {

    private static class ElementStatus {

        private String name;
        private boolean hasData;
        private boolean hasChild;

        private String getName() {
            return name;
        }

        private void setHasChild() {
            hasChild = true;
        }

        private boolean isHasChild() {
            return hasChild;
        }

        private void setHasData() {
            hasData = true;
        }

        private boolean isHasData() {
            return hasData;
        }

        private boolean isStartElementClosed() {
            return isHasData() || isHasChild();
        }

        private ElementStatus(String name) {
            this.hasData = false;
            this.hasChild = false;
            this.name = name;
        }
    }

    private static final char LT = '<'; // 60
    private static final char GT = '>'; // 62
    private static final char SP = ' '; // 32
    private static final char EQ = '='; // 61
    private static final char QT = '"'; // 34
    private static final char SL = '/'; // 47
    private static final char TB = '\t'; // 9

    private final Appendable out;
    private Stack<ElementStatus> stack;

    XMLBuilder(Appendable out) {
        this.out = out;
    }

    void startDocument(String enc) throws IOException {
        out.append("<?xml version=\"1.0\" encoding=\"").append(enc).append("\"?>");
        newLine();
        stack = new Stack<>();
    }

    void startElement(String elm) throws IOException {
        if (stack == null)
            throw new IllegalStateException("document not started or already end.");
        if (stack.size() > 0) {
            ElementStatus parent = stack.peek();
            if (!parent.isStartElementClosed()) {
                out.append(GT);
                newLine();
            }
            parent.setHasChild();
        }
        stack.push(new ElementStatus(elm));
        for (int i = 0; i < stack.size() - 1; i++)
            out.append(TB);

        out.append(LT).append(elm);
    }

    void attribute(String key, String value) throws IOException {
        if (stack == null)
            throw new IllegalStateException("document not started or already end.");
        ElementStatus current = stack.peek();
        if (current == null)
            throw new IllegalStateException("element not started");
        if (current.isStartElementClosed()) {
            throw new IllegalStateException("attribute only can called after startElement");
        }
        out.append(SP).append(key).append(EQ).append(QT).append(value).append(QT);
    }

    void characters(CharSequence data) throws IOException {
        if (stack == null)
            throw new IllegalStateException("document not started or already end.");
        ElementStatus current = stack.peek();
        if (current == null)
            throw new IllegalStateException("element not started");
        if (!current.isStartElementClosed())
            out.append(GT);
        current.setHasData();
        out.append(data);
    }

    void endElement() throws IOException {
        if (stack == null)
            throw new IllegalStateException("document not started or already end.");
        ElementStatus current = stack.pop();
        if (current == null)
            throw new IllegalStateException("element not started");
        if (current.isHasData())
            out.append(LT).append(SL).append(current.getName()).append(GT);
        else if (current.isHasChild()) {
            for (int i = 0; i < stack.size(); i++)
                out.append(TB);

            out.append(LT).append(SL).append(current.getName()).append(GT);
        } else {
            out.append(SP).append(SL).append(GT);
        }
        newLine();
    }

    void endDocument() throws IOException {
        stack = null;
    }

    void newLine() throws IOException {
        out.append('\n');
    }
}
