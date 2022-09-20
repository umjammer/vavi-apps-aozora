/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraDefaultPane;
import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.core.AozoraUtil;
import com.soso.sgui.SButton;
import com.soso.sgui.SGUIUtil;


abstract class AozoraRankingBasePane extends AozoraDefaultPane {

    static Logger logger = Logger.getLogger(AozoraTopicPane.class.getName());

    AozoraRankingBasePane(AozoraContext context) {
        super(context);
        initGUI();
    }

    private void initGUI() {
        setLayout(new BorderLayout());
        add(createButtonPane(), BorderLayout.NORTH);
        add(createListPane(), BorderLayout.CENTER);
    }

    private JPanel createButtonPane() {
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BorderLayout());
        prevButton = new SButton();
        prevButton.setIcon(AozoraUtil.getIcon(AozoraEnv.Env.GO_LEFT_VIEW_ICON.getString()));
        prevButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Date curDate = getCurrentDate();
                if (curDate == null)
                    curDate = getInitDate();
                setCurrentDate(getPrevDate(curDate));
            }
        });
        SGUIUtil.setSizeALL(prevButton, new Dimension(18, 18));
        buttonPane.add(prevButton, BorderLayout.WEST);
        nextButton = new SButton();
        nextButton.setIcon(AozoraUtil.getIcon(AozoraEnv.Env.GO_RIGHT_VIEW_ICON.getString()));
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Date curDate = getCurrentDate();
                if (curDate == null)
                    curDate = getInitDate();
                setCurrentDate(getNextDate(curDate));
            }
        });
        SGUIUtil.setSizeALL(nextButton, new Dimension(18, 18));
        buttonPane.add(nextButton, BorderLayout.EAST);
        termLabel = new JLabel();
        termLabel.setHorizontalAlignment(JLabel.CENTER);
        buttonPane.add(termLabel, BorderLayout.CENTER);
        sumLabel = new JLabel();
        sumLabel.setHorizontalAlignment(JLabel.RIGHT);
        Font font = sumLabel.getFont();
        if (font != null)
            font = new Font(font.getName(), Font.BOLD, font.getSize());
        sumLabel.setFont(font);
        buttonPane.add(sumLabel, BorderLayout.SOUTH);
        return buttonPane;
    }

    private JComponent createListPane() {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listPane = new AozoraRankingListPane(getAzContext());
        scrollPane.getVerticalScrollBar().setUnitIncrement(30);
        scrollPane.setViewportView(listPane);
        return scrollPane;
    }

    Date getCurrentDate() {
        return current;
    }

    void setCurrentDate(Date date) {
        if (date == null)
            throw new IllegalArgumentException("date cannot be null");
        current = date;
        termLabel.setText(getTermText(date, getNextDate(date)));
        List<AozoraRankingEntry> rankingList = getRankingList(getURL(date));
        listPane.setRanking(rankingList);
        int sum = 0;
        for (AozoraRankingEntry entry : rankingList) {
            sum += entry.getCount();
        }

        sumLabel.setText("閲覧総数 (" + sum + ")");
    }

    String getTermText(Date start, Date end) {
        StringBuilder sb = new StringBuilder();
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        if (start != null)
            sb.append(format.format(start));
        sb.append(" - ");
        if (end != null)
            sb.append(format.format(new Date(end.getTime() - 1L)));
        return sb.toString();
    }

    List<AozoraRankingEntry> getRankingList(URL url) {
        List<AozoraRankingEntry> rankingList = new ArrayList<AozoraRankingEntry>();
        try {
            InputStream in = null;
            BufferedReader br = null;
            try {
                in = AozoraUtil.getInputStream(url);
                br = new BufferedReader(new InputStreamReader(in, "utf8"));
                String line;
                while ((line = br.readLine()) != null && line.length() > 0) {
                    String array[] = line.split(",");
                    rankingList.add(new AozoraRankingEntry(array[0], Integer.parseInt(array[1])));
                }

            } finally {
                br.close();
                if (in != null)
                    in.close();
            }
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "記録がありません。:" + e.getMessage(), e);
            return null;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        }
        return rankingList;
    }

    abstract Date getInitDate();

    abstract Date getPrevDate(Date date);

    abstract Date getNextDate(Date date);

    abstract URL getURL(Date date);

    Calendar getTodayCalender() {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(2);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.AM_PM, 0);
        return cal;
    }

    private JButton prevButton;
    private JButton nextButton;
    private JLabel termLabel;
    private JLabel sumLabel;
    private Date current;
    private AozoraRankingListPane listPane;
}
