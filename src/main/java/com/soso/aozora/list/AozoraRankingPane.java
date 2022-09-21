/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.awt.BorderLayout;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraDefaultPane;
import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.event.AozoraListenerAdapter;


public class AozoraRankingPane extends AozoraDefaultPane implements ChangeListener {

    private static abstract class AozoraMonthlyBaseRankingPane extends AozoraRankingBasePane {

        Date getInitDate() {
            Calendar cal = getTodayCalender();
            cal.set(Calendar.DATE, 1);
            return cal.getTime();
        }

        Date getPrevDate(Date date) {
            Calendar cal = getTodayCalender();
            cal.setTime(date);
            cal.add(Calendar.MONTH, -1);
            return cal.getTime();
        }

        Date getNextDate(Date date) {
            Calendar cal = getTodayCalender();
            cal.setTime(date);
            cal.add(Calendar.MONTH, 1);
            return cal.getTime();
        }

        URL getURL(Date date) {
            Calendar cal = getTodayCalender();
            cal.setTime(date);
            String fileName = "monthly."+ new SimpleDateFormat("yyyy.MM").format(cal.getTime()) + getSuffix();
            try {
                return new URL(AozoraEnv.getRankingDataURL(), fileName);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        abstract String getSuffix();

        AozoraMonthlyBaseRankingPane(AozoraContext context) {
            super(context);
        }
    }

    private static class AozoraMonthlyAuthorRankingPane extends AozoraMonthlyBaseRankingPane {

        String getSuffix() {
            return ".author.txt";
        }

        AozoraMonthlyAuthorRankingPane(AozoraContext context) {
            super(context);
        }
    }

    private static class AozoraMonthlyWorkRankingPane extends AozoraMonthlyBaseRankingPane {

        String getSuffix() {
            return ".card.txt";
        }

        AozoraMonthlyWorkRankingPane(AozoraContext context) {
            super(context);
        }
    }

    private static abstract class AozoraWeeklyBaseRankingPane extends AozoraRankingBasePane {

        Date getInitDate() {
            Calendar cal = getTodayCalender();
            cal.set(Calendar.DAY_OF_WEEK, 2);
            return cal.getTime();
        }

        Date getPrevDate(Date date) {
            Calendar cal = getTodayCalender();
            cal.setTime(date);
            cal.add(Calendar.WEEK_OF_YEAR, -1);
            return cal.getTime();
        }

        Date getNextDate(Date date) {
            Calendar cal = getTodayCalender();
            cal.setTime(date);
            cal.add(Calendar.WEEK_OF_YEAR, 1);
            return cal.getTime();
        }

        URL getURL(Date date) {
            Calendar cal = getTodayCalender();
            cal.setTime(date);
            String fileName = "weekly."+ new SimpleDateFormat("yyyy.MM.dd").format(cal.getTime()) + getSuffix();
            try {
                return new URL(AozoraEnv.getRankingDataURL(), fileName);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        abstract String getSuffix();

        AozoraWeeklyBaseRankingPane(AozoraContext context) {
            super(context);
        }
    }

    private static class AozoraWeeklyAuthorRankingPane extends AozoraWeeklyBaseRankingPane {

        String getSuffix() {
            return ".author.txt";
        }

        AozoraWeeklyAuthorRankingPane(AozoraContext context) {
            super(context);
        }
    }

    private static class AozoraWeeklyWorkRankingPane extends AozoraWeeklyBaseRankingPane {

        String getSuffix() {
            return ".card.txt";
        }

        AozoraWeeklyWorkRankingPane(AozoraContext context) {
            super(context);
        }
    }

    public AozoraRankingPane(AozoraContext context) {
        super(context);
        initGUI();
    }

    private void initGUI() {
        setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
        try {
            tabbedPane.addTab("作品別 [週間]", new AozoraWeeklyWorkRankingPane(getAzContext()));
            tabbedPane.addTab("作品別 [月間]", new AozoraMonthlyWorkRankingPane(getAzContext()));
            tabbedPane.addTab("著者別 [週間]", new AozoraWeeklyAuthorRankingPane(getAzContext()));
            tabbedPane.addTab("著者別 [月間]", new AozoraMonthlyAuthorRankingPane(getAzContext()));
            tabbedPane.addChangeListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        getAzContext().getListenerManager().add(new AozoraListenerAdapter() {
            public void lineModeChanged(AozoraEnv.LineMode lineMode) {
                boolean isConnectable = lineMode.isConnectable();
                enableInputMethods(isConnectable);
            }
        });
    }

    public void stateChanged(ChangeEvent e) {
        AozoraRankingBasePane selectedRankingPane = (AozoraRankingBasePane) tabbedPane.getSelectedComponent();
        if (selectedRankingPane.getCurrentDate() == null)
            selectedRankingPane.setCurrentDate(selectedRankingPane.getInitDate());
    }

    private JTabbedPane tabbedPane;
}
