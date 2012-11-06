/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraDefaultPane;
import com.soso.aozora.core.AozoraUtil;
import com.soso.aozora.data.AozoraAuthor;
import com.soso.aozora.data.AozoraCacheManager;
import com.soso.aozora.data.AozoraWork;
import com.soso.sgui.SGUIUtil;


class AozoraCacheHolderPane extends AozoraDefaultPane {

    AozoraCacheHolderPane(AozoraContext context, String cacheID) {
        super(context);
        this.cacheID = cacheID;
        author = null;
        work = null;
        isBroken = true;
        initGUI();
    }

    AozoraCacheHolderPane(AozoraContext context, AozoraAuthor author, AozoraWork work) {
        super(context);
        cacheID = work.getID();
        this.author = author;
        this.work = work;
        isBroken = false;
        initGUI();
    }

    private void initGUI() {
        setLayout(new BorderLayout());
        titleLabel = new JLabel(getTitleName());
        titleLabel.setBorder(new EmptyBorder(1, 5, 1, 0));
        add(titleLabel, BorderLayout.CENTER);
        mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                requestSelected();
                if (e.getButton() == MouseEvent.BUTTON3)
                    showMenu(e.getX(), e.getY());
                else if (e.getClickCount() == 2)
                    showViewer();
            }

            public void mouseEntered(MouseEvent e) {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                titleLabel.setForeground(Color.BLUE);
            }

            public void mouseExited(MouseEvent e) {
                setCursor(null);
                titleLabel.setForeground(null);
            }
        };
        addMouseListener(mouseListener);
        initKeyAction();
    }

    private String getTitleName() {
        if (isBroken())
            return "壊れたキャッシュ (ID=" + getCacheID() + ")";
        else
            return getWork().getTitleName() + " - " + getAuthor().getName();
    }

    private void initKeyAction() {
        AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_FOCUSED, KeyStroke.getKeyStroke("UP"), "AozoraCacheHolderPane.upAction", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                requestSelectedPrev();
            }
        });
        AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_FOCUSED, KeyStroke.getKeyStroke("DOWN"), "AozoraCacheHolderPane.downAction", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                requestSelectedNext();
            }
        });
        AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_FOCUSED, KeyStroke.getKeyStroke("ENTER"), "AozoraCacheHolderPane.enterAction", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                showViewer();
            }
        });
    }

    private boolean isBroken() {
        return isBroken;
    }

    String getCacheID() {
        return cacheID;
    }

    AozoraAuthor getAuthor() {
        return author;
    }

    AozoraWork getWork() {
        return work;
    }

    private void requestSelected() {
        if (getParent() instanceof AozoraCacheListPane)
            ((AozoraCacheListPane) getParent()).setSelected(this);
    }

    private void requestSelectedNext() {
        if (getParent() instanceof AozoraCacheListPane)
            ((AozoraCacheListPane) getParent()).setSelectedNext(this);
    }

    private void requestSelectedPrev() {
        if (getParent() instanceof AozoraCacheListPane)
            ((AozoraCacheListPane) getParent()).setSelectedPrev(this);
    }

    void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        setOpaque(isSelected);
        setBackground(isSelected ? Color.LIGHT_GRAY : null);
        setBorder(isSelected ? LineBorder.createGrayLineBorder() : null);
        if (isSelected) {
            JViewport viewport = SGUIUtil.getParentInstanceOf(this, JViewport.class);
            if (viewport != null && !viewport.getViewRect().contains(getLocation()))
                scrollRectToVisible(new Rectangle(0, 0, 0, getHeight()));
            requestFocusInWindow();
        }
    }

    boolean isSelected() {
        return isSelected;
    }

    private void showMenu(int x, int y) {
        JPopupMenu menu = new JPopupMenu();
        if (!isBroken()) {
            JMenuItem openItem = new JMenuItem(new AbstractAction("キャッシュを開く") {
                public void actionPerformed(ActionEvent e) {
                    showViewer();
                }
            });
            menu.add(openItem);
        }
        JMenuItem deleteItem = new JMenuItem(new AbstractAction("キャッシュを削除") {
            public void actionPerformed(ActionEvent e) {
                cacheDelete();
            }
        });
        menu.add(deleteItem);
        menu.show(this, x, y);
    }

    private void showViewer() {
        getAzContext().getRootMediator().showViewer(getAuthor(), getWork());
    }

    private void cacheDelete() {
        if (!getAzContext().checkCachePermitted())
            throw new IllegalStateException("キャッシュ権限がありません。");
        AozoraCacheManager cacheManager = getAzContext().getCacheManager();
        if (cacheManager == null)
            throw new IllegalStateException("キャッシュマネージャーがありません。");
        if (cacheManager.isReadOnly())
            throw new IllegalStateException("キャッシュは読み込み専用です。");
        try {
            int rs = JOptionPane.showInternalConfirmDialog(getAzContext().getDesktopPane(), getTitleName() + " のキャッシュを削除しますか。", "削除の確認", 2, 3);
            if (rs == 0) {
                cacheManager.removeCache(getCacheID());
                JOptionPane.showInternalMessageDialog(getAzContext().getDesktopPane(), getTitleName() + " のキャッシュを削除しました。");
            }
        } catch (Exception e) {
            log(e);
            Object mssg = getTitleName() + " のキャッシュの削除でエラーが発生しました。";
            if (e.getMessage() != null)
                mssg = (new String[] {
                    (String) mssg, e.getMessage()
                });
            JOptionPane.showInternalMessageDialog(getAzContext().getDesktopPane(), mssg, "エラー", 0);
        }
    }

    private final String cacheID;
    private final AozoraAuthor author;
    private final AozoraWork work;
    private final boolean isBroken;
    private MouseListener mouseListener;
    private JLabel titleLabel;
    private boolean isSelected;
}
