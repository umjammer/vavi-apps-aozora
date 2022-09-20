/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.event.AozoraListenerAdapter;
import com.soso.aozora.viewer.AozoraCommentDecorator;
import com.soso.sgui.SGUIUtil;
import com.soso.sgui.letter.SLetterPane;


public class AozoraMenuPane extends AozoraDefaultPane {

    public AozoraMenuPane(AozoraContext context) {
        super(context);
        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        JPanel westPane = new JPanel();
        westPane.setLayout(new FlowLayout());
        JPanel centerPane = new JPanel();
        centerPane.setLayout(new FlowLayout());
        centerPane.add(Box.createHorizontalGlue());
        centerPane.add(getLafChooserButton());
        centerPane.add(getFontChooserButton());
        centerPane.add(getColorChooserButton());
        centerPane.add(getSaveButton());
        centerPane.add(getLineSpaceWideButton());
        centerPane.add(getLineSpaceNallowButton());
        centerPane.add(getFontRatioWideButton());
        centerPane.add(getFontRatioNallowButton());
        centerPane.add(getCommentButton());
        centerPane.add(getBookmarkButton());
        centerPane.add(Box.createHorizontalGlue());
        add(westPane, BorderLayout.WEST);
        add(centerPane, BorderLayout.CENTER);
        for (JComponent comp : SGUIUtil.getChildInstanceOf(this, JComponent.class)) {
            comp.setOpaque(false);
        }

        setOpaque(false);
    }

