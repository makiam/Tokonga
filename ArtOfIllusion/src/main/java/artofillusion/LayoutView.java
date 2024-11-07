package artofillusion;

import artofillusion.ui.EditingTool;
import artofillusion.ui.EditingWindow;
import artofillusion.ui.ToolPalette;
import buoy.widget.BFrame;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

@Slf4j
public class LayoutView extends BFrame implements EditingWindow {

    private Scene scene;
    private final UndoStack undoStack = new UndoStack();

    @Override
    protected JFrame createComponent() {
        return new LayoutViewImpl(this);
    }

    @Override
    public ToolPalette getToolPalette() {
        return null;
    }

    @Override
    public void setTool(EditingTool tool) {

    }

    @Override
    public void setHelpText(String text) {
        ((LayoutViewImpl)component).setHelpText(text);
    }

    @Override
    public BFrame getFrame() {
        return this;
    }

    @Override
    public void updateImage() {

    }

    @Override
    public void updateMenus() {

    }

    /**
     * Set the current UndoRecord for this EditingWindow.
     */
    @Override
    public void setUndoRecord(UndoRecord command) {

    }

    @Override
    public void setModified() {

    }

    @Override
    public Scene getScene() {
        return scene;
    }

    @Override
    public ViewerCanvas getView() {
        return null;
    }

    /**
     * Get all ViewerCanvases contained in this window. This may return null
     * if there is no ViewerCanvas.
     */
    @Override
    public ViewerCanvas[] getAllViews() {
        return null;
    }

    /**
     * Confirm whether this window should be closed (possibly by displaying a message to the
     * user), and then close it. If the closing is canceled, this should return false.
     */
    @Override
    public boolean confirmClose() {
        return true;
    }

    public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                new LayoutView().setVisible(true);
            });
    }
}
