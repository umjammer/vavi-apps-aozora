/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraDefaultPane;
import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.data.AozoraComment;
import com.soso.aozora.data.AozoraCommentManager;
import com.soso.aozora.event.AozoraListenerAdapter;


public class AozoraCommentPane extends AozoraDefaultPane {

    public AozoraCommentPane(AozoraContext context) {
        super(context);
        visibleStart = -1;
        visibleLength = 0;
        initGUI();
        if (getAzContext().getLineMode().isConnectable())
            initDataAsynchronous();
    }

    private void initGUI() {
        setLayout(new BorderLayout());
        viewPane = new JPanel();
        viewPane.setLayout(new BoxLayout(viewPane, BoxLayout.PAGE_AXIS) {
            public Dimension preferredLayoutSize(Container target) {
                Dimension boxPrefSize = super.preferredLayoutSize(target);
                int parentWidth = getWidth();
                return new Dimension(Math.min(boxPrefSize.width, parentWidth), boxPrefSize.height);
            }
        });
        viewPane.setOpaque(true);
        viewPane.setBackground(AozoraEnv.DEFAULT_BACKGROUND_COLOR);
        add(viewPane, BorderLayout.CENTER);
        scrollBar = new JScrollBar(1);
        add(scrollBar, BorderLayout.EAST);
        scrollBar.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                switch (e.getAdjustmentType()) {
                case AdjustmentEvent.UNIT_INCREMENT:
                    setVisibleStart(getVisibleStart() + e.getValue());
                    break;
                case AdjustmentEvent.UNIT_DECREMENT:
                    setVisibleStart(getVisibleStart() - e.getValue());
                    break;
                case AdjustmentEvent.BLOCK_INCREMENT:
                    setVisibleStart(getVisibleStart() + getVisibleLength() * e.getValue());
                    break;
                case AdjustmentEvent.BLOCK_DECREMENT:
                    setVisibleStart(getVisibleStart() - getVisibleLength() * e.getValue());
                    break;
                case AdjustmentEvent.TRACK:
                    setVisibleStart(scrollBar.getMaximum() - 1 - e.getValue());
                    break;
                }
            }
        });
        addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                switch (e.getScrollType()) {
                case MouseWheelEvent.WHEEL_UNIT_SCROLL:
                    setVisibleStart(getVisibleStart() - e.getWheelRotation());
                    break;
                case MouseWheelEvent.WHEEL_BLOCK_SCROLL:
                    setVisibleStart(getVisibleStart() - getVisibleLength() * e.getWheelRotation());
                    break;
                }
            }
        });
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                resetComentHolders();
                getAzContext().getListenerManager().add(new AozoraListenerAdapter() {
                    public void commentAdded(AozoraComment comment) {
                        addComment(comment);
                    }
                });
                removeComponentListener(this);
                addComponentListener(new ComponentAdapter() {
                    public void componentResized(ComponentEvent e) {
                        resetComentHolders();
                    }
                });
            }
        });
        getAzContext().getListenerManager().add(new AozoraListenerAdapter() {
            public void lineModeChanged(AozoraEnv.LineMode lineMode) {
                boolean isConnectable = lineMode.isConnectable();
                if (isConnectable)
                    initDataAsynchronous();
                enableInputMethods(isConnectable);
            }
        });
    }

    private void resetComentHolders() {
        AozoraCommentManager commentManager = getAzContext().getCommentManager();
        if (commentManager.getCommentCount() == 0) {
            viewPane.add(new JLabel("コメントはありません。"));
            return;
        }
        int length = viewPane.getHeight() / 80;
        if (commentManager.getCommentCount() < length)
            length = commentManager.getCommentCount();
        setVisibleLength(length);
        if (getVisibleStart() == -1)
            setVisibleStart(commentManager.getCommentCount() - 1);
        else
            setVisibleStart(getVisibleStart());
    }

    private void setVisibleStart(int index) {
        AozoraCommentManager commentManager = getAzContext().getCommentManager();
        index = Math.max(index, visibleLength - 1);
        index = Math.min(index, commentManager.getCommentCount() - 1);
        visibleStart = index;
        for (int i = 0; i < getVisibleLength(); i++) {
            AozoraCommentHolderPane holderPane = (AozoraCommentHolderPane) viewPane.getComponent(i);
            AozoraComment comment = commentManager.getComment(index - i);
            holderPane.setComment(comment);
        }

        scrollBar.setMinimum(0);
        scrollBar.setMaximum(commentManager.getCommentCount());
        scrollBar.setValue(scrollBar.getMaximum() - 1 - index);
        scrollBar.setVisibleAmount(getVisibleLength());
        scrollBar.setUnitIncrement(1);
        scrollBar.setBlockIncrement(getVisibleLength());
    }

    private int getVisibleStart() {
        return visibleStart;
    }

    private void setVisibleLength(int length) {
        visibleLength = length;
        viewPane.removeAll();
        viewPane.revalidate();
        for (int i = 0; i < getVisibleLength(); i++) {
            AozoraCommentHolderPane holderPane = new AozoraCommentHolderPane(getAzContext());
            viewPane.add(holderPane);
        }
    }

    private int getVisibleLength() {
        return visibleLength;
    }

    private void initDataAsynchronous() {
        new Thread(new Runnable() {
            public void run() {
                initData();
            }
        }, "AozoraCommentPane_initData").start();
    }

    private void initData() {
        AozoraCommentManager commentManager = getAzContext().getCommentManager();
        if (commentManager == null)
            throw new IllegalStateException("コメントマネージャが見つかりません。");
        try {
            commentManager.updateComments();
        } catch (IOException e) {
            getAzContext().log(e);
            getAzContext().log("コメントのロードに失敗しました。");
        }
        resetComentHolders();
    }

    private void addComment(AozoraComment comment) {
        int current = getVisibleStart();
        int latest = getAzContext().getCommentManager().getCommentCount() - 1;
        if (getVisibleLength() == latest - 1) {
            resetComentHolders();
            setVisibleStart(latest);
        } else if (current == latest - 1)
            setVisibleStart(latest);
        else
            setVisibleStart(current);
    }

    public void focusSelectedComment() {
        requestFocusInWindow();
        AozoraCommentHolderPane firstHolderPane = null;
        int count = viewPane.getComponentCount();
        for (int i = 0; i < count; i++) {
            Component comp = viewPane.getComponent(i);
            if (comp instanceof AozoraCommentHolderPane) {
                AozoraCommentHolderPane holderPane = (AozoraCommentHolderPane) comp;
                if (firstHolderPane == null)
                    firstHolderPane = holderPane;
                if (holderPane.isSelected()) {
                    holderPane.setSelected(true);
                    return;
                }
            }
        }

        if (firstHolderPane != null)
            setSelected(firstHolderPane);
    }

    void setSelected(AozoraCommentHolderPane holderPane) {
        setSelected(holderPane, 0);
    }

    void setSelectedPrev(AozoraCommentHolderPane holderPane) {
        setSelected(holderPane, -1);
    }

    void setSelectedNext(AozoraCommentHolderPane holderPane) {
        setSelected(holderPane, 1);
    }

    private void setSelected(AozoraCommentHolderPane selectionBaseHolderPane, int selectionIndexDiff) {
        synchronized (getTreeLock()) {
            int selectionBaseIndex = -1;
            List<AozoraCommentHolderPane> tempHolderPaneList = new ArrayList<AozoraCommentHolderPane>();
            int count = viewPane.getComponentCount();
            for (int i = 0; i < count; i++) {
                Component comp = viewPane.getComponent(i);
                if (comp instanceof AozoraCommentHolderPane) {
                    AozoraCommentHolderPane holderPane = (AozoraCommentHolderPane) comp;
                    holderPane.setSelected(false);
                    tempHolderPaneList.add(holderPane);
                    if (selectionBaseHolderPane == holderPane)
                        selectionBaseIndex = tempHolderPaneList.size() - 1;
                }
            }

            if (selectionBaseIndex != -1) {
                int selectionIndex = selectionBaseIndex + selectionIndexDiff;
                if (selectionIndex < 0) {
                    setVisibleStart(getVisibleStart() + 1);
                    setSelected(selectionBaseHolderPane);
                } else if (selectionIndex >= tempHolderPaneList.size()) {
                    setVisibleStart(getVisibleStart() - 1);
                    setSelected(selectionBaseHolderPane);
                } else {
                    tempHolderPaneList.get(selectionIndex).setSelected(true);
                }
            }
        }
        repaint();
    }

    private JPanel viewPane;
    private JScrollBar scrollBar;
    private int visibleStart;
    private int visibleLength;
}
