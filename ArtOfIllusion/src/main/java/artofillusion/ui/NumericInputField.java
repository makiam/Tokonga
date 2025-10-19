package artofillusion.ui;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

import java.awt.*;
import java.beans.JavaBean;

@Slf4j
@JavaBean(defaultProperty = "UIClassID", description = "A component which allows for the editing of a single line of text.")
@SwingContainer(false)
public final class NumericInputField extends JTextField {

    @Getter
    private Double value = 0.;
    private final Color ob;

    public NumericInputField() {
        super("0.0", 10);
        this.getDocument().addDocumentListener((artofillusion.ui.TextInputListener) this::textChanged);
        ob = this.getBackground();


    }

    private void textChanged(javax.swing.event.DocumentEvent de) {
        var doc = this.getDocument();
        var text = "";
        try {
            text = doc.getText(0, doc.getLength());
        } catch (javax.swing.text.BadLocationException e) {
        }
        double iv = 0;
        try {
            iv = Double.parseDouble(text);
        } catch(NumberFormatException nfe) {
            this.setBackground(Color.PINK);
            return;
        }
        value = iv;
        this.setBackground(ob);
    }
}
