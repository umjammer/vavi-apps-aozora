/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.boot;

import java.awt.Component;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.RootPaneContainer;

import com.soso.aozora.core.AozoraContentPane;
import com.soso.aozora.core.AozoraDesktopPane;
import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.core.AozoraIniFileBean;
import com.soso.aozora.core.AozoraSettingFileHandler;
import com.soso.aozora.data.AozoraBookmarks;
import com.soso.aozora.data.AozoraHistories;
import com.soso.aozora.data.AozoraSettings;
import com.soso.sgui.SGUIUtil;

import de.javasoft.plaf.synthetica.SyntheticaSilverMoonLookAndFeel;


@SuppressWarnings("deprecation")
public class AozoraBootLoader {

    private static AozoraContext _context;
    private static AozoraServerSocket _socket;
    private static boolean isPremier;

    static boolean initServerSocket() {
        AozoraServerSocket serverSocket = getServerSocket(true);
        AozoraSplashWindow.setProgress(AozoraSplashWindow.PROGRESS.SOCKET_OPEN);
        return serverSocket != null;
    }

    static boolean requestAnotherVM(String[] args) {
        getLog().log("boot|起動済みのAozoraViewerへの接続を試みます。");
        try {
            int port = AozoraEnv.getSocketPort();
            getLog().log("boot|ポート[" + port + "]に接続します。");
            SocketAddress address = new InetSocketAddress(InetAddress.getByName(null), port);
            Socket socket = new Socket();
            try {
                socket.connect(address, AozoraEnv.getSocketTimeout());
                BufferedWriter out = null;
                try {
                    out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                    BufferedReader in = null;
                    try {
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                        String hello = in.readLine();
                        if (hello == null || !hello.equals("Hello! This is AozoraViewer.")) {
                            socket.close();
                            throw new IllegalStateException("Unknown hello : " + hello);
                        }
                        out.write("Hello! This is AozoraViewer.\n");
                        out.flush();
                        for (String arg : args) {
                            out.write(arg + "\n");
                        }
        
                        out.write("\n");
                        out.flush();
                        String status = in.readLine();
                        if (status == null || !status.equals("OK")) {
                            socket.close();
                            throw new IllegalStateException("Status : " + status);
                        }
                        getLog().log("boot|起動済みのAozoraViewerへ処理を移行します。");
                        return true;
                    } finally {
                        try {
                            if (in != null)
                                in.close();
                        } catch (Exception e) {
                            e.printStackTrace(System.err);
                        }
                    }
                } finally {
                    try {
                        if (out != null)
                            out.close();
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                }
            } finally {
                socket.close();
            }
        } catch (Exception e) {
            getLog().log(e);
            getLog().log("boot|起動済みのAozoraViewerへの接続に失敗しました。");
            return false;
        }
    }

    static AozoraContext load(RootPaneContainer container, String[] args) {
        initContext();
        setupRootPane(container);
        applyArgs(args);
        AozoraServerSocket socket = getServerSocket(false);
        if (socket != null) {
            getLog().log("boot|他のAozoraViewerからの接続を受け付けます。");
            socket.accept(getContext());
        }
        return getContext();
    }

    private static AozoraLog getLog() {
        return AozoraLog.getInstance();
    }

    static void registerPremier() {
        isPremier = true;
    }

    private static AozoraContext getContext() {
        return _context;
    }

    private static synchronized AozoraContext initContext() {
        if (_context != null)
            throw new IllegalStateException("コンテキストは初期化済みです。");
        _context = new AozoraContext();
        if (isPremier)
            _context.initPremierID();
        compatible_loadIniBean();
        try {
            _context.setSettings(AozoraSettings.load());
        } catch (Exception e) {
            _context.log(e);
            _context.log("設定ファイルのロードに失敗しました。");
            _context.setSettings(AozoraSettings.create());
        }
        AozoraSplashWindow.setProgress(AozoraSplashWindow.PROGRESS.SETTINGS);
        try {
            _context.setBookmarks(AozoraBookmarks.load());
        } catch (Exception e) {
            _context.log(e);
            _context.log("しおりのロードに失敗しました。");
            _context.setBookmarks(AozoraBookmarks.create());
        }
        AozoraSplashWindow.setProgress(AozoraSplashWindow.PROGRESS.BOOKMARKS);
        try {
            _context.setHistories(AozoraHistories.load());
        } catch (Exception e) {
            _context.log(e);
            _context.log("履歴のロードに失敗しました。");
            _context.setHistories(AozoraHistories.create());
        }
        AozoraSplashWindow.setProgress(AozoraSplashWindow.PROGRESS.HISTORIES);
        if (_context.checkCachePermitted())
            try {
                _context.initCacheManager(false);
            } catch (Exception e) {
                _context.log(e);
                _context.log("キャッシュマネージャの初期化でエラーが発生しました。");
                try {
                    _context.initCacheManager(true);
                } catch (Exception e2) {
                    _context.log(e2);
                    _context.log("キャッシュマネージャの初期化で再びエラーが発生しました。");
                    _context.log("キャッシュは利用できません。");
                }
            }
        AozoraSplashWindow.setProgress(AozoraSplashWindow.PROGRESS.CACHE);
        try {
            _context.initCommentManager();
        } catch (Exception e) {
            _context.log(e);
            _context.log("コメントマネージャの初期化でエラーが発生しました。");
        }
        try {
            _context.initListenerManager();
        } catch (Exception e) {
            _context.log(e);
            _context.log("リスナマネージャの初期化でエラーが発生しました。");
        }
        AozoraSplashWindow.setProgress(AozoraSplashWindow.PROGRESS.CONTEXT);
        return _context;
    }

    private static synchronized AozoraServerSocket getServerSocket(boolean init) {
        int port = AozoraEnv.getSocketPort();
        try {
            if (_socket == null && init) {
                getLog().log("boot|ポート[" + port + "]をLISTENします。");
                _socket = new AozoraServerSocket(port);
            }
        } catch (Exception e) {
            getLog().log(e);
            getLog().log("boot|ポート[" + port + "]のLISTENに失敗しました。");
        }
        return _socket;
    }

    private static void setupRootPane(RootPaneContainer container) {
        AozoraContext context = getContext();
        setupFrameIcon(container.getRootPane());
        setupLookAndFeel(container.getRootPane(), context.getSettings());
        AozoraDesktopPane desktopPane = new AozoraDesktopPane(context);
        context.setDesktopPane(desktopPane);
        context.setRootMediator(desktopPane);
        container.setLayeredPane(desktopPane);
        AozoraContentPane contentPane = new AozoraContentPane(context);
        container.setContentPane(contentPane);
    }

    private static void applyArgs(String[] args) {
        AozoraContext context = getContext();
        context.log("args "+ (args == null ? null : Arrays.toString(args)));
        if (args != null)
            context.applyArgs(args);
    }

    private static void compatible_loadIniBean() {
        File compatIniFile = new File(AozoraEnv.getUserHomeDir(), "aozora.xml");
        if (!compatIniFile.exists())
            return;
        try {
            AozoraIniFileBean compatIniBean = (new AozoraSettingFileHandler()).getIni();
            AozoraSettings settings = AozoraSettings.create();
            settings.setFont(compatIniBean.getSystemFont());
            settings.setRowSpace(compatIniBean.getRowSpace());
            settings.setFontRatio(compatIniBean.getFontRatio());
            settings.setLookAndFeel(compatIniBean.getLookAndFeel());
            settings.setForeground(compatIniBean.getForeground());
            settings.setBackground(compatIniBean.getBackground());
            settings.store();
            AozoraBookmarks bookmarks = AozoraBookmarks.create();
            for (String book : compatIniBean.getBookmarks().keySet()) {
                bookmarks.addBookmark(book, Integer.valueOf(compatIniBean.getBookmark(book).intValue()));
            }

            bookmarks.store();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        if (!compatIniFile.delete())
            compatIniFile.deleteOnExit();
    }

    private static void setupLookAndFeel(Component comp, AozoraSettings settings) {
        try {
            javax.swing.LookAndFeel laf;
            if (settings.getLookAndFeel() == null) {
                laf = new SyntheticaSilverMoonLookAndFeel();
                settings.setLookAndFeel(laf);
            } else {
                laf = settings.getLookAndFeel();
            }
            SGUIUtil.setLookAndFeel(laf, SGUIUtil.getParentRecursive(comp));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setupFrameIcon(Component comp) {
        try {
            Frame frame = SGUIUtil.getParentFrame(comp);
            java.net.URL iconURL = AozoraEnv.getIconURL();
            frame.setIconImage((new ImageIcon(iconURL)).getImage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
