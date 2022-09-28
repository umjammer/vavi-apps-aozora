/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;

import java.lang.reflect.Field;


public class SUIModel {

    public static class Font {

        public static String[] getAll() {
            return SUIModel.log(SUIModel.Font.class);
        }

        public static String Button = "Button.font";
        public static String CheckBox = "CheckBox.font";
        public static String CheckBoxMenuItem = "CheckBoxMenuItem.font";
        public static String CheckBoxMenuItemAccelerator = "CheckBoxMenuItem.acceleratorFont";
        public static String ColorChooser = "ColorChooser.font";
        public static String ComboBox = "ComboBox.font";
        public static String EditorPane = "EditorPane.font";
        public static String FormattedTextField = "FormattedTextField.font";
        public static String InternalFrameTitle = "InternalFrame.titleFont";
        public static String Label = "Label.font";
        public static String List = "List.font";
        public static String Menu = "Menu.font";
        public static String MenuAccelerator = "Menu.acceleratorFont";
        public static String MenuBar = "MenuBar.font";
        public static String MenuItem = "MenuItem.font";
        public static String MenuItemAccelerator = "MenuItem.acceleratorFont";
        public static String OptionPane = "OptionPane.font";
        public static String Panel = "Panel.font";
        public static String PasswordField = "PasswordField.font";
        public static String PopupMenu = "PopupMenu.font";
        public static String ProgressBar = "ProgressBar.font";
        public static String Radio = "RadioButton.font";
        public static String RadioButtonMenuItem = "RadioButtonMenuItem.font";
        public static String RadioButtonMenuItemAccelerator = "RadioButtonMenuItem.acceleratorFont";
        public static String ScrollPane = "ScrollPane.font";
        public static String TabbedPane = "TabbedPane.font";
        public static String Table = "Table.font";
        public static String TableHeader = "TableHeader.font";
        public static String TextArea = "TextArea.font";
        public static String TextField = "TextField.font";
        public static String TextPane = "TextPane.font";
        public static String TitledBorder = "TitledBorder.font";
        public static String Toggle = "ToggleButton.font";
        public static String ToolBar = "ToolBar.font";
        public static String ToolTip = "ToolTip.font";
        public static String Tree = "Tree.font";
        public static String ViewPort = "ViewPort.font";
   }

    static String[] log(Class<?> clazz) {
        Field[] fields = clazz.getFields();
        String[] names = new String[fields.length];
        for (int i = 0; i < fields.length; i++)
            try {
                names[i] = fields[i].get(fields[i].getName()).toString();
            } catch (Exception ignored) {
            }

        return names;
    }
}
