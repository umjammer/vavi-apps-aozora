/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;


class AozoraTopicFolderNode extends DefaultMutableTreeNode {

    AozoraTopicFolderNode(String name) {
        super(name, true);
    }

    void setTreePane(AozoraTopicFolderTreePane treePane) {
        this.treePane = treePane;
    }

    public void add(MutableTreeNode newChild) {
        if (!(newChild instanceof AozoraTopicNode))
            throw new IllegalArgumentException("child must be AozoraTopicNode");
        AozoraTopicNode newTopicNode = (AozoraTopicNode) newChild;
        synchronized (this) {
            super.add(newTopicNode);
        }
        if (treePane != null) {
            newTopicNode.setTreePane(treePane);
            treePane.fireNodeChanged();
        }
    }

    private AozoraTopicFolderTreePane treePane;
}
