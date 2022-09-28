/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import javax.swing.JTree;


interface AozoraTreeSelectionManager {

    void addTree(JTree tree);

    void setSelectOnly(JTree selectedTree);

    void removeTree(JTree tree);
}
