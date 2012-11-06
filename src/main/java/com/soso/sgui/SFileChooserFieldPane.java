/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.filechooser.FileSystemView;


public class SFileChooserFieldPane extends JPanel {

    public SFileChooserFieldPane() {
        initGUI();
    }

    private void initGUI() {
        setLayout(new BorderLayout(5, 5));
        action = new  AbstractAction("SFileChooserFieldPane.comboBoxMnemonicAction") {
            public void actionPerformed(ActionEvent event) {
                requestComboBoxFocusInWindow();
            }
        };
        comboBox = new SComboBox();
        comboBox.getActionMap().put(action.getValue("Name"), action);
        comboBox.setEditable(true);
        add(comboBox, BorderLayout.CENTER);
        button = new SButton();
        button.setText("参照");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                showFileChooser();
            }
        });
        add(button, BorderLayout.EAST);
        fileChooser = new SFileChooser();
    }

    public boolean requestComboBoxFocusInWindow() {
        return comboBox.requestFocusInWindow();
    }

    public void setButtonName(String text, char mnemonic) {
        button.setText(text);
        button.setMnemonic(mnemonic);
    }

    public File getSelectedFile() {
        Object selection = comboBox.getSelectedItem();
        if (selection == null)
            return null;
        if (selection instanceof File)
            return (File) selection;
        String filename = selection.toString();
        if (filename.length() > 0)
            return new File(filename);
        else
            return null;
    }

    public SComboBox getComboBox() {
        return comboBox;
    }

    public SFileChooser getFileChooser() {
        return fileChooser;
    }

    public void showFileChooser() {
        File directory = null;
        File selectedFile = getSelectedFile();
        if (selectedFile != null && selectedFile.isAbsolute()) {
            File file = selectedFile;
            while (file != null) {
                if (file.exists() && file.isDirectory()) {
                    directory = file;
                    break;
                }
                file = file.getParentFile();
            }
        }
        if (directory == null)
            directory = FileSystemView.getFileSystemView().getHomeDirectory();
        fileChooser.setCurrentDirectory(directory);
        if (selectedFile != null && directory.equals(selectedFile.getParentFile()))
            fileChooser.setSelectedFile(selectedFile);
        int r = fileChooser.showDialog(this, null);
        File selectedFile2 = fileChooser.getSelectedFile();
        if (r == 0 && selectedFile2 != null) {
            boolean flag = false;
            int count = comboBox.getItemCount();
            for (int i = 0; i < count; i++) {
                Object item = comboBox.getItemAt(count);
                if (selectedFile2.equals(item)) {
                    flag = true;
                    break;
                }
            }
            if (!flag)
                comboBox.addItem(selectedFile2);
            comboBox.setSelectedItem(selectedFile2);
        }
    }

    private static final long serialVersionUID = 0xaf088394L;
    private SComboBox comboBox;
    private Action action;
    private SButton button;
    private SFileChooser fileChooser;
}
