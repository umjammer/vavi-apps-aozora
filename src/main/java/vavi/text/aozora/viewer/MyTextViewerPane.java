/*
 * http://www.35-35.net/aozora/
 */

package vavi.text.aozora.viewer;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.awt.MediaTracker;
import java.awt.geom.AffineTransform;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.System.Logger.Level;
import java.lang.System.Logger;
import javax.accessibility.AccessibleContext;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.core.AozoraUtil;
import com.soso.aozora.data.AozoraCharacterUtil;
import com.soso.aozora.data.AozoraContentsParser;
import com.soso.aozora.data.AozoraContentsParserHandler;
import com.soso.sgui.SGUIUtil;
import com.soso.sgui.letter.SLetterCell;
import com.soso.sgui.letter.SLetterCellFactory;
import com.soso.sgui.letter.SLetterConstraint;
import com.soso.sgui.letter.SLetterGlyphCell;
import com.soso.sgui.letter.SLetterImageCell;
import com.soso.sgui.letter.SLetterLineEnd;
import com.soso.sgui.letter.SLetterPane;
import com.soso.sgui.letter.SLetterPaneObserver;
import com.soso.sgui.letter.SLetterPaneObserverHelper;
import com.soso.sgui.letter.SLetterRuby;
import com.soso.sgui.text.CharacterUtil;
import vavi.text.UnicodeUtil;
import vavi.util.Debug;

import static javax.swing.SwingUtilities.invokeAndWait;


/**
 * based on "com.soso.aozora.viewer.TextViewerPane"
 *
 * A ruby is one {@link com.soso.sgui.letter.SLetterRuby} over the whole base letters, and
 * western text is one {@link com.soso.sgui.letter.SLetterWestern} run of proportional letters.
 * <p>
 * The layout of aozora html is taken from the elements, the indents (jisage_N, burasage), the
 * alignments to the line end (chitsuki_N), the headings (o-midashi, naka-midashi, ko-midashi)
 * and the illustrations (img class="illustration") with their captions.
 */
public class MyTextViewerPane extends JPanel {

    static final Logger logger = System.getLogger(MyTextViewerPane.class.getName());

    private class SearchFieldPane extends JPanel {

        private static class NoFocusButton extends JButton {

            @Override
            public boolean isFocusTraversable() {
                return false;
            }

            @Override
            public void requestFocus() {
            }

            @Override
            public AccessibleContext getAccessibleContext() {
                AccessibleContext ac = super.getAccessibleContext();
                if (uiKey != null) {
                    ac.setAccessibleName(UIManager.getString(uiKey));
                    uiKey = null;
                }
                return ac;
            }

            private String uiKey;

            public NoFocusButton(String uiKey) {
                this.uiKey = uiKey;
                setFocusPainted(false);
                setMargin(new Insets(0, 0, 0, 0));
                setOpaque(true);
            }
        }

        private JLabel titleLabel;
        private JTextField textField;
        @SuppressWarnings("hiding")
        private JButton nextButton;
        @SuppressWarnings("hiding")
        private JButton prevButton;
        private JButton closeButton;
        private JLabel messageLabel;

        private void initGUI() {
            setLayout(new FlowLayout(FlowLayout.LEADING, 2, 1));
            add(getCloseButton());
            add(getTitleLabel());
            add(getTextField());
            add(Box.createHorizontalStrut(2));
            add(getNextButton());
            add(getPrevButton());
            add(Box.createHorizontalStrut(5));
            add(getMessageLabel());
            setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
            resetButtonEnabled();
        }

        private SLetterPane.MenuItemProducer createSearchMenuItemProducer() {
            SLetterPane.MenuItemProducer producer = new SLetterPane.MenuItemProducer() {
                @Override public JMenuItem produceMenuItem(Point p, SLetterCell[] cells, boolean isSelected) {
                    return searchItem;
                }

                final JMenuItem searchItem = new JMenuItem(new AbstractAction("文章内検索") {
                    @Override public void actionPerformed(ActionEvent e) {
                        setSearchEnable(true);
                    }
                });
            };
            return producer;
        }

        private JLabel getTitleLabel() {
            if (titleLabel == null)
                titleLabel = new JLabel("文章内検索：");
            return titleLabel;
        }

        private JLabel getMessageLabel() {
            if (messageLabel == null)
                messageLabel = new JLabel();
            return messageLabel;
        }

