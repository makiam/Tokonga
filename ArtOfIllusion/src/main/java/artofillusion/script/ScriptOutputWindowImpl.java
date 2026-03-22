/* Copyright (C) 2026 by Maksim Khramov
 * 
 *
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.script;

import artofillusion.ArtOfIllusion;
import artofillusion.ui.Translate;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class ScriptOutputWindowImpl extends JFrame {
    

    private JTextArea textArea;

    public JTextArea getTextArea() {
        return textArea;
    }
        
    @Override
    protected void frameInit() {
        super.frameInit();
        this.setIconImage(ArtOfIllusion.APP_ICON.getImage());

        setTitle("Script Output");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null); 

        // Create the textArea area with some initial content and scrollbars
        textArea = new JTextArea(10, 60);
        textArea.setFont(textArea.getFont().deriveFont(12f));
        textArea.setText("");
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        
        // Wrap textArea area in JScrollPane for scrolling capability
        JScrollPane scrollPane = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        // Create the clear button
        JButton clearButton = new JButton(Translate.text("button.close"));
        
        // Set up the main layout using BorderLayout
        setLayout(new BorderLayout());
        
        // Add textArea area (with scrollpane) to center - it will expand to fill available space
        add(scrollPane, BorderLayout.CENTER);
        
        // Create a panel for the button with GridBagLayout for horizontal centering
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0; // Allows horizontal centering
        gbc.fill = GridBagConstraints.NONE; // Button keeps its preferred size
        
        buttonPanel.add(clearButton, gbc);
        
        // Add button panel to the bottom
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Add action listener to clear button
        clearButton.addActionListener(e -> closeWindow());
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ScriptOutputWindowImpl.this.closeWindow();
            }            
        });
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
        }        
        SwingUtilities.invokeLater(() -> {
            ScriptOutputWindowImpl frame = new ScriptOutputWindowImpl();
            frame.pack(); // Size window to fit components nicely
            frame.setVisible(true);
        });
    }
    
    private void closeWindow() {
        this.setVisible(false);
        textArea.setText("");
    }
}
