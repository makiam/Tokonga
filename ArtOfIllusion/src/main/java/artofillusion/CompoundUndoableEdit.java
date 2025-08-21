package artofillusion;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class CompoundUndoableEdit implements UndoableEdit {

    private final LinkedList<UndoableEdit> commands = new LinkedList<>();

    public void add(UndoableEdit edit) {
        commands.add(edit);
    }

    @Override
    public void undo() {
        commands.descendingIterator().forEachRemaining(command -> command.undo());
    }

    @Override
    public void redo() {
        commands.forEach(command -> command.execute());
    }

    @Override
    public String getName() {
        return commands.isEmpty() ? UndoableEdit.super.getName() : commands.get(0).getName();
    }


}
