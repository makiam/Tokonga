/* Copyright (C) 2002-2013 by Peter Eastman
   Changes Copyright (C) 2023 by Lucas Stanek

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.script;

import artofillusion.*;
import artofillusion.object.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import javax.swing.JFileChooser;

/**
 * This class presents a user interface for entering object scripts.
 */
public class ScriptedObjectEditorWindow extends BFrame {

    private final EditingWindow window;
    private final ObjectInfo info;
    private final ScriptEditingWidget scriptWidget;
    private final BComboBox languageChoice;
    private String scriptName;
    private final Runnable onClose;

    private static File scriptDir;

    public ScriptedObjectEditorWindow(EditingWindow parent, ObjectInfo obj, Runnable onClose) {
        super("Script '" + obj.getName() + "'");
        window = parent;
        info = obj;
        this.onClose = onClose;
        scriptName = "Untitled";
        if (scriptDir == null) {
            scriptDir = new File(ArtOfIllusion.OBJECT_SCRIPT_DIRECTORY);
        }
        BorderContainer content = new BorderContainer();
        setContent(content);
        scriptWidget = new ScriptEditingWidget(((ScriptedObject) info.getObject()).getScript());

        content.add(scriptWidget, BorderContainer.CENTER);
        languageChoice = new BComboBox(ScriptRunner.getLanguageNames());
        languageChoice.setSelectedValue(((ScriptedObject) info.getObject()).getLanguage());
        RowContainer languageRow = new RowContainer();
        languageRow.add(Translate.label("language"));
        languageRow.add(languageChoice);
        content.add(languageRow, BorderContainer.NORTH,
                new LayoutInfo(LayoutInfo.EAST, LayoutInfo.NONE));
        content.add(BOutline.createBevelBorder(scriptWidget, false),
                BorderContainer.CENTER);
        RowContainer buttons = new RowContainer();
        content.add(buttons, BorderContainer.SOUTH, new LayoutInfo());
        buttons.add(Translate.button("ok", this, "commitChanges"));
        buttons.add(Translate.button("Load", "...", this, "loadScript"));
        buttons.add(Translate.button("Save", "...", this, "saveScript"));
        buttons.add(Translate.button("scriptParameters", this, "editParameters"));
        buttons.add(Translate.button("cancel", this, "dispose"));
        addEventLink(WindowClosingEvent.class, this, "commitChanges");
        languageChoice.addEventLink(ValueChangedEvent.class, this, "updateLanguage");
        scriptWidget.getContent().setCaretPosition(0);
        pack();
        updateLanguage();
        UIUtilities.centerWindow(this);
        scriptWidget.requestFocus();
        setVisible(true);
    }

    /**
     * Make syntax highlighing match current scripting language
     */
    private void updateLanguage() {
        scriptWidget.setLanguage((String) languageChoice.getSelectedValue());
    }

    /**
     * Display a dialog for editing the parameters.
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    private void editParameters() {
        new ParametersDialog();
    }

    /**
     * Prompt the user to load a script.
     */
    private void loadScript() {
        var chooser = new JFileChooser();
        chooser.setName(Translate.text("selectScriptToLoad"));

        // Save program working directory
        File workingDir = chooser.getCurrentDirectory();
        chooser.setCurrentDirectory(scriptDir);

        if(chooser.showOpenDialog(this.getComponent()) == JFileChooser.APPROVE_OPTION) {
            scriptDir = chooser.getCurrentDirectory();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            File f = chooser.getSelectedFile();
            String filename = f.getName();
            String language = ScriptRunner.getLanguageForFilename(filename);
            if (language.equals(ScriptRunner.UNKNOWN_LANGUAGE)) {
                new BStandardDialog(null, new String[]{Translate.text("errorReadingScript"),
                    "Unrecognized file language : " + filename}, BStandardDialog.ERROR).showMessageDialog(this);
            } else {
                languageChoice.setSelectedValue(language);
                
                try {
                    scriptWidget.getContent().setText(Files.readString(f.toPath()));
                } catch (IOException ex) {
                    new BStandardDialog(null, new String[]{Translate.text("errorReadingScript"),
                        ex.getMessage() == null ? "" : ex.getMessage()}, BStandardDialog.ERROR).showMessageDialog(this);
                }
                setScriptNameFromFile(filename);
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                updateLanguage();
            }            
        }

        // Restore working directory
        chooser.setCurrentDirectory(workingDir);
    }

