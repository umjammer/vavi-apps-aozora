/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.text;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;

import javax.swing.JComponent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabExpander;
import javax.swing.text.TabableView;
import javax.swing.text.View;

import com.soso.sgui.SVerticalTextPane;


/** i */
class SView extends View implements TabableView, Cloneable {

    public SView(Element elem) {
        super(elem);
        bytes_e = null;
    }

    public float getPreferredSpan(int axis) {
        switch (axis) {
        case X_AXIS:
            return getWidth_i() * getLineSpaceRatio();
        case Y_AXIS:
            int s = getStartOffset();
            int e = getEndOffset();
            return getTabbedSpan_b(s, e, 0);
        }
        throw new IllegalArgumentException("Invalid axis: "  + axis);
    }

    private float getLineSpaceRatio() {
        Container parent = getContainer();
        if (parent == null)
            return 1.0F;
        else
            return ((SVerticalTextPane) parent).getLineSpaceRatio();
    }

    public float getAlignment(int axis) {
        switch (axis) {
        case X_AXIS:
            return 0.5F;
        case Y_AXIS:
            return 0.0F;
        }
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }

    public void paint(Graphics g, Shape allocation) {
        boolean flag = false;
        Container container = getContainer();
        int so = getStartOffset();
        int eo = getEndOffset();
        Rectangle rectangle = allocation instanceof Rectangle ? (Rectangle) allocation : allocation.getBounds();
        Color color = getBackground();
        Color disabledColor = getForeground();
        Object obj = container;
        if (container instanceof JTextComponent && !((JTextComponent) obj).isEnabled())
            disabledColor = ((JTextComponent) obj).getDisabledTextColor();
        if (color != null) {
            g.setColor(color);
            g.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        }
        Highlighter highlighter = ((JTextComponent) (obj = container)).getHighlighter();
        if ((container instanceof JTextComponent) && highlighter instanceof LayeredHighlighter)
            ((LayeredHighlighter) highlighter).paintLayeredHighlights(g, so, eo, allocation, ((JTextComponent) obj), this);
        if (getElement().getAttributes().isDefined(StyleConstants.ComposedTextAttribute)) {
            if (((obj = getContainer()) instanceof JComponent) && (g instanceof Graphics2D)) {
//                Object obj1 = null;
                setBounds_a(allocation.getBounds());
                flag = true;
            }
        } else if (container instanceof JTextComponent) {
            JTextComponent comp = (JTextComponent) container;
            Color selectedTextColor = comp.getSelectedTextColor();
            Highlighter.Highlight[] highlights = comp.getHighlighter().getHighlights();
            if (comp.getHighlighter() != null &&
                selectedTextColor != null &&
                !selectedTextColor.equals(disabledColor) &&
                highlights.length != 0) {

                boolean flag1 = false;
                int p = 0;
                for (Highlighter.Highlight highlight : highlights) {
                    int s1 = highlight.getStartOffset();
                    int e1 = highlight.getEndOffset();
                    if (s1 > eo || e1 < so)
                        continue;
                    if (s1 <= so && e1 >= eo) {
                        paint_a(g, allocation, selectedTextColor, so, eo);
                        flag = true;
                        break;
                    }
                    if (!flag1) {
                        b(so, eo);
                        flag1 = true;
                    }
                    s1 = Math.max(so, s1);
                    e1 = Math.min(eo, e1);
                    paint_a(g, allocation, selectedTextColor, s1, e1);
                    bytes_e[s1 - so]++;
                    bytes_e[e1 - so]--;
                    p++;
                }

                if (!flag && p > 0) {
                    int i = -1;
                    int l1 = 0;
                    int l = eo - so;
                    while (i++ < l) {
                        while (i < l && bytes_e[i] == 0)
                            i++;
                        if (l1 != i)
                            paint_a(g, allocation, disabledColor, so + l1, so + i);
                        int l2 = 0;
                        while (i < l && (l2 += bytes_e[i]) != 0)
                            i++;
                        l1 = i;
                    }

                    flag = true;
                }
            }
        }
        if (!flag)
            paint_a(g, allocation, disabledColor, so, eo);
    }

