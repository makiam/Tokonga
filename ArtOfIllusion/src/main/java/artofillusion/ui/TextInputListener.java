package artofillusion.ui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// Copied to Polymesh FindSimilarFaces Dialog. Extract to shared code?
@FunctionalInterface
public interface TextInputListener extends DocumentListener {
    void update(DocumentEvent de);

    @Override
    default void changedUpdate(DocumentEvent de) {
        update(de);
    }

    @Override
    default void insertUpdate(DocumentEvent de) {
        update(de);
    }

    @Override
    default void removeUpdate(DocumentEvent de) {
        update(de);
    }

}
