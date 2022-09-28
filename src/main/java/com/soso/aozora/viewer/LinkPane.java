/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.viewer;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.border.TitledBorder;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraDefaultPane;
import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.data.AozoraAuthor;
import com.soso.aozora.data.AozoraWork;
import com.soso.sgui.SLinkLabel;


class LinkPane extends AozoraDefaultPane {

    static Logger logger = Logger.getLogger(LinkPane.class.getName());

    LinkPane(AozoraContext context, AozoraAuthor author, AozoraWork work) {
        super(context);
        this.author = author;
        this.work = work;
        initGUI();
    }

    private void initGUI() {
        gbc = new GridBagConstraints();
        setLayout(new GridBagLayout());
        setBackground(getAzContext().getDefaultBGColor());
        gbc.insets = new Insets(1, 3, 1, 3);
        setBorder(new TitledBorder("青空文庫へのリンク"));
        addLink("著者カード：", "http://www.aozora.gr.jp/index_pages/" + getAuthor().getID() + ".html");
        addLink("作品カード：", getWork().getMetaURL());
        addLink("本文ページ：", getWork().getContentURL());
    }

    private void addLink(String header, String url) {
        try {
            addLink(header, new URL(url));
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void addLink(String header, URL url) {
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0D;
        gbc.weighty = 0.0D;
        gbc.fill = GridBagConstraints.NONE;
        SLinkLabel linkLabel = new SLinkLabel(url);
        addComp(header, linkLabel, -1);
    }

    private void addComp(String header, Component comp, int headerGridwidth) {
        if (header != null) {
            gbc.gridwidth = headerGridwidth;
            JLabel headerLabel = new JLabel();
            headerLabel.setText(header);
            headerLabel.setBackground(getAzContext().getDefaultBGColor());
            headerLabel.setForeground(AozoraEnv.HEADER_COLOR);
            add(headerLabel, gbc);
        }
        gbc.gridwidth = 0;
        add(comp, gbc);
    }

    private AozoraAuthor getAuthor() {
        return author;
    }

    private AozoraWork getWork() {
        return work;
    }

    private final AozoraAuthor author;
    private final AozoraWork work;
    private GridBagConstraints gbc;
}
