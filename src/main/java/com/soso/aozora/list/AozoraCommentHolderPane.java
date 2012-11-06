/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraDefaultPane;
import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.core.AozoraUtil;
import com.soso.aozora.data.AozoraAuthor;
import com.soso.aozora.data.AozoraComment;
import com.soso.aozora.data.AozoraWork;
import com.soso.aozora.data.AozoraWorkParserHandler;
import com.soso.aozora.viewer.AozoraCommentDecorator;
import com.soso.sgui.SGUIUtil;
import com.soso.sgui.SLineBorder;


class AozoraCommentHolderPane extends AozoraDefaultPane {

    AozoraCommentHolderPane(AozoraContext context) {
        super(context);
        initGUI();
    }

    private void initGUI() {
        setDefaultBackground(Color.WHITE);
        setSelectedBackground(AozoraEnv.COMMENT_BALLONE_BACKGROUND_COLOR);
        setOpaque(true);
        setBackground(getDefaultBackground());
        setLayout(new LayoutManager() {
            public void addLayoutComponent(String name, Component comp) {
            }

            public void removeLayoutComponent(Component comp) {
            }

            public Dimension preferredLayoutSize(Container parent) {
                Container parentParent = parent.getParent();
                if (parentParent == null)
                    return new Dimension(0, FIXED_HEIGHT);
                else
                    return new Dimension(parentParent.getWidth(), FIXED_HEIGHT);
            }

            public Dimension minimumLayoutSize(Container parent) {
                return new Dimension(0, FIXED_HEIGHT);
            }

            public void layoutContainer(Container parent) {
                Container parentParent = parent.getParent();
                if (parentParent != null) {
                    Dimension dataPref = getDataLabel().getPreferredSize();
                    Dimension commentatorPref = getCommentatorLabel().getPreferredSize();
                    Dimension timestampPref = getTimestampLabel().getPreferredSize();
                    Dimension workPref = getWorkLabel().getPreferredSize();
                    Dimension authorPref = getAuthorLabel().getPreferredSize();
                    int parentWidth = parentParent.getWidth();
                    getCommentatorLabel().setBounds(4, 2, parentWidth - 4, commentatorPref.height);
                    getDataLabel().setBounds(4, 2 + commentatorPref.height + 2, parentWidth - 4, dataPref.height);
                    getWorkLabel().setBounds(parentWidth - authorPref.width - 2 - workPref.width - 4, FIXED_HEIGHT - timestampPref.height - 2 - workPref.height - 2, workPref.width, workPref.height);
                    getAuthorLabel().setBounds(parentWidth - authorPref.width - 4, FIXED_HEIGHT - timestampPref.height - 2 - authorPref.height - 2, authorPref.width, authorPref.height);
                    getTimestampLabel().setBounds(parentWidth - timestampPref.width - 4, FIXED_HEIGHT - timestampPref.height - 2, timestampPref.width, timestampPref.height);
                }
            }
        });
        add(getDataLabel());
        add(getCommentatorLabel());
        add(getTimestampLabel());
        add(getWorkLabel());
        add(getAuthorLabel());
        setBorder(new SLineBorder(getDefaultBackground().darker(), 1, true, 10));
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                requestSelected();
                showViewer();
            }

