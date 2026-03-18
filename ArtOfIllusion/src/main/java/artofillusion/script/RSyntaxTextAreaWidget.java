package artofillusion.script;

import artofillusion.ArtOfIllusion;
import buoy.widget.AWTWidget;
import buoy.widget.BorderContainer;
import lombok.extern.slf4j.Slf4j;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import java.awt.*;
import java.awt.event.KeyListener;
import java.io.IOException;

@Slf4j
public class RSyntaxTextAreaWidget extends BorderContainer {

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

    private RSyntaxTextArea editor;

    public RSyntaxTextAreaWidget(String text) {
        super();
        editor = new RSyntaxTextArea(text, 25, 100);
        editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
        editor.setCodeFoldingEnabled(true);

        try {
            Theme theme = Theme.load(ArtOfIllusion.class.getResourceAsStream("/scriptEditorTheme.xml"));
            theme.apply(editor);

        } catch (IOException ex) {
            //shouldn't happen unless we are pointing at a non-existent file
            log.atError().setCause(ex).log("Unable to load Editor theme: {}", ex.getMessage());
        }

        RTextScrollPane sp = new RTextScrollPane(editor, true);
        this.add(new AWTWidget(sp), CENTER);

    }

    public void setLanguage(String lang) {
        editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
    }

    public void setText(String text) {
        editor.setText(text);
    }

    public void setCaretPosition(int i) {
        editor.setCaretPosition(i);
    }

    @Override
    public void setBackground(Color background) {
        editor.setBackground(background);
    }

    public void setEditable(boolean b) {
        editor.setEditable(b);
    }

    public String getText() {
        return editor.getText();
    }

    public String getSelectedText() {
        return editor.getSelectedText();
    }

    public int getCaretPosition() {
        return editor.getCaretPosition();
    }

    @Override
    public void requestFocus() {
        editor.requestFocus();
    }

    void setEditorTypingListener(KeyListener listener) {
        editor.addKeyListener(listener);
    }
}
