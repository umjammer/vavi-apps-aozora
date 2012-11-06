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
import javax.swing.JSeparator;
import javax.swing.JTextPane;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraDefaultPane;
import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.data.AozoraWork;
import com.soso.sgui.SGUIUtil;


public class WorkViewerPane extends AozoraDefaultPane {

    public WorkViewerPane(AozoraContext context, AozoraWork work) {
        super(context);
        this.work = work;
        initGUI();
    }

    private void initGUI() {
        gbc = new GridBagConstraints();
        setLayout(new GridBagLayout());
        setBackground(getAzContext().getDefaultBGColor());
        gbc.insets = new Insets(1, 1, 1, 1);
        addLabel("作品名：", getWork().getTitleName());
        addLabel("作品名読み：", getWork().getTitleKana());
        addLabel("原題：", getWork().getOrginalBook());
        addLabel("著者：", null);
        addSeparator();
        addText("作品について：", getWork().getNote());
        addLabel("仮名遣い種別：", getWork().getKanaType());
        addLabel("翻訳者：", null);
        addSeparator();
        addLabel("底本：", getWork().getOrginalBook());
        addLabel("出版社：", getWork().getPublisher());
        addLabel("初版発行日：", getWork().getFirstDate());
        addLabel("入力に使用：", getWork().getInputBase());
        addLabel("校正に使用：", getWork().getProofBase());
        addSeparator();
        addLabel("親本の底本：", getWork().getCompleteOrginalBook());
        addLabel("親本出版社：", getWork().getCompletePublisher());
        addLabel("親本初版発行日：", getWork().getCompleteFirstDate());
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
            gbc.fill = GridBagConstraints.NONE;
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
            gbc.fill = GridBagConstraints.HORIZONTAL;
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

    private void addSeparator() {
        JSeparator separator = new JSeparator(0);
        addComp(null, separator, 0);
    }

    private void addComp(String header, Component comp, int headerGridwidth) {
        if (header != null) {
            gbc.gridwidth = headerGridwidth;
            JLabel headerLabel = new JLabel();
            headerLabel.setText(header);
            headerLabel.setForeground(AozoraEnv.HEADER_COLOR);
            add(headerLabel, gbc);
        }
        gbc.gridwidth = 0;
        add(comp, gbc);
    }

    private AozoraWork getWork() {
        return work;
    }

    private static final Dimension TEXTPANE_SIZE = new Dimension(200, 60);
    private final AozoraWork work;
    private GridBagConstraints gbc;
}
