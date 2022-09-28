/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraDefaultPane;
import com.soso.aozora.core.AozoraUtil;
import com.soso.aozora.viewer.AozoraViewerPane;


class AozoraAuthorTreePane extends AozoraDefaultPane implements TreeSelectionListener, AncestorListener {

    static Logger logger = Logger.getLogger(AozoraViewerPane.class.getName());

    private class RightAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            boolean isActionCalled = false;
            TreePath selectionPath = tree.getSelectionPath();
            if (selectionPath != null) {
                Object selectedNode = selectionPath.getLastPathComponent();
                if (selectedNode instanceof AozoraAuthorNode) {
                    AozoraAuthorNode selectedAuthorNode = (AozoraAuthorNode) selectedNode;
                    if (!isExpanded() || !selectedAuthorNode.isWorkLoaded()) {
                        listMediator.loadWorks(selectedAuthorNode);
                        selectedAuthorNode.focus(null, false, true);
                        isActionCalled = true;
                    }
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
                if (selectedNode instanceof AozoraAuthorNode) {
                    AozoraAuthorNode selectedAuthorNode = (AozoraAuthorNode) selectedNode;
                    if (!isExpanded() || !selectedAuthorNode.isWorkLoaded()) {
                        listMediator.focusNext(selectedAuthorNode);
                        isActionCalled = true;
                    }
                } else if (selectedNode instanceof AozoraWorkNode) {
                    AozoraWorkNode selectedWorkNode = (AozoraWorkNode) selectedNode;
                    AozoraAuthorNode authorNode = (AozoraAuthorNode) selectedWorkNode.getParent();
                    if (selectedWorkNode == authorNode.getLastChild()) {
                        listMediator.focusNext(authorNode);
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
                if (selectedNode instanceof AozoraAuthorNode) {
                    AozoraAuthorNode selectedAuthorNode = (AozoraAuthorNode) selectedNode;
                    listMediator.focusPrev(selectedAuthorNode);
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

    public AozoraAuthorTreePane(AozoraContext context, AozoraListMediator listMediator, AozoraAuthorNode authorNode) {
        super(context);
        this.listMediator = listMediator;
        this.authorNode = authorNode;
        initGUI();
    }

    private void initGUI() {
        authorNode.setTreePane(this);
        tree = new JTree(createTreeModel());
        ToolTipManager.sharedInstance().registerComponent(tree);
        tree.setRootVisible(false);
        tree.collapseRow(0);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
        tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3)
                    showPopupMenu(e.getX(), e.getY());
                else if (e.getClickCount() == 2)
                    clicked(true);
                else if (e.getClickCount() == 1)
                    clicked(false);
            }
        });
        initKeyAction();
        tree.setCellRenderer(new AozoraListTreeRenderer(getAzContext()));
        add(tree);
        collapse();
        addAncestorListener(this);
        workaround_checkRowHeight();
    }

    private void workaround_checkRowHeight() {
        if (getPreferredSize().height > 16000 && getAuthorNode().getChildCount() == 0) {
            logger.info("Workaround for JTree preffered height bug | " + getAuthorNode() + " | " + getPreferredSize());
            fireNodeChanged();
            collapse();
            SwingUtilities.invokeLater(() -> workaround_checkRowHeight());
        }
    }

    public void ancestorAdded(AncestorEvent event) {
        if (event.getComponent() == this)
            listMediator.addTree(tree);
    }

    public void ancestorMoved(AncestorEvent event) {
    }

    public void ancestorRemoved(AncestorEvent event) {
        if (event.getComponent() == this)
            listMediator.removeTree(tree);
    }

    AozoraAuthorNode getAuthorNode() {
        return authorNode;
    }

    private TreeModel createTreeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        root.add(authorNode);
        TreeModel model = new DefaultTreeModel(root, true);
        return model;
    }

