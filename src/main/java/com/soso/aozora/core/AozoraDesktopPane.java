/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.core;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.boot.AozoraSplashWindow;
import com.soso.aozora.data.AozoraAuthor;
import com.soso.aozora.data.AozoraAuthorParserHandler;
import com.soso.aozora.data.AozoraBookmarks;
import com.soso.aozora.data.AozoraCacheManager;
import com.soso.aozora.data.AozoraComment;
import com.soso.aozora.data.AozoraWork;
import com.soso.aozora.data.AozoraWorkParserHandler;
import com.soso.aozora.html.TagReader;
import com.soso.aozora.list.AozoraCachePane;
import com.soso.aozora.list.AozoraListPane;
import com.soso.aozora.viewer.AozoraViewerPane;
import com.soso.sgui.SDesktopPane;
import com.soso.sgui.SFontChooser;
import com.soso.sgui.SGUIUtil;
import com.soso.sgui.SInternalFrame;
import com.soso.sgui.SLookAndFeelChooser;
import com.soso.sgui.SOptionPane;
import com.soso.sgui.letter.SLetterPane;


public class AozoraDesktopPane extends SDesktopPane implements AozoraRootMediator {

    private static class BookmarkWorkMenu extends JMenu {

        private AozoraContext getAzContext() {
            return context;
        }

        private String getWorkID() {
            return workID;
        }

        private void setWork(AozoraWork work) {
            this.work = work;
        }

        private AozoraWork getWork() {
            return work;
        }

        private void showWork() {
            getAzContext().getRootMediator().focusWork(getWork());
            getAzContext().getRootMediator().getAozoraAuthorAsynchronous(getWork().getAuthorID(), new AozoraAuthorParserHandler() {
                public void author(AozoraAuthor author) {
                    getAzContext().getRootMediator().showViewer(author, getWork());
                }
            });
        }