    protected final void paint_a(Graphics g, Shape allocation, Color color, int start, int end) {
        g.setColor(color);
        paint_a(g, allocation, start, end);
        boolean isUnderline = isUnderline();
        boolean isStrikeThrough = isStrikeThrough();
        if (isUnderline || isStrikeThrough) {
            Rectangle rectangle = (allocation instanceof Rectangle) ? (Rectangle) allocation : allocation.getBounds();
            View view = getParent();
            if (view != null && view.getEndOffset() == end) {
                Segment segment = getSegment_a(start, end);
                while (segment.count > 0 && Character.isWhitespace(segment.array[segment.count - 1])) {
                    end--;
                    segment.count--;
                }
            }
            int y = rectangle.y;
            int y0 = getStartOffset();
            if (y0 != start)
                y += getTabbedSpan_b(y0, start, y);
            int y1 = y + getTabbedSpan_b(start, end, y);
            int x1 = rectangle.x + rectangle.width;
            if (isUnderline) {
                int x = x1;
                x--;
                g.drawLine(x, y, x, y1);
            }
            if (isStrikeThrough) {
                int x = (int) ((x = x1) - getPreferredSpan(0) * 0.4F);
                g.drawLine(x, y, x, y1);
            }
        }
    }

    protected final void paint_a(Graphics g, Shape allocation, int start, int end) {
        g.setFont(getFont_c());
        Rectangle bounds = (allocation instanceof Rectangle) ? (Rectangle) allocation : allocation.getBounds();
        float span = getPreferredSpan(0);
        float alignment = getAlignment(0);
        int x1 = bounds.x;
        int w = x1 + (int) (span * alignment);
        FontMetrics metrics = g.getFontMetrics();
        int h1 = metrics.getHeight();
        int asc = metrics.getAscent();
        int desc = metrics.getDescent();
        int leading = metrics.getLeading();
        int h = bounds.y - desc;
        if (start != getStartOffset())
            h += getTabbedSpan_b(getStartOffset(), start, bounds.y);
        Segment segment = getSegment_a(start, end);
        double halfPI = Math.PI / 2;
        boolean is2d = g instanceof Graphics2D;
        Graphics2D g2 = is2d ? (Graphics2D) g : null;
        boolean flag1 = segment.count > 2 &&
                getContainer().getHeight() < bounds.height &&
                CharacterUtil.isLineHeadForbidden(segment.array[(segment.offset + segment.count) - 1]) &&
                CharacterUtil.isLineHeadForbidden(segment.array[(segment.offset + segment.count) - 2]);

        for (int i = 0; i < segment.count; i++) {
            int ofs = segment.offset + i;
            char c1 = segment.array[ofs];
            if (flag1 && i >= segment.count - 1)
                h -= charWidth(metrics, c1) / 2;
            if (CharacterUtil.isTab(c1)) {
                h = nextTabStop(h, i);
                continue;
            }
            int w3 = metrics.charWidth(c1);
            int k3 = (int) (w3 * alignment);
            if (is2d && CharacterUtil.isToRotateKana(c1)) {
                float ha = CharacterUtil.getHorizontalAlign(c1);
                int x0 = h + desc;
                int y0 = ((-w) + asc / 2) - leading - (int) (desc * ha);
                g2.rotate(halfPI, 0.0D, 0.0D);
                g2.drawChars(segment.array, ofs, 1, x0, y0);
                g2.rotate(-halfPI, 0.0D, 0.0D);
                h += charWidth(metrics, c1);
                continue;
            }
            if (is2d && CharacterUtil.isToRotate(c1)) {
                int x2 = h + desc;
                int x3 = ((-w) + asc / 2) - leading - desc / 2;
                g2.rotate(halfPI, 0.0D, 0.0D);
                g2.drawChars(segment.array, ofs, 1, x2, x3);
                g2.rotate(-halfPI, 0.0D, 0.0D);
                h += charWidth(metrics, c1);
                continue;
            }
            int x;
            int y1;
            if (CharacterUtil.isSmallKana(c1)) {
                h += charWidth(metrics, c1);
                x = w - w3 / 3;
                y1 = h;
                g.drawChars(segment.array, desc, 1, x, y1);
            } else  if (CharacterUtil.isPunctuateKana(c1)) {
                h += h1;
                x = w + w3 / 6;
                y1 = h - h1 / 2;
                g.drawChars(segment.array, ofs, 1, x, y1);
            } else {
                h += h1;
                x = (w - k3) + 1;
                g.drawChars(segment.array, ofs, 1, x, h);
            }
        }
    }

