/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.core;

import java.awt.Color;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ResourceBundle;

import javax.swing.KeyStroke;


public class AozoraEnv {
    public enum ShortCutKey {
        AOZORA_SHORTCUT("ヘルプ", "F1", "ヘルプを表示します。"),
        CHANGE_LOOK_AND_FEEL_SHORTCUT("デザイン変更", "alt L", "デザイン変更ダイアログを表示します。"),
        CHANGE_FONT_SHORTCUT("フォント変更", "alt F", "フォント変更ダイアログを表示します。"),
        CHANGE_COLOR_SHORTCUT("色変更", "alt M", "色変更ダイアログを表示します。"),
        SAVE_SHORTCUT("設定を保存する", "alt S", "デザインなどの設定を保存します。"),
        LINE_SPACE_WIDE_SHORTCUT("行間を広げる", "F7", "行間を広げます。"),
        LINE_SPACE_NARROW_SHORTCUT("行間を狭める", "F8", "行間を狭めます。"),
        FONT_RATIO_WIDE_SHORTCUT("文字間を広げる", "F9", "文字間を広げます。"),
        FONT_RATIO_NARROW_SHORTCUT("文字間を狭める", "F10", "文字間を狭めます。"),
        BOOKMARK_OPEN_SHORTCUT("しおりを開く", "alt B", "しおりを開く選択メニューを表示します。"),
        COMMENT_SHORTCUT("コメント表示", "alt C", "コメントの表示／非表示を切り替えます。"),
        LINE_MODE_SHORTCUT("オンライン／オフライン", "PAUSE", "オンライン／オフラインを切り替えます。"),
        VIEWER_CLOSE_SHORTCUT("閉じる", "ctrl W", "作品閲覧ウィンドウを閉じます。"),
        WORKINFO_SHORTCUT("作品情報", "shift F1", "作品情報の表示／非表示を切り替えます。"),
        BOOKMARK_SAVE_SHORTCUT("しおりをはさむ", "ctrl S", "しおりをはさみます。"),
        CACHE_DOWNLOAD_SHORTCUT("作品をキャッシュ", "ctrl D", "作品をローカルにキャッシュします。"),
        CACHE_DELETE_SHORTCUT("キャッシュを削除", "DELETE", "ローカルにキャッシされた作品を削除します。"),
        PAGE_NEXT_LEFT_SHORTCUT("次のページ", "LEFT", "次のページを表示します。（縦書き）"),
        PAGE_NEXT_DOWN_SHORTCUT("次のページ", "DOWN", "次のページを表示します。（横書き）"),
        PAGE_NEXT_RIGHT_SHORTCUT("次のページ", "RIGHT", "次のページを表示します。（TBLR）"),
        PAGE_PREV_RIGHT_SHORTCUT("前のページ", "RIGHT", "前のページを表示します。（縦書き）"),
        PAGE_PREV_UP_SHORTCUT("前のページ", "UP", "前のページを表示します。（横書き）"),
        PAGE_PREV_LEFT_SHORTCUT("前のページ", "LEFT", "前のページを表示します。（TBLR）"),
        SEARCH_IN_WORK_SHORTCUT("文章内検索", "meta F", "文章内検索フィールドを表示します。"),
        SEARCH_IN_WORK_NEXT_SHORTCUT("次を検索", "F3", "文章内検索で次を検索します。"),
        SEARCH_IN_WORK_PREV_SHORTCUT("前を検索", "shift F3", "文章内検索で前を検索します。"),
        SEARCH_IN_WORK_CLOSE_SHORTCUT("文章内検索を閉じる", "ESCAPE", "文章内検索フィールドを閉じます。");

        private static String keyStrokeToHelpTitle(String keyStrole) {
            return keyStrole.replaceAll("shift", "Shift")
                            .replaceAll("alt", "Alt")
                            .replaceAll("ctrl", "Ctrl")
                            .replaceAll("ESCAPE", "Esc")
                            .replaceAll("DELETE", "Del")
                            .replaceAll("PAUSE", "Pause")
                            .replaceAll("LEFT", "←")
                            .replaceAll("RIGHT", "→")
                            .replaceAll("UP", "↑")
                            .replaceAll("DOWN", "↓")
                            .replaceAll(" ", " + ");
        }

