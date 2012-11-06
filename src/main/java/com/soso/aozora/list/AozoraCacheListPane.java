/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraDefaultPane;
import com.soso.aozora.data.AozoraAuthor;
import com.soso.aozora.data.AozoraWork;


class AozoraCacheListPane extends AozoraDefaultPane {

    AozoraCacheListPane(AozoraContext context) {
        super(context);
        initGUI();
    }

    private void initGUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    void addCachePane(AozoraAuthor author, AozoraWork work) {
        add(new AozoraCacheHolderPane(getAzContext(), author, work), 0);
    }

    void addBrokenCachePane(String cacheID) {
        add(new AozoraCacheHolderPane(getAzContext(), cacheID), 0);
    }

    void removeCachePane(String cacheID) {
        synchronized (getTreeLock()) {
            for (Component comp : getComponents()) {
                if (!(comp instanceof AozoraCacheHolderPane))
                    continue;
                AozoraCacheHolderPane holderPane = (AozoraCacheHolderPane) comp;
                if (holderPane.getCacheID().equals(cacheID))
                    remove(comp);
            }
        }
    }

    AozoraAuthor getAozoraAuthor(String authorID) {
        synchronized (getTreeLock()) {
            for (Component comp : getComponents()) {
                if (comp instanceof AozoraCacheHolderPane) {
                    AozoraCacheHolderPane holderPane = (AozoraCacheHolderPane) comp;
                    AozoraAuthor author = holderPane.getAuthor();
                    if (author != null && author.getID().equals(authorID))
                        return author;
                }
            }
        }
        return null;
    }

    AozoraWork getAozoraWork(String workID) {
        synchronized (getTreeLock()) {
            for (Component comp : getComponents()) {
                if (comp instanceof AozoraCacheHolderPane) {
                    AozoraCacheHolderPane holderPane = (AozoraCacheHolderPane) comp;
                    AozoraWork work = holderPane.getWork();
                    if (work != null && work.getID().equals(workID))
                        return work;
                }
            }
        }
        return null;
    }

    void focusWork(AozoraWork work) {
        synchronized (getTreeLock()) {
            for (Component comp : getComponents()) {
                if (comp instanceof AozoraCacheHolderPane) {
                    AozoraCacheHolderPane holderPane = (AozoraCacheHolderPane) comp;
                    if (holderPane.getCacheID().equals(work.getID())) {
                        setSelected(holderPane);
                        break;
                    }
                }
            }
        }
    }

    void focusSelectedCache() {
        requestFocusInWindow();
        int count = getComponentCount();
        for (int i = 0; i < count; i++) {
            Component comp = getComponent(i);
            if (comp instanceof AozoraCacheHolderPane) {
                AozoraCacheHolderPane holderPane = (AozoraCacheHolderPane) comp;
                if (holderPane.isSelected()) {
                    holderPane.setSelected(true);
                    return;
                }
            }
        }
    }

    void setSelected(AozoraCacheHolderPane holderPane) {
        setSelected(holderPane, 0);
    }

    void setSelectedPrev(AozoraCacheHolderPane holderPane) {
        setSelected(holderPane, -1);
    }

    void setSelectedNext(AozoraCacheHolderPane holderPane) {
        setSelected(holderPane, 1);
    }

    private void setSelected(AozoraCacheHolderPane selectionBaseHolderPane, int selectionIndexDiff) {
        synchronized (getTreeLock()) {
            int selectionBaseIndex = -1;
            List<AozoraCacheHolderPane> tempHolderPaneList = new ArrayList<AozoraCacheHolderPane>();
            int count = getComponentCount();
            for (int i = 0; i < count; i++) {
                Component comp = getComponent(i);
                if (comp instanceof AozoraCacheHolderPane) {
                    AozoraCacheHolderPane holderPane = (AozoraCacheHolderPane) comp;
                    holderPane.setSelected(false);
                    tempHolderPaneList.add(holderPane);
                    if (selectionBaseHolderPane == holderPane)
                        selectionBaseIndex = tempHolderPaneList.size() - 1;
                }
            }

            if (selectionBaseIndex != -1) {
                int selectionIndex = selectionBaseIndex + selectionIndexDiff;
                if (selectionIndex < 0)
                    tempHolderPaneList.get(0).setSelected(true);
                else if (selectionIndex >= tempHolderPaneList.size())
                    tempHolderPaneList.get(tempHolderPaneList.size() - 1).setSelected(true);
                else
                    tempHolderPaneList.get(selectionIndex).setSelected(true);
            }
        }
        repaint();
    }
}
