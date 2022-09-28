/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.MatteBorder;


public class SLinkLabel extends JLabel {

    public SLinkLabel(URL url) {
        this(url, url.toExternalForm());
    }

    public SLinkLabel(URL url, String text) {
        this.url = url;
        setOpaque(false);
        setForeground(Color.BLUE);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setText(text);
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1) {
                    open();
                } else if (event.getButton() == MouseEvent.BUTTON3)
                    menu(event.getX(), event.getY());
            }

            public void mouseEntered(MouseEvent event) {
                setBorder(new MatteBorder(0, 0, 1, 0, getForeground()));
            }

            public void mouseExited(MouseEvent event) {
                setBorder(null);
            }
        });
    }

    public URL getURL() {
        return url;
    }

    protected void menu(int x, int y) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem openMenuItem = new JMenuItem("リンクを開く(O)");
        openMenuItem.setMnemonic('O');
        openMenuItem.addActionListener(event -> open());
        popupMenu.add(openMenuItem);
        JMenuItem copyMenuItem = new JMenuItem("リンクをコピー(C)");
        copyMenuItem.setMnemonic('C');
        copyMenuItem.addActionListener(event -> {
            StringSelection selection = new StringSelection(getURL().toExternalForm());
            getToolkit().getSystemClipboard().setContents(selection, selection);
        });
        popupMenu.add(copyMenuItem);
        popupMenu.show(this, x, y);
    }

    protected boolean open() {
        boolean opened = openByDesktopBrowse() ||
            openByJnlpBasicService() ||
            openByAppletContext() ||
            openByAppleEioFileManager() ||
            openByRun32FileProtocolHandler() ||
            openByRuntimeExecBrowsers();
        if (!opened)
            System.err.println("Error|fail to launch browser");
        return opened;
    }

    protected boolean openByDesktopBrowse() {
        try {
            Class<?> clazz = Class.forName("java.awt.Desktop");
            Method method = clazz.getMethod("getDesktop");
            Object desktop = method.invoke(clazz);
            Method browseMethod = clazz.getMethod("browse", URI.class);
            browseMethod.invoke(desktop, getURL().toURI());
            return true;
        } catch (Exception e) {
            System.err.println("Warning|fail to launch browser with java.awt.Desktop : " + e);
            return false;
        }
    }

    protected boolean openByJnlpBasicService() {
        try {
            Class<?> clazz = Class.forName("javax.jnlp.ServiceManager");
            Method method = clazz.getMethod("lookup", String.class);
            Object basicService = method.invoke(clazz, "javax.jnlp.BasicService");
            Class<?> class2 = Class.forName("javax.jnlp.BasicService");
            Method showDocumentMethod = class2.getMethod("showDocument", URL.class);
            Object result = (showDocumentMethod).invoke(basicService, getURL());
            if (result instanceof Boolean)
                return (Boolean) result;
            else
                return false;
        } catch (Exception e) {
            System.err.println("Warning|fail to launch browser with javax.jnlp.BasicService : " + e);
            return false;
        }
    }

    protected boolean openByAppletContext() {
        Applet applet = SGUIUtil.getParentInstanceOf(this, Applet.class);
        try {
            if (applet != null) {
                applet.getAppletContext().showDocument(getURL(), "_blank");
                return true;
            }
        } catch (Exception e) {
            System.err.println("Warning|fail to launch browser with java.applet.AppletContext : " + e);
        }
        return false;
    }

    protected boolean openByAppleEioFileManager() {
        try {
            if (System.getProperty("os.name").startsWith("Mac OS")) {
                Class<?> class1 = Class.forName("com.apple.eio.FileManager");
                Method method = class1.getDeclaredMethod("openURL", String.class);
                method.invoke(null, getURL().toExternalForm());
                return true;
            }
        } catch (Exception e) {
            System.err.println("Warning|fail to launch browser with com.apple.eio.FileManager : " + e);
        }
        return false;
    }

    protected boolean openByRun32FileProtocolHandler() {
        try {
            if (System.getProperty("os.name").startsWith("Windows")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + getURL().toExternalForm());
                return true;
            }
        } catch (Exception e) {
            System.err.println("Warning|fail to launch browser with rundll32 url.dll,FileProtocolHandler : " + e);
        }
        return false;
    }

    protected boolean openByRuntimeExecBrowsers() {
        String[] browsers = {
            "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"
        };
        try {
            for (String browser : browsers) {
                Process process = Runtime.getRuntime().exec(new String[] { "which", browser });
                if (process.waitFor() == 0) {
                    Runtime.getRuntime().exec(new String[] { browser, getURL().toExternalForm() });
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Warning|fail to launch browser with java.lang.Runtime : " + e);
        }
        return false;
    }

    private static final long serialVersionUID = 0x25c0bf17L;

    private final URL url;
}
