package artofillusion.polymesh;

import artofillusion.ui.Translate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class ProgressDialog extends JDialog {

    private PolyMeshEditorWindow owner;
    private JProgressBar progressBar;
    @Getter
    private JTextArea textArea;
    private JButton button;
    private JScrollPane scrollPane;

    public ProgressDialog(@NotNull PolyMeshEditorWindow owner) {
        this(owner.getComponent());
        this.owner = owner;
    }
    public ProgressDialog(Frame owner) {
        super(owner, Translate.text("polymesh:meshUnfolding"), true);
        initializeComponents();
        setupLayout();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true); // Make dialog resizable
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void initializeComponents() {

        // Create progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        // Create text area with scroll pane
        textArea = new JTextArea(10, 30);
        textArea.setEditable(false);
        textArea.setFont(textArea.getFont().deriveFont(12f));
        scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Create button
        button = new JButton(Translate.text("polymesh:proceed"));
        button.addActionListener((ActionEvent e) -> {
            setVisible(false);
            dispose();
        });
    }

    private void setupLayout() {
        // Create the layout
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        // Enable auto-creation of gaps between components
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        // Horizontal layout
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(progressBar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(button)
        );

        // Vertical layout
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(button)
        );
    }

    // Public methods to update components
    public void setProgress(int value) {
        progressBar.setValue(value);
    }

    public void appendText(String text) {
        textArea.append(text + "\n");
        // Auto-scroll to bottom
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    public void setButtonText(String text) {
        button.setText(text);
    }

    public JButton getButton() {
        return button;
    }

    public void clearText() {
        textArea.setText("");
    }


}