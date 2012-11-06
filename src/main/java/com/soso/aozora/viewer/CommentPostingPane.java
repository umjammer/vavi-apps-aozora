/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.io.UnsupportedEncodingException;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraDefaultPane;
import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.data.AozoraComment;
import com.soso.aozora.data.AozoraWork;
import com.soso.sgui.SButton;
import com.soso.sgui.SGUIUtil;
import com.soso.sgui.SLineBorder;


class CommentPostingPane extends AozoraDefaultPane {

    CommentPostingPane(AozoraContext context, AozoraWork work, int position, int length, String selectedText) {
        super(context);
        this.work = work;
        this.position = position;
        this.length = length;
        initGUI(selectedText);
    }

    private void initGUI(String selectedText) {
        setBorder(new EmptyBorder(2, 5, 2, 5));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel headerPane = new JPanel(new BorderLayout(0, 0));
        JLabel titleLabel = new JLabel("作品「" + work.getTitleName() + "」");
        Font titleFont = titleLabel.getFont();
        if (titleFont != null)
            titleFont = new Font(titleFont.getName(), Font.BOLD, titleFont.getSize());
        titleLabel.setFont(titleFont);
        headerPane.add(titleLabel, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(new EmptyBorder(2, 6, 0, 6));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setText(selectedText);
        textArea.setRows(!selectedText.contains("\n") && selectedText.length() <= 40 ? 1 : 2);
        scrollPane.setViewportView(textArea);
        headerPane.add(scrollPane, BorderLayout.CENTER);
        JPanel southPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPane.add(new JLabel("へのコメント"));
        headerPane.add(southPane, BorderLayout.SOUTH);
        add(headerPane);
        add(new JSeparator(JSeparator.HORIZONTAL));
        JPanel fieldPane = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(1, 3, 1, 3);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0D;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        fieldPane.add(new JLabel("お名前："), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        commentatorField = new JTextField();
        commentatorField.setColumns(10);
        commentatorField.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                resetPostEnabled();
            }
        });
        fieldPane.add(commentatorField, gbc);
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        fieldPane.add(new JLabel("コメント："), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        dataField = new JTextField();
        dataField.setColumns(40);
        dataField.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                resetPostEnabled();
            }
        });
        fieldPane.add(dataField, gbc);
        add(fieldPane);
        add(new JSeparator(JSeparator.HORIZONTAL));
        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
        add(Box.createHorizontalStrut(10));
        postButton = new SButton();
        postButton.setAction(new AbstractAction("コメントを投稿") {
            public void actionPerformed(ActionEvent e) {
                postComment();
            }
        });
        buttonPane.add(postButton);
        cancelButton = new SButton();
        cancelButton.setAction(new AbstractAction("キャンセル") {
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });
        buttonPane.add(cancelButton);
        add(buttonPane);
        resetPostEnabled();
    }

    private void resetPostEnabled() {
        commentatorField.setForeground(null);
        dataField.setForeground(null);
        boolean isValueError = false;
        String commentator = commentatorField.getText();
        if (commentator == null)
            isValueError = true;
        else
            try {
                if (commentator.getBytes("UTF-8").length > 30) {
                    isValueError = true;
                    commentatorField.setForeground(Color.RED);
                }
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
        String data = dataField.getText();
        if (data == null || data.length() == 0)
            isValueError = true;
        else
            try {
                if (data.getBytes("UTF-8").length > 150) {
                    isValueError = true;
                    dataField.setForeground(Color.RED);
                }
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
        postButton.setEnabled(!isValueError);
    }

    private void postComment() {
        resetPostEnabled();
        if (!postButton.isEnabled())
            return;
        try {
            String commentator = commentatorField.getText();
            String data = dataField.getText();
            int answer = JOptionPane.showInternalConfirmDialog(this, createConfirmPane(commentator, data), "コメント投稿の確認", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) {
                getAzContext().getCommentManager().postComment(work.getID(), position, length, commentator, data);
                getAzContext().getCommentManager().updateComments();
            }
            if (answer == JOptionPane.YES_OPTION || answer == JOptionPane.CANCEL_OPTION)
                dispose();
        } catch (Exception e) {
            getAzContext().log(e);
            Object mssg = "コメントの投稿でエラーが発生しました。";
            if (e.getMessage() != null)
                mssg = new String[] {
                    (String) mssg, e.getMessage()
                };
            JOptionPane.showInternalMessageDialog(getAzContext().getDesktopPane(), mssg, "エラー", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JComponent createConfirmPane(String commentator, String data) {
        JPanel confirmPane = new JPanel();
        confirmPane.setLayout(new BorderLayout());
        JPanel warningPane = new JPanel();
        warningPane.setBorder(new TitledBorder("注意"));
        warningPane.setLayout(new BoxLayout(warningPane, 1));
        warningPane.add(new JLabel("投稿されたコメントは、インターネット上に公開されます。"));
        warningPane.add(new JLabel("一度投稿されたコメントの編集や削除は出来ません。"));
        confirmPane.add(warningPane, "North");
        final SLineBorder commentBorder = new SLineBorder(Color.BLACK, 1, true, 8);
        JPanel fieldPane = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                Color color0 = g.getColor();
                Shape borderInnerShape = commentBorder.getBorderInnerShape(0, 0, getWidth(), getHeight());
                if (g instanceof Graphics2D) {
                    g.setColor(AozoraEnv.COMMENT_BALLONE_BACKGROUND_COLOR);
                    ((Graphics2D) g).fill(borderInnerShape);
                } else {
                    super.paintComponent(g);
                }
                if (color0 != null)
                    g.setColor(color0);
            }
        };
        fieldPane.setBorder(commentBorder);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0.0D;
        gbc.gridx = 0;
        fieldPane.add(new JLabel("お名前："), gbc);
        gbc.weightx = 1.0D;
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        if (commentator == null || commentator.length() == 0)
            fieldPane.add(new JLabel(AozoraComment.NANASHISAN), gbc);
        else
            fieldPane.add(new JLabel(commentator), gbc);
        gbc.weightx = 0.0D;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        fieldPane.add(new JLabel("コメント："), gbc);
        gbc.weightx = 1.0D;
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        fieldPane.add(new JLabel(data), gbc);
        confirmPane.add(fieldPane, "Center");
        JPanel messagePane = new JPanel();
        messagePane.setLayout(new BoxLayout(messagePane, BoxLayout.Y_AXIS));
        messagePane.add(Box.createVerticalStrut(10));
        messagePane.add(new JLabel("この内容でコメントを投稿します。よろしいですか？"));
        confirmPane.add(messagePane, "South");
        return confirmPane;
    }

    private void cancel() {
        dispose();
    }

    private void dispose() {
        JInternalFrame iframe = SGUIUtil.getParentInstanceOf(this, JInternalFrame.class);
        iframe.setVisible(false);
        iframe.dispose();
    }

    private final AozoraWork work;
    private final int position;
    private final int length;
    private JTextField commentatorField;
    private JTextField dataField;
    private JButton postButton;
    private JButton cancelButton;
}
