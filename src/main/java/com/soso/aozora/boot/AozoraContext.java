/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.boot;

import java.awt.Color;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDesktopPane;
import javax.swing.SwingUtilities;

import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.core.AozoraRootMediator;
import com.soso.aozora.data.AozoraAuthor;
import com.soso.aozora.data.AozoraAuthorParserHandler;
import com.soso.aozora.data.AozoraBookmarks;
import com.soso.aozora.data.AozoraHistories;
import com.soso.aozora.data.AozoraSettings;
import com.soso.aozora.data.AozoraWork;
import com.soso.aozora.data.AozoraWorkParserHandler;
import com.soso.aozora.event.AozoraListenerManager;
import com.soso.sgui.SGUIUtil;
import com.soso.sgui.SOptionPane;


public class AozoraContext {

    static Logger logger = Logger.getLogger(AozoraContext.class.getName());

    AozoraContext() {
        File aozoraDir = new File(AozoraEnv.getUserHomeDir(), "aozora");
        if (!aozoraDir.exists())
            aozoraDir.mkdirs();
    }

    public AozoraSettings getSettings() {
        return settings;
    }

    void setSettings(AozoraSettings settings) {
        this.settings = settings;
        AozoraListenerManager listenerManager = getListenerManager();
        if (listenerManager != null)
            settings.setListenerManager(listenerManager);
    }

    public AozoraBookmarks getBookmarks() {
        return bookmarks;
    }

    void setBookmarks(AozoraBookmarks bookmarks) {
        this.bookmarks = bookmarks;
    }

    public AozoraHistories getHistories() {
        return histories;
    }

    void setHistories(AozoraHistories histories) {
        this.histories = histories;
    }

    public JDesktopPane getDesktopPane() {
        return desktopPane;
    }

    void setDesktopPane(JDesktopPane desktopPane) {
        this.desktopPane = desktopPane;
    }

    public AozoraRootMediator getRootMediator() {
        return rootMediator;
    }

    void setRootMediator(AozoraRootMediator rootMediator) {
        this.rootMediator = rootMediator;
    }

    public Color getDefaultBGColor() {
        return AozoraEnv.DEFAULT_BACKGROUND_COLOR;
    }

    void applyArgs(String[] args) {
        String authorID = null;
        String cardID = null;
        for (String arg : args) {
            if (arg.startsWith("author=")) {
                authorID = arg.substring(7);
            } else if (arg.startsWith("card="))
                cardID = arg.substring(5);
        }

        focusLater(authorID, cardID);
        Window window = SGUIUtil.getParentInstanceOf(getDesktopPane(), Window.class);
        if (window != null) {
            window.toFront();
            window.requestFocus();
        }
    }

    private void focusLater(final String authorID, final String cardID) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                focus(authorID, cardID);
                            }
                        });
                    }
                });
            }
        });
    }

    class AuthorWorkFocusCallback implements AozoraAuthorParserHandler, AozoraWorkParserHandler {

        public void author(AozoraAuthor author) {
            this.author = author;
            if (author == null) {
                SOptionPane.showConfirmDialog(getDesktopPane(), "ID " + authorID + " の著者情報が見つかりません。", "エラー", -1, 0);
                return;
            }
            logger.info("args focusing author=" + authorID);
            getRootMediator().focusAuthor(author);
            if (work != null)
                showViewer();
        }

        public void work(AozoraWork work) {
            this.work = work;
            if (work == null) {
                SOptionPane.showConfirmDialog(getDesktopPane(), "ID " + cardID + " の作品が見つかりません。", "エラー", -1, 0);
                return;
            }
            logger.info("args focusing card=" + cardID);
            getRootMediator().focusWork(work);
            if (author != null)
                showViewer();
        }

        private void showViewer() {
            if (!work.getAuthorID().equals(author.getID())) {
                SOptionPane.showConfirmDialog(getDesktopPane(), "作品「" + work.getTitleName() + "」(ID " + cardID + ")" + "の著者は「" + author.getName() + "」(ID " + authorID + ")" + "ではありません。", "エラー", -1, 0);
            } else {
                logger.info("args showing author=" + authorID + " card=" + cardID);
                getRootMediator().showViewer(author, work);
            }
        }

        private String authorID;
        private String cardID;
        private AozoraAuthor author;
        private AozoraWork work;

        AuthorWorkFocusCallback(String authorID, String cardID) {
            this.authorID = authorID;
            this.cardID = cardID;
        }
    }

    private void focus(String authorID, String cardID) {

        AuthorWorkFocusCallback callback = new AuthorWorkFocusCallback(authorID, cardID);

        if (authorID != null) {
            logger.info("args loading author=" + authorID);
            getRootMediator().getAozoraAuthorAsynchronous(authorID, callback);
        }
        if (cardID != null) {
            logger.info("args loading card=" + cardID);
            getRootMediator().getAozoraWorkAsynchronous(cardID, callback);
        }
    }

    public void setLineMode(final AozoraEnv.LineMode lineMode) {
        if (lineMode.isConnectable()) {
            boolean testConnect = false;
            try {
                AozoraManifest.getOnlineVersion();
                testConnect = true;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "テスト接続に失敗 : " + e.getMessage(), e);
            }
            if (!testConnect)
                switch (SOptionPane.showInternalCustomeDialog(getDesktopPane(), new Object[] {
                    "ネットワークへのテスト接続に失敗しました。",
                    "本当にオンラインモードへ切り替えますか？"
                }, "オンラインへの切り替え", 1, 2, null, null, null, null, "再試行", "強制", null)) {
                case 0:
                    logger.info("テスト接続を再試行します。");
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            setLineMode(lineMode);
                        }
                    });
                    return;
                case 1:
                    logger.info("テスト接続に失敗しましたが、" + lineMode + " に設定します。");
                    break;
                case 2:
                    logger.info(lineMode + " への設定がキャンセルされました。");
                    return;
                default:
                    return;
                }
        }
    }

    void initListenerManager() throws IOException {
        if (listenerManager != null) {
            throw new IllegalStateException("リスナマネージャは初期化済みです。");
        } else {
            logger.info("リスナマネージャを初期化します。");
            listenerManager = new AozoraListenerManager(this);
            getSettings().setListenerManager(listenerManager);
            return;
        }
    }

    public AozoraListenerManager getListenerManager() {
        return listenerManager;
    }

    public AozoraManifest getManifest() {
        if (manifest == null)
            manifest = AozoraManifest.getThisVersion();
        return manifest;
    }

    private JDesktopPane desktopPane;
    private AozoraRootMediator rootMediator;
    private AozoraSettings settings;
    private AozoraBookmarks bookmarks;
    private AozoraHistories histories;
    private AozoraListenerManager listenerManager;
    private AozoraManifest manifest;
}
