/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraDefaultPane;
import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.core.AozoraUtil;
import com.soso.aozora.data.AozoraAuthor;
import com.soso.aozora.data.AozoraAuthorParserHandler;
import com.soso.aozora.data.AozoraHistories;
import com.soso.aozora.data.AozoraWork;
import com.soso.aozora.data.AozoraWorkParserHandler;
import com.soso.aozora.event.AozoraListenerAdapter;


public class AozoraHistoryPane extends AozoraDefaultPane {

    static Logger logger = Logger.getLogger(AozoraHistoryPane.class.getName());

    public AozoraHistoryPane(AozoraContext context) {
        super(context);
        dataThread = null;
        initGUI();
        initTermNodes();
    }

    private void initGUI() {
        setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(getTree());
        add(scrollPane, BorderLayout.CENTER);
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                initData();
                removeComponentListener(this);
            }
        });
        JButton deleteButton = new JButton(new AbstractAction("全ての履歴を削除") {
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showInternalConfirmDialog(getAzContext().getDesktopPane(), "全ての履歴を削除します。よろしいですか？", "全ての履歴を削除", 2, 3) == 0)
                    removeAllEntories();
            }
        });
        add(deleteButton, BorderLayout.NORTH);
        AozoraUtil.putKeyStrokeAction(tree, JComponent.WHEN_FOCUSED, KeyStroke.getKeyStroke("ENTER"), "AozoraHistoryPane.enterAction", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                showLastSelectedHistory();
            }
        });
        getAzContext().getListenerManager().add(new AozoraListenerAdapter() {
            public void lineModeChanged(AozoraEnv.LineMode lineMode) {
                reloadAllAuthorAndWork();
            }
        });
    }

    private JTree getTree() {
        if (tree == null) {
            tree = new JTree(getTreeModel());
            ToolTipManager.sharedInstance().registerComponent(tree);
            tree.setRootVisible(false);
            tree.setCellRenderer(new AozoraHistoryTreeCellRenderer());
            tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            tree.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        showPopupMenu(e.getX(), e.getY());
                    } else {
                        if (e.getClickCount() == 2)
                            showLastSelectedHistory();
                        updateData();
                    }
                }
            });
            tree.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    updateData();
                }
            });
        }
        return tree;
    }

    private TreeModel getTreeModel() {
        if (model == null)
            model = new DefaultTreeModel(getRootNode(), true);
        return model;
    }

    private DefaultMutableTreeNode getRootNode() {
        if (rootNode == null)
            rootNode = new DefaultMutableTreeNode("root");
        return rootNode;
    }

    private void initTermNodes() {
        Calendar cal = Calendar.getInstance();
        cal.set(11, 0);
        cal.set(12, 0);
        cal.set(13, 0);
        cal.set(14, 0);
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        long end = 0xffffffffL;
        long start = cal.getTimeInMillis();
        String title = "今日";
        String toolTip = df.format(new Date(start)) + " の履歴";
        getRootNode().add(new AozoraHistoryTermNode(title, toolTip, start, end));
        end = cal.getTimeInMillis() - 1L;
        cal.add(5, -1);
        start = cal.getTimeInMillis();
        title = "昨日";
        toolTip = df.format(new Date(start)) + " の履歴";
        getRootNode().add(new AozoraHistoryTermNode(title, toolTip, start, end));
        end = cal.getTimeInMillis() - 1L;
        cal.add(5, -6);
        start = cal.getTimeInMillis();
        title = "7日前より最近";
        toolTip = df.format(new Date(start)) + " から " + df.format(new Date(end)) + " の履歴";
        getRootNode().add(new AozoraHistoryTermNode(title, toolTip, start, end));
        end = cal.getTimeInMillis() - 1L;
        cal.add(5, -23);
        start = cal.getTimeInMillis();
        title = "30日前より最近";
        toolTip = df.format(new Date(start)) + " から " + df.format(new Date(end)) + " の履歴";
        getRootNode().add(new AozoraHistoryTermNode(title, toolTip, start, end));
        end = cal.getTimeInMillis() - 1L;
        start = 0L;
        title = "それ以前";
        toolTip = df.format(new Date(end)) + " 以前の履歴";
        getRootNode().add(new AozoraHistoryTermNode(title, toolTip, start, end));
        reload();
    }

    private AozoraHistoryTermNode[] getTermNodes() {
        int count = getRootNode().getChildCount();
        AozoraHistoryTermNode[] termNodes = new AozoraHistoryTermNode[count];
        for (int i = 0; i < count; i++)
            termNodes[i] = (AozoraHistoryTermNode) getRootNode().getChildAt(i);

        return termNodes;
    }

    private AozoraHistoryTermNode getTermNode(long timestamp) {
        for (AozoraHistoryTermNode termNode : getTermNodes()) {
            if (termNode.getTermStart() <= timestamp && timestamp <= termNode.getTermEnd())
                return termNode;
        }

        throw new IllegalStateException("HistoryTermNode not found for " + timestamp);
    }

    private void initData() {
        if (SwingUtilities.isEventDispatchThread()) {
            synchronized (DATA_MUTEX) {
                if (dataThread == null) {
                    dataThread = new Thread(new Runnable() {
                        public void run() {
                            initData();
                        }
                    }, "AozoraHistoryPane_initData");
                    dataThread.start();
                }
            }
        } else {
            for (AozoraHistories.AozoraHistoryEntry entry : getAzContext().getHistories().toArray()) {
                addEntory(entry);
            }
    
            synchronized (DATA_MUTEX) {
                dataThread = null;
            }
        }
    }

    private void updateData() {
        if (SwingUtilities.isEventDispatchThread()) {
            synchronized (DATA_MUTEX) {
                if (dataThread == null) {
                    dataThread = new Thread(new Runnable() {
                        public void run() {
                            updateData();
                        }
                    }, "AozoraHistoryPane_updateData");
                    dataThread.start();
                }
            }
        } else {
            for (AozoraHistories.AozoraHistoryEntry entry : getAzContext().getHistories().toArray()) {
                if (latestTimestamp < entry.getTimestamp())
                    addEntory(entry);
            }
    
            synchronized (DATA_MUTEX) {
                dataThread = null;
            }
        }
    }

    private void addEntory(com.soso.aozora.data.AozoraHistories.AozoraHistoryEntry entry) {
        synchronized (DATA_MUTEX) {
            if (latestTimestamp < entry.getTimestamp())
                latestTimestamp = entry.getTimestamp();
        }
        AozoraHistoryTermNode termNode = getTermNode(entry.getTimestamp());
        AozoraHistoryEntryNode entryNode = new AozoraHistoryEntryNode(entry);
        termNode.insert(entryNode, 0);
        reload();
        loadAuthorAndWork(entryNode);
    }

    private void loadAuthorAndWork(final AozoraHistoryEntryNode entryNode) {
        getAzContext().getRootMediator().getAozoraWorkAsynchronous(entryNode.getEntry().getBook(), new AozoraWorkParserHandler() {
            public void work(AozoraWork work) {
                entryNode.work(work);
                if (work != null)
                    getAzContext().getRootMediator().getAozoraAuthorAsynchronous(work.getAuthorID(), new AozoraAuthorParserHandler() {
                        public void author(AozoraAuthor author) {
                            entryNode.author(author);
                            reload();
                        }
                    });
                reload();
            }
        });
    }

    private void reloadAllAuthorAndWork() {
        for (AozoraHistoryTermNode termNode : getTermNodes()) {
            for (AozoraHistoryEntryNode entryNode : termNode.getEntryNodes()) {
                entryNode.reset();
                loadAuthorAndWork(entryNode);
            }
        }
    }

    private void removeEntory(AozoraHistoryEntryNode entryNode) {
        getAzContext().getHistories().removeHistory(entryNode.getEntry());
        try {
            getAzContext().getHistories().store();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "履歴の保存に失敗しました。", e);
        }
        ((AozoraHistoryTermNode) entryNode.getParent()).remove(entryNode);
        reload();
    }

    private void removeEntories(AozoraHistoryTermNode termNode) {
        synchronized (DATA_MUTEX) {
            while (dataThread != null)
                try {
                    Thread.sleep(100L);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            for (AozoraHistoryEntryNode entryNode : termNode.getEntryNodes()) {
                getAzContext().getHistories().removeHistory(entryNode.getEntry());
                termNode.remove(entryNode);
            }

            try {
                getAzContext().getHistories().store();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "履歴の保存に失敗しました。", e);
            }
            reload();
        }
    }

    private void removeAllEntories() {
        synchronized (DATA_MUTEX) {
            while (dataThread != null)
                try {
                    Thread.sleep(100L);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            for (AozoraHistories.AozoraHistoryEntry entry : getAzContext().getHistories().toArray()) {
                getAzContext().getHistories().removeHistory(entry);
            }

            try {
                getAzContext().getHistories().store();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "履歴の保存に失敗しました。", e);
            }
            for (AozoraHistoryTermNode termNode : getTermNodes()) {
                termNode.removeAllChildren();
            }

            reload();
        }
    }

    private void reload() {
        if (!SwingUtilities.isEventDispatchThread())
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        reload();
                    }
                });
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        TreePath rootPath = new TreePath(getRootNode().getPath());
        TreePath selectionPath = tree.getSelectionPath();
        List<TreePath> expandedPaths = new ArrayList<TreePath>();
        Enumeration<TreePath> e = getTree().getExpandedDescendants(rootPath);
        while (e.hasMoreElements()) {
            TreePath expandedPath = e.nextElement();
            expandedPaths.add(expandedPath);
        }

        ((DefaultTreeModel) getTreeModel()).reload();
        for (AozoraHistoryTermNode termNode : getTermNodes()) {
            TreePath reloadPath = new TreePath(termNode.getPath());
            getTree().expandPath(reloadPath);
            if (!expandedPaths.contains(reloadPath))
                getTree().collapsePath(reloadPath);
        }

        if (selectionPath != null)
            tree.setSelectionPath(selectionPath);
        repaint();
    }

    private void showPopupMenu(int x, int y) {
        TreePath path = getTree().getPathForLocation(x, y);
        if (path == null)
            return;
        getTree().setSelectionPath(path);
        if (path.getLastPathComponent() instanceof AozoraHistoryEntryNode)
            showPopupMenu((AozoraHistoryEntryNode) path.getLastPathComponent(), x, y);
        else if (path.getLastPathComponent() instanceof AozoraHistoryTermNode)
            showPopupMenu((AozoraHistoryTermNode) path.getLastPathComponent(), x, y);
    }

    private void showPopupMenu(final AozoraHistoryEntryNode entryNode, int x, int y) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem openItem = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                showHistory(entryNode);
            }
        });
        if (entryNode.getAuthor() == null || entryNode.getWork() == null)
            openItem.setEnabled(false);
        popupMenu.add(openItem);
        JMenuItem deleteItem = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeEntory(entryNode);
            }
        });
        popupMenu.add(deleteItem);
        popupMenu.show(getTree(), x, y);
    }

    private void showPopupMenu(final AozoraHistoryTermNode termNode, int x, int y) {
        if (termNode.getChildCount() != 0) {
            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem deleteItem = new JMenuItem(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (JOptionPane.showInternalConfirmDialog(getAzContext().getDesktopPane(),
                          termNode.getUserObject() + " の履歴を全て削除します。よろしいですか？",
                          termNode + " の履歴を全/て削除", 2, 3) == 0)
                        removeEntories(termNode);
                }
            });
            popupMenu.add(deleteItem);
            popupMenu.show(getTree(), x, y);
        }
    }

    private void showLastSelectedHistory() {
        TreePath path = getTree().getSelectionPath();
        if (path != null) {
            Object node = path.getLastPathComponent();
            if (node instanceof AozoraHistoryEntryNode) {
                AozoraHistoryEntryNode entryNode = (AozoraHistoryEntryNode) node;
                showHistory(entryNode);
            }
        }
    }

    private void showHistory(AozoraHistoryEntryNode entryNode) {
        AozoraAuthor author = entryNode.getAuthor();
        AozoraWork work = entryNode.getWork();
        AozoraHistories.AozoraHistoryEntry entry = entryNode.getEntry();
        if (author != null && work != null)
            getAzContext().getRootMediator().showViewer(author, work, entry.getPosition());
    }

    public void focusSelectedHistory() {
        tree.requestFocusInWindow();
        TreePath selectedPath = tree.getSelectionPath();
        if (selectedPath != null)
            tree.scrollPathToVisible(selectedPath);
    }

    private JTree tree;
    private TreeModel model;
    private DefaultMutableTreeNode rootNode;
    private final Object DATA_MUTEX = new Object();
    private Thread dataThread;
    private long latestTimestamp;
}
