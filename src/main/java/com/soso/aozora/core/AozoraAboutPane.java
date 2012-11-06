/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.data.AozoraPremierID;
import com.soso.sgui.SLinkLabel;


class AozoraAboutPane extends AozoraDefaultPane {

    AozoraAboutPane(AozoraContext context) {
        super(context);
        initGUI();
    }

    private void initGUI() {
        setLayout(new BorderLayout());
        icon = AozoraUtil.getIcon(AozoraEnv.Env.AOZORA_ICON_URL.getString());
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("このソフトウェア", createVersionInfoPane());
        if (getAzContext().isPremier())
            tabbedPane.addTab(PREMIER_INFO_TITLE, createPremierInfoPane());
        tabbedPane.addTab("青空文庫について", createAozoraBukoPane());
        tabbedPane.addTab("ショートカット一覧", createShortCutListPane());
        tabbedPane.setTabPlacement(2);
        tabbedPane.setTabLayoutPolicy(1);
        add(tabbedPane, "Center");
        setPreferredSize(new Dimension(480, 240));
    }

    void showPremierTab() {
        int index = tabbedPane.indexOfTab(PREMIER_INFO_TITLE);
        if (index == -1) {
            JOptionPane.showInternalMessageDialog(getAzContext().getDesktopPane(), "Premier 状態 を表示できません。", "Aozora Viewer", 2);
        } else {
            tabbedPane.setSelectedIndex(index);
            tabbedPane.getComponentAt(index).requestFocusInWindow();
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Rectangle lastTabBounds = tabbedPane.getUI().getTabBounds(tabbedPane, tabbedPane.getTabCount() - 1);
        int x = lastTabBounds.x + (lastTabBounds.width - icon.getIconWidth()) / 2;
        int y = (((getHeight() - getInsets().bottom) + lastTabBounds.y + lastTabBounds.height) - icon.getIconHeight()) / 2;
        icon.paintIcon(this, g, x, y);
    }

    private JComponent createVersionInfoPane() {
        JPanel pane = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        pane.setLayout(gbl);
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 0;
        pane.add(new JLabel(AozoraUtil.getIcon(AozoraEnv.Env.AOZORA_SPLASH_URL.getString())), gbc);
        gbc.insets.top = 1;
        gbc.insets.left = 10;
        gbc.insets.bottom = 1;
        gbc.insets.right = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        pane.add(new JLabel("タイトル"), gbc);
        pane.add(new JLabel("："), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 0;
        pane.add(new JLabel(getAzContext().getManifest().getSpecificationTitle()), gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        pane.add(new JLabel("ベンダー"), gbc);
        pane.add(new JLabel("："), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 0;
        pane.add(new JLabel(getAzContext().getManifest().getSpecificationVendor()), gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        pane.add(new JLabel("バージョン"), gbc);
        pane.add(new JLabel("："), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 0;
        pane.add(new JLabel(getAzContext().getManifest().getSpecificationVersion() + " (" + getAzContext().getManifest().getImplementationVersion() + ")"), gbc);
        gbc.weightx = 1.0D;
        gbc.weighty = 1.0D;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 0;
        JComponent filler = new JPanel();
        pane.add(filler, gbc);
        gbc.insets.top = 0;
        gbc.insets.bottom = 0;
        gbc.insets.right = 10;
        gbc.weightx = 0.0D;
        gbc.weighty = 0.0D;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 0;
        pane.add(new SLinkLabel(getAzContext().getManifest().getImplementationVendorURL()), gbc);
        gbc.insets.bottom = 5;
        pane.add(new JLabel("Copyright \251 by " + getAzContext().getManifest().getImplementationVendor()), gbc);
        return pane;
    }

    private JComponent createPremierInfoPane() {
        AozoraPremierID premierID = getAzContext().getPremierID();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        JPanel pane = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        pane.setLayout(gbl);
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets.top = 1;
        gbc.insets.left = 10;
        gbc.insets.bottom = 1;
        gbc.insets.right = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        pane.add(new JLabel("ID"), gbc);
        pane.add(new JLabel("："), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 0;
        pane.add(new JLabel(premierID.getID()), gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        pane.add(new JLabel("状態"), gbc);
        pane.add(new JLabel("："), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 0;
        pane.add(new JLabel(premierID.getStatus().getDesc()), gbc);
        if (premierID.getStatus() == AozoraPremierID.Status.TRIAL) {
            gbc.fill = GridBagConstraints.NONE;
            gbc.gridwidth = 1;
            pane.add(new JLabel("試用残回数"), gbc);
            pane.add(new JLabel("："), gbc);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridwidth = 0;
            pane.add(new JLabel(String.valueOf(premierID.getTrialRemainderCount())), gbc);
        }
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        pane.add(new JLabel("作成日時"), gbc);
        pane.add(new JLabel("："), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 0;
        pane.add(new JLabel(dateFormat.format(new Date(premierID.getCreatedTimestamp()))), gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        pane.add(new JLabel("前回認証日時"), gbc);
        pane.add(new JLabel("："), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 0;
        String lastCheckTimestamp = premierID.getLastCheckTimestamp() == 0L ? "なし" : dateFormat.format(new Date(premierID.getLastCheckTimestamp()));
        pane.add(new JLabel(lastCheckTimestamp), gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        pane.add(new JLabel("保存日時"), gbc);
        pane.add(new JLabel("："), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 0;
        pane.add(new JLabel(dateFormat.format(new Date(premierID.getStoredTimestamp()))), gbc);
        if (premierID.getStatus() != AozoraPremierID.Status.CONFIRMED) {
            gbc.fill = GridBagConstraints.NONE;
            gbc.gridwidth = 1;
            pane.add(new JLabel("お支払URL"), gbc);
            pane.add(new JLabel("："), gbc);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridwidth = 0;
            pane.add(new SLinkLabel(AozoraEnv.getPremierPayURL(premierID.getID())), gbc);
        }
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        pane.add(new JLabel("お問い合わせ"), gbc);
        pane.add(new JLabel("："), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 0;
        JLabel mailto = null;
        try {
            mailto = new SLinkLabel(new URL("mailto:customer@35-35.com"), "customer@35-35.com");
        } catch (MalformedURLException e) {
            log(e);
        }
        pane.add(mailto == null ? new JLabel("customer@35-35.com") : mailto, gbc);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 0;
        gbc.weightx = 1.0D;
        gbc.weighty = 1.0D;
        JComponent filler = new JPanel();
        pane.add(filler, gbc);
        return pane;
    }

    private JComponent createShortCutListPane() {
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());
        final JComponent titleFiller = new JLabel();
        JComponent descriptionFiller = new JLabel();
        final JPanel titlePane = new JPanel();
        titlePane.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
        final JLabel titleLabel = new JLabel() {
            public Dimension getPreferredSize() {
                Dimension prefSize = super.getPreferredSize();
                Dimension fillerSize = titleFiller.getSize();
                return new Dimension(fillerSize.width == 0 ? prefSize.width : fillerSize.width, prefSize.height);
            }
        };
        titleLabel.setOpaque(true);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBackground(Color.GRAY);
        titlePane.add(titleLabel);
        JLabel descriptionLabel = new JLabel() {
            public Dimension getPreferredSize() {
                Dimension prefSize = super.getPreferredSize();
                Dimension paneSize = titlePane.getSize();
                Dimension titleSize = titleLabel.getSize();
                return new Dimension(paneSize.width - titleSize.width - 3, prefSize.height);
            }
        };
        descriptionLabel.setOpaque(true);
        descriptionLabel.setForeground(Color.WHITE);
        descriptionLabel.setBackground(Color.GRAY);
        titlePane.add(descriptionLabel);
        pane.add(titlePane, BorderLayout.NORTH);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                titlePane.revalidate();
            }
        });
        JPanel valuePane = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        valuePane.setLayout(gbl);
        gbc.ipadx = 10;
        gbc.ipady = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0D;
        gbc.weighty = 0.0D;
        Color bgColor1 = Color.WHITE;
        Color bgColor2 = new Color(0xeeeeff);
        Color bgColor = bgColor1;
        for (AozoraEnv.ShortCutKey shortCutKey : AozoraEnv.ShortCutKey.values()) {
            gbc.gridwidth = 1;
            JLabel titleLabel2 = new JLabel(" " + shortCutKey.getHelpTitle());
            titleLabel2.setOpaque(true);
            titleLabel2.setBackground(bgColor);
            valuePane.add(titleLabel2, gbc);
            gbc.gridwidth = 0;
            descriptionLabel = new JLabel(shortCutKey.getHelpDescription());
            descriptionLabel.setOpaque(true);
            descriptionLabel.setBackground(bgColor);
            valuePane.add(descriptionLabel, gbc);
            bgColor = bgColor != bgColor1 ? bgColor1 : bgColor2;
        }

        gbc.weighty = 1.0D;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 1;
        titleFiller.setOpaque(true);
        titleFiller.setBackground(bgColor);
        valuePane.add(titleFiller, gbc);
        gbc.gridwidth = 0;
        descriptionFiller.setOpaque(true);
        descriptionFiller.setBackground(bgColor);
        valuePane.add(descriptionFiller, gbc);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(31);
        scrollPane.setViewportView(valuePane);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        pane.add(scrollPane, BorderLayout.CENTER);
        return pane;
    }

    private JComponent createAozoraBukoPane() {
        JPanel pane = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        pane.setLayout(gbl);
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 0;
        SLinkLabel bunkoLabel = new SLinkLabel(AozoraEnv.getAozoraBunkoURL());
        bunkoLabel.setIcon(AozoraUtil.getIcon(AozoraEnv.Env.AOZORA_BUNKO_ICON_URL.getString()));
        pane.add(bunkoLabel, gbc);
        gbc.insets.top = 1;
        gbc.insets.left = 10;
        gbc.insets.bottom = 10;
        gbc.insets.right = 10;
        gbc.weightx = 1.0D;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 0;
        JTextPane textPane = new JTextPane();
        textPane.setText("青空文庫とは、著作権の切れた作品や書き手自身が「自由に読んでもらってかまわない」" +
                         "としたものをテキスト、HTML、エキスパンドブックの三つの形式でそろえた無料公開の" +
                         "インターネット電子図書館です。\n青空文庫では「青空工作員」と呼ばれるボランティア" +
                         "の方たちが 入力、校正、ファイル作成などを行っています。 ");
        textPane.setEditable(false);
        pane.add(textPane, gbc);
        gbc.weightx = 1.0D;
        gbc.weighty = 1.0D;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 0;
        JComponent filler = new JPanel();
        pane.add(filler, gbc);
        return pane;
    }

    private Icon icon;
    private JTabbedPane tabbedPane;
    private static final String PREMIER_INFO_TITLE = "Premier 状態";
}
