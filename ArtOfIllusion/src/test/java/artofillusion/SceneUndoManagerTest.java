package artofillusion;

import artofillusion.model.Material;
import artofillusion.model.SceneObject;
import artofillusion.model.Texture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SceneUndoManagerTest {

    @org.junit.jupiter.api.BeforeEach
    void setUp() {

    }

    @Test
    public void createNewScene() {
        var scene = SceneFactory.create();
        Assertions.assertNotNull(scene);
        Assertions.assertFalse(scene.isModified());
    }

    @Test
    public void createNewSceneAndAddAndAddAndUndo() {
        var scene = SceneFactory.create();
        var manager = UndoManager.getUndoManager(scene);

        scene.add(new SceneObject("Test 1"));
        Assertions.assertTrue(scene.isModified());
        scene.add(new SceneObject("Test 2"));
        Assertions.assertTrue(scene.isModified());

        manager.undo();
        Assertions.assertTrue(scene.isModified());
    }
    @Test
    public void createNewSceneAndAddObjectAndUndoSaveAndRedo() {
        var scene = SceneFactory.create();
        var manager = UndoManager.getUndoManager(scene);

        scene.add(new SceneObject());
        Assertions.assertTrue(scene.isModified());

        Assertions.assertEquals(1, scene.getObjects().size());
        Assertions.assertTrue(manager.canUndo());
        manager.undo();

        Assertions.assertFalse(scene.isModified());
        scene.save();
        manager.redo();
        Assertions.assertTrue(scene.isModified());
    }

    @Test
    public void createAddAndUndo() {
        var scene = SceneFactory.create();
        var manager = UndoManager.getUndoManager(scene);

        scene.add(new SceneObject());
        manager.undo();
        Assertions.assertFalse(scene.isModified());
    }

    @Test
    public void createAddAndAdd() {
        var scene = SceneFactory.create();
        var manager = UndoManager.getUndoManager(scene);

        scene.add(new SceneObject());
        scene.add(new SceneObject());

        Assertions.assertTrue(scene.isModified());
    }


    @Test
    public void createAddUndoAndRedo() {
        var scene = SceneFactory.create();
        var manager = UndoManager.getUndoManager(scene);

        scene.add(new SceneObject());
        manager.undo();
        manager.redo();
        Assertions.assertTrue(scene.isModified());
    }

    @Test
    public void createAddUndoSaveAndRedo() {
        var scene = SceneFactory.create();
        var manager = UndoManager.getUndoManager(scene);

        scene.add(new SceneObject());
        manager.undo();
        scene.save();
        manager.redo();
        Assertions.assertTrue(scene.isModified());
    }

    @Test
    public void createAddUndoSaveAndRedoAndUndo() {
        var scene = SceneFactory.create();
        var manager = UndoManager.getUndoManager(scene);

        scene.add(new SceneObject());
        manager.undo();
        scene.save();
        manager.redo();
        manager.undo();
        Assertions.assertFalse(scene.isModified());
    }

    @Test
    public void testAddSomeObjectsAndUndo() {
        var scene = SceneFactory.create();
        var manager = UndoManager.getUndoManager(scene);

        scene.add(new Material(""));
        scene.add(new Texture(""));

        manager.undo();
        manager.undo();

        Assertions.assertFalse(scene.isModified());
    }

    @Test
    public void testAddSomeObjectsMoreAndUndo() {
        var scene = SceneFactory.create();
        var manager = UndoManager.getUndoManager(scene);

        scene.add(new Material(""));
        scene.add(new Texture(""));
        scene.add(new SceneObject());

        manager.undo();
        manager.undo();

        Assertions.assertTrue(scene.isModified());
    }

    @Test
    public void testAddSaveUndoAndRedo() {
        var scene = SceneFactory.create();
        var manager = UndoManager.getUndoManager(scene);
        scene.add(new Material(""));
        scene.save();
        Assertions.assertFalse(scene.isModified());
        manager.undo();
        manager.redo();
        Assertions.assertFalse(scene.isModified());
    }
}