        private void initGUI() {
            AozoraWorkParserHandler handler = new AozoraWorkParserHandler() {
                public void work(AozoraWork work) {
                    setWork(work);
                    if (work != null) {
                        setText(work.getTitleName());
                        addMouseListener(new MouseAdapter() {
                            public void mouseClicked(MouseEvent e) {
                                showWork();
                                ((JPopupMenu) getParent()).menuSelectionChanged(false);
                            }
                        });
                        JMenuItem openItem = new JMenuItem("しおりを開く");
                        openItem.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                showWork();
                            }
                        });
                        add(openItem, 0);
                        AozoraCacheManager cacheManager = getAzContext().getCacheManager();
                        if (cacheManager != null)
                            try {
                                if (cacheManager.isCached(work.getID()))
                                    setForeground(AozoraEnv.CACHED_COLOR);
                            } catch (Exception e) {
                                getAzContext().log(e);
                            }
                    } else {
                        setText("見つかりません");
                        setToolTipText("作品ID[" + getWorkID() + "]");
                    }
                }
            };
            AozoraWork work = getAzContext().getRootMediator().getAozoraWork(getWorkID());
            if (work != null)
                handler.work(work);
            else
                getAzContext().getRootMediator().getAozoraWorkAsynchronous(getWorkID(), handler);
            JMenuItem deleteItem = new JMenuItem("しおりを削除");
            deleteItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (SOptionPane.showSInternalConfirmDialog(getAzContext().getDesktopPane(),
                                                               (getWork() == null ? "作品ID[" + getWorkID() + "]"
                                                                                  : "[" + getWork().getTitleName() + "]") +
                                                                                  "のしおりを削除してよろしいですか？",
                                                               "削除の確認", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        getAzContext().log("delete boolmark ID:" + getWorkID() + "; work:" + getWork());
                        getAzContext().getBookmarks().removeBookmark(getWorkID());
                        try {
                            getAzContext().getBookmarks().store();
                        } catch (Exception e1) {
                            e1.printStackTrace(System.err);
                        }
                    }
                }
            });
            add(deleteItem);
        }

        private AozoraContext context;
        private String workID;
        private transient AozoraWork work;

        private BookmarkWorkMenu(AozoraContext context, String workID) {
            super("ロード中...");
            this.context = context;
            this.workID = workID;
            initGUI();
        }
    }

    public AozoraDesktopPane(AozoraContext context) {
        this.context = context;
        initGUI();
        AozoraSplashWindow.setProgress(AozoraSplashWindow.PROGRESS.GUI_DESKTOP);
    }

    private void initGUI() {
        lastViewPoint = new Point(INIT_X, INIT_Y);
        setBackground(new Color(0xfefeff));
        JPanel graphics2DCheckerPane = new JPanel() {
            public void paint(Graphics g) {
                boolean is2D = g instanceof Graphics2D;
                if (!is2D)
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            JOptionPane.showInternalConfirmDialog(null, new String[] {
                                "この Java VM は、Graphics2D をサポートしていないので、",
                                "一部表示が乱れる箇所があります。"
                            }, "Graphics2D 未サポート", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                        }
                    });
                getAzContext().log("System | Java VM Name : " + System.getProperty("java.vm.name"));
                getAzContext().log("System | Java Version : " + System.getProperty("java.version"));
                getAzContext().log("System | Java Vendor  : " + System.getProperty("java.vendor"));
                getAzContext().log("System | Graphics " + g.getClass());
                getAzContext().log("System | " + (is2D ? "Graphics2D found." : " !! NO Graphics2D FOUND !!"));
                getParent().remove(this);
            }
        };
        graphics2DCheckerPane.setBounds(0, 0, 1, 1);
        add(graphics2DCheckerPane);
    }

    public void showViewer(AozoraAuthor author, AozoraWork work) {
        showViewer(author, work, 0);
    }

    public void showViewer(AozoraAuthor author, AozoraWork work, int position) {
        showViewer(author, work, position, null);
    }

    public void showViewer(AozoraAuthor author, AozoraWork work, AozoraComment comment) {
        showViewer(author, work, comment.getPosition(), comment);
    }

    private AozoraViewerPane showViewer(AozoraAuthor author, AozoraWork work, int position, AozoraComment comment) {
        boolean isCached = false;
        try {
            AozoraCacheManager cacheManager = getAzContext().getCacheManager();
            if (cacheManager != null)
                isCached = cacheManager.isCached(work.getID());
        } catch (Exception e) {
            log(e);
        }
        if (!getAzContext().getLineMode().isConnectable() && !isCached) {
            JOptionPane.showInternalMessageDialog(this, "作品「" + work.getTitleName() + "」のキャッシュが見つかりません。");
            return null;
        }
        for (Component comp : getComponents()) {
            if (comp instanceof SInternalFrame) {
                SInternalFrame iframe = (SInternalFrame) comp;
                if (iframe.getContentPane() instanceof AozoraViewerPane) {
                    AozoraViewerPane viewerPane = (AozoraViewerPane) iframe.getContentPane();
                    if (viewerPane.getWork().equals(work) && viewerPane.getAuthor().equals(author)) {
                        iframe.toFront();
                        viewerPane.focus();
                        viewerPane.setStartPosition(position);
                        if (comment != null)
                            viewerPane.setStartPositionByComment(comment);
                        return viewerPane;
                    }
                }
            }
        }

        final SInternalFrame iframe = new SInternalFrame();
        iframe.setClosable(true);
        iframe.setIconifiable(true);
        iframe.setMaximizable(true);
        iframe.setResizable(true);
        iframe.setDefaultCloseOperation(SInternalFrame.DISPOSE_ON_CLOSE);
        final AozoraViewerPane viewerPane = new AozoraViewerPane(getAzContext(), author, work, isCached, position);
        iframe.setContentPane(viewerPane);
        iframe.setBounds(nextFrameBounds(720, 600));
        iframe.setTitle(viewerPane.getTitle());
        AozoraUtil.putKeyStrokeAction(iframe, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, AozoraEnv.ShortCutKey.VIEWER_CLOSE_SHORTCUT.getKeyStroke(), "AozoraViewerPane.closeAction", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                iframe.dispose();
            }
        });
        iframe.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosed(InternalFrameEvent e) {
                viewerPane.close();
                showNextViewer();
            }
        });
        setLayer(iframe, 50);
        add(iframe);
        iframe.setVisible(true);
        viewerPane.focus();
        if (comment != null)
            viewerPane.setStartPositionByComment(comment);
        postRanking(author, work);
        return viewerPane;
    }

    private void showNextViewer() {
        JInternalFrame nextFrame = null;
        for (Component comp : getComponents()) {
            if (comp instanceof JInternalFrame) {
                JInternalFrame f = (JInternalFrame) comp;
                if (getLayer(f) == 50 && f.isVisible() && !f.isIcon()) {
                    if (nextFrame == null) {
                        nextFrame = f;
                        continue;
                    }
                    if (getPosition(nextFrame) > getPosition(f))
                        nextFrame = f;
                }
            }
        }

        if (nextFrame != null) {
            nextFrame.requestFocusInWindow();
            for (AozoraViewerPane nextViewerPane : SGUIUtil.getChildInstanceOf(nextFrame, AozoraViewerPane.class)) {
                nextViewerPane.focus();
            }
        } else {
            AozoraContentPane contentPane = getContentPane();
            if (contentPane != null)
                contentPane.focusSelectedPane();
        }
    }

    void postRanking(AozoraAuthor author, AozoraWork work) {
        if (!getAzContext().getLineMode().isConnectable()) {
            log("オフラインモードではランキング投票しません。");
            return;
        }
        log("ランキング投票:" + author.getID() + "," + work.getID());
        try {
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) AozoraUtil.getURLConnection(AozoraEnv.getRankingCgiURL());
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                con.setDoInput(true);
                con.connect();
                OutputStream out = null;
                try {
                    out = con.getOutputStream();
                    String post = "author=" + author.getID() + "&card=" + work.getID();
                    out.write(post.getBytes("UTF-8"));
                    out.flush();
                } finally {
                    if (out != null)
                        out.close();
                }
                int responseCode = con.getResponseCode();
                if (responseCode != 200)
                    throw new IllegalStateException("ResponseCode:" + responseCode);
                InputStream in = null;
                try {
                    in = con.getInputStream();
                    TagReader tin = new TagReader(new InputStreamReader(in, "UTF-8"));
                    try {
                        tin.skipToStartTag("error");
                        int error = Integer.parseInt(tin.readToEndTag());
                        tin.skipToStartTag("message");
                        String mssg = tin.readToEndTag();
                        mssg = "ランキング投票:" + mssg + "(" + error + ")";
                        if (error != 0)
                            throw new IllegalStateException(mssg);
                        log(mssg);
                    } finally {
                        tin.close();
                    }
                } finally {
                    if (in != null)
                        in.close();
                }
            } finally {
                if (con != null)
                    con.disconnect();
            }
        } catch (Exception e) {
            log(e);
        }
    }

    AozoraContext getAzContext() {
        return context;
    }

    void log(Object obj) {
        getAzContext().log(obj, getClass());
    }

    void log(Throwable t) {
        getAzContext().log(t);
    }

    private Rectangle nextFrameBounds(int width, int height) {
        int nextX = lastViewPoint.x + GRID_X;
        int nextY = lastViewPoint.y + GRID_Y;
        if (nextX + width > getWidth())
            nextX = INIT_X;
        if (nextY + height > getHeight())
            nextY = INIT_Y;
        lastViewPoint.setLocation(nextX, nextY);
        return new Rectangle(lastViewPoint.x, lastViewPoint.y, width, height);
    }

    public void showLookAndFeelChooser() {
        LookAndFeel oldLookAndFeel = UIManager.getLookAndFeel();
        try {
            SLookAndFeelChooser.showInternalDialog(this, "デザインの変更", new String[] {
                    "de.javasoft.plaf.synthetica.SyntheticaSilverMoonLookAndFeel",
                    "de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel",
                    "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel",
                    "com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
                    "com.apple.mrj.swing.MacLookAndFeel",
                    "com.sun.java.swing.plaf.mac.MacLookAndFeel",
                    "apple.laf.AquaLookAndFeel",
                    "javax.swing.plaf.metal.MetalLookAndFeel"
                }, null);
            getAzContext().getSettings().setLookAndFeel(UIManager.getLookAndFeel());
        } catch (Exception e) {
            log("[ERROR] An error occured while show SLookAndFeelChooser \n" + e);
            try {
                SGUIUtil.setLookAndFeel(oldLookAndFeel, SGUIUtil.getParentFrame(this));
            } catch (Exception e2) {
                e2.printStackTrace(System.err);
            }
        }
    }

    public void showFontChooser() {
        Font current = getAzContext().getSettings().getFont();
        if (current != null)
            ;
        log(current);
        Font nFont = SFontChooser.showInternalFrame(this, "フォントの変更", current);
        if (current != nFont) {
            getAzContext().getSettings().setFont(nFont);
            getAzContext().getRootMediator().setViewerFont(nFont);
        }
    }

    public void setViewerFont(Font font) {
        for (SLetterPane textPane : SGUIUtil.getChildInstanceOf(SGUIUtil.getParentRecursive(getRootPane()), SLetterPane.class)) {
            textPane.setFont(font);
        }
    }

    public void showColorChooser() {
        AozoraColorChooserPane.showDialog(getAzContext());
    }

    public void setViewerForeground(Color color) {
        for (SLetterPane textPane : SGUIUtil.getChildInstanceOf(SGUIUtil.getParentRecursive(getRootPane()), SLetterPane.class)) {
            textPane.setForeground(color);
        }
    }

    public void setViewerBackground(Color color) {
        for (SLetterPane textPane : SGUIUtil.getChildInstanceOf(SGUIUtil.getParentRecursive(getRootPane()), SLetterPane.class)) {
            textPane.setBackground(color);
        }
    }

    public void showBookmarkList(int x, int y) {
        JPopupMenu popup = new JPopupMenu("しおり一覧");
        AozoraBookmarks.AozoraBookmarkEntry[] bookmarks = getAzContext().getBookmarks().toArray();
        if (bookmarks == null || bookmarks.length == 0) {
            popup.add(new JLabel("しおりはありません"));
        } else {
            for (AozoraBookmarks.AozoraBookmarkEntry bookmark : bookmarks) {
                popup.add(new BookmarkWorkMenu(getAzContext(), bookmark.getBook()));
            }
        }
        popup.show(this, x, y);
    }

    private AozoraContentPane getContentPane() {
        JRootPane rootPane = getRootPane();
        if (rootPane != null) {
            Container contentPane = rootPane.getContentPane();
            if (contentPane != null && contentPane instanceof AozoraContentPane)
                return (AozoraContentPane) contentPane;
        }
        return null;
    }

    private AozoraListPane getListPane() {
        AozoraContentPane contentPane = getContentPane();
        if (contentPane != null)
            return contentPane.getAozoraListPane();
        else
            return null;
    }

    private AozoraCachePane getCachePane() {
        AozoraContentPane contentPane = getContentPane();
        if (contentPane != null)
            return contentPane.getAozoraCachePane();
        else
            return null;
    }

    public AozoraAuthor getAozoraAuthor(String authorID) {
        AozoraAuthor author = null;
        AozoraListPane listPane = getListPane();
        if (listPane != null)
            author = listPane.getAozoraAuthor(authorID);
        if (author == null) {
            AozoraCachePane cachePane = getCachePane();
            if (cachePane != null)
                return cachePane.getAozoraAuthor(authorID);
        }
        return author;
    }

    public void getAozoraAuthorAsynchronous(String authorID, AozoraAuthorParserHandler callback) {
        AozoraAuthor author = getAozoraAuthor(authorID);
        if (author != null || !getAzContext().getLineMode().isConnectable()) {
            callback.author(author);
            return;
        }
        AozoraListPane listPane = getListPane();
        if (listPane != null)
            listPane.getAozoraAuthorAsynchronous(authorID, callback);
    }

    public AozoraWork getAozoraWork(String workID) {
        return getAozoraWork(workID, true);
    }

    public AozoraWork getAozoraWork(String workID, boolean loadImmediate) {
        AozoraWork work = null;
        AozoraListPane listPane = getListPane();
        if (listPane != null)
            work = listPane.getAozoraWork(workID, loadImmediate);
        if (work == null) {
            AozoraCachePane cachePane = getCachePane();
            if (cachePane != null)
                return cachePane.getAozoraWork(workID);
        }
        return work;
    }

    public void getAozoraWorkAsynchronous(String workID, AozoraWorkParserHandler callback) {
        AozoraWork work = getAozoraWork(workID, false);
        if (work != null || !getAzContext().getLineMode().isConnectable()) {
            callback.work(work);
            return;
        }
        AozoraListPane listPane = getListPane();
        if (listPane != null)
            listPane.getAozoraWorkAsynchronous(workID, callback);
    }

    public void focusAuthor(AozoraAuthor author) {
        if (!getAzContext().getLineMode().isConnectable())
            return;
        AozoraListPane listPane = getListPane();
        if (listPane != null) {
            Container listPaneParent = listPane.getParent();
            if (listPaneParent != null && (listPaneParent instanceof JTabbedPane))
                ((JTabbedPane) listPaneParent).setSelectedComponent(listPane);
            listPane.focusAuthor(author);
        }
    }

    public void focusWork(AozoraWork work) {
        if (!getAzContext().getLineMode().isConnectable()) {
            AozoraCachePane cachePane = getCachePane();
            if (cachePane != null)
                cachePane.focusWork(work);
            return;
        }
        AozoraListPane listPane = getListPane();
        if (listPane != null) {
            Container listPaneParent = listPane.getParent();
            if (listPaneParent != null && (listPaneParent instanceof JTabbedPane))
                ((JTabbedPane) listPaneParent).setSelectedComponent(listPane);
            listPane.focusWork(work);
        }
    }

    public void aboutAozora() {
        JInternalFrame iframe;
        if (aboutPane == null) {
            aboutPane = new AozoraAboutPane(getAzContext());
            iframe = new JInternalFrame();
            iframe.setTitle("ヘルプ");
            iframe.setClosable(true);
            iframe.setMaximizable(true);
            iframe.setIconifiable(false);
            iframe.setResizable(true);
            iframe.setContentPane(aboutPane);
            iframe.setDefaultCloseOperation(1);
            iframe.pack();
            setLayer(iframe, MODAL_LAYER.intValue());
            add(iframe);
            SGUIUtil.setCenter(this, iframe);
            final JInternalFrame theIFrame = iframe;
            AozoraUtil.putKeyStrokeAction(theIFrame, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, AozoraEnv.ShortCutKey.VIEWER_CLOSE_SHORTCUT.getKeyStroke(), "AozoraAboutPane.closeAction", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    theIFrame.setVisible(false);
                }
            });
        } else {
            iframe = SGUIUtil.getParentInstanceOf(aboutPane, JInternalFrame.class);
        }
        iframe.setVisible(true);
        iframe.requestFocusInWindow();
    }

    public void aboutPremier() {
        aboutAozora();
        aboutPane.showPremierTab();
    }

    public static final int LIST_COMMENT_LAYER = 20;
    public static final int VIEWER_LAYER = 50;
    public static final int VIEWER_COMMENT_LAYER = 70;
    private static final int INIT_X = 200;
    private static final int INIT_Y = 20;
    private static final int GRID_X = 24;
    private static final int GRID_Y = 20;
    private final AozoraContext context;
    private Point lastViewPoint;
    private AozoraAboutPane aboutPane;
}