    private JButton getLafChooserButton() {
        if (lafChooserButton == null) {
            lafChooserButton = new JButton();
            lafChooserButton.setName("AozoraMenuPane.lafChooserButton");
            lafChooserButton.setAction(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    getAzContext().getRootMediator().showLookAndFeelChooser();
                }
            });
            lafChooserButton.setToolTipText(AozoraEnv.ShortCutKey.CHANGE_LOOK_AND_FEEL_SHORTCUT.getNameWithHelpTitle());
            AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_IN_FOCUSED_WINDOW, AozoraEnv.ShortCutKey.CHANGE_LOOK_AND_FEEL_SHORTCUT.getKeyStroke(), lafChooserButton);
            lafChooserButton.setIcon(AozoraUtil.getIcon(AozoraEnv.Env.CHANGE_LOOK_AND_FEEL_ICON.getString()));
            lafChooserButton.setMaximumSize(lafChooserButton.getPreferredSize());
        }
        return lafChooserButton;
    }

    private JButton getFontChooserButton() {
        if (fontChooserButton == null) {
            fontChooserButton = new JButton();
            fontChooserButton.setName("AozoraMenuPane.fontChooserButton");
            fontChooserButton.setAction(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    getAzContext().getRootMediator().showFontChooser();
                }
            });
            fontChooserButton.setToolTipText(AozoraEnv.ShortCutKey.CHANGE_FONT_SHORTCUT.getNameWithHelpTitle());
            AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_IN_FOCUSED_WINDOW, AozoraEnv.ShortCutKey.CHANGE_FONT_SHORTCUT.getKeyStroke(), fontChooserButton);
            fontChooserButton.setIcon(AozoraUtil.getIcon(AozoraEnv.Env.CHANGE_FONT_ICON.getString()));
            fontChooserButton.setMaximumSize(fontChooserButton.getPreferredSize());
        }
        return fontChooserButton;
    }

    private JButton getColorChooserButton() {
        if (colorChooserButton == null) {
            colorChooserButton = new JButton();
            colorChooserButton.setName("AozoraMenuPane.colorChooserButton");
            colorChooserButton.setAction(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    getAzContext().getRootMediator().showColorChooser();
                }
            });
            colorChooserButton.setToolTipText(AozoraEnv.ShortCutKey.CHANGE_COLOR_SHORTCUT.getNameWithHelpTitle());
            AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_IN_FOCUSED_WINDOW, AozoraEnv.ShortCutKey.CHANGE_COLOR_SHORTCUT.getKeyStroke(), colorChooserButton);
            colorChooserButton.setIcon(AozoraUtil.getIcon(AozoraEnv.Env.CHANGE_COLOR_ICON.getString()));
            colorChooserButton.setMaximumSize(colorChooserButton.getPreferredSize());
        }
        return colorChooserButton;
    }

    private JButton getSaveButton() {
        if (saveButton == null) {
            saveButton = new JButton();
            saveButton.setName("AozoraMenuPane.saveButton");
            saveButton.setAction(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        getAzContext().getSettings().store();
                        JOptionPane.showInternalMessageDialog(getAzContext().getDesktopPane(), "設定を保存しました");
                    } catch (IOException e1) {
                        e1.printStackTrace(System.err);
                    }
                }
            });
            saveButton.setToolTipText(AozoraEnv.ShortCutKey.SAVE_SHORTCUT.getNameWithHelpTitle());
            AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_IN_FOCUSED_WINDOW, AozoraEnv.ShortCutKey.SAVE_SHORTCUT.getKeyStroke(), saveButton);
            saveButton.setIcon(AozoraUtil.getIcon(AozoraEnv.Env.SAVE_ICON.getString()));
            saveButton.setMaximumSize(saveButton.getPreferredSize());
        }
        return saveButton;
    }

    private JButton getLineSpaceWideButton() {
        if (lineSpaceWideButton == null) {
            lineSpaceWideButton = new JButton();
            lineSpaceWideButton.setName("AozoraMenuPane.lineSpaceWideButton");
            lineSpaceWideButton.setAction(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    for (SLetterPane textPane : SGUIUtil.getChildInstanceOf(SGUIUtil.getParentRecursive(getRootPane()), SLetterPane.class)) {
                        int rowSpace = textPane.getRowSpace();
                        if (rowSpace < 100) {
                            textPane.setRowSpace(rowSpace + 1);
                            textPane.repaint();
                        } else {
                            lineSpaceWideButton.setEnabled(false);
                        }
                        lineSpaceNallowButton.setEnabled(true);
                    }

                }
            });
            lineSpaceWideButton.setToolTipText(AozoraEnv.ShortCutKey.LINE_SPACE_WIDE_SHORTCUT.getNameWithHelpTitle());
            AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_IN_FOCUSED_WINDOW, AozoraEnv.ShortCutKey.LINE_SPACE_WIDE_SHORTCUT.getKeyStroke(), lineSpaceWideButton);
            lineSpaceWideButton.setIcon(AozoraUtil.getIcon(AozoraEnv.Env.LINE_SPACE_WIDE_ICON.getString()));
            lineSpaceWideButton.setMaximumSize(lineSpaceWideButton.getPreferredSize());
        }
        return lineSpaceWideButton;
    }

    private JButton getLineSpaceNallowButton() {
        if (lineSpaceNallowButton == null) {
            lineSpaceNallowButton = new JButton();
            lineSpaceNallowButton.setName("AozoraMenuPane.lineSpaceNallowButton");
            lineSpaceNallowButton.setAction(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    for (SLetterPane textPane : SGUIUtil.getChildInstanceOf(SGUIUtil.getParentRecursive(getRootPane()), SLetterPane.class)) {
                        int rowSpace = textPane.getRowSpace();
                        if (rowSpace > 0) {
                            textPane.setRowSpace(rowSpace - 1);
                            textPane.repaint();
                        } else {
                            lineSpaceNallowButton.setEnabled(false);
                        }
                        lineSpaceWideButton.setEnabled(true);
                    }
                }
            });
            lineSpaceNallowButton.setToolTipText(AozoraEnv.ShortCutKey.LINE_SPACE_NARROW_SHORTCUT.getNameWithHelpTitle());
            AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_IN_FOCUSED_WINDOW, AozoraEnv.ShortCutKey.LINE_SPACE_NARROW_SHORTCUT.getKeyStroke(), lineSpaceNallowButton);
            lineSpaceNallowButton.setIcon(AozoraUtil.getIcon(AozoraEnv.Env.LINE_SPACE_NARROW_ICON.getString()));
            lineSpaceNallowButton.setMaximumSize(lineSpaceNallowButton.getPreferredSize());
        }
        return lineSpaceNallowButton;
    }

    private JButton getFontRatioWideButton() {
        if (fontRatioWideButton == null) {
            fontRatioWideButton = new JButton();
            fontRatioWideButton.setName("AozoraMenuPane.fontRatioWideButton");
            fontRatioWideButton.setAction(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    for (SLetterPane textPane : SGUIUtil.getChildInstanceOf(SGUIUtil.getParentRecursive(getRootPane()), SLetterPane.class)) {
                        float fontRatio = textPane.getFontRangeRatio();
                        fontRatio -= 0.05F;
                        if (fontRatio > 0.5F) {
                            textPane.setFontRangeRatio(fontRatio);
                            textPane.repaint();
                        } else {
                            fontRatioWideButton.setEnabled(false);
                        }
                        fontRatioNallowButton.setEnabled(true);
                    }
                }
            });
            fontRatioWideButton.setToolTipText(AozoraEnv.ShortCutKey.FONT_RATIO_WIDE_SHORTCUT.getNameWithHelpTitle());
            AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_IN_FOCUSED_WINDOW, AozoraEnv.ShortCutKey.FONT_RATIO_WIDE_SHORTCUT.getKeyStroke(), fontRatioWideButton);
            fontRatioWideButton.setIcon(AozoraUtil.getIcon(AozoraEnv.Env.FONT_RATIO_WIDE_ICON.getString()));
            fontRatioWideButton.setMaximumSize(fontRatioWideButton.getPreferredSize());
        }
        return fontRatioWideButton;
    }

    private JButton getFontRatioNallowButton() {
        if (fontRatioNallowButton == null) {
            fontRatioNallowButton = new JButton();
            fontRatioNallowButton.setName("AozoraMenuPane.fontRatioNallowButton");
            fontRatioNallowButton.setAction(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    for (SLetterPane textPane : SGUIUtil.getChildInstanceOf(SGUIUtil.getParentRecursive(getRootPane()), SLetterPane.class)) {
                        float fontRatio = textPane.getFontRangeRatio();
                        fontRatio += 0.05F;
                        if (fontRatio < 1.5F) {
                            textPane.setFontRangeRatio(fontRatio);
                            textPane.repaint();
                        } else {
                            fontRatioNallowButton.setEnabled(false);
                        }
                        fontRatioWideButton.setEnabled(true);
                    }
                }
            });
            fontRatioNallowButton.setToolTipText(AozoraEnv.ShortCutKey.FONT_RATIO_NARROW_SHORTCUT.getNameWithHelpTitle());
            AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_IN_FOCUSED_WINDOW, AozoraEnv.ShortCutKey.FONT_RATIO_NARROW_SHORTCUT.getKeyStroke(), fontRatioNallowButton);
            fontRatioNallowButton.setIcon(AozoraUtil.getIcon(AozoraEnv.Env.FONT_RATIO_NARROW_ICON.getString()));
            fontRatioNallowButton.setMaximumSize(fontRatioNallowButton.getPreferredSize());
        }
        return fontRatioNallowButton;
    }

    private JButton getCommentButton() {
        if (commentButton == null) {
            commentBalloneIcon = AozoraUtil.getIcon(AozoraEnv.Env.COMMENT_ICON.getString());
            BufferedImage commentNoneImage = new BufferedImage(commentBalloneIcon.getIconWidth(), commentBalloneIcon.getIconHeight(), 6);
            Graphics commentNoneGraphics = commentNoneImage.getGraphics();
            commentBalloneIcon.paintIcon(null, commentNoneGraphics, 0, 0);
            commentNoneGraphics.setColor(Color.RED);
            commentNoneGraphics.setFont(new Font("Monospaced", 1, 16));
            commentNoneGraphics.drawString("\327", 1, 16);
            commentNoneIcon = new ImageIcon(commentNoneImage);
            commentButton = new JButton();
            commentButton.setName("AozoraMenuPane.commentButton");
            commentButton.setAction(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (commentButton.getIcon() == commentNoneIcon)
                        getAzContext().getSettings().setCommentType(AozoraCommentDecorator.CommentType.ballone);
                    else if (commentButton.getIcon() == commentBalloneIcon)
                        getAzContext().getSettings().setCommentType(AozoraCommentDecorator.CommentType.none);
                }
            });
            commentButton.setToolTipText(AozoraEnv.ShortCutKey.COMMENT_SHORTCUT.getNameWithHelpTitle());
            AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_IN_FOCUSED_WINDOW, AozoraEnv.ShortCutKey.COMMENT_SHORTCUT.getKeyStroke(), commentButton);
            commentButton.setMaximumSize(commentButton.getPreferredSize());
            setCommentButtonIcon(getAzContext().getSettings().getCommentType());
            getAzContext().getListenerManager().add(new AozoraListenerAdapter() {
                public void commentTypeChanged(AozoraCommentDecorator.CommentType commentType) {
                    setCommentButtonIcon(commentType);
                }
            });
        }
        return commentButton;
    }

    private void setCommentButtonIcon(AozoraCommentDecorator.CommentType commentType) {
        if (commentType == null)
            commentType = AozoraCommentDecorator.CommentType.none;

        switch (commentType) {
        case none:
            commentButton.setIcon(commentNoneIcon);
            break;
        case ballone:
        default:
            commentButton.setIcon(commentBalloneIcon);
            break;
        }
    }

    private JButton getBookmarkButton() {
        if (bookmarkButton == null) {
            bookmarkButton = new JButton();
            bookmarkButton.setName("AozoraMenuPane.bookmarkButton");
            bookmarkButton.setAction(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    Rectangle a = SGUIUtil.getBoundsRecursive(getAzContext().getDesktopPane(), bookmarkButton);
                    getAzContext().getRootMediator().showBookmarkList(a.x, a.y + a.height);
                }
            });
            bookmarkButton.setToolTipText(AozoraEnv.ShortCutKey.BOOKMARK_OPEN_SHORTCUT.getNameWithHelpTitle());
            AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_IN_FOCUSED_WINDOW, AozoraEnv.ShortCutKey.BOOKMARK_OPEN_SHORTCUT.getKeyStroke(), bookmarkButton);
            bookmarkButton.setIcon(AozoraUtil.getIcon(AozoraEnv.Env.BOOKMARK_ICON.getString()));
            bookmarkButton.setMaximumSize(bookmarkButton.getPreferredSize());
        }
        return bookmarkButton;
    }

    private JButton lafChooserButton;
    private JButton fontChooserButton;
    private JButton colorChooserButton;
    private JButton saveButton;
    private JButton lineSpaceWideButton;
    private JButton lineSpaceNallowButton;
    private JButton fontRatioWideButton;
    private JButton fontRatioNallowButton;
    private JButton commentButton;
    private Icon commentBalloneIcon;
    private Icon commentNoneIcon;
    private JButton bookmarkButton;
}
