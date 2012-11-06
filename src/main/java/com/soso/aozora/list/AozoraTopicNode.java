/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import javax.swing.tree.DefaultMutableTreeNode;


abstract class AozoraTopicNode extends DefaultMutableTreeNode {

    AozoraTopicNode() {
        this(null);
    }

    AozoraTopicNode(Object userObject) {
        super(userObject, false);
    }

    void setTreePane(AozoraTopicFolderTreePane treePane) {
        this.treePane = treePane;
    }

    void fireNodeChanged() {
        if (treePane != null)
            treePane.fireNodeChanged();
    }

    abstract void showTopic();

    private AozoraTopicFolderTreePane treePane;
}
