/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.boot;

import java.awt.Color;
import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.core.AozoraRootMediator;
import com.soso.aozora.core.AozoraUtil;
import com.soso.aozora.data.AozoraAuthor;
import com.soso.aozora.data.AozoraAuthorParserHandler;
import com.soso.aozora.data.AozoraBookmarks;
import com.soso.aozora.data.AozoraCacheManager;
import com.soso.aozora.data.AozoraCommentManager;
import com.soso.aozora.data.AozoraHistories;
import com.soso.aozora.data.AozoraPremierID;
import com.soso.aozora.data.AozoraSettings;
import com.soso.aozora.data.AozoraWork;
import com.soso.aozora.data.AozoraWorkParserHandler;
import com.soso.aozora.event.AozoraListenerManager;
import com.soso.sgui.SGUIUtil;
import com.soso.sgui.SLinkLabel;
import com.soso.sgui.SOptionPane;


public class AozoraContext {

    AozoraContext() {
        lineMode = isJNLPxOffline() ? AozoraEnv.LineMode.Offline : AozoraEnv.LineMode.Online;
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

    public void log(Object obj) {
        AozoraLog.getInstance().log(obj);
    }

    public void log(Object obj, Class<?> c) {
        AozoraLog.getInstance().log(obj, c);
    }

    public void log(Class<?> c, String format, Object... args) {
        AozoraLog.getInstance().log(c, format, args);
    }

    public void log(Throwable t) {
        AozoraLog.getInstance().log(t);
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
            log("args focusing author=" + authorID);
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
            log("args focusing card=" + cardID);
            getRootMediator().focusWork(work);
            if (author != null)
                showViewer();
        }

        private void showViewer() {
            if (!work.getAuthorID().equals(author.getID())) {
                SOptionPane.showConfirmDialog(getDesktopPane(), "作品「" + work.getTitleName() + "」(ID " + cardID + ")" + "の著者は「" + author.getName() + "」(ID " + authorID + ")" + "ではありません。", "エラー", -1, 0);
            } else {
                log("args showing author=" + authorID + " card=" + cardID);
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
            log("args loading author=" + authorID);
            getRootMediator().getAozoraAuthorAsynchronous(authorID, callback);
        }
        if (cardID != null) {
            log("args loading card=" + cardID);
            getRootMediator().getAozoraWorkAsynchronous(cardID, callback);
        }
    }

    void initPremierID() {
        log(" premier | setup ...");
        com.soso.aozora.data.AozoraFileLock lock = null;
        try {
            lock = AozoraPremierID.lock();
        } catch (Exception e) {
            log(e);
            exitWithError(
                "Premier認証ロックの取得に失敗しました。",
                "他に Aozora Viewer が起動していないことを確認して、",
                "もう一度起動してください。"
            );
        }
        try {
            premierID = AozoraPremierID.load(lock);
        } catch (Exception e) {
            log(e);
            exitWithError(
                "Premier認証ファイルの読み込みに失敗しました。"
            );
        }
        if (premierID == null)
            try {
                premierID = AozoraPremierID.create(lock);
            } catch (Exception e) {
                log(e);
                exitWithError(
                    "Premier認証IDの取得に失敗しました。",
                    "ネットワークに接続されていることを確認し、",
                    "もう一度起動してください。"
                );
            }
        else if (getLineMode().isConnectable())
            checkOnlinePremierID();
        checkPremierStatus();
    }

    private void checkPremierStatus() {
        if (premierID == null)
            exitWithError(
                "Premier認証IDの状態が不正です。"
            );
        AozoraPremierID.Status status = premierID.getStatus();
        log(" premier | check status for " + status);

        switch (status) {
        case TRIAL:
            if (premierID.canTrialSkip()) {
                premierID.doTrialSkip();
                storePremierID();
                break;
            }
            if (getLineMode().isConnectable())
                exitTrialForAccounting();
            checkOnlinePremierID();
            if (premierID.getStatus() != AozoraPremierID.Status.TRIAL)
                checkPremierStatus();
            else
                exitTrialForAccounting();
            break;
        case ACCOUNTING:
            if (getLineMode().isConnectable()) {
                log("premier | status is ACCOUNTING now");
                break;
            }
            if (premierID.isAccountingTerm()) {
                log("premier | status is ACCOUNTING " + new Date(premierID.getLastCheckTimestamp()));
            } else {
                checkOnlinePremierID();
                checkPremierStatus();
            }
            break;
        case CONFIRMED:
            log("premier | status is CONFIRMED");
            break;
        case INVALID:
            if (!getLineMode().isConnectable())
                checkOnlinePremierID();
            if (premierID.getStatus() != AozoraPremierID.Status.INVALID)
                checkPremierStatus();
            else
                exitWithInvalid();
            break;
        default:
            throw new IllegalArgumentException("Unknown status " + premierID.getStatus());
        }
    }

    private void checkOnlinePremierID() {
        if (!getLineMode().isConnectable()) {
            AozoraSplashWindow.hideSplash();
            if (JOptionPane.showConfirmDialog(getDesktopPane(), "オフライン起動ですが、Premierオンライン認証を行います。", "Aozora Viewer", 2, 2) != 0)
                exitWithError(
                    "終了します。",
                    "ネットワークに接続できることを確認し、",
                    "もう一度起動してください。"
                );
            AozoraSplashWindow.showSplash();
        }
        try {
            premierID.checkOnline();
        } catch (IOException e) {
            log(e);
            AozoraSplashWindow.hideSplash();
            if (SOptionPane.showCustomeDialog(getDesktopPane(), "Premierオンライン認証に失敗しました。",
                                              "Aozora Viewer", 2, 2, null, null, null,
                                              "再試行", null, null, "スキップ") == 0) {
                checkOnlinePremierID();
                return;
            }
            AozoraSplashWindow.showSplash();
        }
        storePremierID();
    }

    private void storePremierID() {
        try {
            premierID.store();
        } catch (Exception e) {
            log(e);
            AozoraSplashWindow.hideSplash();
            if (SOptionPane.showCustomeDialog(getDesktopPane(), "Premier認証ファイルの書き込みに失敗しました。",
                                              "Aozora Viewer", 2, 2, null, null, null, "再試行", null, null, "終了") == 0) {
                storePremierID();
                AozoraSplashWindow.showSplash();
            } else {
                AozoraSplashWindow.disposeSplash();
                System.exit(0);
            }
        }
    }

    private void exitTrialForAccounting() {
        AozoraSplashWindow.hideSplash();
        JOptionPane.showMessageDialog(getDesktopPane(), new Object[] {
            new JLabel(AozoraUtil.getIcon(AozoraEnv.Env.AOZORA_SPLASH_URL.getString())),
            "Aozora Viewer プレミア をご利用いただきありがとうございます。",
            "試用回数を越えましたので、引き続きご利用いただく場合には、",
            "次のアドレスにブラウザからアクセスして、代金をお支払いください。",
            new SLinkLabel(AozoraEnv.getPremierPayURL(premierID.getID())), " ", "その後、ネットワークに接続できることを確認し、もう一度起動してください。"
        }, "Aozora Viewer", -1);
        AozoraSplashWindow.disposeSplash();
        System.exit(0);
    }

    private void exitWithInvalid() {
        Object mailto = null;
        try {
            mailto = new SLinkLabel(new URL("mailto:customer@35-35.com"), "customer@35-35.com");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        exitWithError(
            "Aozora Viewer プレミア 利用代金のお支払いが無効になっています。",
            " ",
            "次のアドレスにブラウザからアクセスして、代金をお支払いください。",
            new SLinkLabel(AozoraEnv.getPremierPayURL(premierID.getID())),
            " ",
            "またはメール受付アドレスまでお問い合わせください。",
            mailto == null ? "customer@35-35.com" : mailto
        );
    }

    private/* transient */void exitWithError(Object... mssg) {
        AozoraSplashWindow.hideSplash();
        JOptionPane.showMessageDialog(getDesktopPane(), mssg, "Aozora Viewer", 2);
        AozoraSplashWindow.disposeSplash();
        System.exit(1);
    }

    public AozoraPremierID getPremierID() {
        return premierID;
    }

    public boolean isPremier() {
        return getPremierID() != null;
    }

    public boolean checkCachePermitted() {
        if (!isPremier())
            return false;
        if (premierID != null)
            return premierID.isValid();
        else
            return false;
    }

    public void setLineMode(final AozoraEnv.LineMode lineMode) {
        if (!isPremier() && lineMode != AozoraEnv.LineMode.Online)
            throw new IllegalStateException("オンライン以外のモードはプレミア版でのみ使用可能です。");
        if (lineMode.isConnectable()) {
            boolean testConnect = false;
            try {
                AozoraManifest.getOnlineVersion();
                testConnect = true;
            } catch (Exception e) {
                log(e);
                log("テスト接続に失敗 : " + e.getMessage());
            }
            if (!testConnect)
                switch (SOptionPane.showInternalCustomeDialog(getDesktopPane(), new Object[] {
                    "ネットワークへのテスト接続に失敗しました。",
                    "本当にオンラインモードへ切り替えますか？"
                }, "オンラインへの切り替え", 1, 2, null, null, null, null, "再試行", "強制", null)) {
                case 0:
                    log("テスト接続を再試行します。");
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            setLineMode(lineMode);
                        }
                    });
                    return;
                case 1:
                    log("テスト接続に失敗しましたが、" + lineMode + " に設定します。");
                    break;
                case 2:
                    log(lineMode + " への設定がキャンセルされました。");
                    return;
                default:
                    return;
                }
        }
        this.lineMode = lineMode;
        getListenerManager().lineModeChanged(lineMode);
        if (lineMode.isConnectable())
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    checkOnlinePremierID();
                    checkPremierStatus();
                    AozoraManifest manifest = getManifest();
                    AozoraManifest onlineManifest = AozoraManifest.getOnlineVersion();
                    if (onlineManifest.getBuildDate().after(manifest.getBuildDate()))
                        JOptionPane.showInternalMessageDialog(getDesktopPane(), new Object[] {
                            new JLabel(AozoraUtil.getIcon(AozoraEnv.Env.AOZORA_SPLASH_URL.getString())),
                            "新しいバージョンの Aozora Viewer が公開されています。",
                            "現在のバージョン : " + manifest.getSpecificationVersion() + " (" + manifest.getImplementationVersion() + ")",
                            "新しいバージョン : " + onlineManifest.getSpecificationVersion() + " (" + onlineManifest.getImplementationVersion() + ")",
                            " ",
                            "新しいバージョンを使用するためには、この Aozora Viewer Premier を一度閉じ、",
                            "以下のホームページから再度 Aozora Viewer Premier を起動してください。",
                            new SLinkLabel(onlineManifest.getImplementationVendorURL())
                        }, "新しいバージョン", -1);
                }
            });
    }

    public AozoraEnv.LineMode getLineMode() {
        return lineMode;
    }

    private boolean isJNLPxOffline() {
        try {
            Class<?> managerClass = Class.forName("javax.jnlp.ServiceManager");
            Method lookupMethod = managerClass.getMethod("lookup", java.lang.String.class);
            Object basicServiceInstance = lookupMethod.invoke(managerClass, "javax.jnlp.BasicService");
            Class<?> basicServiceClass = Class.forName("javax.jnlp.BasicService");
            Method isOffline = basicServiceClass.getMethod("isOffline");
            Object result = isOffline.invoke(basicServiceInstance);
            if (result instanceof Boolean && ((Boolean) result).booleanValue())
                return true;
        } catch (Exception e) {
            log("Warning|fail to launch javax.jnlp.BasicService#isOffline " + e);
        }
        return Boolean.getBoolean("jnlpx.offline");
    }

    void initCacheManager(boolean readOnly) throws IOException {
        if (!isPremier())
            throw new IllegalStateException("キャッシュはプレミア版でのみ使用可能です。");
        if (cacheManager != null)
            throw new IllegalStateException("キャッシュマネージャは初期化済みです。");
        File cacheBase = new File(AozoraEnv.getUserHomeDir(), "aozora/cache");
        if (!cacheBase.exists()) {
            log("キャッシュ用ディレクトリを作成します。" + cacheBase);
            cacheBase.mkdirs();
        }
        if (!cacheBase.exists()) {
            throw new FileNotFoundException("キャッシュ用ディレクトリが存在しません。" + cacheBase);
        } else {
            log("キャッシュマネージャを" + (readOnly ? "読み込み専用" : "読み書き可能") + "で初期化します。");
            cacheManager = new AozoraCacheManager(this, cacheBase, readOnly);
            return;
        }
    }

    public AozoraCacheManager getCacheManager() {
        return cacheManager;
    }

    void initCommentManager() throws IOException {
        if (commentManager != null) {
            throw new IllegalStateException("コメントマネージャは初期化済みです。");
        } else {
            log("コメントマネージャを初期化します。");
            commentManager = new AozoraCommentManager(this);
            return;
        }
    }

    public AozoraCommentManager getCommentManager() {
        return commentManager;
    }

    void initListenerManager() throws IOException {
        if (listenerManager != null) {
            throw new IllegalStateException("リスナマネージャは初期化済みです。");
        } else {
            log("リスナマネージャを初期化します。");
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
    private AozoraEnv.LineMode lineMode;
    private AozoraPremierID premierID;
    private AozoraCacheManager cacheManager;
    private AozoraCommentManager commentManager;
    private AozoraListenerManager listenerManager;
    private AozoraManifest manifest;
}
