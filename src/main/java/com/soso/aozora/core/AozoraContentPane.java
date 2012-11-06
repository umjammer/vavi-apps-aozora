/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.boot.AozoraSplashWindow;
import com.soso.aozora.data.AozoraPremierID;
import com.soso.aozora.event.AozoraListenerAdapter;
import com.soso.aozora.list.AozoraCachePane;
import com.soso.aozora.list.AozoraCommentPane;
import com.soso.aozora.list.AozoraHistoryPane;
import com.soso.aozora.list.AozoraListPane;
import com.soso.aozora.list.AozoraRankingPane;
import com.soso.aozora.list.AozoraTopicPane;


public class AozoraContentPane extends AozoraDefaultPane implements ChangeListener {

    private static class AboutPremierTrialLabel extends JLabel {

        private AozoraContext getAzContext() {
            return context;
        }

        private void initGUI() {
            setOpaque(false);
            setForeground(Color.BLUE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setText("試用ステータスと購入はこちら");
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    getAzContext().getRootMediator().aboutPremier();
                }

                public void mouseEntered(MouseEvent e) {
                    setBorder(new MatteBorder(0, 0, 1, 0, getForeground()));
                }

                public void mouseExited(MouseEvent e) {
                    setBorder(null);
                }
            });
        }

        private final AozoraContext context;

        private AboutPremierTrialLabel(AozoraContext context) {
            this.context = context;
            initGUI();
        }
    }

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
        setTabbedEnabled(getAzContext().getLineMode());
        AozoraSplashWindow.setProgress(AozoraSplashWindow.PROGRESS.GUI_CONTENT);
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
        AozoraSplashWindow.setProgress(AozoraSplashWindow.PROGRESS.GUI_MENU);
        setOpaque(false);
        add(menuPane, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        JPanel southPane = new JPanel();
        southPane.setOpaque(false);
        southPane.setLayout(new BorderLayout());
        southPane.add(Box.createVerticalStrut(40), BorderLayout.CENTER);
        if (getAzContext().isPremier() && getAzContext().getPremierID().getStatus() == AozoraPremierID.Status.TRIAL)
            southPane.add(new AboutPremierTrialLabel(getAzContext()), BorderLayout.EAST);
        add(southPane, BorderLayout.SOUTH);
        listPane = new AozoraListPane(getAzContext());
        tabbedPane.addTab("", AozoraUtil.getIcon(AozoraEnv.Env.DATABASE_ICON.getString()), listPane, "作品一覧");
        AozoraSplashWindow.setProgress(AozoraSplashWindow.PROGRESS.GUI_LIST);
        topicPane = new AozoraTopicPane(getAzContext());
        tabbedPane.addTab("", AozoraUtil.getIcon(AozoraEnv.Env.FEED_ICON.getString()), topicPane, "新着情報");
        AozoraSplashWindow.setProgress(AozoraSplashWindow.PROGRESS.GUI_TOPIC);
        rankingPane = new AozoraRankingPane(getAzContext());
        tabbedPane.addTab("", AozoraUtil.getIcon(AozoraEnv.Env.RANKING_ICON.getString()), rankingPane, "ランキング");
        AozoraSplashWindow.setProgress(AozoraSplashWindow.PROGRESS.GUI_RANKING);
        commentPane = new AozoraCommentPane(getAzContext());
        tabbedPane.addTab("", AozoraUtil.getIcon(AozoraEnv.Env.COMMENT_ICON.getString()), commentPane, "コメント");
        AozoraSplashWindow.setProgress(AozoraSplashWindow.PROGRESS.GUI_COMMENT);
        historyPane = new AozoraHistoryPane(getAzContext());
        tabbedPane.addTab("", AozoraUtil.getIcon(AozoraEnv.Env.HISTORY_ICON.getString()), historyPane, "履歴");
        AozoraSplashWindow.setProgress(AozoraSplashWindow.PROGRESS.GUI_HISTORY);
        if (getAzContext().checkCachePermitted())
            try {
                cachePane = new AozoraCachePane(getAzContext());
                tabbedPane.addTab("", AozoraUtil.getIcon(AozoraEnv.Env.CACHE_FOLDER_ICON.getString()), cachePane, "キャッシュ");
                AozoraSplashWindow.setProgress(AozoraSplashWindow.PROGRESS.GUI_CACHE);
            } catch (Exception e) {
                log(e);
                log("キャッシュ一覧の作成に失敗しました。");
                javax.swing.JComponent filler = new JPanel();
                tabbedPane.addTab("", AozoraUtil.getIcon(AozoraEnv.Env.CACHE_FOLDER_ICON.getString()), filler, "キャッシュ");
                tabbedPane.setEnabledAt(tabbedPane.indexOfComponent(filler), false);
            }
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
        tabbedPane.setEnabledAt(tabbedPane.indexOfComponent(commentPane), isConnectable);
        tabbedPane.setSelectedComponent(isConnectable ? listPane : cachePane);
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

    AozoraCachePane getAozoraCachePane() {
        return cachePane;
    }

    AozoraCommentPane getAozoraCommentPane() {
        return commentPane;
    }

    void focusSelectedPane() {
        Component selectedComp = tabbedPane.getSelectedComponent();
        if (selectedComp != null) {
            selectedComp.requestFocusInWindow();
            if (selectedComp instanceof AozoraListPane)
                ((AozoraListPane) selectedComp).focusSelectedNode();
            else if (selectedComp instanceof AozoraTopicPane)
                ((AozoraTopicPane) selectedComp).focusSelectedTopic();
            else if (selectedComp instanceof AozoraCommentPane)
                ((AozoraCommentPane) selectedComp).focusSelectedComment();
            else if (selectedComp instanceof AozoraHistoryPane)
                ((AozoraHistoryPane) selectedComp).focusSelectedHistory();
            else if (selectedComp instanceof AozoraCachePane)
                ((AozoraCachePane) selectedComp).focusSelectedCache();
        }
    }

    private JSplitPane splitPane;
    private AozoraListPane listPane;
    private AozoraTopicPane topicPane;
    private AozoraRankingPane rankingPane;
    private AozoraCachePane cachePane;
    private AozoraCommentPane commentPane;
    private AozoraHistoryPane historyPane;
    private JTabbedPane tabbedPane;
    private AozoraMenuPane menuPane;
}
