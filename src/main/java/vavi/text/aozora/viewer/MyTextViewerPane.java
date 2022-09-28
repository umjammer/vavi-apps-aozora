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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.accessibility.AccessibleContext;
import javax.swing.AbstractAction;
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
import com.soso.sgui.letter.SLetterPane;
import com.soso.sgui.letter.SLetterPaneObserver;
import com.soso.sgui.letter.SLetterPaneObserverHelper;
import vavi.text.UnicodeUtil;
import vavi.util.Debug;

import static javax.swing.SwingUtilities.invokeAndWait;


/**
 * based on "com.soso.aozora.viewer.TextViewerPane"
 *
 * TODO
 *  - half letter strings are separated into 2 letters, spacing is suck
 *  - half digit 2 letters pair must not be rotated
 */
public class MyTextViewerPane extends JPanel {

    static Logger logger = Logger.getLogger(MyTextViewerPane.class.getName());

    private class SearchFieldPane extends JPanel {

        private class NoFocusButton extends JButton {

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
            if (word != null && word.length() != 0)
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
            if (cells.length != 0) {
                char[][] rtArray = splitRT(rt.toString().toCharArray(), cells.length);
                for (int i = 0; i < cells.length; i++)
                    ((SLetterGlyphCell) cells[i]).setRubys(rtArray[i]);

            }
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

    private class ContentsHandler implements AozoraContentsParserHandler {

        boolean kaeriten;
        boolean notes;
        boolean alternative;
        SLetterGlyphCell rubyAlternative;

        private GaijiRubyBuilder gaijirb;

        private void appendCell(SLetterCell cell) {
            MyTextViewerPane.this.appendCell(cell);
        }

        private SLetterCellFactory cellFactory = SLetterCellFactory.getInstance();

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
            if (kaeriten) {
Debug.println(Level.FINER, "characters|レ点: " + cdata);
                // TODO too large
//                for (char c : cdata.toCharArray()) {
//                    SLetterCell cell = getCellFactory().createKaeritenGlyphCell(c);
//                    appendCell(cell);
//                }
                // bad usage, but beautiful
                SLetterCell cell = cellFactory.createGlyphCell('　', cdata.toCharArray());
                appendCell(cell);

                kaeriten = false;
                return;
            }
            if (notes) {
                if (cdata.startsWith("［＃")) {
                    if (alternative) {
                        String a = parseUnicode(cdata);
                        if (a != null) {
                            char c = (char) Integer.parseInt(a, 16);
Debug.printf("characters|[notes:※:U+%s]: %c, %s", a, c, cdata);
                            SLetterCell cell = cellFactory.createGlyphCell(c);
                            appendCell(cell);
                        } else {
Debug.printf(Level.WARNING, "characters|[notes:※:N/A]: %s", cdata);
                        }
                        alternative = false;
                    } else if (rubyAlternative != null) {
                        String a = parseUnicode(cdata);
                        if (a != null) {
                            char c = (char) Integer.parseInt(a, 16);
Debug.printf("characters|[notes:ruby※:U+%s]: %c, %s", a, c, cdata);
                            rubyAlternative.setMain(c);
                        } else {
Debug.printf(Level.WARNING, "characters|[notes:ruby※:N/A]: %s", cdata);
                        }
                        rubyAlternative = null;
                    } else {
Debug.println("characters|[notes:#]: " + cdata);
                    }
                } else {
Debug.println("characters|[notes]: " + cdata);
                }
                notes = false;
                return;
            }

            if (gaijirb != null) {
                gaijirb.append(cdata);
                return;
            }
            // https://linuxtut.com/en/bdc62f95f6d342705001/
            char[] ca = cdata.trim().toCharArray();
            for (int i = 0; i < ca.length; i++) {
                if (ca[i] == '※') {
Debug.println(Level.FINER, "characters|" + "※※※ NOTED ※※※");
                    alternative = true;
                } else {
                    if (Character.isHighSurrogate(ca[i]) && Character.isSurrogatePair(ca[i], ca[i + 1])) {
Debug.printf(Level.FINE, "surrogate pair: %s", new String(new int[] {cdata.codePointAt(i)}, 0, 1));
                        SLetterCell cell = cellFactory.createGlyphCell(cdata.codePointAt(i), new char[0], null);
                        appendCell(cell);
                        i++;
                    } else {
                        // TODO old-new on/off flag
                        char c = UnicodeUtil.toNew(String.valueOf(ca[i])).charAt(0);
                        SLetterCell cell = cellFactory.createGlyphCell(c);
                        appendCell(cell);
                    }
                }
            }
        }

