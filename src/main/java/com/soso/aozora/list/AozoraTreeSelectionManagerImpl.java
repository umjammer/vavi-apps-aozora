/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JTree;


class AozoraTreeSelectionManagerImpl implements AozoraTreeSelectionManager {

    AozoraTreeSelectionManagerImpl() {
    }

    public void addTree(JTree tree) {
        synchronized (treeSet) {
            treeSet.add(tree);
        }
    }

    public void setSelectOnly(JTree selectedTree) {
        synchronized (treeSet) {
            for (JTree tree : treeSet) {
                if (tree != selectedTree)
                    tree.setSelectionPath(null);
            }
        }
    }

    public void removeTree(JTree tree) {
        synchronized (treeSet) {
            treeSet.remove(tree);
        }
    }

    private final Set<JTree> treeSet = new HashSet<>();
}