    /**
     * Prompt the user to save a script.
     */
    private void saveScript() {
        var chooser = new JFileChooser();
        chooser.setName(Translate.text("saveScriptToFile"));
        
        // Save program working directory
        File workingDir = chooser.getCurrentDirectory();
        chooser.setCurrentDirectory(scriptDir);
        chooser.setSelectedFile(new File(scriptDir, scriptName + '.' + ScriptRunner.getFilenameExtension((String) languageChoice.getSelectedValue())));

        if(chooser.showSaveDialog(this.getComponent()) == JFileChooser.APPROVE_OPTION) {
            scriptDir = chooser.getCurrentDirectory();

            // Write the script to disk.
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            File f = chooser.getSelectedFile();
            try(BufferedWriter out = new BufferedWriter(new FileWriter(f))) {
                out.write(scriptWidget.getContent().getText().toCharArray());
            } catch (Exception ex) {
                new BStandardDialog(null, new String[]{Translate.text("errorWritingScript"),
                    ex.getMessage() == null ? "" : ex.getMessage()}, BStandardDialog.ERROR).showMessageDialog(this);
            }
            setScriptNameFromFile(f.getName());
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));            
        }

        // Restore working directory
        chooser.setCurrentDirectory(workingDir);
    }

    /**
     * Set the script name based on the name of a file that was loaded or saved.
     */
    private void setScriptNameFromFile(String filename) {
        if (filename.contains(".")) {
            scriptName = filename.substring(0, filename.lastIndexOf("."));
        } else {
            scriptName = filename;
        }
    }

    /**
     * Commit changes to the scripted object.
     */
    private void commitChanges() {
        ScriptedObject so = (ScriptedObject) info.getObject();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        so.setScript(scriptWidget.getContent().getText());
        so.setLanguage(languageChoice.getSelectedValue().toString());
        so.sceneChanged(info, window.getScene());
        if (onClose != null) {
            onClose.run();
        }
        window.updateImage();
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        dispose();
    }

    /**
     * This is an inner class for editing the list of parameters on the object.
     */
    private class ParametersDialog extends BDialog {

        private final ScriptedObject script;
        private final BList paramList;
        private final BTextField nameField;
        private final ValueField valueField;
        private String[] name;
        private double[] value;
        private int current;

        public ParametersDialog() {
            super(ScriptedObjectEditorWindow.this, Translate.text("objectParameters"), true);
            script = (ScriptedObject) info.getObject();
            FormContainer content = new FormContainer(new double[]{0.0, 1.0}, new double[]{1.0, 0.0, 0.0, 0.0});
            content.setDefaultLayout(new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.HORIZONTAL, null, null));
            setContent(content);
            name = new String[script.getNumParameters()];
            value = new double[script.getNumParameters()];
            for (int i = 0; i < name.length; i++) {
                name[i] = script.getParameterName(i);
                value[i] = script.getParameterValue(i);
            }
            content.add(UIUtilities.createScrollingList(paramList = new BList()), 0, 0, 2, 1, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH, null, null));
            paramList.setPreferredVisibleRows(5);
            buildParameterList();
            paramList.addEventLink(SelectionChangedEvent.class, this, "selectionChanged");
            content.add(Translate.label("Name"), 0, 1);
            content.add(Translate.label("Value"), 0, 2);
            content.add(nameField = new BTextField(), 1, 1);
            nameField.addEventLink(ValueChangedEvent.class, this, "textChanged");
            nameField.addEventLink(FocusLostEvent.class, this, "focusLost");
            content.add(valueField = new ValueField(0.0, ValueField.NONE), 1, 2);
            valueField.addEventLink(ValueChangedEvent.class, this, "textChanged");
            RowContainer buttons = new RowContainer();
            content.add(buttons, 0, 3, 2, 1, new LayoutInfo());
            buttons.add(Translate.button("add", this, "doAdd"));
            buttons.add(Translate.button("remove", this, "doRemove"));
            buttons.add(Translate.button("ok", this, "doOk"));
            buttons.add(Translate.button("cancel", this, "dispose"));
            setSelectedParameter(name.length == 0 ? -1 : 0);
            pack();
            UIUtilities.centerDialog(this, ScriptedObjectEditorWindow.this);
            setVisible(true);
        }

        /**
         * Build the list of parameters.
         */
        private void buildParameterList() {
            paramList.removeAll();
            for (String item : name) {
                paramList.add(item);
            }
            if (name.length == 0) {
                paramList.add("(no parameters)");
            }
        }

        /**
         * Update the components to show the currently selected parameter.
         */
        private void setSelectedParameter(int which) {
            if (which != paramList.getSelectedIndex()) {
                paramList.clearSelection();
                paramList.setSelected(which, true);
            }
            current = which;
            if (which == -1 || which >= name.length) {
                nameField.setEnabled(false);
                valueField.setEnabled(false);
            } else {
                nameField.setEnabled(true);
                valueField.setEnabled(true);
                nameField.setText(name[which]);
                valueField.setValue(value[which]);
            }
        }

        /**
         * Deal with changes to the text fields.
         */
        private void textChanged(ValueChangedEvent ev) {
            if (current < 0 || current > name.length) {
                return;
            }
            if (ev.getWidget() == nameField) {
                name[current] = nameField.getText();
//          paramList.replaceItem(name[current], current);
            } else {
                value[current] = valueField.getValue();
            }
        }

        /**
         * When the name field loses focus, update it in the list.
         */
        private void focusLost() {
            paramList.replace(current, name[current]);
        }

        /**
         * Deal with selection changes.
         */
        private void selectionChanged() {
            setSelectedParameter(paramList.getSelectedIndex());
        }

        /**
         * Add a new parameter.
         */
        private void doAdd() {
            String[] newName = new String[name.length + 1];
            double[] newValue = new double[value.length + 1];
            System.arraycopy(name, 0, newName, 0, name.length);
            System.arraycopy(value, 0, newValue, 0, value.length);
            newName[name.length] = "";
            newValue[value.length] = 0.0;
            name = newName;
            value = newValue;
            buildParameterList();
            setSelectedParameter(name.length - 1);
            nameField.requestFocus();
        }

        /**
         * Remove a parameter.
         */
        private void doRemove() {
            int which = paramList.getSelectedIndex();
            String[] newName = new String[name.length - 1];
            double[] newValue = new double[value.length - 1];
            for (int i = 0, j = 0; i < name.length; i++) {
                if (i == which) {
                    continue;
                }
                newName[j] = name[i];
                newValue[j] = value[i];
                j++;
            }
            name = newName;
            value = newValue;
            buildParameterList();
            setSelectedParameter(-1);
        }

        /**
         * Save the changes.
         */
        private void doOk() {
            script.setParameters(name, value);
            dispose();
        }
    }
}
