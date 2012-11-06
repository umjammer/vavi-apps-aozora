/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraDefaultPane;
import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.core.AozoraUtil;
import com.soso.aozora.data.AozoraAuthor;
import com.soso.aozora.data.AozoraBookmarks;
import com.soso.aozora.data.AozoraCacheManager;
import com.soso.aozora.data.AozoraComment;
import com.soso.aozora.data.AozoraWork;
import com.soso.sgui.SButton;
import com.soso.sgui.SGUIUtil;
import com.soso.sgui.SLinkLabel;


public class AozoraViewerPane extends AozoraDefaultPane {

    private final AozoraAuthor author;
    private final AozoraWork work;
    private JPanel ctrlPane;
    private JButton infoButton;
    private JButton bookmarkButton;
    private JButton cacheDownloadButton;
    private JButton cacheDeleteButton;
    private AuthorViewerPane authorViewer;
    private WorkViewerPane workViewer;
    private TextViewerPane textViewer;
    private LinkPane linkPane;
    private boolean isInfoVisible;

    public AozoraViewerPane(AozoraContext context, AozoraAuthor author, AozoraWork work, boolean isCache, int position) {
        super(context);
        this.author = author;
        this.work = work;
        if (position == 0) {
            AozoraBookmarks.AozoraBookmarkEntry bookmark = getAzContext().getBookmarks().getBookmark(getWork().getID());
            if (bookmark != null)
                position = bookmark.getPosition();
        }
        initGUI(isCache, position);
        resetCacheButtons();
    }

