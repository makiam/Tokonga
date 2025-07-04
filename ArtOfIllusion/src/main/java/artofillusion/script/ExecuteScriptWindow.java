/* Copyright (C) 2002-2013 by Peter Eastman
   Changes Copyright (C) 2023 by Lucas Stanek
   Changes Copyright (C) 2023-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.script;

import artofillusion.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.prefs.Preferences;
import javax.swing.*;

import lombok.extern.slf4j.Slf4j;

/**
 * This class presents a user interface for entering scripts to be executed.
 */
@Slf4j
public class ExecuteScriptWindow extends BFrame {

    private final LayoutWindow window;
    private final ScriptEditingWidget scriptWidget;
    private final BComboBox languageChoice;
    public static final String NEW_SCRIPT_NAME = Translate.text("untitled");
    private String scriptPath;

    // QUESTION should this be static? It's actually the last directory used for scripts,
    // is there a reason it is shared among all editing windows?
    private static File scriptDir = new File(ArtOfIllusion.TOOL_SCRIPT_DIRECTORY);
    private String language;
    private final BButton save;
    private static final int EDITORS_OFFSET = 32;
    private static final ArrayList<String> openedScripts = new ArrayList<>();

    private final javax.swing.filechooser.FileFilter scriptFileFilter;

    /**
     * Adds a script path to the recent scripts list.
     * This uses a mapping to the current (now) timestamp ; the file paths are in the order of their timestamps,
     * so the older is first. The list is truncated to RecentFiles.MAX_RECENT elements.
     *
     * @see artofillusion.RecentFiles#MAX_RECENT
     * @param filePath
     */
    public static void addRecentScript(String filePath) {
        final Preferences pref = Preferences.userNodeForPackage(ExecuteScriptWindow.class);
        final String[] recentFiles = pref.get("recentFiles", "").split(File.pathSeparator);
        java.util.List<String> newRecentFiles = new ArrayList<>();
        newRecentFiles.add(filePath);
        for (String recentFile : recentFiles) {
            if (!recentFile.equals(filePath)) // If the current file already has a timestamp it will be updated below
            {
                newRecentFiles.add(recentFile);
            }
        }
        pref.put("recentFiles", String.join(File.pathSeparator, newRecentFiles.subList(0, java.lang.Math.min(newRecentFiles.size(), 10))));
    }

    public static String[] getRecentScripts() {
        final Preferences pref = Preferences.userNodeForPackage(ExecuteScriptWindow.class);
        final java.util.List<String> recentScripts = Arrays.asList(pref.get("recentFiles", "").split(File.pathSeparator));
        return recentScripts.toArray(new String[0]);
    }

    public void scriptWasEdited() {
        save.setEnabled(true);
    }

