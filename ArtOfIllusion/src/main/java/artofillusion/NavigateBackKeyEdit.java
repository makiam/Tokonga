package artofillusion;

public class NavigateBackKeyEdit implements UndoableEdit {

    private final LayoutWindow layout;
    private final Double time;

    public NavigateBackKeyEdit(LayoutWindow layout) {
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
        return "Back one key";
    }
}
