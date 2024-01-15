package artofillusion.tools;

import artofillusion.UndoableEdit;
import org.junit.Test;


import static org.junit.Assert.*;

public class SkinToolUndoTest {

    @Test
    public void test() {
        var cue = new SkinTool.CompoundUndoableEdit(new UndoOne());
        cue.add(new UndoTwo());
        System.out.println("Now undoing");
        cue.undo();
        System.out.println("Now redoing");
        cue.redo();
    }

    private class UndoOne implements UndoableEdit {

        @Override
        public void undo() {
            System.out.println("Undo: " + this.getName());
        }

        @Override
        public void redo() {
            System.out.println("Redo: " + this.getName());
        }

        @Override
        public String getName() {
            return "One";
        }
    }

    private class UndoTwo implements UndoableEdit {

        @Override
        public void undo() {
            System.out.println("Undo: " + this.getName());
        }

        @Override
        public void redo() {
            System.out.println("Redo: " + this.getName());
        }

        @Override
        public String getName() {
            return "Two";
        }
    }
}
