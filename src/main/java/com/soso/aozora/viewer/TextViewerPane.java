/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.viewer;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

import javax.accessibility.AccessibleContext;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraDefaultPane;
import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.core.AozoraUtil;
import com.soso.aozora.data.AozoraCharacterUtil;
import com.soso.aozora.data.AozoraComment;
import com.soso.aozora.data.AozoraContentsParser;
import com.soso.aozora.data.AozoraContentsParserHandler;
import com.soso.aozora.data.AozoraWork;
import com.soso.sgui.SButton;
import com.soso.sgui.SGUIUtil;
import com.soso.sgui.SOptionPane;
import com.soso.sgui.letter.SLetterCell;
import com.soso.sgui.letter.SLetterCellDecorator;
import com.soso.sgui.letter.SLetterCellFactory;
import com.soso.sgui.letter.SLetterConstraint;
import com.soso.sgui.letter.SLetterGlyphCell;
import com.soso.sgui.letter.SLetterImageCell;
import com.soso.sgui.letter.SLetterPane;
import com.soso.sgui.letter.SLetterPaneObserver;
import com.soso.sgui.letter.SLetterPaneObserverHelper;


class TextViewerPane extends AozoraDefaultPane {

    static Logger logger = Logger.getLogger(TextViewerPane.class.getName());

    private static class NoFocusButton extends JButton {

        public boolean isFocusTraversable() {
            return false;
        }

        public void requestFocus() {
        }

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

    private class SearchFieldPane extends JPanel {

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
                public JMenuItem procudeMenuItem(Point p, SLetterCell[] cells, boolean isSelected) {
                    return searchItem;
                }

