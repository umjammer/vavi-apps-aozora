/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraDefaultPane;
import com.soso.aozora.data.AozoraAuthor;
import com.soso.aozora.data.AozoraAuthorParserHandler;
import com.soso.aozora.data.AozoraWork;
import com.soso.aozora.data.AozoraWorkParserHandler;
import com.soso.sgui.SGUIUtil;


class AozoraRankingListPane extends AozoraDefaultPane {

    static class RankingHolderPane extends AozoraDefaultPane {

        private enum RankingType {
            card,
            author
        }

        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            size.height = PANE_HEIGHT;
            return size;
        }

        public Dimension getSize() {
            Dimension size = super.getSize();
            size.height = PANE_HEIGHT;
            return size;
        }

        public Dimension getSize(Dimension rv) {
            Dimension size = super.getSize(rv);
            size.height = PANE_HEIGHT;
            return size;
        }

        public Dimension getMaximumSize() {
            Dimension size = super.getMaximumSize();
            size.height = PANE_HEIGHT;
            return size;
        }

        public Dimension getMinimumSize() {
            Dimension size = super.getMinimumSize();
            size.height = PANE_HEIGHT;
            return size;
        }

        private void initGUI() {
            setOpaque(false);
            setLayout(new BorderLayout());
            rankLabel = new JLabel(" 第" + rank + "位 (" + entry.getCount() + "): ");
            Dimension rankLabelPrefSize = rankLabel.getPreferredSize();
            SGUIUtil.setSizeALL(rankLabel, new Dimension(Math.max(100, rankLabelPrefSize.width), Math.max(PANE_HEIGHT, rankLabelPrefSize.height)));
            add(rankLabel, BorderLayout.WEST);
            mouseListener = new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    showContents();
                }

                public void mouseEntered(MouseEvent e) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                    setForeground(Color.BLUE);
                }

                public void mouseExited(MouseEvent e) {
                    setCursor(null);
                    setForeground(null);
                }
            };
            addMouseListener(mouseListener);
            titleLabel = new JLabel();
            titleLabel.addMouseListener(mouseListener);
            add(titleLabel, BorderLayout.CENTER);
            titleLabel.setText("ロード中...");
            resetLabelText();
        }

        public void setForeground(Color fg) {
            super.setForeground(fg);
            if (rankLabel != null)
                rankLabel.setForeground(fg);
            if (titleLabel != null)
                titleLabel.setForeground(fg);
        }

        void resetLabelText() {
            switch (type) {
            case card:
                getAzContext().getRootMediator().getAozoraWorkAsynchronous(entry.getID(), work -> setAozoraWork(work));
                break;
            case author:
                getAzContext().getRootMediator().getAozoraAuthorAsynchronous(entry.getID(), author -> setAozoraAuthor(author));
                break;
            default:
                throw new IllegalStateException("Unknown ranking type:" + type);
            }
        }

        boolean isLoading() {
            switch (type) {
            case card:
                return getAozoraWork() == null || getAozoraAuthor() == null;
            case author:
                return getAozoraAuthor() == null;
            }
            throw new IllegalStateException("Unknown ranking type:" + type);
        }

        void setTitleText(final String text) {
            final JLabel theLabel = titleLabel;
            SwingUtilities.invokeLater(() -> {
                theLabel.setText(text);
                repaint();
            });
        }

        void setAozoraWork(AozoraWork work) {
            this.work = work;
            if (work != null && type == RankingType.card) {
                String personID = work.getAuthorID();
                if (personID == null || personID.length() == 0)
                    personID = work.getTranslatorID();
                if (personID != null && personID.length() != 0)
                    setAozoraAuthor(getAzContext().getRootMediator().getAozoraAuthor(personID));
                setTitleText(work.getTitleName());
            }
        }

        void setAozoraAuthor(AozoraAuthor author) {
            this.author = author;
            if (author != null && type == RankingType.author)
                setTitleText(author.getName());
        }

        private AozoraWork getAozoraWork() {
            return work;
        }

        private AozoraAuthor getAozoraAuthor() {
            return author;
        }

        void showContents() {
            if (!isLoading())
                switch (type) {
                case card:
                    getAzContext().getRootMediator().focusWork(getAozoraWork());
                    getAzContext().getRootMediator().showViewer(getAozoraAuthor(), getAozoraWork());
                    break;
                case author:
                    getAzContext().getRootMediator().focusAuthor(getAozoraAuthor());
                    break;
                default:
                    throw new IllegalStateException("Unknown ranking type:" + type);
                }
        }

        static final int PANE_HEIGHT = 30;
        private final RankingType type;
        private final int rank;
        private final AozoraRankingEntry entry;
        private MouseListener mouseListener;
        private JLabel rankLabel;
        private JLabel titleLabel;
        private AozoraAuthor author;
        private AozoraWork work;

        RankingHolderPane(AozoraContext context, int rank, AozoraRankingEntry entry) {
            super(context);
            this.rank = rank;
            this.entry = entry;
            if (this.entry.getID().startsWith("card"))
                type = RankingType.card;
            else if (this.entry.getID().startsWith("person"))
                type = RankingType.author;
            else
                throw new IllegalStateException("contents not found for ranking id:" + entry.getID());
            initGUI();
        }
    }

    AozoraRankingListPane(AozoraContext context) {
        super(context);
        initGUI();
    }

    private void initGUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    void setRanking(List<AozoraRankingEntry> rankingList) {
        removeAll();
        revalidate();
        if (rankingList == null)
            add(new JLabel("取得に失敗しました。"));
        else if (rankingList.size() == 0) {
            add(new JLabel("記録がありません。"));
        } else {
            Collections.sort(rankingList);
            int curRank = 0;
            int curCount = 0x7fffffff;
            for (AozoraRankingEntry entry : rankingList) {
                if (entry.getCount() < curCount) {
                    curRank++;
                    curCount = entry.getCount();
                }
                add(createHolderPane(curRank, entry));
            }

        }
        add(Box.createGlue());
        repaint();
    }

    private JComponent createHolderPane(int rank, AozoraRankingEntry entry) {
        return new RankingHolderPane(getAzContext(), rank, entry);
    }
}