        private JTextField getTextField() {
            if (textField == null) {
                textField = new JTextField();
                textField.setColumns(20);
                textField.addCaretListener(e -> resetButtonEnabled());
                textField.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER)
                            searchNext(getSearchKeyword());
                    }
                });
            }
            return textField;
        }

        private JButton getNextButton() {
            if (nextButton == null) {
                nextButton = new JButton();
                nextButton.setName("SearchFieldPane.nextButton");
                nextButton.setAction(new AbstractAction(AozoraEnv.ShortCutKey.SEARCH_IN_WORK_NEXT_SHORTCUT.getName(),
                                                        AozoraUtil.getIcon(AozoraEnv.Env.GO_LEFT_VIEW_ICON.getString())) {
                    @Override public void actionPerformed(ActionEvent e) {
                        searchNext(getSearchKeyword());
                    }
                });
                AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, AozoraEnv.ShortCutKey.SEARCH_IN_WORK_NEXT_SHORTCUT.getKeyStroke(), nextButton);
                nextButton.setToolTipText(AozoraEnv.ShortCutKey.SEARCH_IN_WORK_NEXT_SHORTCUT.getNameWithHelpTitle());
            }
            return nextButton;
        }

        private JButton getPrevButton() {
            if (prevButton == null) {
                prevButton = new JButton();
                prevButton.setName("SearchFieldPane.prevButton");
                prevButton.setAction(new AbstractAction(AozoraEnv.ShortCutKey.SEARCH_IN_WORK_PREV_SHORTCUT.getName(),
                                                        AozoraUtil.getIcon(AozoraEnv.Env.GO_RIGHT_VIEW_ICON.getString())) {
                    @Override public void actionPerformed(ActionEvent e) {
                        searchPrev(getSearchKeyword());
                    }
                });
                AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, AozoraEnv.ShortCutKey.SEARCH_IN_WORK_PREV_SHORTCUT.getKeyStroke(), prevButton);
                prevButton.setToolTipText(AozoraEnv.ShortCutKey.SEARCH_IN_WORK_PREV_SHORTCUT.getNameWithHelpTitle());
                prevButton.setHorizontalTextPosition(JButton.LEFT);
            }
            return prevButton;
        }

        private JButton getCloseButton() {
            if (closeButton == null) {
                closeButton = new NoFocusButton("SearchFieldPane.closeButtonAccessibleName");
                closeButton.setName("SearchFieldPane.closeButton");
                closeButton.setContentAreaFilled(false);
                closeButton.putClientProperty("paintActive", Boolean.TRUE);
                closeButton.setBorder(new EmptyBorder(0, 0, 0, 0));
                closeButton.setAction(new AbstractAction(null, UIManager.getIcon("InternalFrame.closeIcon")) {
                    @Override public void actionPerformed(ActionEvent e) {
                        closeSearch();
                    }
                });
                AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, AozoraEnv.ShortCutKey.SEARCH_IN_WORK_CLOSE_SHORTCUT.getKeyStroke(), closeButton);
                closeButton.setToolTipText(AozoraEnv.ShortCutKey.SEARCH_IN_WORK_CLOSE_SHORTCUT.getNameWithHelpTitle());
            }
            return closeButton;
        }

        void setMessage(String message) {
            getMessageLabel().setText(message);
        }

        private void resetButtonEnabled() {
            boolean isHasKeyword = getSearchKeyword() != null;
            getNextButton().setEnabled(isHasKeyword);
            getPrevButton().setEnabled(isHasKeyword);
        }

        private String getSearchKeyword() {
            String word = getTextField().getText();
            if (word != null && !word.isEmpty())
                return word;
            else
                return null;
        }

        private void closeSearch() {
            setVisible(false);
        }

        @Override
        public void setVisible(boolean flag) {
            super.setVisible(flag);
            SGUIUtil.setAllTextSelected(getTextField());
        }

        private SearchFieldPane() {
            initGUI();
        }
    }

    static class GaijiRubyBuilder {

        private static final int STATUS_NONE = 0;
        private static final int STATUS_RB = 1;
        private static final int STATUS_RT = 2;

        private final List<SLetterCell> rb = new ArrayList<>();
        private final StringBuilder rt = new StringBuilder();

        int status;

        void startRB() {
            status = STATUS_RB;
        }

        void startRT() {
            status = STATUS_RT;
        }

        void endRB() {
            status = STATUS_NONE;
        }

        void endRT() {
            status = STATUS_NONE;
        }

        void append(String s) {
            for (char c : s.toCharArray()) {
                append(c);
            }
        }

        void append(char c) {
            switch (status) {
            case STATUS_RB:
                rb.add(SLetterCellFactory.getInstance().createGlyphCell(c));
                break;
            case STATUS_RT:
                rt.append(c);
                break;
            }
        }

        void append(SLetterCell cell) {
            switch (status) {
            case STATUS_RB:
                rb.add(cell);
                break;
            }
        }

        SLetterCell[] getResult() {
            SLetterCell[] cells = rb.toArray(new SLetterCell[0]);
            // the ruby is set on the whole base letters as a group ruby, JLReq 3.3 lays it out
            if (cells.length != 0 && !rt.isEmpty() && cells[0].getRuby() == null)
                SLetterRuby.group(rt.toString(), rb);
            return cells;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder().append(super.toString());
            for (SLetterCell cell : getResult()) {
                sb.append(cell);
            }

            return sb.toString();
        }

        public GaijiRubyBuilder() {
            status = STATUS_NONE;
        }
    }

    /**
     * The layout an element of aozora html gives to the letters in it, a property which is
     * null is taken from the element outside.
     *
     * @see "https://www.aozora.gr.jp/annotation/layout_2.html"
     * @see "https://www.aozora.gr.jp/annotation/heading.html"
     */
    static class Style {

        static final Pattern CLASS = Pattern.compile("class=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);
        static final Pattern MARGIN_LEFT = Pattern.compile("margin-left:\\s*(\\d+)em");
        static final Pattern TEXT_INDENT = Pattern.compile("text-indent:\\s*(-?\\d+)em");
        static final Pattern MARGIN_RIGHT = Pattern.compile("margin-right:\\s*(\\d+)em");
        static final Pattern CHITSUKI = Pattern.compile("chitsuki_(\\d+)");
        static final Pattern SIZE = Pattern.compile("(dai|sho)(\\d+)");

        /**
         * the scale of the letters of the size, one step is "large" or "small" of css and three
         * steps or more are "xx-large" or "xx-small" as aozora html does
         */
        static final float[] LARGER = {1.2f, 1.5f, 2f};
        static final float[] SMALLER = {0.8f, 0.65f, 0.55f};

        /** the element name */
        final String name;
        /** the indent of the first line of a paragraph (字下げ) */
        Integer indent;
        /** the indent of the lines after a line break (折り返し) */
        Integer wrapIndent;
        /** the places left blank at the line end of the text aligned to it (地付き, 地から○字上げ) */
        Integer raise;
        /** the font of a heading, a caption or bold letters (太字) */
        Font font;
        /** italic letters (斜体) */
        boolean italic;
        /** the size of the letters (文字の大きさ) to the letters of the text */
        Float scale;
        /** a heading which is in a line of the text (同行見出し, 窓見出し) */
        boolean inline;

        Style(String name) {
            this.name = name;
        }

        /** @param element the tag without the brackets */
        static Style of(String element, Settings settings) {
            String name = element.split("\\s", 2)[0].toLowerCase();
            Style style = new Style(name);
            Matcher m = CLASS.matcher(element);
            String clazz = m.find() ? m.group(1) : "";
            m = MARGIN_LEFT.matcher(element);
            if (m.find()) {
                // burasage is given by a negative text-indent
                style.wrapIndent = Integer.parseInt(m.group(1));
                Matcher t = TEXT_INDENT.matcher(element);
                style.indent = Math.max(0, style.wrapIndent + (t.find() ? Integer.parseInt(t.group(1)) : 0));
            }
            m = CHITSUKI.matcher(clazz);
            if (m.find()) {
                Matcher r = MARGIN_RIGHT.matcher(element);
                style.raise = r.find() ? Integer.parseInt(r.group(1)) : Integer.parseInt(m.group(1));
            }
            if (clazz.contains("midashi")) {
                style.font = settings.getHeadingFont();
                style.inline = clazz.startsWith("dogyo-") || clazz.startsWith("mado-");
            } else if (clazz.equals("caption")) {
                style.font = settings.getCaptionFont();
            } else if (clazz.equals("futoji")) {
                style.font = settings.getBoldFont();
            } else if (clazz.equals("shatai")) {
                style.italic = true;
            }
            m = SIZE.matcher(clazz);
            if (m.matches()) {
                float[] scales = m.group(1).equals("dai") ? LARGER : SMALLER;
                style.scale = scales[Math.min(Integer.parseInt(m.group(2)), scales.length) - 1];
            }
            return style;
        }

        @Override
        public String toString() {
            return name + "[indent=" + indent + ", wrapIndent=" + wrapIndent + ", raise=" + raise + ", font=" + font + ", italic=" + italic + ", scale=" + scale + "]";
        }
    }

    private class ContentsHandler implements AozoraContentsParserHandler {

        boolean title;
        boolean kaeriten;
        boolean notes;
        boolean alternative;
        SLetterGlyphCell rubyAlternative;
        /** a page break is just made, the line break which follows it is not needed */
        boolean pageBreak;
        /** an illustration is just made, which ends its line, the line break which follows it is not needed */
        boolean afterBlock;
        /** no letter is put in the line yet */
        boolean lineHead = true;

        /** 改丁 and 改見開き are taken as 改ページ, and 改段 as well for a page has one column, no empty page is put for them */
        static final String PAGE_CENTER = "［＃ページの左右中央］";

        static final List<String> pageBreaks = List.of("［＃改ページ］", "［＃改丁］", "［＃改見開き］", "［＃改段］");

        /** the elements which give the layout, the innermost is the last */
        private final Deque<Style> styles = new ArrayDeque<>();

        /** the letters of a run aligned to the line end, which are put when the run ends */
        private List<SLetterCell> lineEndLetters;
        private int lineEndRaise;

        private Integer indent() {
            for (Iterator<Style> i = styles.descendingIterator(); i.hasNext(); ) {
                Style style = i.next();
                if (style.indent != null)
                    return lineHead ? style.indent : style.wrapIndent;
            }
            return 0;
        }

        private Integer raise() {
            for (Iterator<Style> i = styles.descendingIterator(); i.hasNext(); ) {
                Style style = i.next();
                if (style.raise != null)
                    return style.raise;
            }
            return null;
        }

        /** the fonts made for the styles */
        private final Map<List<Object>, Font> fonts = new HashMap<>();

        /** the font of the elements, which is made of the face, the size and italic */
        private Font font() {
            Font face = null;
            Float scale = null;
            boolean italic = false;
            for (Iterator<Style> i = styles.descendingIterator(); i.hasNext(); ) {
                Style style = i.next();
                if (face == null)
                    face = style.font;
                if (scale == null)
                    scale = style.scale;
                italic |= style.italic;
            }
            if (scale == null && !italic)
                return face;
            Font base = face != null ? face : settings.getFont();
            float size = base.getSize2D() * (scale != null ? scale : 1);
            boolean slanted = italic;
            return fonts.computeIfAbsent(Arrays.asList(base, size, slanted), k -> {
                Font font = base.deriveFont(size);
                // there is no italic face of the japanese fonts, the letters are slanted
                return slanted ? font.deriveFont(AffineTransform.getShearInstance(-0.2, 0)) : font;
            });
        }

        /** starts an element */
        private void push(Style style) {
            flush();
            styles.addLast(style);
logger.log(Level.TRACE, "style: push: " + style);
        }

        /** ends the innermost element of the name, and the elements in it which are not ended */
        private void pop(String name) {
            flush();
            if (styles.stream().noneMatch(style -> style.name.equals(name)))
                return;
            Style style;
            do {
                style = styles.removeLast();
logger.log(Level.TRACE, "style: pop: " + style);
            } while (!style.name.equals(name));
            if (raise() == null)
                endLineEnd();
        }

        /** a block starts at a line head */
        private void startLine() {
            flush();
            if (!lineHead)
                appendCell(cellFactory.createGlyphCell('\n'));
        }

        /** a block ends its line */
        private void endLine() {
            flush();
            if (!lineHead)
                appendCell(cellFactory.createGlyphCell('\n'));
        }

        /**
         * Gives the layout of the elements to the letter and puts it. The letters of a run
         * aligned to the line end are kept until the run ends, for the run is placed as a whole.
         */
        private void put(SLetterCell cell) {
            boolean breaking = cell.isConstraintSet(SLetterConstraint.BREAK.NEW_LINE) ||
                               cell.isConstraintSet(SLetterConstraint.BREAK.NEW_PAGE) ||
                               cell.isConstraintSet(SLetterConstraint.PAGE.CENTER);
            if (!breaking) {
                cell.setIndent(indent());
                Font font = font();
                if (font != null && cell instanceof SLetterGlyphCell glyph && glyph.getFont() == null)
                    glyph.setFont(font);
                pageBreak = false;
                afterBlock = false;
                lineHead = false;
                Integer raise = raise();
                if (raise != null) {
                    if (lineEndLetters != null && lineEndRaise != raise)
                        endLineEnd();
                    if (lineEndLetters == null) {
                        lineEndLetters = new ArrayList<>();
                        lineEndRaise = raise;
                    }
                    lineEndLetters.add(cell);
                    return;
                }
            }
            endLineEnd();
            MyTextViewerPane.this.appendCell(cell);
            if (breaking)
                lineHead = true;
        }

        /** puts the run aligned to the line end */
        private void endLineEnd() {
            if (lineEndLetters != null) {
                List<SLetterCell> letters = lineEndLetters;
                lineEndLetters = null;
                SLetterLineEnd.of(lineEndRaise, letters);
                for (SLetterCell letter : letters)
                    MyTextViewerPane.this.appendCell(letter);
            }
        }

        private GaijiRubyBuilder gaijirb;

        /**
         * the text which is not made into letters yet. the texts are collected over the calls,
         * so that a gaiji in a western word (as "g" "é" "ographiques") and the spaces around it
         * are kept in one run of western text (JLReq 3.2.6)
         */
        private final StringBuilder text = new StringBuilder();

        private void appendCell(SLetterCell cell) {
            flush();
            put(cell);
        }

        /** makes the letters of the text collected so far, the white space is as html does */
        private void flush() {
            if (!text.isEmpty()) {
                String s = CharacterUtil.trimSpace(text.toString());
                text.setLength(0);
                for (SLetterCell cell : cellFactory.createCells(s, null)) {
                    put(cell);
                }
            }
        }

        private final SLetterCellFactory cellFactory = SLetterCellFactory.getInstance();

        static final String pattern = ".*[Uu]\\+([0-9a-fA-F]{4,5}).*";
        String parseUnicode(String source) {
            if (source.matches(pattern)) {
                return source.replaceFirst(pattern, "$1");
            } else {
                return null;
            }
        }

        @Override
        public void characters(String cdata) {
            if (title) {
                title = false;
                title(cdata);
            }
            if (kaeriten) {
logger.log(Level.TRACE, "characters|レ点: " + cdata);
                // TODO too large
//                for (char c : cdata.toCharArray()) {
//                    SLetterCell cell = getCellFactory().createKaeritenGlyphCell(c);
//                    appendCell(cell);
//                }
                // bad usage, but beautiful
                SLetterCell cell = cellFactory.createGlyphCell('　', cdata);
                appendCell(cell);

                kaeriten = false;
                return;
            }
            if (notes) {
                if (cdata.startsWith("［＃")) {
                    if (pageBreaks.contains(cdata.trim())) {
logger.log(Level.DEBUG, "characters|[notes:page break]: " + cdata);
                        unnoted();
                        // the pane ends the page at a page separator
                        appendCell(cellFactory.createGlyphCell('\f'));
                        pageBreak = true;
                    } else if (PAGE_CENTER.equals(cdata.trim())) {
logger.log(Level.DEBUG, "characters|[notes:page center]: " + cdata);
                        unnoted();
                        appendCell(cellFactory.createPageCenterCell());
                        pageBreak = true;
                    } else if (alternative) {
                        String a = parseUnicode(cdata);
                        if (a != null) {
                            char c = (char) Integer.parseInt(a, 16);
logger.log(Level.DEBUG, "characters|[notes:※:U+%s]: %c, %s".formatted(a, c, cdata));
                            SLetterCell cell = cellFactory.createGlyphCell(c);
                            appendCell(cell);
                        } else {
logger.log(Level.WARNING, "characters|[notes:※:N/A]: %s".formatted(cdata));
                        }
                        alternative = false;
                    } else if (rubyAlternative != null) {
                        String a = parseUnicode(cdata);
                        if (a != null) {
                            char c = (char) Integer.parseInt(a, 16);
logger.log(Level.DEBUG, "characters|[notes:ruby※:U+%s]: %c, %s".formatted(a, c, cdata));
                            rubyAlternative.setMain(c);
                        } else {
logger.log(Level.WARNING, "characters|[notes:ruby※:N/A]: %s".formatted(cdata));
                        }
                        rubyAlternative = null;
                    } else {
logger.log(Level.DEBUG, "characters|[notes:#]: " + cdata);
                    }
                } else {
logger.log(Level.DEBUG, "characters|[notes]: " + cdata);
                }
                notes = false;
                return;
            }

            if (gaijirb != null) {
                gaijirb.append(cdata);
                return;
            }
            // https://linuxtut.com/en/bdc62f95f6d342705001/
            // the letters are collected and made at once, so that western text among them is
            // kept as a run, which is set with the proportional advances (JLReq 3.2.6)
            char[] ca = cdata.toCharArray();
            for (int i = 0; i < ca.length; i++) {
                if (ca[i] == '※') {
logger.log(Level.TRACE, "characters|" + "※※※ NOTED ※※※");
                    flush();
                    alternative = true;
                } else {
                    unnoted();
                    if (Character.isHighSurrogate(ca[i]) && i + 1 < ca.length && Character.isSurrogatePair(ca[i], ca[i + 1])) {
logger.log(Level.DEBUG, "surrogate pair: %s".formatted(new String(new int[] {cdata.codePointAt(i)}, 0, 1)));
                        text.append(ca[i]).append(ca[i + 1]);
                        i++;
                    } else {
                        // TODO old-new on/off flag
                        text.append(UnicodeUtil.toNew(String.valueOf(ca[i])).charAt(0));
                    }
                }
            }
        }

        /** a '※' which is not followed by its note is shown as it is */
        private void unnoted() {
            if (alternative) {
                alternative = false;
                text.append('※');
            }
        }

        @Override
        public void img(URL src, String alt, boolean isGaiji) {
logger.log(Level.TRACE, "srcAttr: " + src + ", " + alt + ", " + isGaiji);
            if (src.toString().matches(".*(\\d)-(\\d{2})-(\\d{2}).*")) {
                String[] prc = src.toString().replaceFirst(".*(\\d)-(\\d{2})-(\\d{2}).*", "$1,$2,$3").split(",");

                String unicode = UnicodeUtil.toUnicodeChar(Integer.parseInt(prc[0]), Integer.parseInt(prc[1]), Integer.parseInt(prc[2]));
                // TODO why replaceFirst("[※\\(\\)]", "") doesn't work???
                String a = alt.replaceFirst("※", "").replace("(", "").replace(")", "").trim();
                if (unicode != null) {
logger.log(Level.DEBUG, "image: %s -> %s, %s%s".formatted(Arrays.toString(prc), unicode, a, unicode.length() > 1 ? ", surrogate pare" : ""));
                    characters(unicode);
                    return;
                } else {
logger.log(Level.INFO, "image: %s -> not found: %s".formatted(Arrays.toString(prc), a));
                }
            }

            ImageIcon icon = new ImageIcon(src);
            if (!isGaiji && gaijirb == null) {
                // an illustration (挿絵) takes lines of its own
                startLine();
                if (icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
logger.log(Level.WARNING, "Image | not loaded | " + src);
                    characters("［" + (alt != null ? alt : src.getFile()) + "］");
                    endLine();
                    return;
                }
logger.log(Level.INFO, "Image | " + src);
                SLetterImageCell cell = (SLetterImageCell) cellFactory.createImageCell(icon.getImage(), alt);
                cell.setBlock(true);
                cell.setMagnifyable(true);
                appendCell(cell);
                afterBlock = true;
                lineHead = true;
                return;
            }
            Image image = icon.getImage();
            if (icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
                Icon errorIcon = UIManager.getIcon("OptionPane.errorIcon");
                image = new BufferedImage(errorIcon.getIconWidth(), errorIcon.getIconHeight(), 1);
                image.getGraphics().fillRect(0, 0, errorIcon.getIconWidth(), errorIcon.getIconHeight());
                errorIcon.paintIcon(MyTextViewerPane.this, image.getGraphics(), 0, 0);
            }
            SLetterCell cell = cellFactory.createImageCell(image, alt);
            if (gaijirb != null) {
                gaijirb.append(cell);
                return;
            }
            ((SLetterImageCell) cell).setMagnifyable(!isGaiji);
            if (AozoraCharacterUtil.isGaijiToRotate(src.getFile())) {
                logger.log(Level.INFO, "Gaiji | rotate | " + src);
                cell.addConstraint(SLetterConstraint.ROTATE.GENERALLY);
            } else {
                logger.log(Level.INFO, "Gaiji | " + src);
            }
            appendCell(cell);
        }

        @Override
        public void newLine() {
            if (pageBreak || afterBlock) {
                // the page break line itself, otherwise the next page starts with an empty line,
                // and the line of an illustration, which is ended by the illustration
                pageBreak = false;
                afterBlock = false;
                return;
            }
            SLetterCell cell = cellFactory.createGlyphCell('\n');
            appendCell(cell);
        }

        @Override
        public void otherElement(String element) {
            String lowerElement = element.toLowerCase();
            if (lowerElement.startsWith("h1 class=\"title\"")) {
                title = true;
            }
            if (lowerElement.startsWith("sub class=\"kaeriten\"")) {
                kaeriten = true;
            } else if (lowerElement.startsWith("span")) {
                if (lowerElement.startsWith("span class=\"notes\""))
                    notes = true;
                push(Style.of(element, settings));
            } else if (lowerElement.startsWith("/span")) {
                pop("span");
            } else if (lowerElement.matches("div(\\s.*)?")) {
                startLine();
                push(Style.of(element, settings));
            } else if (lowerElement.startsWith("/div")) {
                endLine();
                pop("div");
            } else if (lowerElement.matches("h[3-6]\\s.*midashi.*")) {
                Style style = Style.of(element, settings);
                if (!style.inline)
                    startLine();
                push(style);
            } else if (lowerElement.matches("/h[3-6]") &&
                       styles.stream().anyMatch(style -> style.name.equals(lowerElement.substring(1)))) {
                boolean inline = styles.stream().filter(style -> style.name.equals(lowerElement.substring(1)))
                        .reduce((a, b) -> b).get().inline;
                if (!inline)
                    endLine();
                pop(lowerElement.substring(1));
            } else if (lowerElement.startsWith("ruby")) {
                if (gaijirb != null) {
logger.log(Level.WARNING, "another ruby starts while building: " + gaijirb);
                    flushRuby();
                }
                gaijirb = new GaijiRubyBuilder();
            } else if (lowerElement.startsWith("rb")) {
                if (gaijirb != null)
                    gaijirb.startRB();
            } else if (lowerElement.startsWith("/rb")) {
                if (gaijirb != null)
                    gaijirb.endRB();
            } else if (lowerElement.startsWith("rt")) {
                if (gaijirb != null)
                    gaijirb.startRT();
            } else if (lowerElement.startsWith("/rt")) {
                if (gaijirb != null)
                    gaijirb.endRT();
            } else if (lowerElement.startsWith("/ruby")) {
                flushRuby();
            } else if (lowerElement.startsWith("p") ||
                       lowerElement.startsWith("/p") ||
                       lowerElement.startsWith("h") ||
                       lowerElement.startsWith("/h") ||
                       lowerElement.startsWith("table") ||
                       lowerElement.startsWith("/table") ||
                       lowerElement.startsWith("tr")) {
                newLine();
            } else if (lowerElement.startsWith("li")) {
                newLine();
                characters("・");
            } else if (lowerElement.startsWith("/td")) {
                characters("\t");
            } else {
logger.log(Level.TRACE, "others: " + element);
            }
        }

        /** appends the ruby being built, a broken ruby tag should not stop the whole text */
        private void flushRuby() {
            if (gaijirb != null) {
                GaijiRubyBuilder builder = gaijirb;
                gaijirb = null;
                for (SLetterCell cell : builder.getResult()) {
                    appendCell(cell);
                }
            }
        }

        /**
         * @param rb target text
         * @param rt ruby text
         */
        @Override
        public void ruby(String rb, String rt) {
            if (gaijirb != null) {
logger.log(Level.WARNING, "ruby[" + rb + "," + rt + "] appears while building: " + gaijirb);
                flushRuby();
            }
logger.log(Level.TRACE, rb + ", " + rt);
            if (rb != null) {
                // the ruby is kept as one run over its base letters, it is never divided per letter
                SLetterCell[] cells = cellFactory.createRubyCells(rb, rt, font());
                for (int i = 0; i < cells.length; i++) {
                    appendCell(cells[i]);
                    if (i < rb.length() && rb.charAt(i) == '※') {
                        if (cells.length == 1) {
                            rubyAlternative = (SLetterGlyphCell) cells[i];
logger.log(Level.INFO, "ruby: alternative: " + rubyAlternative);
                        } else {
logger.log(Level.INFO, "ruby: unhandled: ※");
                        }
                    }
                }
            }
        }

        @Override
        public void parseFinished() {
            flushRuby();
            flush();
            endLineEnd();
            MyTextViewerPane.this.parseFinished();
        }
    }

    private class ViewerPaneObserver extends SLetterPaneObserverHelper implements SLetterPaneObserver {

        @Override
        public void colCountChanged(int oldColCount, int newColCount) {
            if (oldColCount < newColCount)
                tryAppend();
            else
                ensureEndPos();
        }

        @Override
        public void rowCountChanged(int oldRowCount, int newRowCount) {
            logger.log(Level.INFO, "cached prev clear " + Arrays.toString(cachedPrevPosStack.toArray()));
            cachedPrevPosStack.clear();
            if (oldRowCount < newRowCount)
                tryAppend();
            else
                ensureEndPos();
        }

        @Override
        public void rowSpaceChanged(int oldRowSpace, int newRowSpace) {
            settings.setRowSpace(newRowSpace);
        }

        @Override
        public void fontRangeRatioChanged(float oldFontRangeRatio, float newFontRangeRatio) {
            settings.setFontRatio(newFontRangeRatio);
        }
    }

    static class Settings {
        public void setFontRatio(float fontRatio) {
            this.fontRatio = fontRatio;
        }
        final Color defaultBGColor = new Color(0xFFFFFF);
        public Color getDefaultBGColor() {
            return defaultBGColor;
        }
        final Color background = new Color(0xFFFFFF);
        public Color getBackground() {
            return background;
        }
        final Color foreground = new Color(0x000000);
        public Color getForeground() {
            return foreground;
        }
        final int fontSize = 32;
        final Font font = new Font("Hiragino Mincho ProN", Font.PLAIN, fontSize);
        public Font getFont() {
            return font;
        }
        /** a heading (見出し) is set in bold, which is a face of its own, the bold style does not choose it */
        final Font headingFont = new Font("HiraMinProN-W6", Font.PLAIN, fontSize);
        public Font getHeadingFont() {
            return headingFont;
        }
        /** bold letters (太字) are set in bold gothic */
        final Font boldFont = new Font("HiraginoSans-W6", Font.PLAIN, fontSize);
        public Font getBoldFont() {
            return boldFont;
        }
        /** a caption (キャプション) is set in gothic */
        final Font captionFont = new Font("Hiragino Sans", Font.PLAIN, fontSize);
        public Font getCaptionFont() {
            return captionFont;
        }
        int rowSpace = fontSize / 2;
        public int getRowSpace() {
            return rowSpace;
        }
        public void setRowSpace(int rowSpace) {
            this.rowSpace = rowSpace;
        }
        float fontRatio = AozoraEnv.DEFAULT_FONT_RATIO;
        public float getFontRatio() {
            return fontRatio;
        }
    }

    final Settings settings = new Settings();

    private SLetterPane textPane;
    private final List<SLetterCell> textCells = new ArrayList<>();
    private int startPos = 0;
    private int endPos = 0;
    private final Stack<Integer> cachedPrevPosStack = new Stack<>();
    private JPanel buttonPanel;
    final String nextAction = "TextViewerPane.nextButton";
    final String prevAction = "TextViewerPane.prevButton";
    boolean nextEnabled;
    boolean prevEnabled;
    private Icon goLeftIcon;
    private Icon goRightIcon;
    private Icon goUpIcon;
    private Icon goDownIcon;
    private JProgressBar progress;
    private boolean isFirstPageLoaded = false;
    private boolean isAllPageLoaded = false;
    private final int firstStartPos;
    private SearchFieldPane searchFieldPane;

    /** called from parser, override me */
    protected void title(String title) {}

    URI uri;
    Reader reader;
    URL base;

    /**
     *
     * @param uri aozora html text
     * @param firstStartPos position of reflow
     */
    public MyTextViewerPane(URI uri, int firstStartPos) {
        this.uri = uri;
        this.firstStartPos = firstStartPos;
        initGUI();
        setup();
    }

    /**
     *
     * @param reader aozora html text
     * @param base dummy
     * @param firstStartPos position of reflow
     */
    public MyTextViewerPane(Reader reader, URL base, int firstStartPos) {
        this.reader = reader;
        this.base = base;
        this.firstStartPos = firstStartPos;
        initGUI();
        setup();
    }

    private void initGUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(settings.getDefaultBGColor());
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(48, 32, 48, 32));
        wrapper.setOpaque(false);
        textPane = SLetterPane.newInstance(SLetterConstraint.ORIENTATION.TBRL);
        textPane.addObserver(new ViewerPaneObserver());
        textPane.setBackground(settings.getBackground());
        textPane.setForeground(settings.getForeground());
        textPane.setRowColCountChangable(true);
        textPane.setFontSizeChangable(true);
        textPane.setLetterBorderRendarer(null);
        textPane.setFont(settings.getFont());
        textPane.setRowSpace(settings.getRowSpace());
        textPane.setFontRangeRatio(settings.getFontRatio());
        wrapper.add(textPane, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);
        goLeftIcon = AozoraUtil.getIcon(AozoraEnv.Env.GO_LEFT_ICON.getString());
        goRightIcon = AozoraUtil.getIcon(AozoraEnv.Env.GO_RIGHT_ICON.getString());
        goUpIcon = AozoraUtil.getIcon(AozoraEnv.Env.GO_UP_ICON.getString());
        goDownIcon = AozoraUtil.getIcon(AozoraEnv.Env.GO_DOWN_ICON.getString());
        nextEnabled = false;
        AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_IN_FOCUSED_WINDOW, AozoraEnv.ShortCutKey.PAGE_NEXT_LEFT_SHORTCUT.getKeyStroke(), nextAction, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                next();
            }
        });
        prevEnabled = false;
        AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_IN_FOCUSED_WINDOW, AozoraEnv.ShortCutKey.PAGE_PREV_RIGHT_SHORTCUT.getKeyStroke(), prevAction, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                prev();
            }
        });
        progress = new JProgressBar(0) {
            @Override protected void paintComponent(Graphics g) {
                if (isProgressBarRevertOrientation()) {
                    double x = getWidth() * 0.5d;
                    double y = getHeight() * 0.5d;
                    ((Graphics2D) g).rotate(Math.PI, x, y);
                }
                super.paintComponent(g);
            }
        };
        progress.addMouseListener(new MouseAdapter() {
            @Override public void mouseReleased(MouseEvent e) {
                setPageByProgressClick(e.getX(), e.getY());
            }
        });
        progress.setOpaque(false);
        buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BorderLayout(3, 3));
        buttonPanel.add(progress, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        searchFieldPane = new SearchFieldPane();
        searchFieldPane.setOpaque(false);
        searchFieldPane.setVisible(false);
        buttonPanel.add(searchFieldPane, BorderLayout.NORTH);
        textPane.addMenuItemProducer(searchFieldPane.createSearchMenuItemProducer());
        textPane.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int hw = getWidth() / 2;
                Rectangle l = new Rectangle(0, 0, hw, getHeight());
                Rectangle r = new Rectangle(hw, 0, getWidth(), getHeight());
                if (l.contains(e.getPoint())) {
//logger.log("mouseClicked: next");
                    next();
                } else if (r.contains(e.getPoint())){
//logger.log("mouseClicked: prev");
                    prev();
                }
            }
        });
        AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_IN_FOCUSED_WINDOW, AozoraEnv.ShortCutKey.SEARCH_IN_WORK_SHORTCUT.getKeyStroke(), "searchAction", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                setSearchEnable(true);
            }
        });
    }

    private void setup() {
        try {
            new Thread(this::setupAsynchronous, "AozoraContentsParser-Thread").start();
        } catch (Exception e) {
            disposeWithError(e);
        }
    }

    /** set text */
    private void setupAsynchronous() {
        try {
            AozoraContentsParserHandler handler = new ContentsHandler();
            AozoraContentsParser parser = new AozoraContentsParser(null, handler);
            if (uri != null) {
                parser.parse(uri.toURL());
            } else if (reader != null) {
                parser.parse(reader, base);
            }
        } catch (Exception e) {
            disposeWithError(e);
        }
    }

    void disposeWithError(final Throwable t) {
        try {
            logger.log(Level.ERROR, t.getMessage(), t);
            invokeAndWait(() -> JOptionPane.showInternalMessageDialog(MyTextViewerPane.this,
                    String.join("\n", Arrays.toString(t.getStackTrace()).split(",")),
                    "作品を表示できません。", JOptionPane.ERROR_MESSAGE));
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        if (!isFirstPageLoaded)
            ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        super.paintComponent(g);
    }

    void setStartPos(int startPos) {
        setStartPos(startPos, false);
    }

    private void tryAppend() {
        setStartPos(endPos, true);
    }

    private void ensureEndPos() {
        synchronized (textCells) {
            SLetterCell lastCell = null;
done:       for (int row = textPane.getRowCount() - 1; row >= 0; row--) {
                for (int col = textPane.getColCount() - 1; col >= 0; col--) {
                    SLetterCell[] cells = textPane.getCell(row, col);
                    if (cells != null) {
                        for (int i = cells.length - 1; i >= 0; i--) {
                            lastCell = cells[i];
                            if (lastCell != null)
                                break done;
                        }
                    }
                }
            }
            if (lastCell != null) {
                int posMax = textCells.size();
                for (int pos = 0; pos < posMax; pos++) {
                    if (lastCell == textCells.get(pos)) {
                        // the end is after the last cell, as setStartPos does
                        endPos = pos + 1;
                        setupButtonEnabled();
                        setupPageNumber();
                        break;
                    }
                }
            }
        }
    }

    private void setStartPos(int startPos, boolean append) {
        synchronized (textCells) {
            try {
                if (!append)
                    textPane.removeCellAll();
                boolean isAdded = false;
                int posMax = textCells.size();
                for (int pos = startPos; pos < posMax; pos++) {
                    SLetterCell cell = textCells.get(pos);
                    if (!textPane.addCell(cell))
                        break;
                    isAdded = true;
                    endPos = pos + 1;
                }
                if (!append)
                    this.startPos = startPos;
                if (isAdded)
                    repaint();
                setupButtonEnabled();
                setupPageNumber();
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
        }
    }

    private void setupButtonEnabled() {
        if (isFirstPageLoaded)
            synchronized (textCells) {
                prevEnabled = startPos > 0;
                nextEnabled = endPos < textCells.size() - 1;
            }
    }

    private void next() {
        if (nextEnabled) {
            nextEnabled = false;
            SwingUtilities.invokeLater(this::nextImpl);
        }
    }

    private void nextImpl() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        int lastStartPos = startPos;
logger.log(Level.INFO, "next," + Arrays.toString(cachedPrevPosStack.toArray()) + "," + endPos);
        setStartPos(endPos);
        cachedPrevPosStack.push(lastStartPos);
        setupButtonEnabled();
        setupPageNumber();

        setCursor(Cursor.getDefaultCursor());
    }

    private void prev() {
        if (prevEnabled) {
            prevEnabled = false;
            SwingUtilities.invokeLater(this::prevImpl);
        }
    }

    private void prevImpl() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        int lastStartPos = startPos;
        List<Integer> triedStartPosList = new ArrayList<>();
        StringBuilder log = new StringBuilder().append("prev");
        if (!cachedPrevPosStack.isEmpty()) {
            log.append(",cached,").append(Arrays.toString(cachedPrevPosStack.toArray()));
            int cachedPrevPos = cachedPrevPosStack.pop();
            setStartPos(cachedPrevPos);
            triedStartPosList.add(cachedPrevPos);
        }
        int diff;
        while ((diff = endPos - lastStartPos) != 0) {
            log.append(",").append(startPos);
            int tryStartPos = startPos - diff;
            if (tryStartPos < 0) {
                setStartPos(0);
                break;
            }
            if (textCells.get(tryStartPos).isConstraintSet(SLetterConstraint.BREAK.BACK_IF_LINE_HEAD))
                tryStartPos++;
            if (triedStartPosList.contains(tryStartPos))
                break;
            setStartPos(tryStartPos);
            triedStartPosList.add(tryStartPos);
        }
        for (int pos = startPos - 1; endPos > lastStartPos && pos >= 0; pos--) {
            setStartPos(pos);
            log.append(">").append(startPos);
        }

        for (int pos = startPos + 1; endPos < lastStartPos && pos <= textCells.size() - 1; pos++) {
            setStartPos(pos);
            log.append("<").append(startPos);
        }

        log.append("|lastStart=").append(lastStartPos).append("|thisEnd=").append(endPos);
        logger.log(Level.INFO, log.toString());
        setupButtonEnabled();
        setupPageNumber();

        setCursor(Cursor.getDefaultCursor());
    }

    private void setPageByProgressClick(int x, int y) {
        if (isFirstPageLoaded) {
            SLetterConstraint.ORIENTATION orientation = getOrientation();
            synchronized (textCells) {
                float percent = orientation.isHorizonal() ? (float) y / (float) progress.getHeight()
                                                          : (float) x / (float) progress.getWidth();
                if (!orientation.isHorizonal() &&
                    !orientation.isLeftToRight() || orientation.isHorizonal() &&
                    !orientation.isTopToButtom())
                    percent = 1.0F - percent;
                int size = textCells.size();
                int pos = (int) (percent * size);
                pos = Math.min(size - 1, pos);
                pos = Math.max(pos, 0);
                setStartPos(pos);
                if (prevEnabled)
                    prevImpl();
            }
        }
    }

    /** @model.api */
    private void searchNext(String keyword) {
        searchFieldPane.setMessage(null);
        int selectionStart = textPane.getSelectionStart();
        int startPos = this.startPos + selectionStart + 1;
        int matchIndex = 0;
logger.log(Level.INFO, "search|next|" + startPos + " ~ " + textCells.size() + ", " + keyword);
        int keywordCodePointLength = keyword.codePointCount(0, keyword.length());
        for (int i = startPos; i < textCells.size(); i++) {
            SLetterCell cell = textCells.get(i);
            if (cell instanceof SLetterGlyphCell) {
                String m = ((SLetterGlyphCell) cell).getMain();
                if (m.length() > 1 && keyword.charAt(matchIndex) == m.charAt(0) && keyword.charAt(matchIndex + 1) == m.charAt(1)) {
logger.log(Level.INFO, "search|next|match surrogate: " + m);
                    matchIndex += 2;
                } else if (keyword.charAt(matchIndex) == m.charAt(0))
                    matchIndex++;
                else
                    matchIndex = 0;
                if (matchIndex == keyword.length()) {
                    int nextStart = (i - keywordCodePointLength) + 1;
                    logger.log(Level.INFO, "search|next| find at " + nextStart);
                    setSelection(nextStart, keywordCodePointLength);
                    return;
                }
            } else {
                matchIndex = 0;
            }
        }

        searchFieldPane.setMessage("文末まで検索しました");
    }

    /** @model.api */
    private void searchPrev(String keyword) {
        searchFieldPane.setMessage(null);
        int selectionStart = textPane.getSelectionStart();
        int startPos = (this.startPos + selectionStart) - 1;
        int matchIndex = 0;
logger.log(Level.INFO, "search|prev|" + startPos + " ~ 0, " + keyword);
        int keywordCodePointLength = keyword.codePointCount(0, keyword.length());
        for (int i = startPos; i >= 0; i--) {
            SLetterCell cell = textCells.get(i);
            if (cell instanceof SLetterGlyphCell) {
                String m = ((SLetterGlyphCell) cell).getMain();
                if (m.length() > 1 && keyword.charAt(keyword.length() - 2 - matchIndex) == m.charAt(0) && keyword.charAt(keyword.length() - 2 - matchIndex + 1) == m.charAt(1)) {
logger.log(Level.INFO, "search|prev|match surrogate: " + m);
                    matchIndex += 2;
                } else if (keyword.charAt(keyword.length() - 1 - matchIndex) == m.charAt(0))
                    matchIndex++;
                else
                    matchIndex = 0;
                if (matchIndex == keyword.length()) {
                    int prevStart = i;
                    logger.log(Level.INFO, "search|prev| find at " + prevStart);
                    setSelection(prevStart, keywordCodePointLength);
                    return;
                }
            } else {
                matchIndex = 0;
            }
        }

        searchFieldPane.setMessage("文頭まで検索しました");
    }

    private void setSelection(int startPos, int length) {
        setStartPos(startPos);
        prevImpl();
        do {
            int diff = (startPos - this.startPos) / 2;
            if (diff <= 0)
                break;
            setStartPos(this.startPos + diff);
        } while (endPos <= startPos + length);
        int selectionStart = (startPos - this.startPos) + 1;
        int selectionLength = Math.min(length, endPos - this.startPos);
        int selectionEnd = (selectionStart + selectionLength) - 1;
        textPane.setSelection(selectionStart, selectionEnd);
    }

    void setSearchEnable(boolean visible) {
        searchFieldPane.setVisible(visible);
        StringBuilder sb = new StringBuilder();
        for (SLetterCell cell : textPane.getSelectedCells()) {
            if (cell instanceof SLetterGlyphCell)
                sb.append(((SLetterGlyphCell) cell).getMain());
        }

        String selected = sb.toString();
        if (!selected.isEmpty())
            searchFieldPane.getTextField().setText(selected);
    }

    private void setupPageNumber() {
        if (isFirstPageLoaded)
            synchronized (textCells) {
                int size = textCells.size();
                setPageNumber(endPos, size);
            }
    }

    private void setPageNumber(int pos, int size) {
        float length = size;
        float end = pos;
        float percent = end / length;
        progress.setValue(Math.round(percent * 100F));
        progress.setToolTipText((isFirstPageLoaded ? "" : "Loading... ") +
                                new DecimalFormat("##0.0%").format(percent) + " ( " +
                                new DecimalFormat("###,###,###").format(pos) + " / " +
                                new DecimalFormat("###,###,###").format(size) +
                                (isAllPageLoaded ? " ALL " : " part ") + ")");
    }

    int getStartPos() {
        return startPos;
    }

    private void appendCell(SLetterCell cell) {
        if (cell == null)
            throw new IllegalArgumentException("cell null");
        synchronized (textCells) {
            int appendStartPos = firstStartPos;
            boolean isAdded = false;
            textCells.add(cell);
            int textSize = textCells.size();
            if (!isFirstPageLoaded)
                if (textSize < appendStartPos) {
                    startPos = textSize;
                    endPos = textSize;
                    setPageNumber(textSize, appendStartPos);
                } else if (textSize == appendStartPos) {
                    setStartPos(appendStartPos);
                } else {
                    isAdded = textPane.addCell(cell);
                    if (isAdded)
                        endPos = textSize;
                    else
                        isFirstPageLoaded = true;
                }
            if (isFirstPageLoaded) {
                setupButtonEnabled();
                setupPageNumber();
                repaint();
            }
        }
    }

    private void parseFinished() {
        synchronized (textCells) {
            isFirstPageLoaded = true;
            isAllPageLoaded = true;
            setupButtonEnabled();
            setupPageNumber();
            repaint();
        }
    }

    boolean isAllPageLoaded() {
        return isAllPageLoaded;
    }

    SLetterConstraint.ORIENTATION getOrientation() {
        return textPane.getOrientation();
    }

    boolean isProgressBarRevertOrientation() {
        SLetterConstraint.ORIENTATION orientation = getOrientation();
        return switch (orientation) {
            case TBRL -> true;
            case LRTB -> true;
            case RLTB -> true;
            case TBLR -> false;
        };
    }

    void setOrientation(SLetterConstraint.ORIENTATION orientation) {
        progress.setOrientation(orientation.isHorizonal() ? JProgressBar.VERTICAL : JProgressBar.HORIZONTAL);
        InputMap pageButtonInputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        for (KeyStroke keyStroke : pageButtonInputMap.keys()) {
            Object actionMapKey = pageButtonInputMap.get(keyStroke);
            if (nextAction.equals(actionMapKey)) {
                pageButtonInputMap.remove(keyStroke);
                continue;
            }
            if (prevAction.equals(actionMapKey))
                pageButtonInputMap.remove(keyStroke);
        }

        switch (orientation) {
        case TBRL:
            pageButtonInputMap.put(AozoraEnv.ShortCutKey.PAGE_NEXT_LEFT_SHORTCUT.getKeyStroke(), nextAction);
            pageButtonInputMap.put(AozoraEnv.ShortCutKey.PAGE_PREV_RIGHT_SHORTCUT.getKeyStroke(), prevAction);
            buttonPanel.add(progress, BorderLayout.CENTER);
            buttonPanel.add(searchFieldPane, BorderLayout.NORTH);
            add(buttonPanel, BorderLayout.SOUTH);
            break;
        case LRTB:
            pageButtonInputMap.put(AozoraEnv.ShortCutKey.PAGE_NEXT_DOWN_SHORTCUT.getKeyStroke(), nextAction);
            pageButtonInputMap.put(AozoraEnv.ShortCutKey.PAGE_PREV_UP_SHORTCUT.getKeyStroke(), prevAction);
            buttonPanel.add(progress, BorderLayout.CENTER);
            add(searchFieldPane, BorderLayout.SOUTH);
            add(buttonPanel, BorderLayout.EAST);
            break;
        case RLTB:
            pageButtonInputMap.put(AozoraEnv.ShortCutKey.PAGE_NEXT_DOWN_SHORTCUT.getKeyStroke(), nextAction);
            pageButtonInputMap.put(AozoraEnv.ShortCutKey.PAGE_PREV_UP_SHORTCUT.getKeyStroke(), prevAction);
            buttonPanel.add(progress, BorderLayout.CENTER);
            add(searchFieldPane, BorderLayout.SOUTH);
            add(buttonPanel, BorderLayout.WEST);
            break;
        case TBLR:
            pageButtonInputMap.put(AozoraEnv.ShortCutKey.PAGE_NEXT_RIGHT_SHORTCUT.getKeyStroke(), nextAction);
            pageButtonInputMap.put(AozoraEnv.ShortCutKey.PAGE_PREV_LEFT_SHORTCUT.getKeyStroke(), prevAction);
            buttonPanel.add(progress, BorderLayout.CENTER);
            buttonPanel.add(searchFieldPane, BorderLayout.NORTH);
            add(buttonPanel, BorderLayout.SOUTH);
            break;
        default:
            throw new UnsupportedOperationException("orientation " + orientation);
        }
        progress.revalidate();
        textPane.setOrientation(orientation);
        textPane.revalidate();
        repaint();
    }

    void close() {
        synchronized (textPane) {
            textPane.removeCellAll();
        }
    }
}