    /**
     *
     * @param win
     * @param scriptAbsolutePath {@link ExecuteScriptWindow#NEW_SCRIPT_NAME} if this is a new script
     * @param scriptLanguage May be {@link ScriptRunner#UNKNOWN_LANGUAGE} if this is a new script
     */
    public ExecuteScriptWindow(LayoutWindow win, String scriptAbsolutePath, String scriptLanguage) throws IOException {
        super(scriptAbsolutePath);
        setScriptNameFromFile(scriptAbsolutePath);
        language = scriptLanguage;
        scriptPath = scriptAbsolutePath;
        // Get the extensions dynamically
        final java.util.List<String> extensions = new ArrayList<>();
        for (String language : ScriptRunner.getLanguageNames()) {
            extensions.add(ScriptRunner.getFilenameExtension(language));
        }
        scriptFileFilter = new javax.swing.filechooser.FileNameExtensionFilter(
                "Script files", extensions.toArray(new String[0]));

        BorderContainer content = new BorderContainer();
        setContent(content);
        window = win;
        String editorTextContent = "";
        if (scriptLanguage != ScriptRunner.UNKNOWN_LANGUAGE && scriptAbsolutePath.contains(".")) {
            editorTextContent = ArtOfIllusion.loadFile(new File(scriptAbsolutePath));
        }
        scriptWidget = new ScriptEditingWidget(editorTextContent);
        scriptWidget.getContent().addEventLink(KeyTypedEvent.class, this, "scriptWasEdited");

        content.add(scriptWidget, BorderContainer.CENTER);
        languageChoice = new BComboBox(ScriptRunner.getLanguageNames());
        languageChoice.getComponent().setRenderer(new LanguageRenderer());
        BorderContainer tools = new BorderContainer();
        content.add(tools, BorderContainer.NORTH);
        RowContainer buttons = new RowContainer();
        buttons.add(Translate.button("load", "...", event -> loadScript()));
        buttons.add(Translate.button("saveAs", "...", event -> saveScriptAs()));
        buttons.add(save = Translate.button("save", "", event -> saveScript()));
        save.setEnabled(false);

        tools.add(buttons, BorderContainer.WEST, new LayoutInfo(LayoutInfo.WEST, LayoutInfo.NONE));

        // another center row for the "execute selected" and various debugging items
        RowContainer debugTools = new RowContainer();
        KeyStroke rc = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
        ActionListener ra = e -> executeScript();
        this.getComponent().getRootPane().registerKeyboardAction(ra, rc, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        debugTools.add(Translate.button("executeScript", ra));

        debugTools.add(Translate.button("executeToCursor", event -> executeToCursor()));
        debugTools.add(Translate.button("executeSelected", event -> executeSelected()));

        tools.add(debugTools, BorderContainer.CENTER, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE));

        RowContainer languageRow = new RowContainer();
        languageRow.add(Translate.label("language"));
        languageRow.add(languageChoice);
        if (scriptLanguage != ScriptRunner.UNKNOWN_LANGUAGE) {
            languageChoice.setSelectedValue(scriptLanguage);
            languageChoice.setEnabled(false);
        }
        tools.add(languageRow, BorderContainer.EAST, new LayoutInfo(LayoutInfo.EAST, LayoutInfo.NONE));
        this.getComponent().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ExecuteScriptWindow.this.closeWindow();
            }
        });

        languageChoice.addEventLink(ValueChangedEvent.class, this, "updateLanguage");
        scriptWidget.getContent().setCaretPosition(0);
        pack();
        updateLanguage();
        UIUtilities.centerWindow(this);
        // We add an offset to every window so one does not exactly hide the others
        int editorFrameOffset = EDITORS_OFFSET * openedScripts.size();
        setBounds(new Rectangle(this.getBounds().x + editorFrameOffset, this.getBounds().y + editorFrameOffset,
                this.getBounds().width, this.getBounds().height));
        scriptWidget.requestFocus();
        setVisible(true);
        updateEditableStatus(NEW_SCRIPT_NAME, scriptAbsolutePath);
    }

    class LanguageRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean hasFocus) {
            JLabel cc = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
            cc.setIcon(ThemeManager.getIcon("/artofillusion/Icons/" + value + ".png"));
            return cc;
        }
    }

    private void updateEditableStatus(String previousScriptAbsolutePath, String scriptAbsolutePath) {
        if (!previousScriptAbsolutePath.equals(scriptAbsolutePath)) {
            boolean isOpen = openedScripts.contains(scriptAbsolutePath);
            scriptWidget.getContent().setEditable(!isOpen);
            scriptWidget.getContent().setEnabled(!isOpen);
            scriptWidget.getContent().setBackground(isOpen ? Color.LIGHT_GRAY : Color.WHITE);
            if (isOpen) {
                new BStandardDialog(null,
                        new String[]{Translate.text("alreadyOpenedScript"),
                            "This window is read-only : this script is open in other window(s) " + scriptAbsolutePath},
                        BStandardDialog.ERROR).showMessageDialog(this);
            }
            openedScripts.remove(previousScriptAbsolutePath);
            openedScripts.add(scriptAbsolutePath);
        }
    }

    /**
     * Make syntax highlighting match current scripting language
     * {@link  ScriptRunner#Language}
     */
    private void updateLanguage() {
        scriptWidget.setLanguage(language);
    }

    private void closeWindow() {
        // Default action in the options dialog is "close anyway"
        int action = 1;
        // Warning message if the script hasn't been saved
        if (save.isEnabled()) {
            action = new BStandardDialog(null, new String[]{Translate.text("unsavedChanges"),
                Translate.text("unsavedChangesPrompt")}, BStandardDialog.ERROR)
                    .showOptionDialog(this, new String[]{
                Translate.text("saveAndClose"),
                Translate.text("discardChangesAndClose"),
                Translate.text("cancelClosing")}, scriptPath);
        }
        // Action 0 is save and close
        if (action == 0) {
            saveScript();
        }
        // Action 2 is cancel closing
        if (action != 2) {
            dispose();
            openedScripts.remove(scriptPath);
        }
    }

    /**
     * Prompt the user to load a script.
     */
    private void loadScript() {
        var chooser = new JFileChooser();
        chooser.setName(Translate.text("selectScriptToLoad"));
        
        // Save the current program working directory
        File workingDir = chooser.getCurrentDirectory();
        chooser.setCurrentDirectory(scriptDir);
        
        chooser.setFileFilter(scriptFileFilter);
        if (chooser.showOpenDialog(this.getComponent()) == JFileChooser.APPROVE_OPTION) {
            scriptDir = chooser.getCurrentDirectory();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            File scriptFile = chooser.getSelectedFile();
            try {
                scriptWidget.getContent().setText(ArtOfIllusion.loadFile(scriptFile));
                updateEditableStatus(scriptPath, scriptFile.getAbsolutePath());
                scriptPath = scriptFile.getAbsolutePath();
                scriptWidget.getContent().setCaretPosition(0);
                String filename = scriptFile.getName();
                String fileLanguage = ScriptRunner.getLanguageForFilename(filename);
                if (!fileLanguage.equals(ScriptRunner.UNKNOWN_LANGUAGE)) {
                    language = fileLanguage;
                    languageChoice.setSelectedValue(fileLanguage);
                    languageChoice.setEnabled(false);
                    setScriptNameFromFile(scriptFile.getAbsolutePath());
                    for (EditingWindow edWindow : ArtOfIllusion.getWindows()) {
                        if (edWindow instanceof LayoutWindow) {
                            ((LayoutWindow) edWindow).rebuildRecentScriptsMenu();
                        }
                    }
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    updateLanguage();
                    // disable the "Save" button,
                    // to be re-enabled as soon as the text changes
                    save.setEnabled(false);
                } else {
                    new BStandardDialog(null, new String[]{Translate.text("errorReadingScript"),
                        Translate.text("unsupportedFileExtension") + " : " + filename}, BStandardDialog.ERROR).showMessageDialog(this);
                }
            } catch (IOException ex) {
                log.atError().setCause(ex).log("Error reading script: {}", ex.getMessage());
                new BStandardDialog(null, new String[]{Translate.text("errorReadingScript"),
                    ex.getMessage() == null ? "" : ex.getMessage()}, BStandardDialog.ERROR).showMessageDialog(this);
            }
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        // Restore program working directory for other file-choosers
        chooser.setCurrentDirectory(workingDir);
    }

    /**
     * Prompt the user to save a script.
     */
    private void saveScriptAs() {
        var chooser = new JFileChooser();
        chooser.setName(Translate.text("saveScriptToFile"));
        

        // Save current program working directory
        File workingDir = chooser.getCurrentDirectory();
        chooser.setCurrentDirectory(scriptDir);
        if (language.equals(ScriptRunner.UNKNOWN_LANGUAGE)) {
            language = (String) languageChoice.getSelectedValue();
        }
        chooser.setSelectedFile(new File(scriptPath));
        chooser.setFileFilter(scriptFileFilter);
        if (chooser.showSaveDialog(this.getComponent()) == JFileChooser.APPROVE_OPTION) {
            scriptDir = chooser.getCurrentDirectory();

            // Write the script to disk.
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            File f = chooser.getSelectedFile();
            try( BufferedWriter out = new BufferedWriter(new FileWriter(f))) {

                out.write(scriptWidget.getContent().getText().toCharArray());

            } catch (IOException ex) {
                log.atError().setCause(ex).log("Error writing script: {}", ex.getMessage());
                new BStandardDialog(null, new String[]{Translate.text("errorWritingScript"),
                    ex.getMessage() == null ? "" : ex.getMessage()}, BStandardDialog.ERROR).showMessageDialog(this);
            }
            // Now we have saved, we can't change the language
            languageChoice.setEnabled(false);
            updateEditableStatus(scriptPath, f.getAbsolutePath());
            scriptPath = f.getAbsolutePath();

            setScriptNameFromFile(f.getAbsolutePath());
            // Update the Scripts menus in all windows.
            for (EditingWindow edWin : ArtOfIllusion.getWindows()) {
                if (edWin instanceof LayoutWindow) {
                    ((LayoutWindow) edWin).rebuildScriptsMenu();
                }
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        save.setEnabled(false);
        // Restore program working directory
        chooser.setCurrentDirectory(workingDir);
    }

    /**
     * Save the current script to its current file path, without user input.
     */
    private void saveScript() {
        if (language.equals(ScriptRunner.UNKNOWN_LANGUAGE)) {
            language = (String) languageChoice.getSelectedValue();
        }

        // Write the script to disk.
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try(BufferedWriter out = new BufferedWriter(new FileWriter(Paths.get(scriptPath).toFile()))) {            
            out.write(scriptWidget.getContent().getText().toCharArray());
        } catch (IOException ex) {
            new BStandardDialog(null, new String[]{Translate.text("errorWritingScript"),
                scriptPath + (ex.getMessage() == null ? "" : ex.getMessage())}, BStandardDialog.ERROR).showMessageDialog(this);
        }
        // Now we have saved, we can't change the language
        languageChoice.setEnabled(false);
        save.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Set the script name based on the name of a file that was loaded or saved.
     *
     * @param filePath NEW_SCRIPT_NAME or an absolute file path
     */
    private void setScriptNameFromFile(String filePath) {
        if (!filePath.equals(NEW_SCRIPT_NAME)) {
            addRecentScript(filePath);
        }
        setTitle(filePath);
    }

    private void executeSelected() {
        executeText(scriptWidget.getContent().getSelectedText());
        window.updateImage();
        scriptWidget.requestFocus();
    }

    private void executeToCursor() {
        final String substringAfterCaret = scriptWidget.getContent().getText()
                .substring(scriptWidget.getContent().getCaretPosition());
        int charactersUntilEndOfLine = substringAfterCaret.indexOf("\n");
        if (charactersUntilEndOfLine == -1) {
            charactersUntilEndOfLine = substringAfterCaret.length();
        }
        final String textToEndOfCaretLine = scriptWidget.getContent().getText()
                .substring(0, scriptWidget.getContent().getCaretPosition()
                        + charactersUntilEndOfLine);
        executeText(textToEndOfCaretLine);
        window.updateImage();
        scriptWidget.requestFocus();
    }

    /**
     * Execute the script.
     */
    private void executeScript() {
        executeText(scriptWidget.getContent().getText());
        window.updateImage();
        scriptWidget.requestFocus();
    }

    public void executeText(final String text) {
        try {
            String scriptLanguage = (language == ScriptRunner.UNKNOWN_LANGUAGE)
                    ? (String) languageChoice.getSelectedValue()
                    : language;

            ToolScript script = ScriptRunner.parseToolScript(scriptLanguage, text);
            script.execute(window);
        } catch (Exception e) {
            int line = ScriptRunner.displayError(language, e);
            if (line > -1) {
                // Find the start of the line containing the error.
                int index = 0;
                for (int i = 0; i < line - 1; i++) {
                    int next = text.indexOf('\n', index);
                    if (next == -1) {
                        index = -1;
                        break;
                    }
                    index = next + 1;
                }
                if (index > -1) {
                    scriptWidget.getContent().setCaretPosition(index);
                }
                scriptWidget.requestFocus();
            }
        }
    }
}