                final JMenuItem searchItem = new JMenuItem(new AbstractAction("文章内検索") {
                    public void actionPerformed(ActionEvent e) {
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
                textField.addCaretListener(new CaretListener() {
                    public void caretUpdate(CaretEvent e) {
                        resetButtonEnabled();
                    }
                });
                textField.addKeyListener(new KeyAdapter() {
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
                nextButton = new SButton();
                nextButton.setName("SearchFieldPane.nextButton");
                nextButton.setAction(new AbstractAction(AozoraEnv.ShortCutKey.SEARCH_IN_WORK_NEXT_SHORTCUT.getName(),
                                                        AozoraUtil.getIcon(AozoraEnv.Env.GO_LEFT_VIEW_ICON.getString())) {
                    public void actionPerformed(ActionEvent e) {
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
                prevButton = new SButton();
                prevButton.setName("SearchFieldPane.prevButton");
                prevButton.setAction(new AbstractAction(AozoraEnv.ShortCutKey.SEARCH_IN_WORK_PREV_SHORTCUT.getName(),
                                                        AozoraUtil.getIcon(AozoraEnv.Env.GO_RIGHT_VIEW_ICON.getString())) {
                    public void actionPerformed(ActionEvent e) {
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
                    public void actionPerformed(ActionEvent e) {
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

        private final List<SLetterCell> rb = new ArrayList<SLetterCell>();
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
            SLetterCell[] cells = rb.toArray(new SLetterCell[rb.size()]);
            if (cells.length != 0) {
                char[][] rtArray = TextViewerPane.splitRT(rt.toString().toCharArray(), cells.length);
                for (int i = 0; i < cells.length; i++)
                    ((SLetterGlyphCell) cells[i]).setRubys(rtArray[i]);

            }
            return cells;
        }

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

        private GaijiRubyBuilder gaijirb;

        private void appendCell(SLetterCell cell) {
            TextViewerPane.this.appendCell(cell);
        }

        private SLetterCellFactory getCellFactory() {
            return SLetterCellFactory.getInstance();
        }

        public void characters(String cdata) {
            if (gaijirb != null) {
                gaijirb.append(cdata);
                return;
            }
            for (char c : cdata.replaceAll("\\s", "").toCharArray()) {
                SLetterCell cell = getCellFactory().createGlyphCell(c);
                if (cell != null)
                    appendCell(cell);
            }
        }

        public void img(URL src, String alt, boolean isGaiji) {
            Image image = null;
            image = new ImageIcon(src).getImage();
            if (image == null) {
                Icon errorIcon = UIManager.getIcon("OptionPane.errorIcon");
                image = new BufferedImage(errorIcon.getIconWidth(), errorIcon.getIconHeight(), 1);
                image.getGraphics().fillRect(0, 0, errorIcon.getIconWidth(), errorIcon.getIconHeight());
                errorIcon.paintIcon(TextViewerPane.this, image.getGraphics(), 0, 0);
            }
            SLetterCell cell = getCellFactory().createImageCell(image, alt);
            if (gaijirb != null) {
                gaijirb.append(cell);
                return;
            }
            ((SLetterImageCell) cell).setMaximizable(!isGaiji);
            if (isGaiji) {
                if (AozoraCharacterUtil.isGaijiToRotate(src.getFile())) {
                    logger.info("Gaiji | rotate | " + src + (isCache() ? " | cache" : ""));
                    cell.addConstraint(com.soso.sgui.letter.SLetterConstraint.ROTATE.GENERALLY);
                } else {
                    logger.info("Gaiji | " + src + (isCache() ? " | cache" : ""));
                }
            } else {
                logger.info("Image | " + src + (isCache() ? " | cache" : ""));
            }
            if (cell != null)
                appendCell(cell);
        }

        public void newLine() {
            SLetterCell cell = getCellFactory().createGlyphCell('\n');
            if (cell != null)
                appendCell(cell);
        }

        public void otherElement(String element) {
            String lowerElement = element.toLowerCase();
            if (lowerElement.startsWith("ruby")) {
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
                       lowerElement.startsWith("tr"))
                newLine();
            else if (lowerElement.startsWith("li")) {
                newLine();
                characters("・");
            } else if (lowerElement.startsWith("/td"))
                characters("\t");
        }

        public void ruby(String rb, String rt) {
            if (gaijirb != null)
                throw new IllegalStateException("ruby[" + rb + "," + rt + "] apperes while building " + gaijirb);
            if (rb != null) {
                char[] rbcs = rb.toCharArray();
                char[] rtcs = rt == null ? null : rt.toCharArray();
                if (rbcs.length == 1) {
                    SLetterCell cell = getCellFactory().createGlyphCell(rbcs[0], rtcs);
                    if (cell != null)
                        appendCell(cell);
                } else if (rbcs.length == 0) {
                    SLetterCell cell = getCellFactory().createGlyphCell('　', rtcs);
                    if (cell != null)
                        appendCell(cell);
                } else {
                    char[][] rtArray = TextViewerPane.splitRT(rtcs, rbcs.length);
                    for (int rbi = 0; rbi < rbcs.length; rbi++) {
                        char rbc = rbcs[rbi];
                        char[] rtcsi = rtArray[rbi];
                        SLetterCell cell = getCellFactory().createGlyphCell(rbc, rtcsi);
                        if (cell != null)
                            appendCell(cell);
                    }
                }
            }
        }

        public void parseFinished() {
            TextViewerPane.this.parseFinished();
        }
    }

    private class ViewerPaneObserver extends SLetterPaneObserverHelper implements SLetterPaneObserver {

        public void colCountChanged(int oldColCount, int newColCount) {
            if (oldColCount < newColCount)
                tryAppend();
            else
                ensureEndPos();
        }

        public void rowCountChanged(int oldRowCount, int newRowCount) {
            logger.info("cached prev clear " + Arrays.toString(cachedPrevPosStack.toArray()));
            cachedPrevPosStack.clear();
            if (oldRowCount < newRowCount)
                tryAppend();
            else
                ensureEndPos();
        }

        public void rowSpaceChanged(int oldRowSpace, int newRowSpace) {
            getAzContext().getSettings().setRowSpace(newRowSpace);
        }

        public void fontRangeRatioChanged(float oldFontRangeRatio, float newFontRangeRatio) {
            getAzContext().getSettings().setFontRatio(newFontRangeRatio);
        }
    }

    private final AozoraWork work;
    private SLetterPane textPane;
    private List<SLetterCell> textCells = new ArrayList<SLetterCell>();
    private int startPos = 0;
    private int endPos = 0;
    private Stack<Integer> cachedPrevPosStack = new Stack<Integer>();
    private JPanel buttonPanel;
    private JButton nextButton;
    private JButton prevButton;
    private Icon goLeftIcon;
    private Icon goRightIcon;
    private Icon goUpIcon;
    private Icon goDownIcon;
    private JProgressBar progress;
    private boolean isFirstPageLoaded = false;
    private boolean isAllPageLoaded = false;
    private int firstStartPos;
    private SearchFieldPane searchFieldPane;
    private final boolean isCache;

    TextViewerPane(AozoraContext context, AozoraWork work, boolean isCache, int firstStartPos) {
        super(context);
        this.work = work;
        this.isCache = isCache;
        this.firstStartPos = firstStartPos;
        initGUI();
        setup();
    }

    private void initGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 0, 0, 0));
        setBackground(getAzContext().getDefaultBGColor());
        textPane = SLetterPane.newInstance(SLetterConstraint.ORIENTATION.TBRL);
        textPane.addObserver(new ViewerPaneObserver());
        textPane.setBackground(getAzContext().getSettings().getBackground());
        textPane.setForeground(getAzContext().getSettings().getForeground());
        textPane.setRowColCountChangable(true);
        textPane.setFontSizeChangable(true);
        textPane.setLetterBorderRendarer(null);
        textPane.setFont(getAzContext().getSettings().getFont());
        textPane.setRowSpace(getAzContext().getSettings().getRowSpace());
        textPane.setFontRangeRatio(getAzContext().getSettings().getFontRatio());
        add(textPane, BorderLayout.CENTER);
        goLeftIcon = AozoraUtil.getIcon(AozoraEnv.Env.GO_LEFT_ICON.getString());
        goRightIcon = AozoraUtil.getIcon(AozoraEnv.Env.GO_RIGHT_ICON.getString());
        goUpIcon = AozoraUtil.getIcon(AozoraEnv.Env.GO_UP_ICON.getString());
        goDownIcon = AozoraUtil.getIcon(AozoraEnv.Env.GO_DOWN_ICON.getString());
        nextButton = new SButton();
        nextButton.setAction(new AbstractAction(null, goLeftIcon) {
            public void actionPerformed(ActionEvent e) {
                next();
            }
        });
        nextButton.setEnabled(false);
        nextButton.setName("TextViewerPane.nextButton");
        AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_IN_FOCUSED_WINDOW, AozoraEnv.ShortCutKey.PAGE_NEXT_LEFT_SHORTCUT.getKeyStroke(), nextButton);
        nextButton.setToolTipText(AozoraEnv.ShortCutKey.PAGE_NEXT_LEFT_SHORTCUT.getNameWithHelpTitle());
        prevButton = new SButton();
        prevButton.setAction(new AbstractAction(null, goRightIcon) {
            public void actionPerformed(ActionEvent e) {
                prev();
            }
        });
        prevButton.setEnabled(false);
        prevButton.setName("TextViewerPane.prevButton");
        AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_IN_FOCUSED_WINDOW, AozoraEnv.ShortCutKey.PAGE_PREV_RIGHT_SHORTCUT.getKeyStroke(), prevButton);
        prevButton.setToolTipText(AozoraEnv.ShortCutKey.PAGE_PREV_RIGHT_SHORTCUT.getNameWithHelpTitle());
        progress = new JProgressBar(0) {
            protected void paintComponent(Graphics g) {
                if (isProgressBarRevertOrientation()) {
                    double x = getWidth() * 0.5d;
                    double y = getHeight() * 0.5d;
                    ((Graphics2D) g).rotate(Math.PI, x, y);
                }
                super.paintComponent(g);
            }
        };
        progress.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                setPageByProgressClick(e.getX(), e.getY());
            }
        });
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout(3, 3));
        buttonPanel.add(nextButton, BorderLayout.WEST);
        buttonPanel.add(progress, BorderLayout.CENTER);
        buttonPanel.add(prevButton, BorderLayout.EAST);
        add(buttonPanel, BorderLayout.SOUTH);
        searchFieldPane = new SearchFieldPane();
        searchFieldPane.setVisible(false);
        buttonPanel.add(searchFieldPane, BorderLayout.NORTH);
        textPane.addMenuItemProducer(searchFieldPane.createSearchMenuItemProducer());
    }

    private void setup() {
        try {
            new Thread(new Runnable() {
                public void run() {
                    setupAsynchronous();
                }
            }, "AozoraContentsParser-Thread").start();
        } catch (Exception e) {
            disposeWithError(e);
        }
    }

    private void setupAsynchronous() {
        try {
            AozoraContentsParserHandler handler = new ContentsHandler();
            AozoraContentsParser parser = new AozoraContentsParser(getAzContext(), handler);
            URL workURL = new URL(getWork().getContentURL());
            parser.parse(workURL);
        } catch (Exception e) {
            disposeWithError(e);
        }
    }

    void disposeWithError(final Throwable t) {
        try {
            t.printStackTrace(System.err);
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    SOptionPane.showInternalErrorDialog(TextViewerPane.this, "作品を表示できません。", t, false);
                }
            });
            JInternalFrame parentFrame = SGUIUtil.getParentInstanceOf(this, JInternalFrame.class);
            if (parentFrame != null)
                parentFrame.dispose();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public void paint(Graphics g) {
        if (!isFirstPageLoaded && (g instanceof Graphics2D))
            ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        super.paint(g);
    }

