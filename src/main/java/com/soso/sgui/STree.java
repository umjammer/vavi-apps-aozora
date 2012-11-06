/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;

import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;


public class STree extends JTree {

    public STree() {
    }

    public STree(Object[] value) {
        super(value);
    }

    public STree(Vector<?> value) {
        super(value);
    }

    public STree(Hashtable<?, ?> value) {
        super(value);
    }

    public STree(TreeNode root) {
        super(root);
    }

    public STree(TreeNode root, boolean asksAllowsChildren) {
        super(root, asksAllowsChildren);
    }

    public STree(TreeModel newModel) {
        super(newModel);
    }

    protected boolean rightButtonAction(MouseEvent event) {
        TreePath path = getClosestPathForLocation(event.getX(), event.getY());
        if (SwingUtilities.isRightMouseButton(event) && path != null) {
            setSelectionPath(path);
            return true;
        } else {
            return false;
        }
    }

    public void reload() {
        collapsePath(getSelectionPath());
    }

    private static final long serialVersionUID = 1L;
}
