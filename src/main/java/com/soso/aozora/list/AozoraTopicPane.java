/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraDefaultPane;
import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.core.AozoraUtil;
import com.soso.aozora.event.AozoraListenerAdapter;
import com.soso.aozora.html.TagReader;
import com.soso.aozora.html.TagUtil;


public class AozoraTopicPane extends AozoraDefaultPane implements AozoraTopicMediator {

    private enum TopicLoadStatus {
        NONE,
        INIT,
        LOADING,
        LOADED,
        FAIL
    }

    public AozoraTopicPane(AozoraContext context) {
        super(context);
        status = TopicLoadStatus.NONE;
        initGUI();
        if (getAzContext().getLineMode().isConnectable())
            initData();
    }

    private void initGUI() {
        selectionManager = new AozoraTreeSelectionManagerImpl();
        setLayout(new BorderLayout());
        scroll = new JScrollPane();
        add(scroll, BorderLayout.CENTER);
        topicPane = new JPanel();
        topicPane.setLayout(new BoxLayout(topicPane, BoxLayout.Y_AXIS));
        JPanel topicMinimalPane = new JPanel();
        topicMinimalPane.setLayout(new BorderLayout());
        topicMinimalPane.add(topicPane, BorderLayout.NORTH);
        scroll.setViewportView(topicMinimalPane);
        scroll.getVerticalScrollBar().setUnitIncrement(50);
        repaint();
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                for (Component c : topicPane.getComponents()) {
                    if (c instanceof AozoraTopicFolderTreePane) {
                        removeComponentListener(this);
                        ((AozoraTopicFolderTreePane) c).clicked(true);
                        break;
                    }
                }
            }
        });
        getAzContext().getListenerManager().add(new AozoraListenerAdapter() {
            public void lineModeChanged(AozoraEnv.LineMode lineMode) {
                boolean isConnectable = getAzContext().getLineMode().isConnectable();
                if (isConnectable)
                    synchronized (status) {
                        if (status == TopicLoadStatus.NONE)
                            initData();
                    }
                enableInputMethods(isConnectable);
            }
        });
    }

    private void initData() {
        synchronized (status) {
            status = TopicLoadStatus.INIT;
        }
        clearTopics();
        new Thread(new Runnable() {
            public void run() {
                try {
                    loadNewWorkTopic();
                } catch (Exception e) {
                    log(e);
                }
            }
        }, "AozoraTopicLoader").start();
    }

    private void loadNewWorkTopic() throws IOException {
        synchronized (status) {
            if (status == TopicLoadStatus.LOADING)
                throw new IllegalStateException("Topic is now loading");
            status = TopicLoadStatus.LOADING;
        }
        String curDate = null;
        URL curIndexURL = null;
        TagReader tin = null;
        try {
            URL backupListURL = new URL(AozoraEnv.getBackupURL(), "?C=N;O=D");
            tin = new TagReader(new InputStreamReader(AozoraUtil.getInputStream(backupListURL), "UTF-8"));
            String tag;
label0:     do
                do {
                    String href;
                    do {
                        if ((tag = tin.readNextTag()) == null)
                            break label0;
                        if (!tag.startsWith("a") || tag.indexOf("href") == -1)
                            continue label0;
                        href = TagUtil.getAttValue(tag, "href");
                    } while (!href.startsWith("index.") || !href.endsWith(".txt"));
                    String backDate = href.substring(6, 12);
                    URL oldIndexURL = new URL(AozoraEnv.getBackupURL(), href);
                    if (curDate != null && curIndexURL != null) {
                        addNewWorkTopic(curDate, curIndexURL, oldIndexURL);
                        log((new StringBuilder()).append("topics ").append(href).append(" listed").toString());
                    }
                    curDate = backDate;
                    curIndexURL = oldIndexURL;
                } while (true);
            while (!tag.equals("/html"));
            synchronized (status) {
                if (status != TopicLoadStatus.LOADING)
                    throw new IllegalStateException("Topics loaded concurrently");
                status = TopicLoadStatus.LOADED;
            }
        } finally {
            if (tin != null)
                tin.close();
            synchronized (status) {
                if (status == TopicLoadStatus.LOADING)
                    status = TopicLoadStatus.FAIL;
            }
        }
    }

    private void addNewWorkTopic(String curDate, URL curIndexURL, URL oldIndexURL) {
        AozoraNewWorkTopicLoader loader = new AozoraNewWorkTopicLoader(getAzContext(), curDate, curIndexURL, oldIndexURL);
        AozoraTopicFolderTreePane treePane = new AozoraTopicFolderTreePane(getAzContext(), this, createNewWorkTopicTitle(curDate));
        treePane.addTopicLoader(loader);
        topicPane.add(treePane);
    }

    private String createNewWorkTopicTitle(String curDate) {
        String title = curDate;
        try {
            (new SimpleDateFormat("yyMMdd")).parse(curDate);
            char[] cs = title.toCharArray();
            StringBuilder sb = new StringBuilder();
            sb.append("20").append(cs[0]).append(cs[1]).append("年");
            sb.append(cs[2] != '0' ? cs[2] : ' ').append(cs[3]).append("月");
            sb.append(cs[4] != '0' ? cs[4] : ' ').append(cs[5]).append("日");
            sb.append("の新着");
            title = sb.toString();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return title;
    }

    private void clearTopics() {
        synchronized (status) {
            if (status == TopicLoadStatus.LOADING)
                throw new IllegalStateException("Topic is now loading");
            topicPane.removeAll();
            topicPane.revalidate();
            status = TopicLoadStatus.NONE;
        }
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

    public void focusSelectedTopic() {
        requestFocusInWindow();
        for (Component comp : topicPane.getComponents()) {
            if (!(comp instanceof AozoraTopicFolderTreePane))
                continue;
            AozoraTopicFolderTreePane treePane = (AozoraTopicFolderTreePane) comp;
            if (treePane.isSelected())
                treePane.focus(treePane.getSelectionPath());
        }
    }

    public void focusNext(AozoraTopicFolderTreePane fromTreePane) {
        boolean next = false;
        for (Component comp : topicPane.getComponents()) {
            if (comp == fromTreePane) {
                next = true;
                continue;
            }
            if (next && (comp instanceof AozoraTopicFolderTreePane)) {
                AozoraTopicFolderTreePane nextTreePane = (AozoraTopicFolderTreePane) comp;
                TreePath focusPath = new TreePath(nextTreePane.getFolderNode().getPath());
                nextTreePane.focus(focusPath);
                return;
            }
        }
    }

    public void focusPrev(AozoraTopicFolderTreePane fromTreePane) {
        AozoraTopicFolderTreePane prev = null;
        for (Component comp : topicPane.getComponents()) {
            if (comp == fromTreePane) {
                if (prev != null) {
                    TreePath focusPath = null;
                    if (prev.isExpanded()) {
                        TreeNode lastChild = prev.getFolderNode().getLastChild();
                        if (lastChild instanceof DefaultMutableTreeNode)
                            focusPath = new TreePath(((DefaultMutableTreeNode) lastChild).getPath());
                    } else {
                        focusPath = new TreePath(prev.getFolderNode().getPath());
                    }
                    prev.focus(focusPath);
                    return;
                }
            } else if (comp instanceof AozoraTopicFolderTreePane)
                prev = (AozoraTopicFolderTreePane) comp;
        }
    }

    private JScrollPane scroll;
    private JPanel topicPane;
    private AozoraTreeSelectionManager selectionManager;
    private TopicLoadStatus status;
}
