/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;


public class SFontChooser extends JPanel {

    public static class FontChooserPane extends JPanel {

        private void initGUI(Font font) {
            setLayout(new BorderLayout());
            add(getFontChooser(font), BorderLayout.CENTER);
            add(getButtonPanel(), BorderLayout.SOUTH);
        }

        private SFontChooser getFontChooser(Font font) {
            if (fontChooser == null)
                fontChooser = new SFontChooser(font);
            return fontChooser;
        }

        public void addTextSizeChangeListener(final Window window) {
            fontChooser.addPropertyChangeListener(SFontChooser.eventName, event -> window.pack());
        }

        public void addTextSizeChangeListener(final JInternalFrame iframe) {
            fontChooser.addPropertyChangeListener(SFontChooser.eventName, event -> iframe.pack());
        }

        final Font getSelectedFont() {
            return font;
        }

        final void setSelectedFont(Font font) {
            this.font = font;
        }

        private JPanel getButtonPanel() {
            if (buttonPanel == null) {
                buttonPanel = new JPanel();
                buttonPanel.add(getOkButton());
                buttonPanel.add(getCancelButton());
            }
            return buttonPanel;
        }

        private JButton getCancelButton() {
            if (cancelButton == null) {
                cancelButton = new JButton();
                cancelButton.setText("キャンセル");
                cancelButton.addActionListener(this::actionPerformed_a);
            }
            return cancelButton;
        }

        private JButton getOkButton() {
            if (okButton == null) {
                okButton = new JButton();
                okButton.setText("OK");
                okButton.addActionListener(event -> {
                    setSelectedFont(FontChooserPane.getSFontChooser(FontChooserPane.this).getSelectedFont());
                    fireFontChange();
                    actionPerformed_a(event);
                });
            }
            return okButton;
        }

        final void actionPerformed_a(ActionEvent event) {
            if (closeAction != null)
                closeAction.actionPerformed(event);
        }

        public void fireFontChange() {
            for (Component component : getFontListenerComponents()) {
                SGUIUtil.setFontAll(component, getSelectedFont());
            }
        }

        public boolean addFontListenerComponent(Component component) {
            return fontListenerComponents.add(component);
        }

        public List<Component> getFontListenerComponents() {
            return fontListenerComponents;
        }

        public boolean removeFontListenerComponent(Component component) {
            return fontListenerComponents.remove(component);
        }

        public void setCloseAction(ActionListener listener) {
            closeAction = listener;
        }

        static SFontChooser getSFontChooser(FontChooserPane pane) {
            return pane.fontChooser;
        }

        private JPanel buttonPanel;
        private JButton okButton;
        private JButton cancelButton;
        private SFontChooser fontChooser;
        private List<Component> fontListenerComponents;
        private ActionListener closeAction;
        private Font font;

        public FontChooserPane() {
            fontListenerComponents = new ArrayList<>();
            initGUI(getFont());
        }

        public FontChooserPane(Font font) {
            fontListenerComponents = new ArrayList<>();
            this.font = font;
            initGUI(font);
        }
    }

    final class N_FontModel implements SFontModel {

        private N_FontModel() {
            names = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        }

        public String[] getFontFamilyNames() {
            return names;
        }

        public int getFontFamilyNameIndex(String name) {
            for (int i = 0; i < names.length; i++)
                if (name.equals(names[i]))
                    return i;

            return -1;
        }

        public Integer[] getSizes() {
            return sizes;
        }

        public int getSizeIndex(int size) {
            for (int i = 0; i < sizes.length; i++)
                if (size == sizes[i])
                    return i;

            return -1;
        }

        public int getSizeRoundIndex(int size) {
            for (int i = 0; i < sizes.length; i++)
                if (size <= sizes[i])
                    return i;

            return -1;
        }

        public String[] getStyleNames() {
            return styleNames;
        }

        public int[] getStyleTypes() {
            return styleTypes;
        }

        public int styleNameToType(String name) {
            for (int i = 0; i < styleNames.length; i++)
                if (name.equals(styleNames[i]))
                    return getStyleTypes()[i];

            return -1;
        }

