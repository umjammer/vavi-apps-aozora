/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.boot;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.core.AozoraUtil;
import com.soso.sgui.SGUIUtil;


public class AozoraSplashWindow extends JWindow {

    public enum PROGRESS {
        MIN,
        SOCKET_OPEN,
        SETTINGS,
        BOOKMARKS,
        HISTORIES,
        CACHE,
        CONTEXT,
        GUI_DESKTOP,
        GUI_MENU,
        GUI_LIST,
        GUI_TOPIC,
        GUI_RANKING,
        GUI_COMMENT,
        GUI_HISTORY,
        GUI_CACHE,
        GUI_CONTENT,
        SOCKET_ACCEPT,
        MAX;
    }

    static AozoraSplashWindow getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AozoraSplashWindow();
            INSTANCE.pack();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension splashSize = INSTANCE.getSize();
            Point centerPoint = SGUIUtil.getCenter(screenSize, splashSize);
            INSTANCE.setBounds(new Rectangle(centerPoint, splashSize));
            INSTANCE.initProgressBar();
            INSTANCE.pack();
            setProgress(PROGRESS.MIN);
        }
        return INSTANCE;
    }

    static void showSplash() {
        getInstance().setVisible(true);
    }

    static void hideSplash() {
        getInstance().setVisible(false);
    }

    static void disposeSplash() {
        setProgress(PROGRESS.MAX);
        new Thread(new Runnable() {

            public void run() {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            AozoraSplashWindow.getInstance().setVisible(false);
                            AozoraSplashWindow.getInstance().dispose();
                        }
                    });
                }
            }

        }, "AozoraSplashWindow_dispose").start();
    }

    public static void setProgress(PROGRESS progress) {
        getInstance().setProgressValue(progress.ordinal());
    }

    private AozoraSplashWindow() {
        disposed = false;
        initSplashLabel();
    }

    private void initSplashLabel() {
        Icon splashIcon = AozoraUtil.getIcon(AozoraEnv.Env.AOZORA_SPLASH_URL.getString());
        JLabel splashLabel = new JLabel(splashIcon);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(splashLabel, BorderLayout.CENTER);
    }

    private void initProgressBar() {
        progressBar = new JProgressBar();
        progressBar.setOrientation(0);
        progressBar.setMinimum(PROGRESS.MIN.ordinal());
        progressBar.setMaximum(PROGRESS.MAX.ordinal());
        getContentPane().add(progressBar, BorderLayout.SOUTH);
    }

    private void setProgressValue(int progress) {
        progressBar.setValue(progress);
    }

    public void setVisible(boolean visible) {
        if (!disposed)
            super.setVisible(visible);
        if (!visible)
            super.setVisible(false);
    }

    public void dispose() {
        super.dispose();
        disposed = true;
    }

    private static AozoraSplashWindow INSTANCE;
    boolean disposed;
    private JProgressBar progressBar;
}
