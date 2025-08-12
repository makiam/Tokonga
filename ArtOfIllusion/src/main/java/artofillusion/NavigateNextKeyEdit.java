package artofillusion;

public final class NavigateNextKeyEdit implements UndoableEdit {

    private final LayoutWindow layout;
    private final Double time;

    NavigateNextKeyEdit(LayoutWindow layout) {
        this.layout = layout;
        this.time = layout.getScene().getTime();
    }

    @Override
    public void undo() {
        layout.setTime(time);
    }

    @Override
    public void redo() {
        layout.getScore().getSelectedTracks();
    }

    @Override
    public String getName() {
        return "Navigate next key";
    }
}