        public String getName() {
            return name;
        }

        public KeyStroke getKeyStroke() {
            return keyStroke;
        }

        public String getHelpTitle() {
            return helpTitle;
        }

        public String getHelpDescription() {
            return helpDescription;
        }

        public String getNameWithHelpTitle() {
            return getName() + " (" + getHelpTitle() + ")";
        }

        private String name;
        private KeyStroke keyStroke;
        private String helpTitle;
        private String helpDescription;

        private ShortCutKey(String name, String keyStroke, String helpDescription) {
            this.name = name;
            this.keyStroke = KeyStroke.getKeyStroke(keyStroke);
            this.helpTitle = keyStrokeToHelpTitle(keyStroke);
            this.helpDescription = helpDescription;
        }
    }

    public enum Env {
        AOZORA_SOCKET_PORT("aozora.socket.port", null),
        AOZORA_SOCKET_TIMEOUT("aozora.socket.timeout", null),
        AOZORA_DATA_URL("aozora.data.url", null),
        AOZORA_ICON_URL("aozora.icon.url", null),
        AOZORA_SPLASH_URL("aozora.splash.url", null),
        AOZORA_BUNKO_SITE_URL("aozora.bunko.site.url", null),
        AOZORA_BUNKO_ICON_URL("aozora.bunko.icon.url", null),
        AOZORA_MANIFEST_URL("aozora.manifest.url", null),
        LINE_SPACE_WIDE_ICON("line.space.wide.icon", null),
        LINE_SPACE_NARROW_ICON("line.space.narrow.icon", null),
        FONT_RATIO_WIDE_ICON("font.ratio.wide.icon", null),
        FONT_RATIO_NARROW_ICON("font.ratio.narrow.icon", null),
        CHANGE_LOOK_AND_FEEL_ICON("change.look.and.feel.icon", null),
        CHANGE_FONT_ICON("change.font.icon", null),
        CHANGE_COLOR_ICON("change.color.icon", null),
        SAVE_ICON("save.icon", null),
        BOOKMARK_ICON("bookmark.icon", null),
        DATABASE_ICON("database.icon", null),
        FEED_ICON("feed.icon", null),
        NEW_ICON("new.icon", null),
        RANKING_ICON("ranking.icon", null),
        GO_LEFT_ICON("go.left.icon", null),
        GO_RIGHT_ICON("go.right.icon", null),
        GO_UP_ICON("go.up.icon", null),
        GO_DOWN_ICON("go.down.icon", null),
        GO_LEFT_VIEW_ICON("go.left.view.icon", null),
        GO_RIGHT_VIEW_ICON("go.right.view.icon", null),
        INFO_ICON("info.icon", null),
        HORIZ_VERT_SWITCH_ICON("horiz.vert.switch.icon", null),
        OFFLINE_ICON("offline.icon", null),
        ONLINE_ICON("online.icon", null),
        CACHE_FOLDER_ICON("cache.folder.icon", null),
        CACHE_DOWNLOAD_ICON("cache.download.icon", null),
        CACHE_DELETE_ICON("cache.delete.icon", null),
        COMMENT_ICON("comment.icon", null),
        HISTORY_ICON("history.icon", null);

        public String getString() {
            return getString(BUNDLE_NAME, keyName, defaultValue);
        }

        private static String getString(String bundle, String key, String defaultValue) {
            String value = null;
            try {
                value = ResourceBundle.getBundle(bundle).getString(key);
                if (value.trim().length() == 0)
                    value = defaultValue;
            } catch (Exception e) {
                e.printStackTrace();
                value = defaultValue;
            }
            return System.getProperty(key, value);
        }

        private static final String BUNDLE_NAME = "env";

        private String keyName;
        private String defaultValue;

