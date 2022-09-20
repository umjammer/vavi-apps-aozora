/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraDefaultPane;
import com.soso.aozora.core.AozoraUtil;


class AozoraTopicFolderTreePane extends AozoraDefaultPane implements TreeSelectionListener, AncestorListener {

    static Logger logger = Logger.getLogger(AozoraTopicFolderTreePane.class.getName());

    private static class TopicTreeCellRenderer extends DefaultTreeCellRenderer {
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component comp = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (value instanceof AozoraTopicFolderNode) {
                Font f = comp.getFont();
                if (f != null && !"Monospaced".equals(f.getName())) {
                    if (orginalFont == null)
                        orginalFont = f;
                    comp.setFont(new Font("Monospaced", f.getStyle(), f.getSize()));
                }
            } else if (orginalFont != null)
                comp.setFont(orginalFont);
            return comp;
        }

        private Font orginalFont;

        private TopicTreeCellRenderer() {
            orginalFont = null;
        }
    }

    private class RightAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            boolean isActionCalled = false;
            TreePath selectionPath = tree.getSelectionPath();
            if (selectionPath != null) {
                Object selectedNode = selectionPath.getLastPathComponent();
                if ((selectedNode instanceof AozoraTopicFolderNode) && (!isExpanded() || !isTopicLoaded)) {
                    loadTopics();
                    expand();
                    isActionCalled = true;
                }
            }
            if (!isActionCalled && orginalAction != null)
                orginalAction.actionPerformed(e);
        }

        private final Action orginalAction;

        private RightAction(Action orginalAction) {
            this.orginalAction = orginalAction;
        }
    }

    private class DownAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            boolean isActionCalled = false;
            TreePath selectionPath = tree.getSelectionPath();
            if (selectionPath != null) {
                Object selectedNode = selectionPath.getLastPathComponent();
                if (selectedNode instanceof AozoraTopicFolderNode) {
                    if (!isExpanded() || !isTopicLoaded) {
                        topicMediator.focusNext(AozoraTopicFolderTreePane.this);
                        isActionCalled = true;
                    }
                } else if (selectedNode instanceof AozoraTopicNode) {
                    AozoraTopicNode selectedTopicNode = (AozoraTopicNode) selectedNode;
                    AozoraTopicFolderNode folderNode = (AozoraTopicFolderNode) selectedTopicNode.getParent();
                    if (selectedTopicNode == folderNode.getLastChild()) {
                        topicMediator.focusNext(AozoraTopicFolderTreePane.this);
                        isActionCalled = true;
                    }
                }
            }
            if (!isActionCalled && orginalAction != null)
                orginalAction.actionPerformed(e);
        }

        private final Action orginalAction;

        private DownAction(Action orginalAction) {
            this.orginalAction = orginalAction;
        }
    }

    private class UpAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            boolean isActionCalled = false;
            TreePath selectionPath = tree.getSelectionPath();
            if (selectionPath != null) {
                Object selectedNode = selectionPath.getLastPathComponent();
                if (selectedNode instanceof AozoraTopicFolderNode) {
                    topicMediator.focusPrev(AozoraTopicFolderTreePane.this);
                    isActionCalled = true;
                }
            }
            if (!isActionCalled && orginalAction != null)
                orginalAction.actionPerformed(e);
        }

        private final Action orginalAction;

        private UpAction(Action orginalAction) {
            this.orginalAction = orginalAction;
        }
    }

    AozoraTopicFolderTreePane(AozoraContext context, AozoraTopicMediator topicMediator, String name) {
        super(context);
        isTopicLoaded = false;
        this.topicMediator = topicMediator;
        topicFolderNode = new AozoraTopicFolderNode(name);
        topicLoaders = new ArrayList<AozoraTopicLoader>();
        initGUI();
    }

    private void initGUI() {
        getFolderNode().setTreePane(this);
        tree = new JTree(createTreeModel());
        tree.setRootVisible(false);
        tree.setCellRenderer(new TopicTreeCellRenderer());
        tree.collapseRow(0);
        tree.getSelectionModel().setSelectionMode(1);
        tree.addTreeSelectionListener(this);
        tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    clicked(true);
                else if (e.getClickCount() == 1)
                    clicked(false);
            }
        });
        add(tree, BorderLayout.CENTER);
        collapse();
        addAncestorListener(this);
        initKeyAction();
        workaround_checkRowHeight();
    }

    private void workaround_checkRowHeight() {
        if (getPreferredSize().height > 16000 && getFolderNode().getChildCount() == 0) {
            logger.info("Workaround for JTree preffered height bug | " + getFolderNode() + " | " + getPreferredSize());
            fireNodeChanged();
            collapse();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    workaround_checkRowHeight();
                }
            });
        }
    }

    public void ancestorAdded(AncestorEvent event) {
        if (event.getComponent() == this)
            topicMediator.addTree(tree);
    }

    public void ancestorMoved(AncestorEvent event) {
    }

    public void ancestorRemoved(AncestorEvent event) {
        if (event.getComponent() == this)
            topicMediator.removeTree(tree);
    }

    void addTopicLoader(AozoraTopicLoader loader) {
        synchronized (topicLoaders) {
            topicLoaders.add(loader);
        }
    }

    public void valueChanged(TreeSelectionEvent e) {
        if (e.getNewLeadSelectionPath() != null)
            topicMediator.setSelectOnly(tree);
    }

    private TreeModel createTreeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        root.add(getFolderNode());
        TreeModel model = new DefaultTreeModel(root, true);
        return model;
    }

    void fireNodeChanged() {
        TreePath selectionPath = getSelectionPath();
        ((DefaultTreeModel) tree.getModel()).reload();
        expand();
        if (selectionPath != null)
            focus(selectionPath);
    }

    void expand() {
        tree.collapseRow(0);
        tree.expandRow(0);
    }

    void collapse() {
        tree.expandRow(0);
        tree.collapseRow(0);
    }

    boolean isExpanded() {
        return tree.isExpanded(0);
    }

    void clicked(boolean isDouble) {
        boolean isExpand = false;
        if (isTopicLoaded) {
            if (isDouble) {
                showLastSelectedTopic();
                isExpand = true;
            }
        } else {
            loadTopics();
            focus(new TreePath(getFolderNode().getPath()));
            isExpand = true;
        }
        if (isExpand)
            tree.expandRow(0);
    }

    void loadTopics() {
        synchronized (topicLoaders) {
            for (AozoraTopicLoader topicLoader : topicLoaders) {
                for (AozoraTopicNode topicNode : topicLoader.loadTopics()) {
                    getFolderNode().add(topicNode);
                }
            }
        }
        isTopicLoaded = true;
    }

    AozoraTopicFolderNode getFolderNode() {
        return topicFolderNode;
    }

    void showLastSelectedTopic() {
        Object selectedObject = tree.getLastSelectedPathComponent();
        if (selectedObject instanceof AozoraTopicNode)
            ((AozoraTopicNode) selectedObject).showTopic();
    }

    boolean isSelected() {
        return getSelectionPath() != null;
    }

    TreePath getSelectionPath() {
        return tree.getSelectionPath();
    }

    void focus(TreePath path) {
        tree.requestFocusInWindow();
        if (path != null) {
            tree.setSelectionPath(path);
            tree.scrollPathToVisible(path);
        } else {
            tree.scrollRowToVisible(0);
        }
    }

    private void initKeyAction() {
        KeyStroke upKeyStroke = KeyStroke.getKeyStroke("UP");
        Object orginalUpActionKey = tree.getInputMap(JComponent.WHEN_FOCUSED).get(upKeyStroke);
        Action orginalUpAction = orginalUpActionKey == null ? null : tree.getActionMap().get(orginalUpActionKey);
        UpAction upActon = new UpAction(orginalUpAction);
        AozoraUtil.putKeyStrokeAction(tree, JComponent.WHEN_FOCUSED, upKeyStroke, "AozoraTopicFolderTreePane.upAction", upActon);
        KeyStroke downKeyStroke = KeyStroke.getKeyStroke("DOWN");
        Object orginalDownActionKey = tree.getInputMap(JComponent.WHEN_FOCUSED).get(downKeyStroke);
        Action orginalDownAction = orginalDownActionKey == null ? null : tree.getActionMap().get(orginalDownActionKey);
        DownAction downActon = new DownAction(orginalDownAction);
        AozoraUtil.putKeyStrokeAction(tree, JComponent.WHEN_FOCUSED, downKeyStroke, "AozoraTopicFolderTreePane.downActon", downActon);
        KeyStroke rightKeyStroke = KeyStroke.getKeyStroke("RIGHT");
        Object orginalRightActionKey = tree.getInputMap(JComponent.WHEN_FOCUSED).get(rightKeyStroke);
        Action orginalRightAction = orginalRightActionKey == null ? null : tree.getActionMap().get(orginalRightActionKey);
        RightAction rightActon = new RightAction(orginalRightAction);
        AozoraUtil.putKeyStrokeAction(tree, JComponent.WHEN_FOCUSED, rightKeyStroke, "AozoraTopicFolderTreePane.rightActon", rightActon);
        AozoraUtil.putKeyStrokeAction(tree, JComponent.WHEN_FOCUSED, KeyStroke.getKeyStroke("ENTER"), "AozoraAuthorTreePane.enterAction", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                showLastSelectedTopic();
            }
        });
    }

    private final AozoraTopicMediator topicMediator;
    private final AozoraTopicFolderNode topicFolderNode;
    private JTree tree;
    private List<AozoraTopicLoader> topicLoaders;
    private boolean isTopicLoaded;

}
