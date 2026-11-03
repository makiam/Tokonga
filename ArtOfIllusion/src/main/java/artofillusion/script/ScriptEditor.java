/* Copyright (C) 2006 by Peter Eastman
   Changes copyright (C) 2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.script;

import artofillusion.ArtOfIllusion;
import artofillusion.LayoutWindow;
import artofillusion.ui.EditingWindow;
import artofillusion.ui.Translate;
import groovy.lang.Closure;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.runtime.MethodClosure;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jspecify.annotations.NonNull;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.Document;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



@Slf4j
public class ScriptEditor extends JFrame implements ChangeListener {

    private static final String UNTITLED = Translate.text("untitled");
    private static final String TITLE_SUFFIX = " - Script Editor";

    // Last directory used for scripts, shared by all editor windows
    private static File scriptDir = new File(ArtOfIllusion.TOOL_SCRIPT_DIRECTORY);

    static {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            var font = Font.createFont(Font.TRUETYPE_FONT, ArtOfIllusion.class.getResourceAsStream("/fonts/JetBrainsMono-Bold.ttf"));
            ge.registerFont(font);
            font = Font.createFont(Font.TRUETYPE_FONT, ArtOfIllusion.class.getResourceAsStream("/fonts/JetBrainsMono-Italic.ttf"));
            ge.registerFont(font);
            font = Font.createFont(Font.TRUETYPE_FONT, ArtOfIllusion.class.getResourceAsStream("/fonts/JetBrainsMono-BoldItalic.ttf"));
            ge.registerFont(font);
            font = Font.createFont(Font.TRUETYPE_FONT, ArtOfIllusion.class.getResourceAsStream("/fonts/JetBrainsMono-Regular.ttf"));
            ge.registerFont(font);

        } catch (IOException | FontFormatException e) {
            log.atError().setCause(e).log("Unable to load font: {}", e.getMessage());
        }
    }

    private final RSyntaxTextArea editor;
    private final JTextArea output;
    private final JSplitPane splitPane;
    private final FileNameExtensionFilter scriptFileFilter;
    private JMenu file;
    private JMenu edit;
    private JMenuItem clearOutput;
    private JLabel status;
    private JLabel pos;
    private final LayoutWindow layout;
    private File currentFile;
    private boolean modified;

    public ScriptEditor(LayoutWindow layoutWindow) {
        this(layoutWindow, null);
    }

    /**
     * Create an editor with an existing script loaded into it.
     *
     * @param layoutWindow the window scripts are executed against
     * @param scriptFile the script to open, or null to start with an empty document
     */
    public ScriptEditor(LayoutWindow layoutWindow, File scriptFile) {
        super("Script Editor");
        layout = layoutWindow;
        this.setIconImage(ArtOfIllusion.APP_ICON.getImage());

        List<String> extensions = new ArrayList<>();
        for (String language : ScriptRunner.getLanguageNames()) {
            extensions.add(ScriptRunner.getFilenameExtension(language));
        }
        scriptFileFilter = new FileNameExtensionFilter("Script files", extensions.toArray(new String[0]));

        editor = new RSyntaxTextArea("", 25, 100);
        editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
        editor.setCodeFoldingEnabled(true);
        editor.addCaretListener(this::processCaretListener);

        try {
            Theme theme = Theme.load(ArtOfIllusion.class.getResourceAsStream("/scriptEditorTheme.xml"));
            theme.apply(editor);

        } catch (IOException ex) {
            //shouldn't happen unless we are pointing at a non-existent file
            log.atError().setCause(ex).log("Unable to load Editor theme: {}", ex.getMessage());
        }

        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setModified(true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setModified(true);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setModified(true);
            }
        });

        output = new JTextArea(8, 100);
        output.setEditable(false);
        output.setFont(editor.getFont());
        output.setBackground(editor.getBackground());
        output.setForeground(editor.getForeground());
        output.setCaretColor(editor.getCaretColor());

        var clearButton = new JButton(Translate.text("button.clear"));
        clearButton.addActionListener(e -> clearOutput());
        var outputHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        outputHeader.add(new JLabel(Translate.text("scriptOutput")));
        outputHeader.add(clearButton);

        var outputPanel = new JPanel(new BorderLayout());
        outputPanel.add(outputHeader, BorderLayout.NORTH);
        outputPanel.add(new JScrollPane(output), BorderLayout.CENTER);

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new RTextScrollPane(editor, true), outputPanel);
        splitPane.setResizeWeight(0.75);
        splitPane.setOneTouchExpandable(true);
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.7));

        this.getContentPane().add(splitPane, BorderLayout.CENTER);
        createFileMenu();
        createEditMenu();
        if (scriptFile != null) {
            loadScript(scriptFile);
        }
        updateTitle();
    }

    private void createFileMenu() {
        file.add(createMenuItem("newScript", KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), e -> newScript()));
        file.add(createMenuItem("menu.open", KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK), e -> openScript()));
        file.addSeparator();
        file.add(createMenuItem("menu.save", KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), e -> saveScript()));
        file.add(createMenuItem("menu.saveas", KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), e -> saveScriptAs()));
        file.addSeparator();
        file.add(createMenuItem("menu.close", KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK), e -> closeScript()));
    }

    private JMenuItem createMenuItem(String textKey, KeyStroke accelerator, ActionListener listener) {
        var item = new JMenuItem(Translate.text(textKey));
        item.setAccelerator(accelerator);
        item.addActionListener(listener);
        return item;
    }

    private void createEditMenu() {
        edit.add(new JMenuItem(RTextArea.getAction(RTextArea.UNDO_ACTION)));
        edit.add(new JMenuItem(RTextArea.getAction(RTextArea.REDO_ACTION)));
        edit.addSeparator();
        edit.add(new JMenuItem(RTextArea.getAction(RTextArea.CUT_ACTION)));
        edit.add(new JMenuItem(RTextArea.getAction(RTextArea.COPY_ACTION)));
        edit.add(new JMenuItem(RTextArea.getAction(RTextArea.PASTE_ACTION)));
        edit.add(new JMenuItem(RTextArea.getAction(RTextArea.DELETE_ACTION)));
        edit.addSeparator();
        edit.add(new JMenuItem(RTextArea.getAction(RTextArea.SELECT_ALL_ACTION)));
    }

    private void newScript() {
        if (!confirmUnsavedChanges()) return;
        editor.setText("");
        currentFile = null;
        setModified(false);
        status.setText(UNTITLED);
    }

    private void closeScript() {
        if (confirmUnsavedChanges()) {
            dispose();
        }
    }

    private void openScript() {
        if (!confirmUnsavedChanges()) return;
        var chooser = new JFileChooser();
        File workingDir = chooser.getCurrentDirectory();
        chooser.setCurrentDirectory(scriptDir);
        chooser.setFileFilter(scriptFileFilter);
        try {
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                scriptDir = chooser.getCurrentDirectory();
                loadScript(chooser.getSelectedFile());
            }
        } finally {
            chooser.setCurrentDirectory(workingDir);
        }
    }

    private void loadScript(File scriptFile) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            String text = ArtOfIllusion.loadFile(scriptFile);
            editor.setText(text);
            editor.setCaretPosition(0);
            currentFile = scriptFile;
            setModified(false);
            status.setText(scriptFile.getAbsolutePath());
            addRecentScript(scriptFile);
        } catch (IOException ex) {
            log.atError().setCause(ex).log("Error reading script: {}", ex.getMessage());
            showError(Translate.text("errorReadingScript"), "Script file: " + scriptFile.getAbsolutePath() + (ex.getMessage() == null ? "" : " : " + ex.getMessage()));
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private boolean saveScript() {
        if (currentFile == null) {
            return saveScriptAs();
        }
        return writeScript(currentFile);
    }

    private boolean saveScriptAs() {
        var chooser = new JFileChooser();
        File workingDir = chooser.getCurrentDirectory();
        chooser.setCurrentDirectory(scriptDir);
        chooser.setFileFilter(scriptFileFilter);
        chooser.setSelectedFile(currentFile != null ? currentFile : new File("script." + ScriptRunner.getFilenameExtension("Groovy")));
        try {
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return false;
            scriptDir = chooser.getCurrentDirectory();
            File target = chooser.getSelectedFile();
            if (target.getName().indexOf('.') == -1) {
                target = new File(target.getParentFile(), target.getName() + "." + ScriptRunner.getFilenameExtension("Groovy"));
            }
            if (target.exists()) {
                int result = JOptionPane.showConfirmDialog(this, Translate.text("overwriteFile", target.getAbsolutePath()),
                        Translate.text("menu.saveas"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result != JOptionPane.YES_OPTION) return false;
            }
            if (!writeScript(target)) return false;
            currentFile = target;
            updateTitle();
            addRecentScript(target);
            return true;
        } finally {
            chooser.setCurrentDirectory(workingDir);
        }
    }

    private boolean writeScript(File target) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            Files.writeString(target.toPath(), editor.getText());
            setModified(false);
            status.setText(target.getAbsolutePath());
            return true;
        } catch (IOException ex) {
            log.atError().setCause(ex).log("Error writing script: {}", ex.getMessage());
            showError(Translate.text("errorWritingScript"), target.getAbsolutePath() + (ex.getMessage() == null ? "" : " : " + ex.getMessage()));
            return false;
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Prompt to save unsaved changes.
     *
     * @return true if it is safe to discard the current content
     */
    private boolean confirmUnsavedChanges() {
        if (!modified) return true;
        Object[] options = {Translate.text("button.save"), Translate.text("button.dontSave"), Translate.text("button.cancel")};
        int result = JOptionPane.showOptionDialog(this, Translate.text("unsavedChangesPrompt"), Translate.text("unsavedChanges"),
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        if (result == JOptionPane.YES_OPTION) return saveScript();
        return result == JOptionPane.NO_OPTION;
    }

    private void addRecentScript(File scriptFile) {
        ExecuteScriptWindow.addRecentScript(scriptFile.getAbsolutePath());
        for (EditingWindow edWindow : ArtOfIllusion.getWindows()) {
            if (edWindow instanceof LayoutWindow layoutWindow) {
                layoutWindow.rebuildScriptsMenu();
            }
        }
    }

    private void showError(String message, String details) {
        JOptionPane.showMessageDialog(this, message + "\n" + details, Translate.text("errorTitle"), JOptionPane.ERROR_MESSAGE);
    }

    private void clearOutput() {
        if (clearOutput.isSelected()) output.setText("");
    }

    /**
     * Append text to the output pane, from any thread.
     */
    private void appendOutput(String text) {
        if (text == null || text.isEmpty()) return;
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> appendOutput(text));
            return;
        }
        output.append(text);
        output.setCaretPosition(output.getDocument().getLength());
        ensureOutputVisible();
    }

    /**
     * Expand the output pane if the user has collapsed it.
     */
    private void ensureOutputVisible() {
        int height = splitPane.getHeight();
        if (height <= 0) return;
        int minOutputHeight = 100;
        if (splitPane.getDividerLocation() > height - minOutputHeight) {
            splitPane.setDividerLocation(height - minOutputHeight);
        }
    }

    /**
     * Create a stream which writes to the output pane.
     */
    private PrintStream createOutputStream() {
        OutputStream stream = new OutputStream() {
            @Override
            public void write(int b) {
                appendOutput(new String(new byte[]{(byte) b}, StandardCharsets.UTF_8));
            }

            @Override
            public void write(byte @NonNull [] b, int off, int len) {
                appendOutput(new String(b, off, len, StandardCharsets.UTF_8));
            }
        };
        return new PrintStream(stream, true, StandardCharsets.UTF_8);
    }

    private void setModified(boolean value) {
        if (modified == value) return;
        modified = value;
        updateTitle();
    }

    private void updateTitle() {
        String name = currentFile == null ? UNTITLED : currentFile.getName();
        setTitle((modified ? "*" : "") + name + TITLE_SUFFIX);
    }

    private void processCaretListener(CaretEvent e) {
        Document doc = editor.getDocument();
        Element element = doc.getDefaultRootElement();
        int offset = e.getDot();
        if (offset < 0 || offset > doc.getLength()) return;
        int row = element.getElementIndex(offset);
        int column = offset - element.getElement(row).getStartOffset();
        pos.setText(row + " : " + column);
    }
    /**
     * Called by the constructors to init the <code>JFrame</code> properly.
     */
    @Override
    protected void frameInit() {
        super.frameInit();
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setSize(800, 600);

        var menu = new JMenuBar();
        setJMenuBar(menu);
        file = menu.add(new JMenu(Translate.text("menu.file")));
        edit = menu.add(new JMenu(Translate.text("menu.edit")));


        var run = menu.add(new JMenu(Translate.text("button.executeScript")));
        run.add(new ExecuteScriptAction());
        run.addSeparator();
        run.add(clearOutput =new JCheckBoxMenuItem("Clear output", true));
        initStatusPanel();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeScript();
            }
        });
        MenuSelectionManager.defaultManager().addChangeListener(this);
    }

    @Override
    public void dispose() {
        MenuSelectionManager.defaultManager().removeChangeListener(this);
        super.dispose();
    }

    /**
     * Invoked when the target of the listener has changed its state.
     *
     * @param event a ChangeEvent object
     */
    @Override
    public void stateChanged(ChangeEvent event) {
        log.info("Event {}", event);
    }

    private void initStatusPanel() {
        JPanel statusPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // First horizontal separator (spans full row)
        JSeparator topSeparator = new JSeparator(SwingConstants.HORIZONTAL);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        statusPanel.add(topSeparator, gbc);

        // Status label
        status = new JLabel(UNTITLED);
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(1, 3, 1, 3);
        statusPanel.add(status, gbc);

        // Vertical separator
        JSeparator verticalSeparator = new JSeparator(SwingConstants.VERTICAL);
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        statusPanel.add(verticalSeparator, gbc);

        // Row/column number label
        pos = new JLabel("1:1");
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(1, 3, 1, 3);
        statusPanel.add(pos, gbc);

        this.getContentPane().add(statusPanel, BorderLayout.SOUTH);
    }

    class ExecuteScriptAction extends AbstractAction {
        ExecuteScriptAction() {
            super(Translate.text("button.executeScript"));
            this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e the event to be processed
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            clearOutput();
            ensureOutputVisible();

            groovy.lang.Script script;
            var shell = ArtOfIllusion.getShell();
            try {
                script = shell.parse(editor.getText(), currentFile == null ? "null" : currentFile.getName());
            } catch (MultipleCompilationErrorsException mce) {
                log.atError().setCause(mce).log("Script compilation failed");
                appendOutput(mce.getMessage());

                return;
            } catch (Exception ex) {
                log.atError().setCause(ex).log("Unable to compile script");
                appendOutput(ex.toString());
                return;
            }

            PrintStream capture = createOutputStream();
            try {
                script.setProperty("window", ScriptEditor.this.layout);
                script.setProperty("scene", ScriptEditor.this.layout.getScene());
                script.setProperty("out", capture);
                script.setProperty("undo", new MethodClosure(ScriptEditor.this.layout, "undoCommand"));
                script.setProperty("redo", new MethodClosure(ScriptEditor.this.layout, "redoCommand"));
                script.setProperty("log", new LoggerClosure(null));
                script.run();
            } catch (Throwable ex) {
                log.atError().setCause(ex).log("Script execution failed");
                appendOutput(ex.toString());
            } finally {
                capture.flush();
                capture.close();
            }
        }
    }

    private static class LoggerClosure extends Closure<Void> {
        private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ArtOfIllusion.class);

        public LoggerClosure(Object owner) {
            super(owner);
        }

        public Void doCall() { return null; }
        public Void doCall(String format, Object... arguments) {
            logger.info(format, arguments);
            return null;
        }
        public Void doCall(Object... arguments) {
            logger.info("Log: {}", Arrays.toString(arguments));
            return null;
        }
    }
}