            public void mouseEntered(MouseEvent e) {
                requestSelected();
                if (comment != null && author != null && work != null) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                    getDataLabel().setForeground(Color.BLUE);
                }
            }

            public void mouseExited(MouseEvent e) {
                setCursor(null);
                getDataLabel().setForeground(null);
            }
        });
        AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_FOCUSED, KeyStroke.getKeyStroke("UP"), "AozoraCommentHolderPane.upAction", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                requestSelectedPrev();
            }
        });
        AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_FOCUSED, KeyStroke.getKeyStroke("DOWN"), "AozoraCommentHolderPane.downAction", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                requestSelectedNext();
            }
        });
        AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_FOCUSED, KeyStroke.getKeyStroke("ENTER"), "AozoraCommentHolderPane.enterAction", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                showViewer();
            }
        });
    }

    void setDefaultBackground(Color defaultBackground) {
        this.defaultBackground = defaultBackground;
        if (!isSelected())
            setBackground(defaultBackground);
    }

    Color getDefaultBackground() {
        return defaultBackground;
    }

    void setSelectedBackground(Color selectedBackground) {
        this.selectedBackground = selectedBackground;
        if (isSelected())
            setBackground(selectedBackground);
    }

    Color getSelectedBackground() {
        return selectedBackground;
    }

    private JLabel getDataLabel() {
        if (dataLabel == null) {
            dataLabel = new JLabel();
            Font f = dataLabel.getFont();
            if (f != null)
                dataLabel.setFont(new Font(f.getName(), f.getStyle(), f.getSize() + 1));
        }
        return dataLabel;
    }

    private JLabel getCommentatorLabel() {
        if (commentatorLabel == null) {
            commentatorLabel = new JLabel();
            commentatorLabel.setForeground(Color.DARK_GRAY);
        }
        return commentatorLabel;
    }

    private JLabel getTimestampLabel() {
        if (timestampLabel == null) {
            timestampLabel = new JLabel();
            timestampLabel.setForeground(Color.GRAY);
            Font f = timestampLabel.getFont();
            if (f != null)
                timestampLabel.setFont(new Font(f.getName(), f.getStyle(), Math.max(f.getSize() - 1, 1)));
        }
        return timestampLabel;
    }

    private JLabel getWorkLabel() {
        if (workLabel == null) {
            workLabel = new JLabel();
            workLabel.setForeground(Color.GRAY);
            Font f = workLabel.getFont();
            if (f != null)
                workLabel.setFont(new Font(f.getName(), f.getStyle(), Math.max(f.getSize() - 1, 1)));
        }
        return workLabel;
    }

    private JLabel getAuthorLabel() {
        if (authorLabel == null) {
            authorLabel = new JLabel();
            authorLabel.setForeground(Color.GRAY);
            Font f = authorLabel.getFont();
            if (f != null)
                authorLabel.setFont(new Font(f.getName(), f.getStyle(), Math.max(f.getSize() - 1, 1)));
        }
        return authorLabel;
    }

    private static String format(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.set(10, 0);
        cal.set(12, 0);
        cal.set(13, 0);
        cal.set(14, 0);
        long today = cal.getTimeInMillis();
        cal.add(5, -1);
        long yesterday = cal.getTimeInMillis();
        if (today <= timestamp)
            return "今日 " + new SimpleDateFormat("MM/dd HH:mm").format(new Date(timestamp));
        if (yesterday <= timestamp)
            return "昨日 " + new SimpleDateFormat("MM/dd HH:mm").format(new Date(timestamp));
        else
            return new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date(timestamp));
    }

    private void requestSelected() {
        SGUIUtil.getParentInstanceOf(this, AozoraCommentPane.class).setSelected(this);
    }

    private void requestSelectedNext() {
        SGUIUtil.getParentInstanceOf(this, AozoraCommentPane.class).setSelectedNext(this);
    }

    private void requestSelectedPrev() {
        SGUIUtil.getParentInstanceOf(this, AozoraCommentPane.class).setSelectedPrev(this);
    }

    void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        setBackground(isSelected ? getSelectedBackground() : getDefaultBackground());
        scrollRectToVisible(new Rectangle(0, 0, 0, getHeight()));
        requestFocusInWindow();
    }

    boolean isSelected() {
        return isSelected;
    }

    void setComment(AozoraComment comment) {
        synchronized (this) {
            this.comment = comment;
            author = null;
            work = null;
            getDataLabel().setText(comment.getData());
            if (comment.getCommentator() == null || comment.getCommentator().length() == 0)
                getCommentatorLabel().setText(AozoraComment.NANASHISAN);
            else
                getCommentatorLabel().setText(comment.getCommentator() + " さん");
            getTimestampLabel().setText(format(comment.getTimestamp()));
            getWorkLabel().setText(null);
            getAuthorLabel().setText(null);
            getAzContext().getRootMediator().getAozoraWorkAsynchronous(comment.getWorkID(), new AozoraWorkParserHandler() {
                public void work(AozoraWork work) {
                    setWork(work);
                }
            });
        }
    }

    void setWork(AozoraWork work) {
        synchronized (this) {
            if (comment != null && work != null)
                if (comment.getWorkID().equals(work.getID())) {
                    this.work = work;
                    getWorkLabel().setText(work.getTitleName());
                    String personID = work.getAuthorID();
                    if (personID == null)
                        personID = work.getTranslatorID();
                    if (personID != null) {
                        AozoraAuthor author = getAzContext().getRootMediator().getAozoraAuthor(personID);
                        if (author != null) {
                            this.author = author;
                            getAuthorLabel().setText(" - " + author.getName());
                        }
                    }
                    revalidate();
                }
        }
    }

    public Dimension getPreferredSize() {
        Dimension prefSize = super.getPreferredSize();
        return new Dimension(prefSize.width, FIXED_HEIGHT);
    }

    public Dimension getMinimumSize() {
        Dimension minSize = super.getMinimumSize();
        return new Dimension(minSize.width, FIXED_HEIGHT);
    }

    public Dimension getMaximumSize() {
        Dimension maxSize = super.getMaximumSize();
        return new Dimension(maxSize.width, FIXED_HEIGHT);
    }

    protected void paintComponent(Graphics g) {
        Color color0 = g.getColor();
        SLineBorder border = (SLineBorder) getBorder();
        if (border != null) {
            Shape borderInnerShape = border.getBorderInnerShape(0, 0, getWidth(), getHeight());
            if (g instanceof Graphics2D) {
                g.setColor(getBackground());
                ((Graphics2D) g).fill(borderInnerShape);
            } else {
                super.paintComponent(g);
            }
        }
        if (color0 != null)
            g.setColor(color0);
    }

    private void showViewer() {
        synchronized (this) {
            if (comment != null && author != null && work != null) {
                if (!getAzContext().getSettings().isCommentVisible())
                    getAzContext().getSettings().setCommentType(AozoraCommentDecorator.CommentType.ballone);
                getAzContext().getRootMediator().showViewer(author, work, comment);
            }
        }
    }

    static final int FIXED_HEIGHT = 80;
    private JLabel dataLabel;
    private JLabel commentatorLabel;
    private JLabel timestampLabel;
    private JLabel workLabel;
    private JLabel authorLabel;
    private AozoraComment comment;
    private AozoraAuthor author;
    private AozoraWork work;
    private Color defaultBackground;
    private Color selectedBackground;
    private boolean isSelected;
}
