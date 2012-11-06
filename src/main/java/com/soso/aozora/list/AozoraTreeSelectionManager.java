/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import javax.swing.JTree;


interface AozoraTreeSelectionManager {

    public abstract void addTree(JTree tree);

    public abstract void setSelectOnly(JTree selectedTree);

    public abstract void removeTree(JTree tree);
}
