/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import com.soso.aozora.boot.AozoraContext;


class AozoraListTreeRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer {

    AozoraListTreeRenderer(AozoraContext context) {
        this.context = context;
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        if (!(value instanceof AozoraAuthorNode) && (value instanceof AozoraWorkNode)) {
            AozoraWorkNode workNode = (AozoraWorkNode) value;
        }
        return this;
    }

    private final AozoraContext context;
}