        public int getStyleTypeIndex(int type) {
            for (int i = 0; i < styleTypes.length; i++)
                if (type == styleTypes[i])
                    return i;

            return -1;
        }

        N_FontModel(O_ActionListener o) {
            this();
        }

        private Integer[] sizes = {
            6, 7, 8, 9, 10, 11, 12, 13,
            14, 16, 18, 20, 22, 24, 26, 28,
            32, 36, 40, 48, 56, 64, 72
        };

        private int[] styleTypes = {
            0, 1, 2
        };

        private String[] styleNames = {
            "Plain", "Bold", "Italic"
        };

        String[] names;
    }

    public SFontChooser() {
        fontModel = new N_FontModel(null);
        font_h = new Font(null, Font.PLAIN, 14);
        initGUI(getFont());
    }

    public SFontChooser(Font font) {
        fontModel = new N_FontModel(null);
        font_h = new Font(null, Font.PLAIN, 14);
        initGUI(font);
    }

    private void initGUI(Font font) {
        add(getFontFamilyNamesComboBox());
        add(getStyleNamesComboBox());
        add(getSizesComboBox());
        int ni = getModel().getFontFamilyNameIndex(font.getFamily());
        getFontFamilyNamesComboBox().setSelectedIndex(ni == -1 ? 0 : ni);
        int ti = getModel().getStyleTypeIndex(font.getStyle());
        getStyleNamesComboBox().setSelectedIndex(ti == -1 ? 0 : ti);
        int si = getModel().getSizeIndex(font.getSize());
        if (si == -1)
            si = getModel().getSizeRoundIndex(font.getSize());
        getSizesComboBox().setSelectedIndex(si == -1 ? 0 : si);
        add(getTextArea_e());
    }

    private JComboBox getFontFamilyNamesComboBox() {
        if (fontFamilyNamesComboBox == null) {
            fontFamilyNamesComboBox = new JComboBox(getModel().getFontFamilyNames());
            fontFamilyNamesComboBox.addActionListener(getActionListener_d());
        }
        fontFamilyNamesComboBox.setFont(font_h);
        return fontFamilyNamesComboBox;
    }

    private JComboBox getSizesComboBox() {
        if (sizesComboBox == null) {
            sizesComboBox = new JComboBox(getModel().getSizes());
            sizesComboBox.addActionListener(getActionListener_d());
        }
        fontFamilyNamesComboBox.setFont(font_h);
        return sizesComboBox;
    }

    private JComboBox getStyleNamesComboBox() {
        if (styleNamesComboBox == null) {
            styleNamesComboBox = new JComboBox(getModel().getStyleNames());
            styleNamesComboBox.addActionListener(getActionListener_d());
        }
        fontFamilyNamesComboBox.setFont(font_h);
        return styleNamesComboBox;
    }

    private ActionListener getActionListener_d() {
        if (actionListener_f == null)
            actionListener_f = new O_ActionListener(this);
        return actionListener_f;
    }

    public SFontModel getModel() {
        return fontModel;
    }

