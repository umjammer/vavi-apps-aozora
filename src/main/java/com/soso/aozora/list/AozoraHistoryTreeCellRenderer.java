/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.soso.aozora.data.AozoraAuthor;
import com.soso.aozora.data.AozoraWork;


class AozoraHistoryTreeCellRenderer extends DefaultTreeCellRenderer {

    AozoraHistoryTreeCellRenderer() {
        orginalFont = null;
        orginalColor = null;
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if (value instanceof AozoraHistoryTermNode) {
            Font f = getFont();
            if (f != null && !"Monospaced".equals(f.getName())) {
                if (orginalFont == null)
                    orginalFont = f;
                setFont(new Font("Monospaced", f.getStyle(), f.getSize()));
            }
            setToolTipText(((AozoraHistoryTermNode) value).getToolTipText());
        } else if (orginalFont != null)
            setFont(orginalFont);
        if (value instanceof AozoraHistoryEntryNode) {
            AozoraHistoryEntryNode entryNode = (AozoraHistoryEntryNode) value;
            AozoraAuthor author = entryNode.getAuthor();
            AozoraWork work = entryNode.getWork();
            if ((author == null) | (work == null)) {
                if (orginalColor == null)
                    orginalColor = getForeground();
                setForeground(Color.GRAY);
            } else if (orginalColor != null)
                setForeground(orginalColor);
            setToolTipText(((AozoraHistoryEntryNode) value).getToolTipText());
        } else if (orginalColor != null)
            setForeground(orginalColor);
        return this;
    }

    private Font orginalFont;
    private Color orginalColor;
}
