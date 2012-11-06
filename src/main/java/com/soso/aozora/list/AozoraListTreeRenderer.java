/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.core.AozoraUtil;
import com.soso.aozora.data.AozoraCacheManager;
import com.soso.aozora.data.AozoraComment;
import com.soso.aozora.data.AozoraCommentManager;


class AozoraListTreeRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer {

    AozoraListTreeRenderer(AozoraContext context) {
        this.context = context;
    }

    private AozoraContext getAzContext() {
        return context;
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        if (!(value instanceof AozoraAuthorNode) && (value instanceof AozoraWorkNode)) {
            AozoraWorkNode workNode = (AozoraWorkNode) value;
            try {
                AozoraCacheManager cacheManager = getAzContext().getCacheManager();
                if (cacheManager != null) {
                    String cacheID = workNode.getAozoraWork().getID();
                    if (cacheManager.isCached(cacheID) && cacheManager.getCacheBytes(cacheID, "AozoraAuthor") != null && cacheManager.getCacheBytes(cacheID, "AozoraWork") != null)
                        setForeground(AozoraEnv.CACHED_COLOR);
                }
            } catch (MalformedURLException e) {
                getAzContext().log(e);
            } catch (IOException e) {
                getAzContext().log(e);
            }
            AozoraCommentManager commentManager = getAzContext().getCommentManager();
            if (commentManager != null) {
                AozoraComment[] comments = commentManager.getComments(workNode.getAozoraWork().getID());
                if (comments.length != 0) {
                    Icon commentIcon = getCommentIcon();
                    int upMarginX = commentIcon.getIconWidth() / 3;
                    int upCount = comments.length / COMMENT_ICON_UP_UNIT;
                    BufferedImage bufferedImage = new BufferedImage(commentIcon.getIconWidth() + upMarginX * upCount, commentIcon.getIconHeight(), 6);
                    Graphics g = bufferedImage.getGraphics();
                    commentIcon.paintIcon(null, g, 0, 0);
                    for (int i = 0; i < upCount; i++)
                        commentIcon.paintIcon(null, g, upMarginX * (i + 1), 0);

                    if (comments[comments.length - 1].getTimestamp() > System.currentTimeMillis() - 0x5265c00L) {
                        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
                        g.setColor(Color.GRAY);
                        g.drawString("＊", upMarginX * upCount + 4, 12);
                        g.setColor(Color.BLUE);
                        g.drawString("＊", upMarginX * upCount + 3, 11);
                    }
                    setIcon(new ImageIcon(bufferedImage));
                    setToolTipText("コメント総数：" + comments.length);
                }
            }
        }
        return this;
    }

    private Icon getCommentIcon() {
        if (commentIcon == null)
            commentIcon = AozoraUtil.getIcon(AozoraEnv.Env.COMMENT_ICON.getString());
        return commentIcon;
    }

    private static final int COMMENT_ICON_UP_UNIT = 10;
    private final AozoraContext context;
    private Icon commentIcon;
}