    void setStartPosByComment(final AozoraComment comment) {
        if (!isFirstPageLoaded) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setStartPosByComment(comment);
                }
            });
            return;
        }
        setSelection(comment.getPosition(), comment.getLength());
        SLetterCell cell = textCells.get(comment.getPosition());
        for (SLetterCellDecorator decorator : cell.getDecorators()) {
            if ((decorator instanceof AozoraCommentBalloonDecorator) &&
                ((AozoraCommentBalloonDecorator) decorator).getComment() == comment) {
                final AozoraCommentBalloonDecorator theDecorator = (AozoraCommentBalloonDecorator) decorator;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                theDecorator.moveToFront();
                            }
                        });
                    }
                });
            }
        }
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
                e.printStackTrace(System.err);
            }
        }
    }

    private void setupButtonEnabled() {
        if (isFirstPageLoaded)
            synchronized (textCells) {
                prevButton.setEnabled(startPos > 0);
                nextButton.setEnabled(endPos < textCells.size() - 1);
            }
    }

    private void next() {
        synchronized (nextButton) {
            if (nextButton.isEnabled()) {
                nextButton.setEnabled(false);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        nextImpl();
                    }
                });
            }
        }
    }

    private void nextImpl() {
        int lastStartPos = startPos;
        logger.info("next," + Arrays.toString(cachedPrevPosStack.toArray()) + "," + endPos);
        setStartPos(endPos);
        cachedPrevPosStack.push(lastStartPos);
        setupButtonEnabled();
        setupPageNumber();
        if (nextButton.isEnabled())
            nextButton.requestFocusInWindow();
        else if (prevButton.isEnabled())
            prevButton.requestFocusInWindow();
    }

    private void prev() {
        synchronized (prevButton) {
            if (prevButton.isEnabled()) {
                prevButton.setEnabled(false);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        prevImpl();
                    }
                });
            }
        }
    }

    private void prevImpl() {
        int lastStartPos = startPos;
        List<Integer> tryedStartPosList = new ArrayList<Integer>();
        StringBuilder log = new StringBuilder().append("prev");
        if (cachedPrevPosStack.size() != 0) {
            log.append(",cached," + Arrays.toString(cachedPrevPosStack.toArray()));
            int cachedPrevPos = cachedPrevPosStack.pop();
            setStartPos(cachedPrevPos);
            tryedStartPosList.add(cachedPrevPos);
        }
        int diff;
        while ((diff = endPos - lastStartPos) != 0) {
            log.append("," + startPos);
            int tryStartPos = startPos - diff;
            if (tryStartPos < 0) {
                setStartPos(0);
                break;
            }
            if (textCells.get(tryStartPos).isConstraintSet(SLetterConstraint.BREAK.BACK_IF_LINE_HEAD))
                tryStartPos++;
            if (tryedStartPosList.contains(tryStartPos))
                break;
            setStartPos(tryStartPos);
            tryedStartPosList.add(tryStartPos);
        }
        for (int pos = startPos - 1; endPos > lastStartPos && pos >= 0; pos--) {
            setStartPos(pos);
            log.append(">").append(startPos);
        }

        for (int pos = startPos + 1; endPos < lastStartPos && pos <= textCells.size() - 1; pos++) {
            setStartPos(pos);
            log.append("<").append(startPos);
        }

        log.append("|lastStart=" + lastStartPos + "|thisEnd=" + endPos);
        logger.info(log.toString());
        setupButtonEnabled();
        setupPageNumber();
        if (prevButton.isEnabled())
            prevButton.requestFocusInWindow();
        else if (nextButton.isEnabled())
            nextButton.requestFocusInWindow();
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
                if (prevButton.isEnabled())
                    prevImpl();
            }
        }
    }

    private void searchNext(String keyword) {
        logger.info("search|next|" + keyword);
        searchFieldPane.setMessage(null);
        int selectionStart = textPane.getSelectionStart();
        int startPos = this.startPos + selectionStart + 1;
        int matchIndex = 0;
        for (int i = startPos; i < textCells.size(); i++) {
            SLetterCell cell = textCells.get(i);
            if (cell instanceof SLetterGlyphCell) {
                char c = ((SLetterGlyphCell) cell).getMain();
                if (keyword.charAt(matchIndex) == c)
                    matchIndex++;
                else
                    matchIndex = 0;
                if (matchIndex == keyword.length()) {
                    int nextStart = (i - keyword.length()) + 1;
                    logger.info("search|next| find at " + nextStart);
                    setSelection(nextStart, keyword.length());
                    return;
                }
            } else {
                matchIndex = 0;
            }
        }

        searchFieldPane.setMessage("文末まで検索しました");
    }

    private void searchPrev(String keyword) {
        logger.info("search|prev|" + keyword);
        searchFieldPane.setMessage(null);
        int selectionStart = textPane.getSelectionStart();
        int startPos = (this.startPos + selectionStart) - 1;
        int matchIndex = 0;
        for (int i = startPos; i >= 0; i--) {
            SLetterCell cell = textCells.get(i);
            if (cell instanceof SLetterGlyphCell) {
                char c = ((SLetterGlyphCell) cell).getMain();
                if (keyword.charAt(keyword.length() - 1 - matchIndex) == c)
                    matchIndex++;
                else
                    matchIndex = 0;
                if (matchIndex == keyword.length()) {
                    int prevStart = i;
                    logger.info("search|prev| find at " + prevStart);
                    setSelection(prevStart, keyword.length());
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

    private AozoraWork getWork() {
        return work;
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

    boolean isCache() {
        return isCache;
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
            if (nextButton.getName().equals(actionMapKey)) {
                pageButtonInputMap.remove(keyStroke);
                continue;
            }
            if (prevButton.getName().equals(actionMapKey))
                pageButtonInputMap.remove(keyStroke);
        }

        switch (orientation) {
        case TBRL:
            nextButton.setIcon(goLeftIcon);
            prevButton.setIcon(goRightIcon);
            nextButton.setToolTipText(AozoraEnv.ShortCutKey.PAGE_NEXT_LEFT_SHORTCUT.getNameWithHelpTitle());
            prevButton.setToolTipText(AozoraEnv.ShortCutKey.PAGE_PREV_RIGHT_SHORTCUT.getNameWithHelpTitle());
            pageButtonInputMap.put(AozoraEnv.ShortCutKey.PAGE_NEXT_LEFT_SHORTCUT.getKeyStroke(), nextButton.getName());
            pageButtonInputMap.put(AozoraEnv.ShortCutKey.PAGE_PREV_RIGHT_SHORTCUT.getKeyStroke(), prevButton.getName());
            buttonPanel.add(nextButton, BorderLayout.WEST);
            buttonPanel.add(progress, BorderLayout.CENTER);
            buttonPanel.add(prevButton, BorderLayout.EAST);
            buttonPanel.add(searchFieldPane, BorderLayout.NORTH);
            add(buttonPanel, BorderLayout.SOUTH);
            break;
        case LRTB:
            nextButton.setIcon(goDownIcon);
            prevButton.setIcon(goUpIcon);
            nextButton.setToolTipText(AozoraEnv.ShortCutKey.PAGE_NEXT_DOWN_SHORTCUT.getNameWithHelpTitle());
            prevButton.setToolTipText(AozoraEnv.ShortCutKey.PAGE_PREV_UP_SHORTCUT.getNameWithHelpTitle());
            pageButtonInputMap.put(AozoraEnv.ShortCutKey.PAGE_NEXT_DOWN_SHORTCUT.getKeyStroke(), nextButton.getName());
            pageButtonInputMap.put(AozoraEnv.ShortCutKey.PAGE_PREV_UP_SHORTCUT.getKeyStroke(), prevButton.getName());
            buttonPanel.add(nextButton, BorderLayout.SOUTH);
            buttonPanel.add(progress, BorderLayout.CENTER);
            buttonPanel.add(prevButton, BorderLayout.NORTH);
            add(searchFieldPane, BorderLayout.SOUTH);
            add(buttonPanel, BorderLayout.EAST);
            break;
        case RLTB:
            nextButton.setIcon(goDownIcon);
            prevButton.setIcon(goUpIcon);
            nextButton.setToolTipText(AozoraEnv.ShortCutKey.PAGE_NEXT_DOWN_SHORTCUT.getNameWithHelpTitle());
            prevButton.setToolTipText(AozoraEnv.ShortCutKey.PAGE_PREV_UP_SHORTCUT.getNameWithHelpTitle());
            pageButtonInputMap.put(AozoraEnv.ShortCutKey.PAGE_NEXT_DOWN_SHORTCUT.getKeyStroke(), nextButton.getName());
            pageButtonInputMap.put(AozoraEnv.ShortCutKey.PAGE_PREV_UP_SHORTCUT.getKeyStroke(), prevButton.getName());
            buttonPanel.add(nextButton, BorderLayout.SOUTH);
            buttonPanel.add(progress, BorderLayout.CENTER);
            buttonPanel.add(prevButton, BorderLayout.NORTH);
            add(searchFieldPane, BorderLayout.SOUTH);
            add(buttonPanel, BorderLayout.WEST);
            break;
        case TBLR:
            nextButton.setIcon(goRightIcon);
            prevButton.setIcon(goLeftIcon);
            nextButton.setToolTipText(AozoraEnv.ShortCutKey.PAGE_NEXT_RIGHT_SHORTCUT.getNameWithHelpTitle());
            prevButton.setToolTipText(AozoraEnv.ShortCutKey.PAGE_PREV_LEFT_SHORTCUT.getNameWithHelpTitle());
            pageButtonInputMap.put(AozoraEnv.ShortCutKey.PAGE_NEXT_RIGHT_SHORTCUT.getKeyStroke(), nextButton.getName());
            pageButtonInputMap.put(AozoraEnv.ShortCutKey.PAGE_PREV_LEFT_SHORTCUT.getKeyStroke(), prevButton.getName());
            buttonPanel.add(nextButton, BorderLayout.EAST);
            buttonPanel.add(progress, BorderLayout.CENTER);
            buttonPanel.add(prevButton, BorderLayout.WEST);
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

    private static char[][] splitRT(char[] rtcs, int rblen) {
        char[][] rtArray = new char[rblen][];
        int rtiS = 0;
        int rtiE = 0;
        int rtiP = rtcs.length / rblen;
        int rtiD = rtcs.length % rblen;
        for (int rbi = 0; rbi < rblen; rbi++) {
            rtiS = rtiE;
            rtiE = rtiS + rtiP + (rbi != 0 ? 0 : rtiD);
            int rtlen = rtiE - rtiS;
            char[] rtcsi = new char[rtlen];
            System.arraycopy(rtcs, rtiS, rtcsi, 0, rtlen);
            rtArray[rbi] = rtcsi;
        }

        return rtArray;
    }
}
