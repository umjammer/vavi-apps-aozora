/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.core;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.Box;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.soso.aozora.boot.AozoraContext;
import com.soso.sgui.SLineBorder;
import com.soso.sgui.SOptionPane;
import com.soso.sgui.letter.SLetterCell;
import com.soso.sgui.letter.SLetterCellFactory;
import com.soso.sgui.letter.SLetterConstraint;
import com.soso.sgui.letter.SLetterPane;


class AozoraColorChooserPane extends JPanel {

    static void showDialog(AozoraContext context) {
        AozoraColorChooserPane pane = new AozoraColorChooserPane(context);
        if (SOptionPane.showSInternalConfirmDialog(context.getDesktopPane(), pane, "色の変更", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == 0)
            pane.colorChoosed();
    }

    private AozoraColorChooserPane(AozoraContext context) {
        this.context = context;
        initGUI();
    }

    private AozoraContext getAzContext() {
        return context;
    }

    private void initGUI() {
        setLayout(new BorderLayout());
        add(getTabbedPane(), BorderLayout.CENTER);
        add(getPreviewPane(), BorderLayout.SOUTH);
    }

    private JPanel getPreviewPane() {
        if (previewPane == null) {
            previewPane = new JPanel();
            previewPane.setLayout(new BorderLayout());
            previewPane.setBorder(new TitledBorder(new SLineBorder(Color.GRAY, 1, true, 5), "プレビュー"));
            previewPane.add(getPreviewLabel(), BorderLayout.CENTER);
            previewPane.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
            previewPane.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
        }
        return previewPane;
    }

    private SLetterPane getPreviewLabel() {
        if (previewLabel == null) {
            previewLabel = SLetterPane.newInstance(SLetterConstraint.ORIENTATION.LRTB);
            previewLabel.setBackground(getAzContext().getSettings().getBackground());
            previewLabel.setForeground(getAzContext().getSettings().getForeground());
            previewLabel.setRowColCountChangable(true);
            previewLabel.setFontSizeChangable(true);
            previewLabel.setLetterBorderRendarer(null);
            previewLabel.setFont(getAzContext().getSettings().getFont());
            previewLabel.setRowSpace(getAzContext().getSettings().getRowSpace());
            previewLabel.setFontRangeRatio(getAzContext().getSettings().getFontRatio());
            String mssg = "背景と文字の色を選択してください。";
            previewLabel.setColCount(mssg.length());
            previewLabel.setRowCount(1);
            for (char c : mssg.toCharArray()) {
                SLetterCell cell = SLetterCellFactory.getInstance().createGlyphCell(c);
                previewLabel.addCell(cell);
            }

        }
        return previewLabel;
    }

    private JTabbedPane getTabbedPane() {
        if (tabbedPane == null) {
            tabbedPane = new JTabbedPane();
            tabbedPane.setBorder(new TitledBorder(new SLineBorder(Color.GRAY, 1, true), "色の選択"));
            tabbedPane.addTab("背景色", getBackgroundChooser());
            tabbedPane.addTab("文字色", getForegroundChooser());
        }
        return tabbedPane;
    }

    private JColorChooser getForegroundChooser() {
        if (foregroundChooser == null) {
            foregroundChooser = new JColorChooser(getAzContext().getSettings().getForeground());
            foregroundChooser.setPreviewPanel(createEmptyPreview());
            foregroundChooser.getSelectionModel().addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    foregroundSelected();
                }
            });
        }
        return foregroundChooser;
    }

    private JColorChooser getBackgroundChooser() {
        if (backgroundChooser == null) {
            backgroundChooser = new JColorChooser(getAzContext().getSettings().getBackground());
            backgroundChooser.setPreviewPanel(createEmptyPreview());
            backgroundChooser.getSelectionModel().addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    backgroundSelected();
                }
            });
        }
        return backgroundChooser;
    }

    private static JComponent createEmptyPreview() {
        return new JPanel();
    }

    private void colorChoosed() {
        Color foreground = getForegroundChooser().getColor();
        Color background = getBackgroundChooser().getColor();
        getAzContext().getSettings().setForeground(foreground);
        getAzContext().getSettings().setBackground(background);
        getAzContext().getRootMediator().setViewerForeground(foreground);
        getAzContext().getRootMediator().setViewerBackground(background);
    }

    private void foregroundSelected() {
        Color foreground = getForegroundChooser().getColor();
        getPreviewLabel().setForeground(foreground);
    }

    private void backgroundSelected() {
        Color background = getBackgroundChooser().getColor();
        getPreviewLabel().setBackground(background);
    }

    private final AozoraContext context;
    private JPanel previewPane;
    private SLetterPane previewLabel;
    private JTabbedPane tabbedPane;
    private JColorChooser foregroundChooser;
    private JColorChooser backgroundChooser;
}
