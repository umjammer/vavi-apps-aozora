/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.core;

import com.soso.aozora.boot.AozoraContext;

import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;


public class AozoraUtil {

    static Logger logger = Logger.getLogger(AozoraContext.class.getName());

    public static Icon getIcon(String url) {
        Icon icon = null;
        ByteArrayOutputStream baos;
        InputStream in = null;
        int nameIndex = url.lastIndexOf('/');
        if (nameIndex != -1) {
            String name = url.substring(nameIndex + 1);
            URL resURL = AozoraUtil.class.getClassLoader().getResource(name);
            if (resURL != null) {
                logger.finer("icon | " + name + " | " + resURL);
                icon = new ImageIcon(resURL);
            }
        }
        if (icon == null) {
            try {
                logger.info("icon | " + url);
                baos = new ByteArrayOutputStream();
                try {
                    in = getInputStream(new URL(url));
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) != -1)
                        baos.write(buf, 0, len);
                } finally {
                    if (in != null)
                        in.close();
                }
                icon = new ImageIcon(baos.toByteArray(), url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return icon;
    }

    public static InputStream getInputStream(URL url) throws IOException {
        return getURLConnection(url).getInputStream();
    }

    public static URLConnection getURLConnection(URL url) throws IOException {
        URLConnection con = url.openConnection();
        con.setReadTimeout(30000);
        con.setConnectTimeout(30000);
        return con;
    }

    public static void putKeyStrokeAction(JComponent comp, int situation, KeyStroke keyStroke, final AbstractButton button) {
        Object actionMapKey = button.getName();
        if (actionMapKey == null)
            actionMapKey = keyStroke.toString();
        putKeyStrokeAction(comp, situation, keyStroke, actionMapKey, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    button.doClick();
                }
            });
    }

    public static void putKeyStrokeAction(JComponent comp, int situation, KeyStroke keyStroke, Object actionMapKey, Action action) {
        comp.getInputMap(situation).put(keyStroke, actionMapKey);
        comp.getActionMap().put(actionMapKey, action);
    }
}
