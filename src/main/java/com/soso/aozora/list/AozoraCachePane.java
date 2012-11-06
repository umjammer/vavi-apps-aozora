/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraDefaultPane;
import com.soso.aozora.data.AozoraAuthor;
import com.soso.aozora.data.AozoraAuthorParser;
import com.soso.aozora.data.AozoraCacheManager;
import com.soso.aozora.data.AozoraWork;
import com.soso.aozora.data.AozoraWorkParser;
import com.soso.aozora.event.AozoraListenerAdapter;


public class AozoraCachePane extends AozoraDefaultPane {

    public AozoraCachePane(AozoraContext context) {
        super(context);
        initGUI();
        initData();
    }

    private void initGUI() {
        setLayout(new BorderLayout());
        viewPane = new JPanel();
        viewPane.setLayout(new BorderLayout());
        viewPane.add(getListPane(), BorderLayout.NORTH);
        javax.swing.JComponent filler = new JPanel();
        viewPane.add(filler, BorderLayout.CENTER);
        scrollPane = new JScrollPane();
        scrollPane.setViewportView(viewPane);
        scrollPane.getVerticalScrollBar().setUnitIncrement(50);
        add(scrollPane, BorderLayout.CENTER);
        JButton deleteButton = new JButton(new AbstractAction("全てのキャッシュを削除") {
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showInternalConfirmDialog(getAzContext().getDesktopPane(),
                                                          "全てのキャッシュを削除します。よろしいですか？",
                                                          "全てのキャッシュを削除",
                                                          JOptionPane.OK_CANCEL_OPTION,
                                                          JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION)
                    removeAllCaches();
            }
        });
        add(deleteButton, BorderLayout.NORTH);
        AozoraCacheManager cacheManager = getAzContext().getCacheManager();
        if (cacheManager == null)
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showInternalMessageDialog(getAzContext().getDesktopPane(),
                                                          "キャッシュマネージャが見つかりません。",
                                                          "キャッシュマネージャ",
                                                          JOptionPane.ERROR_MESSAGE);
                }
            });
        else if (cacheManager.isReadOnly())
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showInternalMessageDialog(getAzContext().getDesktopPane(),
                                                          "キャッシュは読み込み専用です。\n" +
                                                          "Aozora Viewer が他に起動していないか、ご確認ください。",
                                                          "キャッシュマネージャ",
                                                          JOptionPane.WARNING_MESSAGE);
                }
            });
    }

    private void initData() {
        AozoraCacheManager cacheManager = getAzContext().getCacheManager();
        if (cacheManager == null)
            throw new IllegalStateException("キャッシュマネージャが見つかりません。");
        for (String cacheID : cacheManager.getIDs()) {
            addCachePane(cacheID);
        }

        getAzContext().getListenerManager().add(new AozoraListenerAdapter() {
            public void cacheUpdated(String cacheID) {
                removeCachePane(cacheID);
                addCachePane(cacheID);
            }

            public void cacheDeleted(String cacheID) {
                removeCachePane(cacheID);
            }
        });
    }

    AozoraCacheListPane getListPane() {
        if (listPane == null)
            listPane = new AozoraCacheListPane(getAzContext());
        return listPane;
    }

    public AozoraAuthor getAozoraAuthor(String authorID) {
        return getListPane().getAozoraAuthor(authorID);
    }

    public AozoraWork getAozoraWork(String workID) {
        return getListPane().getAozoraWork(workID);
    }

    public void focusWork(AozoraWork work) {
        getListPane().focusWork(work);
    }

    public void focusSelectedCache() {
        requestFocusInWindow();
        getListPane().focusSelectedCache();
    }

    private void addCachePane(String cacheID) {
        AozoraCacheManager cacheManager;
        cacheManager = getAzContext().getCacheManager();
        if (cacheManager == null)
            throw new IllegalStateException("キャッシュマネージャが見つかりません。");
        if (cacheID.startsWith("card")) {
            try {
                byte[] authorBytes = cacheManager.getCacheBytes(cacheID, "AozoraAuthor");
                if (authorBytes == null) {
                    getListPane().addBrokenCachePane(cacheID);
                    return;
                }
                AozoraAuthor author = AozoraAuthorParser.loadBytes(authorBytes);
                byte[] workBytes = cacheManager.getCacheBytes(cacheID, "AozoraWork");
                if (workBytes == null) {
                    getListPane().addBrokenCachePane(cacheID);
                    return;
                }
                AozoraWork work = AozoraWorkParser.loadBytes(workBytes);
                getListPane().addCachePane(author, work);
            } catch (Exception e) {
                log(e);
                log("キャッシュID " + cacheID + " のロードでエラーが発生しました。");
                getListPane().addBrokenCachePane(cacheID);
            }
        }
    }

    private void removeCachePane(String cacheID) {
        getListPane().removeCachePane(cacheID);
    }

    private void removeAllCaches() {
        if (SwingUtilities.isEventDispatchThread()) {
            new Thread(new Runnable() {
                public void run() {
                    removeAllCaches();
                }
            }, "AozoraCachePane_removeAllCaches").start();
            return;
        }
        AozoraCacheManager cacheManager = getAzContext().getCacheManager();
        if (cacheManager == null)
            throw new IllegalStateException("キャッシュマネージャが見つかりません。");
        if (cacheManager.isReadOnly())
            throw new IllegalStateException("キャッシュマネージャは読み込み専用です。");
        boolean isError = false;
        for (String cacheID : cacheManager.getIDs()) {
            try {
                cacheManager.removeCache(cacheID);
            } catch (Exception e) {
                isError = true;
                getAzContext().log(e);
                getAzContext().log(cacheID + " のキャッシュの削除に失敗しましたが処理を続行します。");
            }
        }

        if (isError)
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showInternalMessageDialog(getAzContext().getDesktopPane(),
                                                          "全てのキャッシュを削除しようとしましたが、\n" +
                                                          "いくつかのキャッシュの削除に失敗しました。",
                                                          "全てのキャッシュを削除",
                                                          JOptionPane.ERROR_MESSAGE);
                }
            });
        else
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showInternalMessageDialog(getAzContext().getDesktopPane(),
                                                          "全てのキャッシュを削除しました。",
                                                          "全てのキャッシュを削除",
                                                          JOptionPane.INFORMATION_MESSAGE);
                }
            });
    }

    private JScrollPane scrollPane;
    private JPanel viewPane;
    private AozoraCacheListPane listPane;
}
