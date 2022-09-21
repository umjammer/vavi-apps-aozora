/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraContentPane;
import com.soso.aozora.core.AozoraDefaultPane;
import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.data.AozoraAuthor;
import com.soso.aozora.data.AozoraAuthorParserHandler;
import com.soso.aozora.data.AozoraWork;
import com.soso.aozora.data.AozoraWorkParserHandler;
import com.soso.aozora.event.AozoraListenerAdapter;
import com.soso.sgui.SButton;
import com.soso.sgui.SFlowLayout;
import com.soso.sgui.SGUIUtil;


public class AozoraListPane extends AozoraDefaultPane implements AozoraListMediator {

    static Logger logger = Logger.getLogger(AozoraListPane.class.getName());

    static class ScrollableListMinimalPane extends JPanel implements Scrollable {

        public Dimension getPreferredScrollableViewportSize() {
            return ((JViewport) getParent()).getExtentSize();
        }

        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 100;
        }

        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        public boolean getScrollableTracksViewportWidth() {
            return false;
        }

        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 100;
        }
    }

    public AozoraListPane(AozoraContext context) {
        super(context);
        initGUI();
    }

    private void initGUI() {
        selectionManager = new AozoraTreeSelectionManagerImpl();
        authorNodeList = new ArrayList<AozoraAuthorNode>();
        setLayout(new BorderLayout());
        scroll = new JScrollPane();
        add(scroll, BorderLayout.CENTER);
        listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
        JPanel listMinimalPane = new JPanel();
        listMinimalPane.setLayout(new BorderLayout());
        listMinimalPane.add(listPane, BorderLayout.NORTH);
        scroll.setViewportView(listMinimalPane);
        scroll.getVerticalScrollBar().setUnitIncrement(50);
        add(createSearchPane(), BorderLayout.NORTH);
        repaint();
        getAzContext().getListenerManager().add(new AozoraListenerAdapter() {
            public void lineModeChanged(AozoraEnv.LineMode lineMode) {
                setListEnabled(lineMode);
            }
        });
    }

    private void setListEnabled(AozoraEnv.LineMode lineMode) {
        boolean isConnectable = lineMode.isConnectable();
        enableInputMethods(isConnectable);
        if (isConnectable)
            initData();
        else
            stopData();
    }

    private JPanel createSearchPane() {
        JPanel searchPane = new JPanel();
        searchPane.setLayout(new BorderLayout(1, 1));
        searchField = new JTextField();
        searchField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                case KeyEvent.VK_ENTER:
                    new Thread(new Runnable() {
                        public void run() {
                            setSearchEnabled(false);
                            setSearchResult(true);
                            search();
                            setSearchEnabled(true);
                        }
                    }, "AozoraSearch").start();
                    break;
                }
            }
        });
        searchPane.add(searchField, BorderLayout.NORTH);
        JPanel buttonPane = new JPanel();
        SFlowLayout layout = new SFlowLayout();
        layout.setAlign(SFlowLayout.CENTER);
        layout.setAxis(SFlowLayout.HORIZONTAL);
        layout.setMargin(0);
        layout.setSpace(0);
        layout.setNowrap(true);
        buttonPane.setLayout(layout);
        searchPane.add(buttonPane, BorderLayout.CENTER);
        searchAllButton = new SButton();
        searchAllButton.setAction(new AbstractAction("全検索") {
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    public void run() {
                        setSearchStop(false);
                        setSearchEnabled(false);
                        loadAllWorks();
                        setSearchResult(true);
                        searchAll();
                        setSearchEnabled(true);
                    }
                }, "AozoraAllSearch").start();
            }
        });
        buttonPane.add(searchAllButton);
        searchAddButton = new SButton();
        searchAddButton.setAction(new AbstractAction("絞り込み") {
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    public void run() {
                        setSearchStop(false);
                        setSearchEnabled(false);
                        setSearchResult(true);
                        search();
                        setSearchEnabled(true);
                    }
                }, "AozoraSearch").start();
            }
        });
        buttonPane.add(searchAddButton);
        searchResetButton = new SButton();
        searchResetButton.setAction(new AbstractAction("再表示") {
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    public void run() {
                        setSearchStop(false);
                        setSearchEnabled(false);
                        setSearchResult(false);
                        searchReset();
                        setSearchEnabled(true);
                    }
                }, "AozoraResetSearch").start();
            }
        });
        buttonPane.add(searchResetButton);
        searchStopButton = new SButton();
        searchStopButton.setAction(new AbstractAction("停止") {
            public void actionPerformed(ActionEvent e) {
                setSearchStop(true);
            }
        });
        buttonPane.add(searchStopButton);
        JPanel progressPane = new JPanel();
        progressPane.setOpaque(false);
        progressLabel = new JLabel();
        SGUIUtil.setSizeALL(progressLabel, new Dimension(50, 20));
        progressPane.setLayout(new BorderLayout());
        progressPane.add(progressLabel, BorderLayout.WEST);
        progressBar = new JProgressBar();
        progressPane.add(progressBar, BorderLayout.CENTER);
        searchPane.add(progressPane, BorderLayout.SOUTH);
        return searchPane;
    }

    public void setSearchEnabled(boolean enabled) {
        setSearchEnabled(enabled, enabled);
    }

    public void setSearchEnabled(boolean visible, boolean enabled) {
        searchField.setEditable(enabled);
        searchAllButton.setVisible(visible);
        searchAllButton.setEnabled(enabled);
        searchAddButton.setVisible(visible);
        searchAddButton.setEnabled(enabled);
        searchResetButton.setVisible(visible);
        searchResetButton.setEnabled(enabled);
        searchStopButton.setVisible(!visible);
        searchStopButton.setEnabled(!enabled);
        progressLabel.setEnabled(!enabled);
        progressBar.setEnabled(!enabled);
    }

    private void stopData() {
        if (loader != null)
            loader.stop();
        loader = null;
        setSearchEnabled(true);
    }

    private void initData() {
        synchronized (authorNodeList) {
            authorNodeList.clear();
            listPane.removeAll();
            listPane.revalidate();
        }
        setSearchEnabled(true, false);
        while (index == null)
            try {
                index = AozoraIndex.getIndex();
            } catch (Exception e) {
                e.printStackTrace();
            }
        new Thread(new Runnable() {
            public void run() {
                progressLabel.setText("初期化");
                progressBar.setMaximum(100);
                progressBar.setMinimum(0);
                progressBar.setValue(progressBar.getMinimum());
                while (progressBar.isEnabled()) {
                    int next = progressBar.getValue() + 1;
                    if (next > progressBar.getMaximum())
                        next = progressBar.getMinimum();
                    progressBar.setValue(next);
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        logger.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
                progressBar.setValue(progressBar.getMaximum());
            }
        }, "AozoraListLoader_sync_progressBar").start();
        loader = new AozoraListLoader(this);
        new Thread(loader, "AozoraListLoader").start();
    }

    void searchAll() {
        search(searchField.getText(), true);
    }

    void search() {
        search(searchField.getText(), false);
    }

    private void search(String search, boolean isAll) {
        if (search != null && search.trim().length() != 0) {
            progressLabel.setText("検索中");
            progressBar.setMaximum(authorNodeList.size());
            progressBar.setValue(0);
            for (AozoraAuthorNode authorNode  : authorNodeList) {
                if (isSearchStop())
                    return;
                authorNode.search(search, isAll);
                progressBar.setValue(progressBar.getValue() + 1);
            }

        }
        progressBar.setValue(progressBar.getMaximum());
    }

    void searchReset() {
        if (!isAuthorLoaded()) {
            initData();
            return;
        }
        searchField.setText(null);
        progressLabel.setText("表示中");
        progressBar.setMaximum(authorNodeList.size());
        progressBar.setValue(0);
        for (final AozoraAuthorNode authorNode : authorNodeList) {
            if (isSearchStop())
                return;
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        authorNode.searchReset();
                    }
                });
                progressBar.setValue(progressBar.getValue() + 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        progressBar.setValue(progressBar.getMaximum());
    }

    void loadAllWorks() {
        progressLabel.setText("取得中");
        progressBar.setMaximum(authorNodeList.size());
        progressBar.setValue(0);
        for (final AozoraAuthorNode authorNode : authorNodeList) {
            if (isSearchStop())
                return;
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        loadWorks(authorNode);
                    }
                });
                progressBar.setValue(progressBar.getValue() + 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        progressBar.setValue(progressBar.getMaximum());
    }

    public void loadWorks(AozoraAuthorNode authorNode) {
        loader.loadWorksImmediate(authorNode);
    }

    public void setSearchResult(boolean isSearchResult) {
        this.isSearchResult = isSearchResult;
    }

    public boolean isSearchResult() {
        return isSearchResult;
    }

    void setSearchStop(boolean isSearchStop) {
        this.isSearchStop = isSearchStop;
    }

    boolean isSearchStop() {
        return isSearchStop;
    }

    public void setAuthorLoaded(boolean isAuthorLoaded) {
        this.isAuthorLoaded = isAuthorLoaded;
        revalidate();
    }

    boolean isAuthorLoaded() {
        return isAuthorLoaded;
    }

    private void addTreePane(AozoraAuthorNode authorNode) {
        listPane.add(new AozoraAuthorTreePane(getAzContext(), this, authorNode));
    }

    public void addTree(JTree tree) {
        selectionManager.addTree(tree);
    }

    public void removeTree(JTree tree) {
        selectionManager.removeTree(tree);
    }

    public void setSelectOnly(JTree selectedTree) {
        selectionManager.setSelectOnly(selectedTree);
    }

    public AozoraAuthorNode getAozoraAuthorNode(String authorID) {
        if (authorID == null || authorID.length() == 0)
            throw new IllegalArgumentException("authorID cannot be null nor blank");
        synchronized (authorNodeList) {
            for (AozoraAuthorNode authorNode : authorNodeList) {
                if (authorID.equals(authorNode.getAozoraAuthor().getID()))
                    return authorNode;
            }
        }
        return null;
    }

    public void addAozoraAuthorNode(AozoraAuthorNode authorNode) {
        synchronized (authorNodeList) {
            authorNodeList.add(authorNode);
            addTreePane(authorNode);
        }
    }

    public AozoraWork getAozoraWork(String workID, boolean loadImmediate) {
        if (workID == null || workID.length() == 0)
            throw new IllegalArgumentException("workID cannot be null nor blank");
        String authorID = matchAuthorID(workID);
        if (authorID != null) {
            AozoraAuthorNode authorNode = getAozoraAuthorNode(authorID);
            if (authorNode == null)
                return null;
            if (loadImmediate && !authorNode.isWorkLoaded())
                loadWorks(authorNode);
            AozoraWorkNode workNode = authorNode.getAozoraWorkNode(workID);
            if (workNode != null)
                return workNode.getAozoraWork();
        }
        return null;
    }

    public void getAozoraWorkAsynchronous(final String workID, final AozoraWorkParserHandler callback) {
        if (workID == null || workID.length() == 0)
            throw new IllegalArgumentException("workID cannot be null nor blank");
        String authorID = matchAuthorID(workID);
        if (authorID != null) {
            AozoraAuthorParserHandler authorCallback = new AozoraAuthorParserHandler() {
                public void author(AozoraAuthor author) {
                    if (author == null) {
                        callback.work(null);
                        return;
                    }
                    final AozoraAuthorNode authorNode = getAozoraAuthorNode(author.getID());
                    if (authorNode == null) {
                        callback.work(null);
                        return;
                    }
                    Runnable worksCallback = new Runnable() {
                        public void run() {
                            AozoraWorkNode workNode = authorNode.getAozoraWorkNode(workID);
                            if (workNode != null)
                                callback.work(workNode.getAozoraWork());
                            else
                                callback.work(null);
                        }
                    };
                    synchronized (authorNode) {
                        if (authorNode.isWorkLoaded())
                            worksCallback.run();
                        else
                            loader.loadWorksAsynchronus(authorNode, worksCallback);
                    }
                }
            };
            getAozoraAuthorAsynchronous(authorID, authorCallback);
        } else {
            callback.work(null);
        }
    }

    public AozoraAuthor getAozoraAuthor(String authorID) {
        if (authorID == null || authorID.length() == 0)
            throw new IllegalArgumentException("authorID cannot be null nor blank");
        AozoraAuthorNode authorNode = getAozoraAuthorNode(authorID);
        if (authorNode != null)
            return authorNode.getAozoraAuthor();
        else
            return null;
    }

    public void getAozoraAuthorAsynchronous(final String authorID, final AozoraAuthorParserHandler callback) {
        if (authorID == null || authorID.length() == 0) {
            throw new IllegalArgumentException("authorID cannot be null nor blank");
        } else {
            Runnable authorsCallback = new Runnable() {
                public void run() {
                    AozoraAuthorNode authorNode = getAozoraAuthorNode(authorID);
                    if (authorNode != null)
                        callback.author(authorNode.getAozoraAuthor());
                    else
                        callback.author(null);
                }
            };
            loader.loadAuthorsAsynchronus(authorsCallback);
            return;
        }
    }

    public String matchAuthorID(String workID) {
        if (workID == null || workID.length() == 0)
            throw new IllegalArgumentException("workID cannot be null nor blank");
        String personID = null;
        if (index != null) {
            personID = index.getAuthorID(workID);
            if (personID == null)
                personID = index.getTranslatorID(workID);
        }
        return personID;
    }

    public void focusAuthor(AozoraAuthor author) {
        final AozoraAuthorNode authorNode = getAozoraAuthorNode(author.getID());
        if (authorNode != null) {
            Runnable callback = new Runnable() {
                public void run() {
                    authorNode.focus(null, true, true);
                }
            };
            if (!authorNode.isWorkLoaded())
                loader.loadWorksAsynchronus(authorNode, callback);
            else
                callback.run();
        }
    }

    public void focusWork(AozoraWork work) {
        String personID = work.getAuthorID();
        if (personID == null || personID.length() == 0)
            personID = work.getTranslatorID();
        if (personID != null && personID.length() != 0) {
            AozoraAuthorNode authorNode = getAozoraAuthorNode(personID);
            if (authorNode != null)
                authorNode.focus(work.getID(), true, true);
        }
    }

    public void focusNext(AozoraAuthorNode fromAuthorNode) {
        int fromIndex = authorNodeList.indexOf(fromAuthorNode);
        for (int i = fromIndex + 1; i < authorNodeList.size(); i++) {
            AozoraAuthorNode nextAuthorNode = authorNodeList.get(i);
            if (nextAuthorNode.isVisible()) {
                nextAuthorNode.focus(null, false, false);
                return;
            }
        }
    }

    public void focusPrev(AozoraAuthorNode fromAuthorNode) {
        int fromIndex = authorNodeList.indexOf(fromAuthorNode);
        for (int i = fromIndex - 1; i >= 0; i--) {
            AozoraAuthorNode prevAuthorNode = authorNodeList.get(i);
            if (prevAuthorNode.isVisible()) {
                if (prevAuthorNode.isWorkLoaded() && prevAuthorNode.isExpanded() && prevAuthorNode.getChildCount() > 0) {
                    AozoraWorkNode workNode = (AozoraWorkNode) prevAuthorNode.getLastChild();
                    prevAuthorNode.focus(workNode.getAozoraWork().getID(), false, false);
                } else {
                    prevAuthorNode.focus(null, false, false);
                }
                return;
            }
        }
    }

    public void focusSelectedNode() {
        requestFocusInWindow();
        for (AozoraAuthorNode authorNode : authorNodeList) {
            if (authorNode.isSelected()) {
                authorNode.focusSelectedNode();
                return;
            }
        }
    }

    private JTextField searchField;
    private JButton searchAllButton;
    private JButton searchAddButton;
    private JButton searchResetButton;
    private JButton searchStopButton;
    private JLabel progressLabel;
    private JProgressBar progressBar;
    private JScrollPane scroll;
    private JPanel listPane;
    private List<AozoraAuthorNode> authorNodeList;
    private AozoraIndex index;
    private AozoraTreeSelectionManager selectionManager;
    private boolean isSearchResult;
    private boolean isSearchStop;
    private boolean isAuthorLoaded;
    private AozoraListLoader loader;
}
