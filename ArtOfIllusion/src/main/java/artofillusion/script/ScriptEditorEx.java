/* Copyright (C) 2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.script;

import artofillusion.ArtOfIllusion;
import artofillusion.LayoutWindow;
import artofillusion.ui.Translate;
import groovy.lang.GroovyRuntimeException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.StackTraceUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextArea;

@Slf4j
public class ScriptEditorEx extends JFrame {
    private GroovyShell shell = ArtOfIllusion.getShell();

    private StatusPanel statusPanel;
    private JLabel statusLabel;
    private RSyntaxTextArea scriptTextArea;
    private JTextArea outputArea;
    
    private org.fife.ui.rtextarea.RTextScrollPane scroller;
    
    private static final long serialVersionUID = 1L;
    private LayoutWindow layout;
    private JMenu editMenu;
    private JCheckBoxMenuItem cleanOut;
    
    public ScriptEditorEx(LayoutWindow owner) {
        this.layout = owner;
        scriptTextArea = new RSyntaxTextArea(20, 60);
        try {
            Theme theme = Theme.load(ScriptEditorEx.class.getResourceAsStream("/scriptEditorTheme.xml"));
            theme.apply(scriptTextArea);
        } catch (IOException ex) {
            //shouldn't happen unless we are pointing at a non-existent file
            log.atError().setCause(ex).log("Unable to load Editor theme: {}", ex.getMessage());
        }

        scriptTextArea.setCodeFoldingEnabled(true);
        scriptTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
        scroller = new org.fife.ui.rtextarea.RTextScrollPane(scriptTextArea);
        scroller.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        JSplitPane esp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        esp.setTopComponent(scroller);
        esp.setBottomComponent(outputArea = new JTextArea());
       
        outputArea.setFont(outputArea.getFont().deriveFont(12f));
        outputArea.setLineWrap(true);
        outputArea.setEditable(false);
        outputArea.setWrapStyleWord(true);        
        getContentPane().add(esp, BorderLayout.CENTER);
        
        editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.UNDO_ACTION)));
        editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.REDO_ACTION)));
        editMenu.addSeparator();
        editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.CUT_ACTION)));
        editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.COPY_ACTION)));
        editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.PASTE_ACTION)));
        editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.DELETE_ACTION)));
        editMenu.addSeparator();
        editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.SELECT_ALL_ACTION)));        
    }

    @Override
    protected void frameInit() {
        super.frameInit();
        this.setTitle(Translate.text("Script Editor"));
        this.setSize(800, 600);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getContentPane().add(statusPanel = new StatusPanel(), BorderLayout.SOUTH);

        statusPanel.add(statusLabel = new JLabel("Ready", SwingConstants.LEFT));
        
        this.setJMenuBar(new JMenuBar());
        var mb = this.getJMenuBar();
        var fileMenu = mb.add(new JMenu(Translate.text("menu.file")));
        fileMenu.add(new NewScriptAction());
        fileMenu.add(new OpenScriptAction());
        fileMenu.add(new SaveAction());
        fileMenu.add(new SaveAsAction());
        fileMenu.addSeparator();
        fileMenu.add(new CloseAction());

        editMenu = mb.add(new JMenu(Translate.text("menu.edit")));

        


        var runMenu = mb.add(new JMenu(Translate.text("Run")));
        runMenu.add(new RunScriptAction());
        runMenu.add(new RunSelectedAction());
        runMenu.addSeparator();
        
        runMenu.add(cleanOut = new JCheckBoxMenuItem("Clean output on run"));
    }

    private static JMenuItem createMenuItem(Action action) {
        JMenuItem item = new JMenuItem(action);
        item.setToolTipText(null); // Swing annoyingly adds tool tip text to the menu item
        return item;
    }

    private final class StatusPanel extends JPanel {
        public StatusPanel() {
            this.setLayout(new FlowLayout(FlowLayout.LEFT));
        }

        private static final long serialVersionUID = 1L;
    }

    
    private class NewScriptAction extends AbstractAction {
        public NewScriptAction() {
            super(Translate.text("New"));
            putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(() -> new ScriptEditorEx(layout).setVisible(true));
        }
    }

    private class OpenScriptAction extends AbstractAction {
        public OpenScriptAction() {
            super(Translate.text("Open"));
            putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
        }
    }

    private class CloseAction extends AbstractAction {
        public CloseAction() {
            super(Translate.text("Close"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {

        }
    }
    

    private class ScriptPrinter {

        public void print(String output) {
            println(output);
        }
        
        public void println(String output) {
            ScriptEditorEx.this.outputArea.append(output + "\n");
        }
        
        public void println() {
            ScriptEditorEx.this.outputArea.append("\n");            
        }
        
        public void print(Object output) {
            println(output.toString());
        }
    }
    private class RunScriptAction extends AbstractAction {
        public RunScriptAction() {
            super(Translate.text("Run"));
            putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        }
        @Override
        public void actionPerformed(ActionEvent event) {
            if(cleanOut.isSelected()) {
                outputArea.setText("");
            }
            Script script;
            try {
                script = shell.parse(ScriptEditorEx.this.scriptTextArea.getText());
                log.atInfo().log("Script class: {}", script.getClass().getSimpleName());
            } catch(CompilationFailedException cfe) {
                ScriptEditorEx.this.outputArea.setText(cfe.getMessage());
                return;
            }
            script.setProperty("out", new ScriptPrinter());
            script.setProperty("window", layout);            
            try {
                script.run();
            } catch (GroovyRuntimeException gre) {
                log.atError().setCause(gre).log("Groovy script exception: {}", gre);
                ScriptEditorEx.this.outputArea.append("\n" + gre.getMessage());
            } catch(Exception e) {
                
                Throwable tt = StackTraceUtils.sanitize(e);
                log.atError().setCause(e).log("Script exception: {}", tt);
                ScriptEditorEx.this.outputArea.append("\n" + tt.getMessage());                
                
                
            }

            
            
        }
    }
    
    private class RunSelectedAction extends AbstractAction {
        public RunSelectedAction() {
            super(Translate.text("Run Selection"));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            
        }
    }
    
    private static class SaveAsAction extends AbstractAction {

        public SaveAsAction() {
            super(Translate.text("menu.save"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {

        }
        
    }

    private static class SaveAction extends AbstractAction {

        public SaveAction() {
            super(Translate.text("menu.saveas"));            
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {

        }        
    }    
}