        Env(String keyName, String defaultValue) {
            this.keyName = keyName;
            this.defaultValue = defaultValue;
        }
    }

    public enum LineMode {
        Offline(false),
        Online(true);

        public boolean isConnectable() {
            return isConnectable;
        }

        boolean isConnectable;

        LineMode(boolean isConnectable) {
            this.isConnectable = isConnectable;
        }
    }

    public static int getSocketPort() {
        return Integer.parseInt(Env.AOZORA_SOCKET_PORT.getString());
    }

    public static int getSocketTimeout() {
        return Integer.parseInt(Env.AOZORA_SOCKET_TIMEOUT.getString());
    }

    public static URL getIconURL() {
        try {
            return new URL(Env.AOZORA_ICON_URL.getString());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static URL getCommentDataURL() {
        try {
            return new URL(getDataURL(), "./comment/");
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static URL getCommentCgiURL() {
        try {
            return new URL(getDataURL(), "../cgi/comment.cgi");
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static URL getIndexURL() {
        try {
            return new URL(getDataURL(), "./index.txt");
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static URL getBackupURL() {
        try {
            return new URL(getDataURL(), "./backup/");
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static URL getRankingDataURL() {
        try {
            return new URL(getDataURL(), "./ranking/");
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static URL getRankingCgiURL() {
        try {
            return new URL(getDataURL(), "../cgi/ranking.cgi");
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static URL getAuthorListURL() {
        try {
            return new URL(getDataURL(), "./authors.xml");
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static URL getWorkBaseURL() {
        try {
            return new URL(getDataURL(), "./works/");
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static URL getWorkURL(String authorID) {
        try {
            return new URL(getWorkBaseURL(), "./" + authorID + ".xml");
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static URL getShortcutURL(String authorID, String workID, String title) {
        try {
            StringBuilder param = new StringBuilder();
            if (authorID != null) {
                param.append(param.length() != 0 ? '&' : '?');
                param.append("author=").append(authorID);
            }
            if (workID != null) {
                param.append(param.length() != 0 ? '&' : '?');
                param.append("card=").append(workID);
            }
            if (title != null) {
                param.append(param.length() != 0 ? '&' : '?');
                try {
                    title = URLEncoder.encode(title, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalStateException(e);
                }
                param.append("title=").append(title);
            }
            return new URL(getDataURL(), "../cgi/aozora.jnlp" + param);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static URL getPremierNewURL() {
        try {
            return new URL(getDataURL(), "../cgi/premier_new.cgi");
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static URL getPremierPayURL(String id) {
        try {
            return new URL(getDataURL(), "../cgi/premier_pay.cgi?id=" + id);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static URL getPremierCheckURL(String id) {
        try {
            return new URL(getDataURL(), "../cgi/premier_check.cgi?id=" + id);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static URL getDataURL() throws MalformedURLException {
        return new URL(Env.AOZORA_DATA_URL.getString());
    }

    public static File getUserHomeDir() {
        String userHome = System.getProperty("user.home");
        return new File(userHome, ".soso");
    }

    public static URL getAozoraBunkoURL() {
        try {
            return new URL(Env.AOZORA_BUNKO_SITE_URL.getString());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static final int URL_READ_TIMEOUT_MILLI = 30000;
    public static final int URL_CONNECT_TIMEOUT_MILLI = 30000;
    public static final String AUTHOR_CACHE_NAME = "AozoraAuthor";
    public static final String WORK_CACHE_NAME = "AozoraWork";
    public static final Color HEADER_COLOR = new Color(165, 42, 42);
    public static final Color DEFAULT_FOREGROUND_COLOR = Color.BLACK;
    public static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    public static final Color COMMENT_BALLONE_BACKGROUND_COLOR = new Color(0xffff99);
    public static final Color CACHED_COLOR = new Color(153, 0, 153);
    public static final int DEFAULT_RAW_SPACE = 10;
    public static final float DEFAULT_FONT_RATIO = 0.9F;
}
