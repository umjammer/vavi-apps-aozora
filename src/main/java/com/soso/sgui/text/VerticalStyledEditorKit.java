/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.text;

import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;


public class VerticalStyledEditorKit extends StyledEditorKit {

    /** g */
    private static class SelectionView extends SBoxView {

        public final String toString() {
            StringBuilder sb = new StringBuilder(super.toString());
            try {
                sb.append("[");
                int count = getViewCount();
                for (int i = 0; i < count; i++) {
                    if (i != 0)
                        sb.append(", ");
                    sb.append(String.valueOf(getView(count)));
                }

                sb.append("]");
            } catch (Exception _ex) {
            }
            return sb.toString();
        }

        SelectionView(Element elem) {
            super(elem, X_AXIS);
        }
    }

    /** b */
    private static class ContentView extends SView {

        public final String toString() {
            StringBuilder sb = new StringBuilder(super.toString());
            try {
                sb.append("[\"")
                  .append(getSegment_a(getStartOffset(), getEndOffset()))
                  .append("\"; offset:")
                  .append(getStartOffset())
                  .append("; limit:")
                  .append(getEndOffset())
                  .append("; height:")
                  .append(getPreferredSpan(Y_AXIS))
                  .append("]");
            } catch (Exception _ex) {
            }
            return sb.toString();
        }

        ContentView(Element elem) {
            super(elem);
        }
    }

    /** e */
    private static class SViewFactory implements ViewFactory {

        public final View create(Element elem) {
            String name = elem.getName();
            if (name != null) {
                if (name.equals("content"))
                    return new ContentView(elem);
                if (name.equals("paragraph"))
                    return new SParagraphView(elem);
                if (name.equals("section"))
                    return new SelectionView(elem);
                if (name.equals("component"))
                    return new ComponentView(elem);
                if (name.equals("icon"))
                    return new IconView(elem);
            }
            return new ContentView(elem);
        }

        SViewFactory() {
        }
    }

    public ViewFactory getViewFactory() {
        return factory;
    }

    private static final long serialVersionUID = 0x52b85473L;

    private static final ViewFactory factory = new SViewFactory();
}
