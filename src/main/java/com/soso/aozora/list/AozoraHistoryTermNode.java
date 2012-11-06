/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import javax.swing.tree.DefaultMutableTreeNode;


class AozoraHistoryTermNode extends DefaultMutableTreeNode {

    AozoraHistoryTermNode(String title, String toolTip, long termStart, long termEnd) {
        super(title, true);
        this.toolTip = toolTip;
        this.termStart = termStart;
        this.termEnd = termEnd;
    }

    long getTermStart() {
        return termStart;
    }

    long getTermEnd() {
        return termEnd;
    }

    String getToolTipText() {
        return toolTip + " (" + getChildCount() + ")";
    }

    AozoraHistoryEntryNode[] getEntryNodes() {
        int count = getChildCount();
        AozoraHistoryEntryNode[] entryNodes = new AozoraHistoryEntryNode[count];
        for (int i = 0; i < count; i++)
            entryNodes[i] = (AozoraHistoryEntryNode) getChildAt(i);

        return entryNodes;
    }

    private String toolTip;
    private final long termStart;
    private final long termEnd;
}