    private void initGUI(boolean isCache, int firstStartPos) {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(5, 5, 5, 5));
        JPanel metaPane = new JPanel();
        metaPane.setBackground(getAzContext().getDefaultBGColor());
        metaPane.setLayout(new BorderLayout(5, 5));
        authorViewer = new AuthorViewerPane(getAzContext(), getAuthor());
        metaPane.add(authorViewer, BorderLayout.WEST);
        workViewer = new WorkViewerPane(getAzContext(), getWork());
        metaPane.add(workViewer, BorderLayout.CENTER);
        linkPane = new LinkPane(getAzContext(), getAuthor(), getWork());
        JPanel linkPaneHolder = new JPanel();
        linkPaneHolder.setOpaque(false);
        linkPaneHolder.add(linkPane);
        metaPane.add(linkPaneHolder, BorderLayout.SOUTH);
        ctrlPane = new JPanel();
        ctrlPane.setLayout(new BoxLayout(ctrlPane, BoxLayout.X_AXIS));
        infoButton = new SButton();
        infoButton.setAction(new AbstractAction(null, AozoraUtil.getIcon(AozoraEnv.Env.INFO_ICON.getString())) {
            public void actionPerformed(ActionEvent e) {
                toggleInfoVisible();
            }
        });
        infoButton.setName("AozoraViewerPane.infoButton");
        AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_IN_FOCUSED_WINDOW, AozoraEnv.ShortCutKey.WORKINFO_SHORTCUT.getKeyStroke(), infoButton);
        infoButton.setToolTipText(AozoraEnv.ShortCutKey.WORKINFO_SHORTCUT.getNameWithHelpTitle());
        ctrlPane.add(infoButton);
        ctrlPane.add(Box.createHorizontalStrut(5));
        ctrlPane.add(createShortcutLinkLabel());
        ctrlPane.add(Box.createHorizontalGlue());
        cacheDownloadButton = new SButton();
        cacheDownloadButton.setAction(new AbstractAction(null, AozoraUtil.getIcon(AozoraEnv.Env.CACHE_DOWNLOAD_ICON.getString())) {
            public void actionPerformed(ActionEvent e) {
                cacheDownload(true);
            }
        });
        cacheDownloadButton.setName("AozoraViewerPane.cacheDownloadButton");
        AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_IN_FOCUSED_WINDOW, AozoraEnv.ShortCutKey.CACHE_DOWNLOAD_SHORTCUT.getKeyStroke(), cacheDownloadButton);
        cacheDownloadButton.setToolTipText(AozoraEnv.ShortCutKey.CACHE_DOWNLOAD_SHORTCUT.getNameWithHelpTitle());
        ctrlPane.add(cacheDownloadButton);
        ctrlPane.add(Box.createHorizontalStrut(5));
        cacheDeleteButton = new SButton();
        cacheDeleteButton.setAction(new AbstractAction(null, AozoraUtil.getIcon(AozoraEnv.Env.CACHE_DELETE_ICON.getString())) {
            public void actionPerformed(ActionEvent e) {
                cacheDelete(true);
            }
        });
        cacheDeleteButton.setName("AozoraViewerPane.cacheDeleteButton");
        AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_IN_FOCUSED_WINDOW, AozoraEnv.ShortCutKey.CACHE_DELETE_SHORTCUT.getKeyStroke(), cacheDeleteButton);
        cacheDeleteButton.setToolTipText(AozoraEnv.ShortCutKey.CACHE_DELETE_SHORTCUT.getNameWithHelpTitle());
        ctrlPane.add(cacheDeleteButton);
        ctrlPane.add(Box.createHorizontalStrut(5));
        bookmarkButton = new SButton();
        bookmarkButton.setAction(new AbstractAction(null, AozoraUtil.getIcon(AozoraEnv.Env.BOOKMARK_ICON.getString())) {
            public void actionPerformed(ActionEvent e) {
                bookmark();
            }
        });
        bookmarkButton.setName("AozoraViewerPane.bookmarkButton");
        AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_IN_FOCUSED_WINDOW, AozoraEnv.ShortCutKey.BOOKMARK_SAVE_SHORTCUT.getKeyStroke(), bookmarkButton);
        bookmarkButton.setToolTipText(AozoraEnv.ShortCutKey.BOOKMARK_SAVE_SHORTCUT.getNameWithHelpTitle());
        ctrlPane.add(bookmarkButton);
        metaPane.add(ctrlPane, BorderLayout.NORTH);
        add(metaPane, BorderLayout.NORTH);
        setInfoVisible(false);
        textViewer = new TextViewerPane(getAzContext(), getWork(), isCache, firstStartPos);
        add(textViewer, BorderLayout.CENTER);
        AozoraUtil.putKeyStrokeAction(this, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, AozoraEnv.ShortCutKey.SEARCH_IN_WORK_SHORTCUT.getKeyStroke(), "TextViewerPane.searchAction", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                textViewer.setSearchEnable(true);
            }
        });
    }

    public AozoraAuthor getAuthor() {
        return author;
    }

    public AozoraWork getWork() {
        return work;
    }

    private SLinkLabel createShortcutLinkLabel() {
        java.net.URL shortcutURL = AozoraEnv.getShortcutURL(getAuthor().getID(), getWork().getID(), getWork().getTitleName());
        SLinkLabel shortcutLinkLabel = new SLinkLabel(shortcutURL);
        Font font = shortcutLinkLabel.getFont();
        if (font != null)
            shortcutLinkLabel.setFont(new Font(font.getName(), 0, font.getSize() - 1));
        Dimension prefSize = shortcutLinkLabel.getPreferredSize();
        shortcutLinkLabel.setMinimumSize(new Dimension(prefSize.height != 0 ? prefSize.height : 16,
                                                       prefSize.width != 0 ? Math.min(100, prefSize.width) : 100));
        return shortcutLinkLabel;
    }

    private void toggleInfoVisible() {
        setInfoVisible(!isInfoVisible);
    }

    private void setInfoVisible(boolean visible) {
        isInfoVisible = visible;
        authorViewer.setVisible(visible);
        workViewer.setVisible(visible);
        linkPane.setVisible(visible);
        revalidate();
    }

    private void bookmark() {
        getAzContext().getBookmarks().addBookmark(getWork().getID(), Integer.valueOf(textViewer.getStartPos()));
        try {
            getAzContext().getBookmarks().store();
            JOptionPane.showInternalMessageDialog(getAzContext().getDesktopPane(), "しおりをはさみました");
        } catch (Exception e) {
            log(e);
            Object mssg = "しおりをはさむ際にエラーが発生しました。";
            if (e.getMessage() != null)
                mssg = new String[] {
                    (String) mssg, e.getMessage()
                };
            JOptionPane.showInternalMessageDialog(getAzContext().getDesktopPane(), mssg, "エラー", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String getTitle() {
        String titleName = getWork().getTitleName() + " - " + getAuthor().getName();
        if (isCache())
            titleName = titleName + " のキャッシュ";
        return titleName;
    }

    void resetCacheButtons() {
        cacheDownloadButton.setEnabled(!isCache() && isCacheDownloadable());
        cacheDownloadButton.setVisible(!isCache());
        cacheDeleteButton.setEnabled(isCache() && isCacheDeletable());
        cacheDeleteButton.setVisible(isCache());
        JInternalFrame iframe = SGUIUtil.getParentInstanceOf(this, JInternalFrame.class);
        if (iframe != null)
            iframe.setTitle(getTitle());
    }

    private boolean isCache() {
        return textViewer.isCache();
    }

    private void setCache(boolean isCache) {
        int lastStartPos = textViewer.getStartPos();
        removeAll();
        initGUI(isCache, lastStartPos);
        revalidate();
    }

    private boolean isCacheDownloadable() {
        try {
            cacheDownload(false);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void cacheDownload(boolean isDoDownload) {
        if (!getAzContext().checkCachePermitted())
            throw new IllegalStateException("キャッシュ権限がありません。");
        AozoraCacheManager cacheManager = getAzContext().getCacheManager();
        if (cacheManager == null)
            throw new IllegalStateException("キャッシュマネージャーがありません。");
        if (cacheManager.isReadOnly())
            throw new IllegalStateException("キャッシュは読み込み専用です。");
        if (!getAzContext().getLineMode().isConnectable())
            throw new IllegalStateException("オフラインモードではキャッシュできません。");
        if (!isDoDownload)
            return;
        try {
            cacheManager.putCache(getAuthor(), getWork());
            JOptionPane.showInternalMessageDialog(getAzContext().getDesktopPane(), "キャッシュしました。");
            setCache(true);
            resetCacheButtons();
        } catch (Exception e) {
            log(e);
            Object mssg = "キャッシュでエラーが発生しました。";
            if (e.getMessage() != null)
                mssg = new String[] {
                    (String) mssg, e.getMessage()
                };
            JOptionPane.showInternalMessageDialog(getAzContext().getDesktopPane(), mssg, "エラー", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isCacheDeletable() {
        try {
            cacheDelete(false);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void cacheDelete(boolean isDoDelete) {
        if (!getAzContext().checkCachePermitted())
            throw new IllegalStateException("キャッシュ権限がありません。");
        AozoraCacheManager cacheManager = getAzContext().getCacheManager();
        if (cacheManager == null)
            throw new IllegalStateException("キャッシュマネージャーがありません。");
        if (cacheManager.isReadOnly())
            throw new IllegalStateException("キャッシュは読み込み専用です。");
        if (!isDoDelete)
            return;
        try {
            int rs = JOptionPane.showInternalConfirmDialog(getAzContext().getDesktopPane(), getTitle() + " を削除しますか。");
            if (rs == JOptionPane.YES_OPTION) {
                cacheManager.removeCache(getWork().getID());
                JOptionPane.showInternalMessageDialog(getAzContext().getDesktopPane(), "キャッシュを削除しました。");
                if (!getAzContext().getLineMode().isConnectable()) {
                    JInternalFrame parentFrame = SGUIUtil.getParentInstanceOf(this, JInternalFrame.class);
                    if (parentFrame != null)
                        parentFrame.dispose();
                } else {
                    setCache(false);
                    resetCacheButtons();
                }
            }
        } catch (Exception e) {
            log(e);
            Object mssg = getTitle() + " の削除でエラーが発生しました。";
            if (e.getMessage() != null)
                mssg = new String[] {
                    (String) mssg, e.getMessage()
                };
            JOptionPane.showInternalMessageDialog(getAzContext().getDesktopPane(), mssg, "エラー", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setStartPosition(int position) {
        textViewer.setStartPos(position);
    }

    public void setStartPositionByComment(AozoraComment comment) {
        textViewer.setStartPosByComment(comment);
    }

    public void focus() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                textViewer.requestFocusInWindow();
            }
        });
    }

    public void close() {
        textViewer.close();
        getAzContext().getHistories().addHistory(getWork().getID(), textViewer.getStartPos());
        try {
            getAzContext().getHistories().store();
        } catch (Exception e) {
            getAzContext().log("履歴の保存に失敗しました。");
            getAzContext().log(e);
        }
    }

    public String toString() {
        return String.format("%s - %s", getAuthor().getName(), getWork().getTitleName());
    }
}
