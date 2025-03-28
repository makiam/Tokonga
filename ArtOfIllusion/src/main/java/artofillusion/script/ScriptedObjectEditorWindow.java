/* Copyright (C) 2002-2013 by Peter Eastman
   Changes Copyright (C) 2023 by Lucas Stanek
   Changes Copyright (C) 2024 by Maksim Khramov

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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * This class presents a user interface for entering object scripts.
 */
@Slf4j
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
        this.getComponent().setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getComponent().setIconImage(ArtOfIllusion.APP_ICON.getImage());
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
        content.add(languageRow, BorderContainer.NORTH, new LayoutInfo(LayoutInfo.EAST, LayoutInfo.NONE));
        content.add(BOutline.createBevelBorder(scriptWidget, false), BorderContainer.CENTER);
        RowContainer buttons = new RowContainer();
        content.add(buttons, BorderContainer.SOUTH, new LayoutInfo());

        buttons.add(Translate.button("ok", event -> commitChanges()));
        buttons.add(Translate.button("Load", "...", event -> loadScript()));
        buttons.add(Translate.button("Save", "...", event -> saveScript()));
        buttons.add(Translate.button("scriptParameters", event -> editParameters()));
        buttons.add(Translate.button("cancel", event -> cancel()));
        this.getComponent().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                commitChanges();
            }
        });

        languageChoice.addEventLink(ValueChangedEvent.class, this, "updateLanguage");
        scriptWidget.getContent().setCaretPosition(0);
        pack();
        updateLanguage();
        UIUtilities.centerWindow(this);
        scriptWidget.requestFocus();
        setVisible(true);
    }

    private void cancel() {
        log.info("Cancelling {} ", this.getClass().getSimpleName());
        this.getComponent().dispose();
    }

    /**
     * Make syntax highlighting match current scripting language
     */
    private void updateLanguage() {
        scriptWidget.setLanguage((String) languageChoice.getSelectedValue());
    }

    /**
     * Display a dialog for editing the parameters.
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    private void editParameters() {
        new ParametersDialog(this, (ScriptedObject) info.getObject());
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
    private static class ParametersDialog extends BDialog {

        private final ScriptedObject script;
        private final BList paramList;
        private final BTextField nameField;
        private final ValueField valueField;

        private int current;

        private final List<ParameterPair> pairs = new ArrayList<>();

        public ParametersDialog(ScriptedObjectEditorWindow owner, ScriptedObject target) {
            super(owner, Translate.text("objectParameters"), true);
            this.getComponent().setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            this.getComponent().setIconImage(ArtOfIllusion.APP_ICON.getImage());

            script = target;
            FormContainer content = new FormContainer(new double[]{0.0, 1.0}, new double[]{1.0, 0.0, 0.0, 0.0});
            content.setDefaultLayout(new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.HORIZONTAL, null, null));
            setContent(content);

            for (int i = 0; i < script.getNumParameters(); i++) {
                pairs.add(new ParameterPair(script.getParameterName(i),script.getParameterValue(i)));
            }
            paramList = new BList();
            content.add(UIUtilities.createScrollingList(paramList), 0, 0, 2, 1, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH, null, null));
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
            buttons.add(Translate.button("add", event -> doAdd()));
            buttons.add(Translate.button("remove", event -> doRemove()));
            buttons.add(Translate.button("ok", event -> doOk()));
            buttons.add(Translate.button("cancel", event -> cancel()));
            setSelectedParameter(pairs.isEmpty() ? -1 : 0);
            this.getComponent().addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    dispose();
                }
            });

            KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
            ActionListener cancelAction = e -> cancel();
            this.getComponent().getRootPane().registerKeyboardAction(cancelAction, escape, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

            pack();
            UIUtilities.centerDialog(this, owner);
            setVisible(true);
        }

        private void cancel() {
            log.info("Cancelling {} tool dialog", this.getClass().getSimpleName());
            this.getComponent().dispose();
        }

        /**
         * Build the list of parameters.
         */
        private void buildParameterList() {
            paramList.removeAll();
            for (ParameterPair item: pairs) {
                paramList.add(item.getName());
            }
            if (pairs.isEmpty()) {
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
            if (which == -1 || which >= pairs.size()) {
                nameField.setEnabled(false);
                valueField.setEnabled(false);
            } else {
                nameField.setEnabled(true);
                valueField.setEnabled(true);
                nameField.setText(pairs.get(which).getName());
                valueField.setValue(pairs.get(which).getValue());
            }
        }

        /**
         * Deal with changes to the text fields.
         */
        private void textChanged(ValueChangedEvent ev) {
            if (current < 0 || current > pairs.size()) {
                return;
            }
            if (ev.getWidget() == nameField) {
                pairs.get(current).setName(nameField.getText());

            } else {
                pairs.get(current).setValue(valueField.getValue());
            }
        }

        /**
         * When the name field loses focus, update it in the list.
         */
        private void focusLost() {
            paramList.replace(current, pairs.get(current).getName());
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
            pairs.add(new ParameterPair(getUnusedParameterName(pairs), 0.0));

            buildParameterList();
            setSelectedParameter(pairs.size() - 1);
            nameField.requestFocus();
        }

        private static String getUnusedParameterName(List<ParameterPair> pairs) {
            int index = pairs.size();

            do {
                index++;
                String newName = "Parameter " + index;
                var matches = pairs.stream().anyMatch(p -> p.getName().equals(newName));
                if(!matches) break;
            } while (true);


            return "Parameter " + index;
        }


        /**
         * Remove a parameter.
         */
        private void doRemove() {
            pairs.remove(paramList.getSelectedIndex());
            buildParameterList();
            setSelectedParameter(-1);
        }

        /**
         * Save the changes.
         */
        private void doOk() {
            var names = pairs.stream().map(ParameterPair::getName).toArray(String[]::new);
            var values = pairs.stream().mapToDouble(ParameterPair::getValue).toArray();
            script.setParameters(names, values);
            dispose();
        }

        @AllArgsConstructor @Getter
        @Setter
        private class ParameterPair {
            String name;
            double value;
        }
    }
}
