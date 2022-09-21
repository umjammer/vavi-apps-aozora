/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.LookAndFeel;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;


public class SGUIUtil {

    private SGUIUtil() {
    }

    public static Rectangle getBoundsRecursive(Component comp0, Component comp1) {
        Rectangle bounds0 = new Rectangle(0, 0, comp1.getWidth(), comp1.getHeight());
        Rectangle bounds1 = null;
        Component obj = comp1;
        while (obj != null) {
            bounds1 = obj.getBounds(bounds1);
            bounds0.x += bounds1.x;
            bounds0.y += bounds1.y;
            if (obj.getParent() == comp0)
                return bounds0;
            obj = obj.getParent();
        }
        return bounds0;
    }

    public static Container getParentRecursive(Component comp) {
        Container container;
        while ((container = comp.getParent()) != null) {
            Container parent = container.getParent();
            if (parent == null) {
                return container;
            }
            container = parent;
            comp = container;
        }
        return null;
    }

    public static Frame getParentFrame(Component comp) {
        return getParentInstanceOf(comp, Frame.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getParentInstanceOf(Component comp, Class<T> clazz) {
        while (comp != null) {
            if (clazz.isInstance(comp))
                return (T) comp;
            comp = comp.getParent();
        }
        return null;
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T[] getChildInstanceOf(Container container, Class<T> clazz) {
        try {
            List<Object> children = new ArrayList<Object>();
            setChildInstanceOf(container, clazz, children);
            return children.toArray((T[]) Array.newInstance(clazz, children.size()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static void setChildInstanceOf(Container container, Class<?> clazz, List<Object> children) {
        for (Component component : container.getComponents()) {
            if (component instanceof Container)
                setChildInstanceOf((Container) component, clazz, children);
            if (clazz.isInstance(component))
                children.add(component);
        }
    }

    public static void setCenter(Component comp0, Component comp1) {
        Dimension size0 = comp0.getSize();
        Dimension size1 = comp1.getSize();
        Point point = getCenter(size0, size1);
        comp1.setBounds(Math.max(point.x, 0), Math.max(point.y, 0), size1.width, size1.height);
    }

    public static Point getCenter(Dimension size0, Dimension size1) {
        int x = (size0.width - size1.width) / 2;
        int y = (size0.height - size1.height) / 2;
        return new Point(x, y);
    }

    public static void setSizeALL(Component comp, Dimension size) {
        comp.setSize(size);
        comp.setPreferredSize(size);
        comp.setMaximumSize(size);
        comp.setMinimumSize(size);
    }

    public static void setAllTextSelected(JTextComponent comp) {
        comp.requestFocusInWindow();
        comp.setSelectionStart(0);
        comp.setSelectionEnd(comp.getText().length());
    }

    public static boolean setLookAndFeel(LookAndFeel laf, Component comp) {
        if (laf.isSupportedLookAndFeel()) {
            try {
                UIManager.setLookAndFeel(laf);
                SwingUtilities.updateComponentTreeUI(comp);
            } catch (UnsupportedLookAndFeelException _ex) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public static LookAndFeel[] selectSupportedLookAndFeel(String[] names) {
        List<LookAndFeel> lookAndFeels = new ArrayList<>();
        for (String name : names) {
            try {
                Class<?> clazz = Class.forName(name, true, Thread.currentThread().getContextClassLoader());
                LookAndFeel lookAndFeel = (LookAndFeel) clazz.newInstance();
                if (lookAndFeel.isSupportedLookAndFeel())
                    lookAndFeels.add(lookAndFeel);
                else
                    throw new UnsupportedLookAndFeelException(lookAndFeel.getName());
            } catch (Exception e) {
                System.err.println("[WARN]" + name + " is skipped. Caused by " + e);
            }
        }

        return lookAndFeels.toArray(new LookAndFeel[0]);
    }

    public static void setTextPanePreferredWidth(JTextComponent comp, int width) {
        if (comp instanceof SVerticalTextPane)
            throw new UnsupportedOperationException("Unsupported text component :" + comp.getClass());
        comp.setSize(new Dimension(width, comp.getHeight()));
        try {
            Rectangle rectangle = comp.modelToView(comp.getText().length());
            if (rectangle != null) {
                int height = rectangle.y + rectangle.height;
                Dimension dimension = new Dimension(width, height);
                comp.setSize(dimension);
                comp.setPreferredSize(dimension);
            }
        } catch (BadLocationException e) {
            throw new IllegalStateException("It has been occured while this cannot happen in spite.", e);
        }
    }

    @Deprecated
    static void startLWModal(Container container) {
        try {
            invoke(container, Container.class, "startLWModal", new Class[0], new Object[0]);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    @Deprecated
    static void stopLWModal(Container container) {
        try {
            invoke(container, Container.class, "stopLWModal", new Class[0], new Object[0]);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    @Deprecated
    static void forceHeavyWeightPopupKey(JComponent comp, boolean value) {
        Object key = get(PopupFactory.class, PopupFactory.class, "forceHeavyWeightPopupKey");
        comp.putClientProperty(key, value);
    }

    private static Object invoke(Object obj, final Class<?> clazz, final String name, final Class<?>[] argTypes, Object[] args) throws InvocationTargetException {
        Object method = AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            Method method1;
            try {
                method1 = clazz.getDeclaredMethod(name, argTypes);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
            method1.setAccessible(true);
            return method1;
        });
        if (method == null)
            throw new IllegalStateException("cannot find method");
        else
            try {
                return ((Method) (method)).invoke(obj, args);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
    }

    private static Object get(Object obj, final Class<?> clazz, final String name) {
        Field field = (Field) AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            Field field1;
            try {
                field1 = clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException(e);
            }
            field1.setAccessible(true);
            return field1;
        });
        try {
            if (field == null)
                throw new IllegalStateException("cannot find field");
            else
                return field.get(obj);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void setFont(JComponent comp, Font font) {
        comp.setFont(font);
        for (Component component : comp.getComponents()) {
            setFont(component, font);
        }
    }

    private static void setFont(Container container, Font font) {
        container.setFont(font);
        for (Component component : container.getComponents()) {
            setFont(component, font);
        }
    }

    private static void setFont(Component component, Font font) {
        if (component instanceof JComponent) {
            setFont((JComponent) component, font);
        } else if (component instanceof Container) {
            setFont((Container) component, font);
        } else {
            component.setFont(font);
        }
    }

    public static void setFontAll(Component component, Font font) {
        setFont(component, font);
    }

    public static void updateFontAll(Font font) {
        updateFont(font);
        String[] fonts = SUIModel.Font.getAll();
        updateFont(fonts, font);
    }

    public static void updateFont(String[] keys, Font font) {
        FontUIResource resource = new FontUIResource(font);
        for (String key : keys) {
            UIManager.put(key, resource);
        }
    }

    public static void updateFont(Font font) {
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        Enumeration<?> e = defaults.keys();
        FontUIResource resource = new FontUIResource(font);
        while (e.hasMoreElements()) {
            Object key = e.nextElement();
            Object value = defaults.get(key);
            if (value instanceof Font)
                defaults.put(key, resource);
        }
    }

    public static void ensureSelectedIsVisible(final JList list) {
        SwingUtilities.invokeLater(() -> {
            int selectedIndex = list.getSelectedIndex();
            int rowCount = list.getVisibleRowCount();
            Rectangle aRect = list.getCellBounds(Math.max(selectedIndex - rowCount / 2, 0),
                                                 Math.min(selectedIndex + rowCount / 2, list.getModel().getSize() - 1));
            if (aRect != null)
                list.scrollRectToVisible(aRect);
            list.ensureIndexIsVisible(selectedIndex);
        });
    }

    public static int negativeRGB(int rgb) {
        int j = rgb & 0xff000000;
        int l = ~rgb & 0xffffff;
        return j | l;
    }

    public static int compromiseRGB(int foreground, int background, float freshnessRatio) {
        int fr = foreground >> 16 & 0xff;
        int fg = foreground >> 8 & 0xff;
        int gb = foreground >> 0 & 0xff;
        int fa = foreground >> 24 & 0xff;
        int br = background >> 16 & 0xff;
        int bg = background >> 8 & 0xff;
        int bb = background >> 0 & 0xff;
        int ba = background >> 24 & 0xff;
        int r = compromise(fr, br, freshnessRatio);
        int g = compromise(fg, bg, freshnessRatio);
        int b = compromise(gb, bb, freshnessRatio);
        int a = compromise(fa, ba, freshnessRatio);
        return (a & 0xff) << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff) << 0;
    }

    public static int compromise(int foreground, int background, float freshnessRatio) {
        int r = (int) (foreground * (1.0F - freshnessRatio) + background * freshnessRatio);
        if (foreground > background) {
            r = Math.min(Math.max(background, r), foreground);
        } else {
            r = Math.min(Math.max(foreground, r), background);
        }
        return r;
    }
}
