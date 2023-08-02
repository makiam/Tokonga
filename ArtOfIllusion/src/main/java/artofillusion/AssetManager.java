package artofillusion;

import artofillusion.ui.EditingWindow;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import java.awt.*;

public class AssetManager extends JFrame {

    private final Scene scene;
    private JTree assetsTree;

    public AssetManager(EditingWindow frame, Scene scene) throws HeadlessException {
        super();
        this.scene = scene;
    }

    @Override
    protected void frameInit() {
        super.frameInit();
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getContentPane().add(assetsTree = new JTree());
        this.setSize(640,480);
        var root = new DefaultMutableTreeNode("Root");
        root.add(new SceneTreeNode(scene));
        DefaultTreeModel model = new DefaultTreeModel(root);
        assetsTree.setModel(model);
    }

    private class SceneTreeNode extends DefaultMutableTreeNode {
        public SceneTreeNode(Scene scene) {
            super(scene);
        }
    }
}