        @Override
        public void img(URL src, String alt, boolean isGaiji) {
Debug.println(Level.FINER, "srcAttr: " + src + ", " + alt + ", " + isGaiji);
            if (src.toString().matches(".*(\\d)-(\\d{2})-(\\d{2}).*")) {
                String[] prc = src.toString().replaceFirst(".*(\\d)-(\\d{2})-(\\d{2}).*", "$1,$2,$3").split(",");

                String unicode = UnicodeUtil.toUnicodeChar(Integer.parseInt(prc[0]), Integer.parseInt(prc[1]), Integer.parseInt(prc[2]));
                // TODO why replaceFirst("[※\\(\\)]", "") doesn't work???
                String a = alt.replaceFirst("※", "").replace("(", "").replace(")", "").trim();
                if (unicode != null) {
Debug.printf(Level.FINE, "image: %s -> %s, %s%s", Arrays.toString(prc), unicode, a, unicode.length() > 1 ? ", surrogate pare" : "");
                    characters(unicode);
                    return;
                } else {
Debug.printf("image: %s -> not found: %s", Arrays.toString(prc), a);
                }
            }

            Image image;
            image = new ImageIcon(src).getImage();
            if (image == null) {
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
            if (isGaiji) {
                if (AozoraCharacterUtil.isGaijiToRotate(src.getFile())) {
                    logger.fine("Gaiji | rotate | " + src);
                    cell.addConstraint(SLetterConstraint.ROTATE.GENERALLY);
                } else {
                    logger.fine("Gaiji | " + src);
                }
            } else {
                logger.info("Image | " + src);
            }
            appendCell(cell);
        }

        @Override
        public void newLine() {
            SLetterCell cell = cellFactory.createGlyphCell('\n');
            appendCell(cell);
        }

        @Override
        public void otherElement(String element) {
            String lowerElement = element.toLowerCase();
            if (lowerElement.startsWith("sub class=\"kaeriten\"")) {
                kaeriten = true;
            } else if (lowerElement.startsWith("span class=\"notes\"")) {
                notes = true;
            } else if (lowerElement.startsWith("ruby")) {
                if (gaijirb != null)
                    throw new IllegalStateException("another rb start while building " + gaijirb);
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
                if (gaijirb != null) {
                    for (SLetterCell cell : gaijirb.getResult()) {
                        appendCell(cell);
                    }
                    gaijirb = null;
                }
            } else if (lowerElement.startsWith("div") ||
                       lowerElement.startsWith("/div") ||
                       lowerElement.startsWith("p") ||
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
            }
        }

        /**
         * @param rb target text
         * @param rt ruby text
         */
        @Override
        public void ruby(String rb, String rt) {
            if (gaijirb != null)
                throw new IllegalStateException("ruby[" + rb + "," + rt + "] appears while building " + gaijirb);
Debug.println(Level.FINER, rb + ", " + rt);
            if (rb != null) {
                char[] textChars = rb.toCharArray();
                char[] rubyChars = rt == null ? null : rt.toCharArray();
                if (textChars.length == 1) {
                    SLetterCell cell = cellFactory.createGlyphCell(textChars[0], rubyChars);
                    if (cell != null) {
                        appendCell(cell);
                        if (textChars[0] == '※') {
                            rubyAlternative = (SLetterGlyphCell) cell;
Debug.println("ruby: alternative: " + rubyAlternative);
                        }
                    }
                } else if (textChars.length == 0) {
                    SLetterCell cell = cellFactory.createGlyphCell('　', rubyChars);
                    if (cell != null)
                        appendCell(cell);
                } else {
                    char[][] rubyAssigns = splitRT(rubyChars, textChars.length);
                    for (int i = 0; i < textChars.length; i++) {
                        char textChar = textChars[i];
                        char[] rubyAssign = rubyAssigns[i];
if (textChar == '※') {
 Debug.println("ruby: unhandled: ※");
}
                        SLetterCell cell = cellFactory.createGlyphCell(textChar, rubyAssign);
                        appendCell(cell);
                    }
                }
            }
        }

        @Override
        public void parseFinished() {
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
            logger.info("cached prev clear " + Arrays.toString(cachedPrevPosStack.toArray()));
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
        Color defaultBGColor = new Color(0xFFFFFF);
        public Color getDefaultBGColor() {
            return defaultBGColor;
        }
        Color background = new Color(0xFFFFFF);
        public Color getBackground() {
            return background;
        }
        Color foreground = new Color(0x000000);
        public Color getForeground() {
            return foreground;
        }
        int fontSize = 32;
        Font font = new Font("Hiragino Mincho ProN", Font.PLAIN, fontSize);
        public Font getFont() {
            return font;
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

    Settings settings = new Settings();

    private SLetterPane textPane;
    private final List<SLetterCell> textCells = new ArrayList<>();
    private int startPos = 0;
    private int endPos = 0;
    private Stack<Integer> cachedPrevPosStack = new Stack<>();
    private JPanel buttonPanel;
    String nextAction = "TextViewerPane.nextButton";
    String prevAction = "TextViewerPane.prevButton";
    boolean nextEnabled;
    boolean prevEnabled;
    private Icon goLeftIcon;
    private Icon goRightIcon;
    private Icon goUpIcon;
    private Icon goDownIcon;
    private JProgressBar progress;
    private boolean isFirstPageLoaded = false;
    private boolean isAllPageLoaded = false;
    private int firstStartPos;
    private SearchFieldPane searchFieldPane;

    URI uri;
    Reader reader;
    URL base;

    public MyTextViewerPane(URI uri, int firstStartPos) {
        this.uri = uri;
        this.firstStartPos = firstStartPos;
        initGUI();
        setup();
    }

    public MyTextViewerPane(Reader reader, URL base, int firstStartPos) {
        this.reader = reader;
        this.base = base;
        this.firstStartPos = firstStartPos;
        initGUI();
        setup();
    }

    private void initGUI() {
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(40, 20, 0, 20));
        setBackground(settings.getDefaultBGColor());
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
        add(textPane, BorderLayout.CENTER);
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
//Debug.println("mouseClicked: next");
                    next();
                } else if (r.contains(e.getPoint())){
//Debug.println("mouseClicked: prev");
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
            t.printStackTrace();
            invokeAndWait(() -> JOptionPane.showInternalMessageDialog(MyTextViewerPane.this,
                    String.join("\n", Arrays.toString(t.getStackTrace()).split(",")),
                    "作品を表示できません。", JOptionPane.ERROR_MESSAGE));
        } catch (Exception e) {
            e.printStackTrace();
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
                        endPos = pos;
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
                e.printStackTrace();
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
logger.info("next," + Arrays.toString(cachedPrevPosStack.toArray()) + "," + endPos);
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
        if (cachedPrevPosStack.size() != 0) {
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
        logger.info(log.toString());
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
logger.info("search|next|" + startPos + " ~ " + textCells.size() + ", " + keyword);
        int keywordCodePointLength = keyword.codePointCount(0, keyword.toCharArray().length);
        for (int i = startPos; i < textCells.size(); i++) {
            SLetterCell cell = textCells.get(i);
            if (cell instanceof SLetterGlyphCell) {
                String m = ((SLetterGlyphCell) cell).getMain();
                if (m.length() > 1 && keyword.charAt(matchIndex) == m.charAt(0) && keyword.charAt(matchIndex + 1) == m.charAt(1)) {
logger.fine("search|next|match surrogate: " + m);
                    matchIndex += 2;
                } else if (keyword.charAt(matchIndex) == m.charAt(0))
                    matchIndex++;
                else
                    matchIndex = 0;
                if (matchIndex == keyword.length()) {
                    int nextStart = (i - keywordCodePointLength) + 1;
                    logger.info("search|next| find at " + nextStart);
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
logger.info("search|prev|" + startPos + " ~ 0, " + keyword);
        int keywordCodePointLength = keyword.codePointCount(0, keyword.toCharArray().length);
        for (int i = startPos; i >= 0; i--) {
            SLetterCell cell = textCells.get(i);
            if (cell instanceof SLetterGlyphCell) {
                String m = ((SLetterGlyphCell) cell).getMain();
                if (m.length() > 1 && keyword.charAt(keyword.length() - 2 - matchIndex) == m.charAt(0) && keyword.charAt(keyword.length() - 2 - matchIndex + 1) == m.charAt(1)) {
logger.fine("search|prev|match surrogate: " + m);
                    matchIndex += 2;
                } else if (keyword.charAt(keyword.length() - 1 - matchIndex) == m.charAt(0))
                    matchIndex++;
                else
                    matchIndex = 0;
                if (matchIndex == keyword.length()) {
                    int prevStart = i;
                    logger.info("search|prev| find at " + prevStart);
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
        if (selected.length() > 0)
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
        switch (orientation) {
        case TBRL:
            return true;
        case LRTB:
            return true;
        case RLTB:
            return true;
        case TBLR:
            return false;
        }
        throw new UnsupportedOperationException("orientation " + orientation);
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

    /**
     * dividing ruby
     *
     * TODO not a good separation (but better than the original)
     */
    private static char[][] splitRT(char[] rubyChars, int textCharsLength) {
        char[][] rubyCharsPerTextChars = new char[textCharsLength][];
        int offset = 0;
        int position = 0;
        int quotient = rubyChars.length / textCharsLength;
        int remainder = rubyChars.length % textCharsLength;
        for (int i = 0; i < textCharsLength; i++) {
            offset = position;
            position = offset + quotient + (remainder-- > 0 ? 1 : 0);
            int rubyLength = position - offset;
            char[] rubyCharsForAText = new char[rubyLength];
            System.arraycopy(rubyChars, offset, rubyCharsForAText, 0, rubyLength);
            rubyCharsPerTextChars[i] = rubyCharsForAText;
        }

        return rubyCharsPerTextChars;
    }
}