    void fireNodeChanged() {
        ((DefaultTreeModel) tree.getModel()).reload();
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

    public void valueChanged(TreeSelectionEvent e) {
        if (e.getNewLeadSelectionPath() != null)
            listMediator.setSelectOnly(tree);
    }

    void clicked(boolean isDouble) {
        boolean isExpand = false;
        if (authorNode.isWorkLoaded()) {
            if (isDouble) {
                boolean isWorkShow = false;
                if (authorNode.getVisibleAozoraWorkNodes().length != 0)
                    isWorkShow = showLastSelectedViewer();
                if (!isWorkShow) {
                    authorNode.searchReset();
                    isExpand = true;
                }
            }
        } else if (isDouble || !listMediator.isSearchResult()) {
            listMediator.loadWorks(authorNode);
            isExpand = true;
        }
        if (isExpand)
            authorNode.focus(null, false, true);
    }

    boolean showLastSelectedViewer() {
        Object selectedObject = tree.getLastSelectedPathComponent();
        if (selectedObject instanceof AozoraWorkNode) {
            getAzContext().getRootMediator().showViewer(authorNode.getAozoraAuthor(), ((AozoraWorkNode) selectedObject).getAozoraWork());
            return true;
        } else {
            return false;
        }
    }

    void showPopupMenu(int x, int y) {
        TreePath path = tree.getPathForLocation(x, y);
        if (path == null)
            return;
        tree.setSelectionPath(path);
        if (path.getLastPathComponent() instanceof AozoraAuthorNode)
            showPopupMenu((AozoraAuthorNode) path.getLastPathComponent(), x, y);
        else if (path.getLastPathComponent() instanceof AozoraWorkNode)
            showPopupMenu((AozoraWorkNode) path.getLastPathComponent(), x, y);
    }

    private void showPopupMenu(AozoraAuthorNode authorNode, int x, int y) {
        JPopupMenu popupMenu = new JPopupMenu();
        getAuthorNode().isWorkLoaded();
        if (popupMenu.getComponentCount() != 0)
            popupMenu.show(tree, x, y);
    }

    private void showPopupMenu(final AozoraWorkNode workNode, int x, int y) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem openItem = new JMenuItem(new AbstractAction("作品を開く") {
            public void actionPerformed(ActionEvent e) {
                getAzContext().getRootMediator().showViewer(getAuthorNode().getAozoraAuthor(), workNode.getAozoraWork());
            }
        });
        popupMenu.add(openItem);
        if (popupMenu.getComponentCount() != 0)
            popupMenu.show(tree, x, y);
    }

    void focus(TreePath path) {
        tree.requestFocusInWindow();
        tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);
    }

    TreePath getSelectionPath() {
        return tree.getSelectionPath();
    }

    private void initKeyAction() {
        KeyStroke upKeyStroke = KeyStroke.getKeyStroke("UP");
        Object orginalUpActionKey = tree.getInputMap(JComponent.WHEN_FOCUSED).get(upKeyStroke);
        Action orginalUpAction = orginalUpActionKey == null ? null : tree.getActionMap().get(orginalUpActionKey);
        UpAction upActon = new UpAction(orginalUpAction);
        AozoraUtil.putKeyStrokeAction(tree, JComponent.WHEN_FOCUSED, upKeyStroke, "AozoraAuthorTreePane.upAction", upActon);
        KeyStroke downKeyStroke = KeyStroke.getKeyStroke("DOWN");
        Object orginalDownActionKey = tree.getInputMap(JComponent.WHEN_FOCUSED).get(downKeyStroke);
        Action orginalDownAction = orginalDownActionKey == null ? null : tree.getActionMap().get(orginalDownActionKey);
        DownAction downActon = new DownAction(orginalDownAction);
        AozoraUtil.putKeyStrokeAction(tree, JComponent.WHEN_FOCUSED, downKeyStroke, "AozoraAuthorTreePane.downActon", downActon);
        KeyStroke rightKeyStroke = KeyStroke.getKeyStroke("RIGHT");
        Object orginalRightActionKey = tree.getInputMap(JComponent.WHEN_FOCUSED).get(rightKeyStroke);
        Action orginalRightAction = orginalRightActionKey == null ? null : tree.getActionMap().get(orginalRightActionKey);
        RightAction rightActon = new RightAction(orginalRightAction);
        AozoraUtil.putKeyStrokeAction(tree, JComponent.WHEN_FOCUSED, rightKeyStroke, "AozoraAuthorTreePane.rightActon", rightActon);
        AozoraUtil.putKeyStrokeAction(tree, JComponent.WHEN_FOCUSED, KeyStroke.getKeyStroke("ENTER"), "AozoraAuthorTreePane.enterAction", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                showLastSelectedViewer();
            }
        });
    }

    private final AozoraListMediator listMediator;
    private JTree tree;
    private final AozoraAuthorNode authorNode;
}
