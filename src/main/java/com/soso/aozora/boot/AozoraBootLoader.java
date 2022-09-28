/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.boot;

import com.soso.aozora.core.AozoraContentPane;
import com.soso.aozora.core.AozoraDesktopPane;
import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.core.AozoraIniFileBean;
import com.soso.aozora.core.AozoraSettingFileHandler;
import com.soso.aozora.data.AozoraBookmarks;
import com.soso.aozora.data.AozoraHistories;
import com.soso.aozora.data.AozoraSettings;
import com.soso.sgui.SGUIUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;


@SuppressWarnings("deprecation")
public class AozoraBootLoader {

    static Logger logger = Logger.getLogger(AozoraBootLoader.class.getName());

    private static AozoraContext _context;

    static AozoraContext load(RootPaneContainer container, String[] args) {
        initContext();
        setupRootPane(container);
        applyArgs(args);
        return getContext();
    }

    private static AozoraContext getContext() {
        return _context;
    }

    private static synchronized AozoraContext initContext() {
        if (_context != null)
            throw new IllegalStateException("コンテキストは初期化済みです。");
        _context = new AozoraContext();
        compatible_loadIniBean();
        try {
            _context.setSettings(AozoraSettings.load());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "設定ファイルのロードに失敗しました。", e);
            _context.setSettings(AozoraSettings.create());
        }
        try {
            _context.setBookmarks(AozoraBookmarks.load());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "しおりのロードに失敗しました。", e);
            _context.setBookmarks(AozoraBookmarks.create());
        }
        try {
            _context.setHistories(AozoraHistories.load());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "履歴のロードに失敗しました。");
            _context.setHistories(AozoraHistories.create());
        }
        try {
            _context.initListenerManager();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "リスナマネージャの初期化でエラーが発生しました。", e);
        }
        return _context;
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
        logger.info("args "+ (args == null ? null : Arrays.toString(args)));
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
                bookmarks.addBookmark(book, compatIniBean.getBookmark(book));
            }

            bookmarks.store();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!compatIniFile.delete())
            compatIniFile.deleteOnExit();
    }

    private static void setupLookAndFeel(Component comp, AozoraSettings settings) {
        try {
            javax.swing.LookAndFeel laf;
            laf = settings.getLookAndFeel();
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