    protected final void setBounds_a(Rectangle bounds) {
        int so = getStartOffset();
        int eo = getEndOffset();
        AttributeSet set = getElement().getAttributes();
        AttributedString string = (AttributedString) set.getAttribute(StyleConstants.ComposedTextAttribute);
        int start = getElement().getStartOffset();
        getFontMetrics_j().getDescent();
        string.addAttribute(TextAttribute.FONT, getFont_c());
        string.addAttribute(TextAttribute.FOREGROUND, getForeground());
        if (StyleConstants.isBold(getAttributes()))
            string.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        if (StyleConstants.isItalic(getAttributes()))
            string.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        if (isUnderline())
            string.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        if (isStrikeThrough())
            string.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
        if (isSuperscript())
            string.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER);
        if (isSubscript())
            string.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB);
        string.getIterator(null, so - start, eo - start);
        throw new UnsupportedOperationException("unsupported vertical ");
    }

    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        Rectangle rect = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
        int s = getStartOffset();
        int e = getEndOffset();
        int p = pos;
        if (s <= p && p <= e) {
            int span = getTabbedSpan_b(s, p, rect.y);
            Rectangle2D.Float frect = new Rectangle2D.Float(rect.x, rect.y + span, getPreferredSpan(0), 0.0F);
            return frect;
        } else {
            throw new BadLocationException("modelToView - can't convert", e);
        }
    }

    public int viewToModel(float x, float y, Shape a, Position.Bias[] biasReturn) {
        Rectangle rectangle = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
        int s = getStartOffset();
        int e = getEndOffset();
        int y1 = (int) y - rectangle.y;
        int r = c(s, e, y1);
        if (r < e) {
            biasReturn[0] = Position.Bias.Forward;
        } else {
            r = e;
            biasReturn[0] = Position.Bias.Backward;
        }
        return r;
    }

    public int getBreakWeight(int axis, float pos, float len) {
        if (axis == 1 && (len - pos) > 0.0D)
            return 2000;
        else
            return super.getBreakWeight(axis, pos, len);
    }

    public View breakView(int axis, int offset, float pos, float len) {
        if (axis == 1) {
            int h1 = (int) Math.max(0.0F, len - pos - getFontMetrics_j().getHeight());
            int p1 = a(offset, getEndOffset(), h1);
            if (p1 == offset)
                p1++;
            if (offset == getStartOffset() && p1 == getEndOffset()) {
                return this;
            } else {
                SView view = (SView) createFragment(offset, p1);
                pos_b = (int) pos;
                return view;
            }
        } else {
            return this;
        }
    }

    private int a(int offset, int l, int i1) {
        int r = c(offset, l, i1);
        Segment segment = getSegment_a(Math.max(r - 1, offset), Math.min(r + 2, l));
        char c;
        if (CharacterUtil.isLineTailForbidden(segment.first()))
            r = Math.max(r - 1, offset);
        else
            while ((c = segment.next()) != '\uFFFF' && CharacterUtil.isLineHeadForbidden(c))
                r = Math.min(r + 1, l);
        return r;
    }

    public View createFragment(int p0, int p1) {
        SView view = (SView) clone();
        view.int_c = p0 - getElement().getStartOffset();
        view.int_d = p1 - p0;
        return view;
    }

    public int getStartOffset() {
        Element element = getElement();
        if (int_d > 0)
            return element.getStartOffset() + int_c;
        else
            return element.getStartOffset();
    }

    public int getEndOffset() {
        Element element = getElement();
        if (int_d > 0)
            return element.getStartOffset() + int_c + int_d;
        else
            return element.getEndOffset();
    }

    public float getPartialSpan(int p0, int p1) {
        int span = getTabbedSpan_b(p0, p1, pos_b);
        return span;
    }

    public float getTabbedSpan(float x, TabExpander e) {
        TabExpander oldValue = this.tabExpander;
        this.tabExpander = e;
        if (this.tabExpander != oldValue)
            preferenceChanged(null, false, true);
        pos_b = (int) x;
        int so = getStartOffset();
        int eo = getEndOffset();
        int ts = getTabbedSpan_b(so, eo, (int) x);
        return ts;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public final Color getBackground() {
        Document document = getDocument();
        AttributeSet attr = getAttributes();
        if (document instanceof StyledDocument && attr.isDefined(StyleConstants.Background))
            return ((StyledDocument) document).getBackground(attr);
        else
            return null;
    }

    public final Color getForeground() {
        Document document = getDocument();
        if (document instanceof StyledDocument) {
            AttributeSet attr = getAttributes();
            return ((StyledDocument) document).getForeground(attr);
        }
        Container container = getContainer();
        if (container != null)
            return container.getForeground();
        else
            return null;
    }

    public final Font getFont_c() {
        Document document = getDocument();
        if (document instanceof StyledDocument) {
            AttributeSet attr = getAttributes();
            return ((StyledDocument) document).getFont(attr);
        }
        Container container = getContainer();
        if (container != null)
            return container.getFont();
        else
            return null;
    }

    public final boolean isUnderline() {
        AttributeSet a = getAttributes();
        return StyleConstants.isUnderline(a);
    }

    public final boolean isStrikeThrough() {
        AttributeSet a = getAttributes();
        return StyleConstants.isStrikeThrough(a);
    }

    public final boolean isSubscript() {
        AttributeSet a = getAttributes();
        return StyleConstants.isSubscript(a);
    }

    public final boolean isSuperscript() {
        AttributeSet a = getAttributes();
        return StyleConstants.isSuperscript(a);
    }

    public final Segment getSegment_a(int offset, int length) {
        Segment txt = new Segment();
        Document document = getDocument();
        try {
            document.getText(offset, length - offset, txt);
        } catch (BadLocationException e) {
            throw new IllegalStateException("VerticalGlyphView: Stale view: " + e);
        }
        return txt;
    }

    private void b(int start, int end) {
        int len = (end - start) + 1;
        if (bytes_e == null || len > bytes_e.length) {
            bytes_e = new byte[len];
            return;
        }
        for (int i = 0; i < len; i++)
            bytes_e[i] = 0;
    }

    private float getWidth_i() {
        Graphics g = getGraphics();
        if (g instanceof Graphics2D) {
            FontRenderContext context = ((Graphics2D) g).getFontRenderContext();
            Rectangle2D maxCharBounds = getFont_c().getMaxCharBounds(context);
            return Math.max((float) maxCharBounds.getWidth(), getFontMetrics_j().getHeight());
        } else {
            return getFontMetrics_j().charWidth('全');
        }
    }

    private int getTabbedSpan_b(int start, int end, int x) {
        FontMetrics fontmetrics = getFontMetrics_j();
        Segment segment = getSegment_a(start, end);
        boolean is2d = getGraphics() instanceof Graphics2D;
        int span = x;
        char c = segment.first();

        for (int pos = start; c != '\uFFFF' && pos < end; pos++) {
            if (CharacterUtil.isTab(c)) {
                span = nextTabStop(span, pos - start);
            } else {
                if (is2d) {
                    span += charWidth(fontmetrics, c);
                } else {
                    span += fontmetrics.getHeight();
                }
            }
            c = segment.next();
        }
        return span - x;
    }

    private int c(int span, int length, int p) {
        FontMetrics fontmetrics = getFontMetrics_j();
        Segment segment = getSegment_a(span, length);
        boolean is2d = getGraphics() instanceof Graphics2D;
        int tabStop = 0;
        int pos = span;
        char c = segment.first();

        for (; c != '\uFFFF' && pos < length; pos++) {
            if (CharacterUtil.isTab(c)) {
                tabStop = nextTabStop(tabStop, pos - span);
            } else {
                if (is2d) {
                    tabStop += charWidth(fontmetrics, c);
                } else {
                    tabStop += fontmetrics.getHeight();
                }
            }
            if (tabStop >= p) {
                return pos;
            }
            c = segment.next();
        }
        return pos;
    }

    private int nextTabStop(int x, int tabbOffset) {
        int h = getFontMetrics_j().getHeight() * 2;
        int r = tabExpander == null ? (x / h + 1) * h : (int) tabExpander.nextTabStop(x, tabbOffset);
        x = r;
        return r;
    }

    private static int charWidth(FontMetrics metrics, char c) {
        if (CharacterUtil.isToRotate(c))
            return metrics.charWidth(c);
        if (CharacterUtil.isSmallKana(c))
            return (metrics.getHeight() * 2) / 3;
        if (CharacterUtil.isLineSeparator(c))
            return metrics.charWidth(c);
        else
            return metrics.getHeight();
    }

    private FontMetrics getFontMetrics_j() {
        Graphics g = getGraphics();
        if (g == null)
            throw new IllegalStateException("Graphics has not been initialized.");
        Font font = getFont_c();
        if (font != null)
            g.setFont(font);
        return g.getFontMetrics();
    }

    TabExpander tabExpander;
    int pos_b;
    int int_c;
    int int_d;
    private byte[] bytes_e;
}
