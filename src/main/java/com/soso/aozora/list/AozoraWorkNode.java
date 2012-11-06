/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import com.soso.aozora.data.AozoraWork;


class AozoraWorkNode extends DefaultMutableTreeNode {

    AozoraWorkNode(AozoraWork work) {
        super(work.getTitleName(), false);
        this.work = work;
    }

    public void add(MutableTreeNode newChild) {
        throw new UnsupportedOperationException("this must be Leaf");
    }

    AozoraWork getAozoraWork() {
        return work;
    }

    void setTreePane(AozoraAuthorTreePane treePane) {
        this.treePane = treePane;
    }

    void fireWorkChange(AozoraWork newWork) {
        if (!work.getID().equals(newWork.getID()))
            throw new IllegalArgumentException("must be same workID old:" + work + "; new:" + newWork);
        work = newWork;
        setUserObject(newWork.getTitleName());
        if (treePane != null)
            treePane.fireNodeChanged();
    }

    private AozoraWork work;
    private AozoraAuthorTreePane treePane;
}
