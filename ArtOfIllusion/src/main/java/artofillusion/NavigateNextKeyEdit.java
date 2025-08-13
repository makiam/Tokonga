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
        var st = layout.getScore().getSelectedTracks();
        if(st.length == 0) return;
    }

    @Override
    public String getName() {
        return "Navigate next key";
    }
}
