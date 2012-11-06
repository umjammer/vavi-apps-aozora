/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.viewer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraDefaultPane;
import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.data.AozoraAuthor;
import com.soso.sgui.SGUIUtil;


class AuthorViewerPane extends AozoraDefaultPane {

    AuthorViewerPane(AozoraContext context, AozoraAuthor author) {
        super(context);
        this.author = author;
        initGUI();
    }

    private void initGUI() {
        gbc = new GridBagConstraints();
        setLayout(new GridBagLayout());
        setBackground(getAzContext().getDefaultBGColor());
        gbc.insets = new Insets(1, 1, 1, 1);
        addLabel("作家名：", getAuthor().getName());
        addLabel("作家名読み：", getAuthor().getKana());
        addLabel("ローマ字表記：", getAuthor().getRomanName());
        addLabel("生年：", getAuthor().getBirthDate());
        addLabel("没年：", getAuthor().getDeadDate());
        addText("人物について：", getAuthor().getNote());
        addGlue();
    }

    private boolean checkValue(String value) {
        return value != null && value.trim().length() != 0;
    }

    private void addLabel(String header, String value) {
        if (checkValue(value)) {
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = 0;
            JLabel valueLabel = new JLabel();
            valueLabel.setText(value);
            addComp(header, valueLabel, -1);
        }
    }

    private void addText(String header, String value) {
        if (checkValue(value)) {
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            JTextPane noteText = new JTextPane();
            noteText.setBackground(getAzContext().getDefaultBGColor());
            noteText.setEditable(false);
            noteText.setText(value);
            JScrollPane noteScroll = new JScrollPane();
            noteScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            noteScroll.setViewportView(noteText);
            SGUIUtil.setSizeALL(noteScroll, TEXTPANE_SIZE);
            addComp(header, noteScroll, 0);
        }
    }

    private void addGlue() {
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0D;
        gbc.weighty = 1.0D;
        gbc.fill = GridBagConstraints.BOTH;
        addComp(null, Box.createGlue(), -1);
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

    private static final Dimension TEXTPANE_SIZE = new Dimension(200, 60);
    private final AozoraAuthor author;
    private GridBagConstraints gbc;

}
