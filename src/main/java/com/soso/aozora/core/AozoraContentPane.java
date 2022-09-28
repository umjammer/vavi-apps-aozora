/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.core;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.event.AozoraListenerAdapter;
import com.soso.aozora.list.AozoraHistoryPane;
import com.soso.aozora.list.AozoraListPane;
import com.soso.aozora.list.AozoraRankingPane;
import com.soso.aozora.list.AozoraTopicPane;


/**
 * Left Tabbed Pane
 */
public class AozoraContentPane extends AozoraDefaultPane implements ChangeListener {

    static Logger logger = Logger.getLogger(AozoraContentPane.class.getName());

    private class ContentTabbedPane extends JTabbedPane implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            int selectedIndex = getSelectedIndex();
            if (lastSelectedIndex == selectedIndex)
                return;
            lastSelectedIndex = selectedIndex;
            int count = getTabCount();
            for (int i = 0; i < count; i++)
                if (i == selectedIndex)
                    setTitleAt(i, getToolTipTextAt(i));
                else
                    setTitleAt(i, "");

            if (splitPane.getDividerLocation() < getMinimumSize().width)
                splitPane.setDividerLocation(getMinimumSize().width);
        }

        public Dimension getPreferredSize() {
            Dimension prefSize = super.getPreferredSize();
            return new Dimension(Math.max(prefSize.width, getMinimumSize().width), prefSize.height);
        }

        public Dimension getMinimumSize() {
            Dimension minSize = super.getMinimumSize();
            Rectangle tabBounds = getTabBounds();
            return new Dimension(Math.max(minSize.width, tabBounds.x + tabBounds.width + 10), minSize.height);
        }

        private Rectangle getTabBounds() {
            Rectangle tabBounds = null;
            int count = getTabCount();
            for (int i = 0; i < count; i++) {
                Rectangle a = getUI().getTabBounds(this, i);
                tabBounds = tabBounds != null ? tabBounds.union(a) : a;
            }

            return tabBounds;
        }

        private int lastSelectedIndex;

        private ContentTabbedPane() {
            lastSelectedIndex = -1;
            addChangeListener(this);
        }
    }

    public AozoraContentPane(AozoraContext context) {
        super(context);
        initGUI();
    }

    private void initGUI() {
        setLayout(new BorderLayout());
        tabbedPane = new ContentTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setTabPlacement(JTabbedPane.TOP);
        tabbedPane.addChangeListener(this);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(tabbedPane);
        splitPane.setRightComponent(Box.createGlue());
        splitPane.setOpaque(false);
        menuPane = new AozoraMenuPane(getAzContext());
        setOpaque(false);
        add(menuPane, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        JPanel southPane = new JPanel();
        southPane.setOpaque(false);
        southPane.setLayout(new BorderLayout());
        southPane.add(Box.createVerticalStrut(40), BorderLayout.CENTER);
        add(southPane, BorderLayout.SOUTH);
        listPane = new AozoraListPane(getAzContext());
        tabbedPane.addTab("", AozoraUtil.getIcon(AozoraEnv.Env.DATABASE_ICON.getString()), listPane, "作品一覧");
        topicPane = new AozoraTopicPane(getAzContext());
        tabbedPane.addTab("", AozoraUtil.getIcon(AozoraEnv.Env.FEED_ICON.getString()), topicPane, "新着情報");
        rankingPane = new AozoraRankingPane(getAzContext());
        tabbedPane.addTab("", AozoraUtil.getIcon(AozoraEnv.Env.RANKING_ICON.getString()), rankingPane, "ランキング");
        historyPane = new AozoraHistoryPane(getAzContext());
        tabbedPane.addTab("", AozoraUtil.getIcon(AozoraEnv.Env.HISTORY_ICON.getString()), historyPane, "履歴");
        getAzContext().getListenerManager().add(new AozoraListenerAdapter() {
            public void lineModeChanged(AozoraEnv.LineMode lineMode) {
                setTabbedEnabled(lineMode);
            }
        });
    }

    private void setTabbedEnabled(AozoraEnv.LineMode lineMode) {
        boolean isConnectable = lineMode.isConnectable();
        tabbedPane.setEnabledAt(tabbedPane.indexOfComponent(listPane), isConnectable);
        tabbedPane.setEnabledAt(tabbedPane.indexOfComponent(topicPane), isConnectable);
        tabbedPane.setEnabledAt(tabbedPane.indexOfComponent(rankingPane), isConnectable);
        splitPane.setDividerLocation(tabbedPane.getMinimumSize().width);
    }

    public void stateChanged(ChangeEvent e) {
        if (tabbedPane.getSelectedComponent() == rankingPane)
            rankingPane.stateChanged(e);
    }

    AozoraListPane getAozoraListPane() {
        return listPane;
    }

    AozoraMenuPane getAozoraMenuPane() {
        return menuPane;
    }

    void focusSelectedPane() {
        Component selectedComp = tabbedPane.getSelectedComponent();
        if (selectedComp != null) {
            selectedComp.requestFocusInWindow();
            if (selectedComp instanceof AozoraListPane)
                ((AozoraListPane) selectedComp).focusSelectedNode();
            else if (selectedComp instanceof AozoraTopicPane)
                ((AozoraTopicPane) selectedComp).focusSelectedTopic();
            else if (selectedComp instanceof AozoraHistoryPane)
                ((AozoraHistoryPane) selectedComp).focusSelectedHistory();
        }
    }

    private JSplitPane splitPane;
    private AozoraListPane listPane;
    private AozoraTopicPane topicPane;
    private AozoraRankingPane rankingPane;
    private AozoraHistoryPane historyPane;
    private JTabbedPane tabbedPane;
    private AozoraMenuPane menuPane;
}
