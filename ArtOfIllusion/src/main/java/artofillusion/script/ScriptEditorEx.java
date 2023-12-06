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

import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;

@Slf4j
public class ScriptEditorEx extends JFrame {

    private StatusPanel statusPanel;
    private JLabel statusLabel;
    private RSyntaxTextArea scriptTextArea;

    private org.fife.ui.rtextarea.RTextScrollPane scroller;
    
    private static final long serialVersionUID = 1L;
    private LayoutWindow layout;
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
        getContentPane().add(scroller, BorderLayout.CENTER);
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
        var fileMenu =mb.add(new JMenu(Translate.text("menu.file")));
        fileMenu.add(new NewScriptAction());
        fileMenu.add(new NewScriptAction());
        fileMenu.addSeparator();
        fileMenu.add(new CloseAction());

        mb.add(new JMenu(Translate.text("menu.edit")));
        mb.add(new JMenu(Translate.text("Run")));
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
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(() -> new ScriptEditorEx(layout).setVisible(true));
        }
    }

    private class OpenScriptAction extends AbstractAction {
        public OpenScriptAction() {
            super(Translate.text("Open"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {

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

    private class RunScriptAction extends AbstractAction {
        public RunScriptAction() {
            super(Translate.text("Run"));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            var shell = ArtOfIllusion.getShell();
        }
    }
}