    private JTextArea getTextArea_e() {
        if (textArea_e == null) {
            textArea_e = new JTextArea();
            textArea_e.setEditable(false);
            textArea_e.setBackground(Color.white);
            textArea_e.setText("abc ABC あいう　アイウ　漢字");
            textArea_e.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent event) {
                    firePropertyChange(SFontChooser.eventName, event, event.getComponent());
                }
            });
        }
        return textArea_e;
    }

    public Font getSelectedFont() {
        return getTextArea_e().getFont();
    }

    public static FontChooserPane createFontChooserPane() {
        return new FontChooserPane();
    }

    public static FontChooserPane createFontChooserPane(Font font) {
        return new FontChooserPane(font);
    }

    public static Font showInternalFrame(JDesktopPane desktopPane, String title, Font font) {
        final SFontChooser chooser = new SFontChooser(font);
        chooser.addPropertyChangeListener(eventName, event -> {
            JInternalFrame iframe = SGUIUtil.getParentInstanceOf(chooser, JInternalFrame.class);
            if (iframe != null)
                iframe.pack();
        });
        int r = JOptionPane.showInternalConfirmDialog(desktopPane, chooser, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
        if (r == 0)
            return chooser.getSelectedFont();
        else
            return font;
    }

    @Deprecated
    public static Font showSInternalFrame(JDesktopPane desktopPane, String title, Font font) {
        final SInternalFrame iframe = new SInternalFrame();
        desktopPane.add(iframe);
        iframe.setTitle(title);
        iframe.setDefaultCloseOperation(2);
        FontChooserPane fontchooserpane = createFontChooserPane(font);
        fontchooserpane.setCloseAction(event -> {
            iframe.setModal(false);
            iframe.setVisible(false);
        });
        iframe.add(fontchooserpane);
        fontchooserpane.addTextSizeChangeListener(iframe);
        iframe.pack();
        desktopPane.validate();
        SGUIUtil.setCenter(desktopPane, iframe);
        iframe.setVisible(true);
        iframe.setForceHeavyWeightPopup(true);
        iframe.setModal(true);
        return fontchooserpane.getSelectedFont();
    }

    public static Font showDialog(Component component, String s) {
        return showDialog(component, s, component.getFont());
    }

    public static Font showDialog(Component component, String title, Font font) {
        final JDialog dialog = new JDialog();
        dialog.setModal(true);
        dialog.setTitle(title);
        dialog.setAlwaysOnTop(true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        FontChooserPane pane = createFontChooserPane(font);
        pane.setCloseAction(event -> dialog.setVisible(false));
        dialog.add(pane);
        pane.addTextSizeChangeListener(dialog);
        dialog.pack();
        if (component != null)
            dialog.setLocationRelativeTo(component);
        dialog.setVisible(true);
        return pane.getSelectedFont();
    }

    private static void initGUI() {
        JFrame frame = new JFrame();
        frame.setSize(640, 480);
        final JDesktopPane desktopPane = new JDesktopPane();
        frame.add(desktopPane);
        final JInternalFrame iframe = new JInternalFrame();
        iframe.setSize(320, 240);
        JTextPane textPane = new JTextPane();
        textPane.setSize(302, 240);
        textPane.setText("ふぉんとをかえるゆーてぃりてぃ");
        iframe.add(textPane);
        iframe.setVisible(true);
        JInternalFrame iframe1 = new JInternalFrame();
        JButton button = new JButton();
        button.setText("execute");
        button.addActionListener(event -> {
            Font font = SFontChooser.showInternalFrame(desktopPane, "フォントの変更", iframe.getFont());
            SGUIUtil.setFontAll(iframe, font);
        });
        iframe1.add(button);
        iframe1.pack();
        iframe1.setVisible(true);
        desktopPane.add(iframe1);
        desktopPane.add(iframe);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        initGUI();
    }

    static JComboBox getComboBox_a(SFontChooser fontChooser) {
        return fontChooser.getFontFamilyNamesComboBox();
    }

    static JComboBox getComboBox_c(SFontChooser fontChooser) {
        return fontChooser.getStyleNamesComboBox();
    }

    static JComboBox getComboBox_b(SFontChooser fontChooser) {
        return fontChooser.getSizesComboBox();
    }

    static JTextArea getTextArea_d(SFontChooser fontChooser) {
        return fontChooser.getTextArea_e();
    }

    private static final long serialVersionUID = 1L;
    private JComboBox fontFamilyNamesComboBox;
    private JComboBox styleNamesComboBox;
    private JComboBox sizesComboBox;
    private JTextArea textArea_e;
    private ActionListener actionListener_f;
    private final SFontModel fontModel;
    static String eventName = "change.size.textarea";
    private Font font_h;

    // Unreferenced inner class com/soso/sgui/O
    static class O_ActionListener implements ActionListener {

        public final void actionPerformed(ActionEvent event) {
            SFontChooser.getTextArea_d(a).setFont(new Font(SFontChooser.getComboBox_a(a).getSelectedItem().toString(), a.getModel().styleNameToType(SFontChooser.getComboBox_c(a).getSelectedItem().toString()), (Integer) SFontChooser.getComboBox_b(a).getSelectedItem()));
        }

        final SFontChooser a;

        O_ActionListener(SFontChooser sfontchooser) {
            a = sfontchooser;
        }
    }
}
