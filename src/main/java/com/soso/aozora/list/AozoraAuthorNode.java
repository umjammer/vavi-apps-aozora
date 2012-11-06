/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import com.soso.aozora.data.AozoraAuthor;
import com.soso.sgui.SGUIUtil;


class AozoraAuthorNode extends DefaultMutableTreeNode {

    AozoraAuthorNode(AozoraAuthor author) {
        super(author.getName(), true);
        isWorkLoaded = false;
        this.author = author;
        allWorkNodeList = new ArrayList<AozoraWorkNode>();
    }

    public void add(MutableTreeNode newChild) {
        if (!(newChild instanceof AozoraWorkNode))
            throw new IllegalArgumentException("child must be AozoraWorkNode");
        AozoraWorkNode newWorkNode = (AozoraWorkNode) newChild;
        synchronized (this) {
            super.add(newWorkNode);
            if (!allWorkNodeList.contains(newWorkNode))
                allWorkNodeList.add(newWorkNode);
        }
        if (treePane != null) {
            newWorkNode.setTreePane(treePane);
            treePane.fireNodeChanged();
        }
    }

    AozoraWorkNode[] getVisibleAozoraWorkNodes() {
        List<AozoraWorkNode> workNodeList = new ArrayList<AozoraWorkNode>();
        synchronized (this) {
            int cnt = getChildCount();
            for (int i = 0; i < cnt; i++) {
                AozoraWorkNode workNode = (AozoraWorkNode) getChildAt(i);
                workNodeList.add(workNode);
            }
        }
        return workNodeList.toArray(new AozoraWorkNode[workNodeList.size()]);
    }

    AozoraWorkNode[] getAllAozoraWorkNodes() {
        synchronized (this) {
            return allWorkNodeList.toArray(new AozoraWorkNode[allWorkNodeList.size()]);
        }
    }

    AozoraWorkNode getAozoraWorkNode(String workID) {
        for (AozoraWorkNode workNode : getAllAozoraWorkNodes()) {
            if (workID.equals(workNode.getAozoraWork().getID()))
                return workNode;
        }

        return null;
    }

    AozoraAuthor getAozoraAuthor() {
        return author;
    }

    void setTreePane(AozoraAuthorTreePane treePane) {
        this.treePane = treePane;
        for (AozoraWorkNode workNode : getAllAozoraWorkNodes()) {
            workNode.setTreePane(treePane);
        }
    }

    void fireAuthorChange(AozoraAuthor newAuthor) {
        if (!author.getID().equals(newAuthor.getID()))
            throw new IllegalArgumentException("must be same authorID old:" + author + "; new:" + newAuthor);
        author = newAuthor;
        setUserObject(newAuthor.getName());
        if (treePane != null)
            treePane.fireNodeChanged();
    }

    void searchReset() {
        reset(allWorkNodeList, true);
    }

    void reset(List<AozoraWorkNode> workNodeList, boolean visible) {
        if (treePane != null)
            treePane.collapse();
        removeAllChildren();
        
        for (AozoraWorkNode workNode : workNodeList) {
            add(workNode);
        }

        if (treePane != null) {
            treePane.fireNodeChanged();
            treePane.setVisible(visible);
            treePane.collapse();
        }
    }

    boolean isVisible() {
        return treePane.isVisible();
    }

    int search(String search, boolean isAll) {
        int hit = 0;
        if (AozoraSearchUtil.searchAuthor(getAozoraAuthor(), search))
            hit++;
        final List<AozoraWorkNode> hitWorkNodeList = new ArrayList<AozoraWorkNode>();
        for (AozoraWorkNode workNode : isAll ? getAllAozoraWorkNodes() : getVisibleAozoraWorkNodes()) {
            if (AozoraSearchUtil.searchWork(workNode.getAozoraWork(), search))
                hitWorkNodeList.add(workNode);
        }

        hit += hitWorkNodeList.size();
        final boolean visible = hit != 0;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                reset(hitWorkNodeList, visible);
            }
        });
        return hit;
    }

    boolean isWorkLoaded() {
        return isWorkLoaded;
    }

    void setWorkLoaded(boolean isWorkLoaded) {
        this.isWorkLoaded = isWorkLoaded;
    }

    void focus(String workID, boolean scrollToTop, boolean expand) {
        if (treePane != null) {
            treePane.setVisible(true);
            if (expand)
                treePane.expand();
            TreePath focusPath = null;
            if (workID != null) {
                AozoraWorkNode workNode = getAozoraWorkNode(workID);
                if (workNode != null)
                    focusPath = new TreePath(workNode.getPath());
            }
            if (focusPath == null)
                focusPath = new TreePath(getPath());
            treePane.focus(focusPath);
            JViewport viewport = SGUIUtil.getParentInstanceOf(treePane, JViewport.class);
            if (scrollToTop)
                viewport.setViewPosition(new Point(0, treePane.getLocation().y));
            else
                viewport.setViewPosition(new Point(0, viewport.getViewPosition().y));
        }
    }

    boolean isExpanded() {
        return treePane.isExpanded();
    }

    boolean isSelected() {
        return treePane.getSelectionPath() != null;
    }

    void focusSelectedNode() {
        TreePath selectionPath = treePane.getSelectionPath();
        if (selectionPath != null)
            treePane.focus(selectionPath);
    }

    private AozoraAuthor author;
    private AozoraAuthorTreePane treePane;
    private List<AozoraWorkNode> allWorkNodeList;
    private boolean isWorkLoaded;
}
