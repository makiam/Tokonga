package artofillusion;

import artofillusion.material.Material;
import artofillusion.texture.Texture;
import artofillusion.ui.EditingWindow;
import artofillusion.ui.Translate;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.nio.file.Path;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.ExpandVetoException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssetManager extends JFrame implements TreeWillExpandListener {
    //<a target="_blank" href="https://icons8.com/icon/80332/object">Object</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
    private final Scene scene;
    private JTree assetsTree;
    
    private static final ImageIcon sceneIcon = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("artofillusion/ui/icons8-object-48.png"));
    
    public AssetManager(EditingWindow frame, Scene scene) throws HeadlessException {
        super();
        
        this.scene = scene;
        this.postConstruct();
    }

    private void postConstruct() {
        var root = new DefaultMutableTreeNode("Root");
        root.add(new SceneTreeNode(scene));
        
        DefaultTreeModel model = new DefaultTreeModel(root);
        assetsTree.setModel(model);        
    } 
    
    @Override
    protected void frameInit() {
        super.frameInit();
        this.setTitle(Translate.text("texturesTitle"));
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);        
        assetsTree = new JTree();
        assetsTree.setRootVisible(true);
        assetsTree.setShowsRootHandles(true);
        assetsTree.setCellRenderer(new AssetsTreeCellRenderer());
        assetsTree.addTreeWillExpandListener(this);

        this.getContentPane().add(assetsTree);
        this.setSize(640,480);

        
    }

    @Override
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
        var expando = event.getPath().getLastPathComponent();
        log.atInfo().log("Will expand {}", expando);
        if(expando instanceof Lazy) {
            Lazy loading = (Lazy)expando;
            if(!loading.loaded()) loading.load();
        } 
    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {        
    }
    
    private interface Lazy {
        boolean loaded();
        void load();
    } 
    
    private static class SceneTreeNode extends DefaultMutableTreeNode implements Lazy {
        private final Scene scene;
        private boolean loaded;
        
        public SceneTreeNode(Scene scene) {
            super(scene);
            this.scene = scene;
            this.add(new DefaultMutableTreeNode("Loading...", false));
        }
        
        @Override
        public String toString() {
            return "Scene" + (scene.getName() == null ? "" : ": " + scene.getName());             
        }

        @Override
        public boolean loaded() {
            return loaded;
        }

        @Override
        public void load() {
            if(loaded) return;
            this.removeAllChildren();
            scene.getTextures().forEach(tex -> add(new TextureTreeNode(tex)));
            scene.getMaterials().forEach(mat -> add(new MaterialTreeNode(mat)));
            loaded = true;
        }
        
    }
    
    private static class FolderTreeNode extends DefaultMutableTreeNode {
        public FolderTreeNode(Path path) {
            super(path);
        }
    }

    private static class TextureTreeNode extends DefaultMutableTreeNode {
        public TextureTreeNode(Texture texture) {
            super(texture);
        }

        @Override
        public String toString() {
            return ((Texture)getUserObject()).getName();
        }
        
    } 
    
    private static class MaterialTreeNode extends DefaultMutableTreeNode {
        public MaterialTreeNode(Material material) {
            super(material);
        }

        @Override
        public String toString() {
            return ((Material)getUserObject()).getName();
        }
        
    }
    
    private static class AssetsTreeCellRenderer extends DefaultTreeCellRenderer {

        public AssetsTreeCellRenderer() {
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component cc = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            //((JLabel)cc).setIcon(AssetManager.sceneIcon);
            return cc;
        }
    }
}
