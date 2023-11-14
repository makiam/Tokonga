/* Copyright (C) 2011 by Helge Hansen and Peter Eastman
   Changes copyright (C) 2016-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion;

import artofillusion.material.*;
import artofillusion.texture.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TexturesAndMaterialsDialog extends BDialog {

    private static final File assetsFolder = new File(ArtOfIllusion.APP_DIRECTORY, "Textures and Materials");


    private final Scene theScene;
    private EditingWindow parentFrame;
    private BTree libraryList;
    private File libraryFile;
    private Scene selectedScene;
    private Texture selectedTexture;
    private Material selectedMaterial;
    private SceneTreeNode selectedSceneNode;
    private int insertLocation;
    private BButton duplicateButton, deleteButton, editButton;
    private BButton loadLibButton, saveLibButton, deleteLibButton;
    private BComboBox typeChoice;
    private BRadioButton showTexturesButton, showMaterialsButton;
    private final List<Texture> textureTypes = PluginRegistry.getPlugins(Texture.class);
    private final List<Material> materialTypes = PluginRegistry.getPlugins(Material.class);
    private MaterialPreviewer preview;
    private BLabel matInfo;

    private boolean showTextures, showMaterials;
    private final List<Object> rootNodes;

    private static final DataFlavor TextureFlavor = new DataFlavor(Texture.class, "Texture");
    private static final DataFlavor MaterialFlavor = new DataFlavor(Material.class, "Material");

    private final ListChangeListener listListener = new ListChangeListener() {
        @Override
        public void itemAdded(int index, java.lang.Object obj) {
            ((SceneTreeModel) libraryList.getModel()).rebuildScenes(null);
        }

        @Override
        public void itemChanged(int index, java.lang.Object obj) {
            ((SceneTreeModel) libraryList.getModel()).rebuildScenes(null);
        }

        @Override
        public void itemRemoved(int index, java.lang.Object obj) {
            ((SceneTreeModel) libraryList.getModel()).rebuildScenes(null);
        }
    };

    TexturesAndMaterialsDialog(EditingWindow frame, Scene aScene) {

        super(frame.getFrame(), Translate.text("texturesTitle"), false);

        parentFrame = frame;
        theScene = aScene;

        theScene.addMaterialListener(listListener);
        theScene.addTextureListener(listListener);

        BorderContainer content = new BorderContainer();
        setContent(BOutline.createEmptyBorder(content, UIUtilities.getStandardDialogInsets()));

        // list:
        libraryList = new BTree();

        libraryList.setMultipleSelectionEnabled(false);
        libraryList.addEventLink(SelectionChangedEvent.class, this, "doSelectionChanged");
        libraryList.addEventLink(MouseClickedEvent.class, this, "mouseClicked");
        libraryList.getComponent().setDragEnabled(true);
        libraryList.getComponent().setDropMode(DropMode.ON);
        libraryList.getComponent().setTransferHandler(new DragHandler());
        libraryList.setRootNodeShown(false);

        BScrollPane listWrapper = new BScrollPane(libraryList, BScrollPane.SCROLLBAR_AS_NEEDED, BScrollPane.SCROLLBAR_AS_NEEDED);
        listWrapper.setBackground(libraryList.getBackground());
        listWrapper.setForceWidth(true);
        listWrapper.setPreferredViewSize(new Dimension(250, 250));

        // Radio buttons for filtering the tree
        FormContainer leftPanel = new FormContainer(new double[]{1}, new double[]{1, 0, 0, 0});
        leftPanel.setDefaultLayout(new LayoutInfo(LayoutInfo.WEST, LayoutInfo.NONE));
        leftPanel.add(listWrapper, 0, 0, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH));
        RadioButtonGroup group = new RadioButtonGroup();
        leftPanel.add(showTexturesButton = new BRadioButton(Translate.text("showTextures"), false, group), 0, 1);
        leftPanel.add(showMaterialsButton = new BRadioButton(Translate.text("showMaterials"), false, group), 0, 2);
        leftPanel.add(new BRadioButton(Translate.text("showBoth"), true, group), 0, 3);
        group.addEventLink(SelectionChangedEvent.class, this, "filterChanged");
        content.add(leftPanel, BorderContainer.WEST);

        // preview:
        BorderContainer matBox = new BorderContainer();

        Texture tx0 = theScene.getTexture(0); // initial texture
        preview = new MaterialPreviewer(tx0, null, 300, 300); // size to be determined
        matBox.add(preview, BorderContainer.CENTER); // preview must be in the center part to be resizeable

        ColumnContainer infoBox = new ColumnContainer();

        matInfo = new BLabel();
        BOutline matBorder = BOutline.createEmptyBorder(matInfo, 3); // a little space around the text
        infoBox.add(matBorder);
        infoBox.setDefaultLayout(new LayoutInfo(LayoutInfo.WEST, LayoutInfo.BOTH));

        matBox.add(infoBox, BorderContainer.SOUTH);

        content.add(matBox, BorderContainer.CENTER);

        // buttons:
        ColumnContainer buttons = new ColumnContainer();
        buttons.setDefaultLayout(new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.HORIZONTAL, null, null));
        content.add(buttons, BorderContainer.EAST);

        buttons.add(new BLabel(Translate.text("sceneFunctions"), BLabel.CENTER));

        typeChoice = new BComboBox();
        typeChoice.add(Translate.text("button.new") + "...");

        textureTypes.forEach(texture -> typeChoice.add(texture.getTypeName() + " texture"));
        materialTypes.forEach(material -> typeChoice.add(material.getTypeName() + " material"));

        typeChoice.addEventLink(ValueChangedEvent.class, this, "doNew");

        buttons.add(typeChoice);

        buttons.add(duplicateButton = Translate.button("duplicate", "...", this, "doCopy"));
        buttons.add(deleteButton = Translate.button("delete", "...", this, "doDelete"));
        buttons.add(editButton = Translate.button("edit", "...", this, "doEdit"));

        buttons.add(new BSeparator());

        buttons.add(new BLabel(Translate.text("libraryFunctions"), BLabel.CENTER));
        buttons.add(loadLibButton = Translate.button("loadFromLibrary", this, "doLoadFromLibrary"));
        buttons.add(saveLibButton = Translate.button("saveToLibrary", this, "doSaveToLibrary"));
        buttons.add(deleteLibButton = Translate.button("deleteFromLibrary", this, "doDeleteFromLibrary"));
        buttons.add(Translate.button("newLibraryFile", this, "doNewLib"));
        buttons.add(Translate.button("showExternalFile", this, "doIncludeLib"));

        hilightButtons();

        addEventLink(WindowClosingEvent.class, this, "dispose");

        rootNodes = new ArrayList<>();
        showTextures = true;
        showMaterials = true;
        rootNodes.add(new SceneTreeNode(null, theScene));
        for (File file : assetsFolder.listFiles()) {
            if (file.isDirectory()) {
                rootNodes.add(new FolderTreeNode(file));
            } else if (file.getName().endsWith(".aoi")) {
                rootNodes.add(new SceneTreeNode(file));
            }
        }
        libraryList.setModel(new SceneTreeModel());
        setSelection(libraryList.getRootNode(), theScene, theScene.getDefaultTexture());
        pack();
        UIUtilities.centerDialog(this, parentFrame.getFrame());

    }

    private void doSelectionChanged() {
        TreePath selection = libraryList.getSelectedNode();
        Texture oldTexture = selectedTexture;
        Material oldMaterial = selectedMaterial;
        selectedTexture = null;
        selectedMaterial = null;
        insertLocation = -1;
        if (selection != null && libraryList.isLeafNode(selection)) {
            TreePath parentNode = libraryList.getParentNode(selection);
            SceneTreeNode sceneNode = (SceneTreeNode) parentNode.getLastPathComponent();
            try {
                selectedSceneNode = sceneNode;
                selectedScene = sceneNode.getScene();
                libraryFile = sceneNode.file;
                Object node = selection.getLastPathComponent();
                if (node instanceof TextureTreeNode) {
                    selectedTexture = selectedScene.getTexture(((TextureTreeNode) node).index);
                    if (selectedTexture != oldTexture) {
                        preview.setTexture(selectedTexture, selectedTexture.getDefaultMapping(preview.getObject().getObject()));
                        preview.setMaterial(null, null);
                        preview.render();
                        setInfoText(Translate.text("textureName") + " " + selectedTexture.getName(), Translate.text("textureType") + " " + selectedTexture.getTypeName());
                    }
                } else {
                    selectedMaterial = selectedScene.getMaterial(((MaterialTreeNode) node).index);
                    if (selectedMaterial != oldMaterial) {
                        Texture tex = UniformTexture.invisibleTexture();
                        preview.setTexture(tex, tex.getDefaultMapping(preview.getObject().getObject()));
                        preview.setMaterial(selectedMaterial, selectedMaterial.getDefaultMapping(preview.getObject().getObject()));
                        preview.render();
                        setInfoText(Translate.text("materialName") + " " + selectedMaterial.getName(), Translate.text("materialType") + " " + selectedMaterial.getTypeName());
                    }
                }
            } catch (IOException ex) {
                MessageDialog.create().withOwner(this.getComponent()).error( Translate.text("errorLoadingFile") + ": " + ex.getLocalizedMessage());
            }
        }
        if (selectedTexture == null && selectedMaterial == null) {
            Texture tex = UniformTexture.invisibleTexture();
            preview.setTexture(tex, tex.getDefaultMapping(preview.getObject().getObject()));
            preview.setMaterial(null, null);
            preview.render();
            setInfoText(Translate.text("noSelection"), "&nbsp;");
        }

        hilightButtons();
    }

    private boolean setSelection(TreePath node, Scene scene, Object object) {
        Object value = node.getLastPathComponent();
        if (value instanceof FolderTreeNode && !libraryList.isNodeExpanded(node)) {
            return false;
        }
        TreeModel model = libraryList.getModel();
        int numChildren = model.getChildCount(value);
        if (value instanceof SceneTreeNode) {
            SceneTreeNode stn = (SceneTreeNode) value;
            if (stn.scene == null || stn.scene.get() != scene) {
                return false;
            }
            for (int i = 0; i < numChildren; i++) {
                Object child = model.getChild(value, i);
                if ((child instanceof TextureTreeNode && scene.getTexture(((TextureTreeNode) child).index) == object)
                        || (child instanceof MaterialTreeNode && scene.getMaterial(((MaterialTreeNode) child).index) == object)) {
                    libraryList.setNodeSelected(node.pathByAddingChild(child), true);
                    doSelectionChanged();
                    return true;
                }
            }
            return false;
        }
        for (int i = 0; i < numChildren; i++) {
            if (setSelection(node.pathByAddingChild(model.getChild(value, i)), scene, object)) {
                return true;
            }
        }
        return false;
    }

    private void hilightButtons() {
        if (selectedTexture == null && selectedMaterial == null) {
            duplicateButton.setEnabled(false);
            deleteButton.setEnabled(false);
            editButton.setEnabled(false);
            loadLibButton.setEnabled(false);
            saveLibButton.setEnabled(false);
            deleteLibButton.setEnabled(false);
        } else {
            hiLight(selectedScene == theScene);
        }
    }

    private void hiLight(boolean h) {
        duplicateButton.setEnabled(h);
        deleteButton.setEnabled(h);
        editButton.setEnabled(h);
        loadLibButton.setEnabled(!h);
        saveLibButton.setEnabled(h);
        deleteLibButton.setEnabled(!h);
    }

    public void mouseClicked(MouseClickedEvent ev) {
        if (ev.getClickCount() == 2) {
            doEdit();
        } else if (ev.getClickCount() == 1) {
            doSelectionChanged();
        }
    }

    @SuppressWarnings("unused")
    private void filterChanged(SelectionChangedEvent ev) {
        if (ev.getWidget() == showTexturesButton) {
            showTextures = true;
            showMaterials = false;
        } else if (ev.getWidget() == showMaterialsButton) {
            showTextures = false;
            showMaterials = true;
        } else {
            showTextures = true;
            showMaterials = true;
        }
        ((SceneTreeModel) libraryList.getModel()).resetFilter();
    }

    @SuppressWarnings("unused")
    public void doNew() {
        int newType = typeChoice.getSelectedIndex() - 1;
        if (newType >= 0) {
            int j = 0;
            String name;
            if (newType >= textureTypes.size()) {
                // A new material

                do {
                    j++;
                    name = "Untitled " + j;
                } while (theScene.getMaterial(name) != null);
                try {
                    Material mat = materialTypes.get(newType - textureTypes.size()).getClass().getDeclaredConstructor().newInstance();
                    mat.setName(name);
                    theScene.addMaterial(mat);
                    mat.edit((WindowWidget)parentFrame.getFrame(), theScene);
                } catch (Exception ex) {
                }
                parentFrame.setModified();
                selectLastCurrentMaterial();
            } else {
                // A new texture

                do {
                    j++;
                    name = "Untitled " + j;
                } while (theScene.getTexture(name) != null);
                try {
                    Texture tex = textureTypes.get(newType).getClass().getDeclaredConstructor().newInstance();
                    tex.setName(name);
                    theScene.addTexture(tex);
                    tex.edit(this, theScene);
                } catch (Exception ex) {
                }
                parentFrame.setModified();
                selectLastCurrentTexture();
            }
            typeChoice.setSelectedIndex(0);
        }
    }

    @SuppressWarnings("unused")
    public void doCopy() {
        if (selectedTexture != null) {
            String name = new BStandardDialog("", Translate.text("newTexName"), BStandardDialog.PLAIN).showInputDialog(this, null, "");
            if (name == null) {
                return;
            }
            Texture tex = selectedTexture.duplicate();
            tex.setName(name);
            theScene.addTexture(tex);
            parentFrame.setModified();
            selectLastCurrentTexture();
        } else if (selectedMaterial != null) {
            String name = new BStandardDialog("", Translate.text("newMatName"), BStandardDialog.PLAIN).showInputDialog(this, null, "");
            if (name == null) {
                return;
            }
            Material mat = selectedMaterial.duplicate();
            mat.setName(name);
            theScene.addMaterial(mat);
            parentFrame.setModified();
            selectLastCurrentMaterial();
        }
    }



    private void doEdit() {
        if (selectedScene != theScene) {
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (selectedTexture != null) {
            Texture tex = selectedTexture;
            tex.edit(this, theScene);
            tex.assignNewID();
            theScene.changeTexture(theScene.indexOf(tex));
            parentFrame.setModified();
        } else if (selectedMaterial != null) {
            Material mat = selectedMaterial;
            mat.edit((WindowWidget)parentFrame.getFrame(), theScene);
            mat.assignNewID();
            theScene.changeMaterial(theScene.indexOf(mat));
            parentFrame.setModified();
        }
        setCursor(Cursor.getDefaultCursor());
    }

    // --
    private void doLoadFromLibrary() {
        if (selectedTexture != null) {
            Texture newTexture = selectedTexture.duplicate();
            theScene.addTexture(newTexture, insertLocation == -1 ? theScene.getNumTextures() : insertLocation);
            parentFrame.setModified();

            selectedScene.getImages().forEach(image -> {
                if(selectedTexture.usesImage(image)) theScene.addImage(image);
            });

            parentFrame.updateImage();
            setSelection(libraryList.getRootNode(), theScene, newTexture);
        } else if (selectedMaterial != null) {
            Material newMaterial = selectedMaterial.duplicate();
            theScene.addMaterial(newMaterial, insertLocation == -1 ? theScene.getNumMaterials() : insertLocation);
            parentFrame.setModified();

            selectedScene.getImages().forEach(image -> {
                if(selectedMaterial.usesImage(image)) theScene.addImage(image);
            });

            parentFrame.updateImage();
            setSelection(libraryList.getRootNode(), theScene, newMaterial);
        }
        hilightButtons();
    }

    @SuppressWarnings("unused")
    public void doSaveToLibrary() {
        String itemText;
        if (selectedTexture == null) {
            itemText = "selectSceneToSaveMaterial";
        } else {
            itemText = "selectSceneToSaveTexture";
        }
        if (selectedTexture != null || selectedMaterial != null) {
            var chooser = new JFileChooser();
            chooser.setName(Translate.text(itemText));
            chooser.setCurrentDirectory(assetsFolder);
            
            if (chooser.showOpenDialog(this.getComponent()) == JFileChooser.APPROVE_OPTION) {
                saveToFile(chooser.getSelectedFile());
            }
        }
    }

    private void saveToFile(File saveFile) {
        if (saveFile.exists()) {
            try {
                Scene saveScene = new Scene(saveFile, true);
                if (selectedTexture != null) {
                    Texture newTexture = selectedTexture.duplicate();
                    saveScene.addTexture(newTexture, insertLocation == -1 ? saveScene.getNumTextures() : insertLocation);
                    selectedScene.getImages().forEach(image -> {
                        if(selectedTexture.usesImage(image)) saveScene.addImage(image);
                    });

                    saveScene.writeToFile(saveFile);
                } else if (selectedMaterial != null) {
                    Material newMaterial = selectedMaterial.duplicate();
                    saveScene.addMaterial(newMaterial, insertLocation == -1 ? saveScene.getNumMaterials() : insertLocation);
                    selectedScene.getImages().forEach(image -> {
                        if(selectedMaterial.usesImage(image)) saveScene.addImage(image);
                    });
                    saveScene.writeToFile(saveFile);
                }
            } catch (IOException ex) {
                log.atError().setCause(ex).log("Error save texture or material: {}", ex.getMessage());
            }
        }
        ((SceneTreeModel) libraryList.getModel()).rebuildScenes(saveFile);
    }

    @SuppressWarnings("unused")
    public void doDelete() {
        if (selectedTexture != null) {
            int choice = MessageDialog.create().withOwner(this.getComponent()).option(Translate.text("deleteTexture", selectedTexture.getName()));
            if (choice == 0) {
                theScene.removeTexture(theScene.indexOf(selectedTexture));
                parentFrame.setModified();
                setSelection(libraryList.getRootNode(), theScene, theScene.getDefaultTexture());
            }
        } else if (selectedMaterial != null) {
            int choice = MessageDialog.create().withOwner(this.getComponent()).option(Translate.text("deleteMaterial", selectedMaterial.getName()));
            if (choice == 0) {
                theScene.removeMaterial(theScene.indexOf(selectedMaterial));
                parentFrame.setModified();
                setSelection(libraryList.getRootNode(), theScene, theScene.getDefaultTexture());
            }
        }
    }

    @SuppressWarnings("unused")
    public void doDeleteFromLibrary() {
        if (selectedScene == null || selectedScene == theScene) {
            return;
        }
        try {
            if (selectedTexture != null) {
                int choice = MessageDialog.create().withOwner(this.getComponent()).option(Translate.text("deleteTexture", selectedTexture.getName()));
                if (choice == 0) {
                    int texIndex = selectedScene.indexOf(selectedTexture);
                    selectedScene.removeTexture(texIndex);
                    selectedScene.writeToFile(libraryFile);
                    ((SceneTreeModel) libraryList.getModel()).rebuildScenes(libraryFile);
                    setSelection(libraryList.getRootNode(), theScene, theScene.getDefaultTexture());
                }
            } else if (selectedMaterial != null) {
                String[] options = new String[]{Translate.text("button.ok"), Translate.text("button.cancel")};
                int choice = MessageDialog.create().withOwner(this.getComponent()).option(Translate.text("deleteMaterial", selectedMaterial.getName()));
                if (choice == 0) {
                    int matIndex = selectedScene.indexOf(selectedMaterial);
                    selectedScene.removeMaterial(matIndex);
                    selectedScene.writeToFile(libraryFile);
                    ((SceneTreeModel) libraryList.getModel()).rebuildScenes(libraryFile);
                    setSelection(libraryList.getRootNode(), theScene, theScene.getDefaultTexture());
                }
            }
        } catch (IOException ex) {
            log.atError().setCause(ex).log("Error delete texture or material: {}", ex.getMessage());
        }
    }

    @SuppressWarnings("unused")
    public void doNewLib() {
        var chooser = new JFileChooser();
        chooser.setName(Translate.text("selectNewLibraryName"));
        chooser.setCurrentDirectory(assetsFolder);
        
        if (chooser.showSaveDialog(this.getComponent()) == JFileChooser.APPROVE_OPTION) {
            File saveFile = chooser.getSelectedFile();
            if (saveFile.exists()) {
                MessageDialog.create().withOwner(this.getComponent()).error(Translate.text("fileAlreadyExists"));
            } else {
                try {
                    new Scene().writeToFile(saveFile);
                } catch (IOException ex) {
                    log.atError().setCause(ex).log("Error create scene: {}", ex.getMessage());
                }
                ((SceneTreeModel) libraryList.getModel()).rebuildLibrary();
            }
        }
    }

    @SuppressWarnings("unused")
    public void doIncludeLib() {
        var chooser = new JFileChooser();
        chooser.setName(Translate.text("selectExternalFile"));
        if (chooser.showOpenDialog(this.getComponent()) == JFileChooser.APPROVE_OPTION) {
            File inputFile = chooser.getSelectedFile();
            if (inputFile.exists()) {
                ((SceneTreeModel) libraryList.getModel()).addScene(inputFile);
            }
        }
    }

    @Override
    public void dispose() {
        theScene.removeMaterialListener(listListener);
        theScene.removeTextureListener(listListener);
        super.dispose();
    }

    private void setInfoText(String line1, String line2) {
        String s = "<html><p>" + line1 + "</p><p>" + line2 + "</p></html>";
        matInfo.setText(s);
    }

    private void selectLastCurrentTexture() {
        TreePath r = libraryList.getRootNode();
        TreePath current = libraryList.getChildNode(r, 0);
        libraryList.setNodeExpanded(current, true);
        int lastIndex = libraryList.getChildNodeCount(current) - theScene.getNumMaterials() - 1;
        libraryList.setNodeSelected(libraryList.getChildNode(current, lastIndex), true);
        doSelectionChanged();
    }

    private void selectLastCurrentMaterial() {
        TreePath r = libraryList.getRootNode();
        TreePath current = libraryList.getChildNode(r, 0);
        libraryList.setNodeExpanded(current, true);
        int lastIndex = libraryList.getChildNodeCount(current) - 1;
        libraryList.setNodeSelected(libraryList.getChildNode(current, lastIndex), true);
        doSelectionChanged();
    }

    private class TextureTreeNode {

        final int index;
        String name;

        TextureTreeNode(SceneTreeNode scene, int index) throws IOException {
            this.index = index;
            name = scene.getScene().getTexture(index).getName();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private class MaterialTreeNode {

        final int index;
        String name;

        MaterialTreeNode(SceneTreeNode scene, int index) throws IOException {
            this.index = index;
            name = scene.getScene().getMaterial(index).getName();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private class SceneTreeNode {

        List<TextureTreeNode> textures;
        List<MaterialTreeNode> materials;
        SoftReference<Scene> scene;
        final File file;

        SceneTreeNode(File file) {
            this.file = file;
        }

        SceneTreeNode(File file, Scene scene) {
            this.file = file;
            this.scene = new SoftReference<>(scene);
        }

        void ensureChildrenValid() {
            if (textures == null) {
                try {
                    Scene theScene = getScene();
                    textures = new ArrayList<>();
                    for (int i = 0; i < theScene.getNumTextures(); i++) {
                        textures.add(new TextureTreeNode(this, i));
                    }
                    materials = new ArrayList<>();
                    for (int i = 0; i < theScene.getNumMaterials(); i++) {
                        materials.add(new MaterialTreeNode(this, i));
                    }
                } catch (IOException ex) {
                    log.atError().setCause(ex).log("Error validate scene: {}", ex.getMessage());
                }
            }
        }

        Scene getScene() throws IOException {
            if (scene != null) {
                Scene theScene = scene.get();
                if (theScene != null) {
                    return theScene;
                }
            }
            Scene theScene = new Scene(file, true);
            scene = new SoftReference<>(theScene);
            return theScene;
        }

        @Override
        public String toString() {
            if (file == null) {
                return Translate.text("currentScene");
            }
            return file.getName().substring(0, file.getName().length() - 4);
        }
    }

    private class FolderTreeNode {

        final File file;
        ArrayList<Object> children;

        FolderTreeNode(File file) {
            this.file = file;
        }

        ArrayList<Object> getChildren() {
            if (children == null) {
                children = new ArrayList<>();
                for (File f : file.listFiles()) {
                    if (f.isDirectory()) {
                        children.add(new FolderTreeNode(f));
                    } else if (f.getName().endsWith(".aoi")) {
                        children.add(new SceneTreeNode(f));
                    }
                }
            }
            return children;
        }

        @Override
        public String toString() {
            return file.getName();
        }
    }

    private class SceneTreeModel implements TreeModel {

        private final List<TreeModelListener> listeners = new ArrayList<>();
        private final DefaultMutableTreeNode root = new DefaultMutableTreeNode();

        @Override
        public void addTreeModelListener(TreeModelListener listener) {
            listeners.add(listener);
        }

        @Override
        public void removeTreeModelListener(TreeModelListener listener) {
            listeners.remove(listener);
        }

        @Override
        public Object getRoot() {
            return root;
        }

        @Override
        public Object getChild(Object o, int i) {
            if (o == root) {
                return rootNodes.get(i);
            }
            if (o instanceof FolderTreeNode) {
                return ((FolderTreeNode) o).getChildren().get(i);
            }
            SceneTreeNode node = (SceneTreeNode) o;
            node.ensureChildrenValid();
            if (showTextures) {
                if (i < node.textures.size()) {
                    return node.textures.get(i);
                }
                i -= node.textures.size();
            }
            return node.materials.get(i);
        }

        @Override
        public int getChildCount(Object o) {
            if (o == root) {
                return rootNodes.size();
            }
            if (o instanceof FolderTreeNode) {
                return ((FolderTreeNode) o).getChildren().size();
            }
            if (!(o instanceof SceneTreeNode)) {
                return 0;
            }
            SceneTreeNode node = (SceneTreeNode) o;
            node.ensureChildrenValid();
            int count = 0;
            if (showTextures) {
                count += node.textures.size();
            }
            if (showMaterials) {
                count += node.materials.size();
            }
            return count;
        }

        @Override
        public boolean isLeaf(Object o) {
            return !(o == root || o instanceof FolderTreeNode || o instanceof SceneTreeNode);
        }

        @Override
        public void valueForPathChanged(TreePath treePath, Object o) {
        }

        @Override
        public int getIndexOfChild(Object o, Object o1) {
            if (o == root) {
                return rootNodes.indexOf(o1);
            }
            if (o instanceof FolderTreeNode) {
                return ((FolderTreeNode) o).getChildren().indexOf(o1);
            }
            SceneTreeNode node = (SceneTreeNode) o;
            node.ensureChildrenValid();
            int texIndex = node.textures.indexOf(o1);
            if (texIndex > -1) {
                return texIndex;
            }
            int matIndex = node.materials.indexOf(o1);
            if (matIndex > -1) {
                return matIndex + (showTextures ? node.textures.size() : 0);
            }
            return -1;
        }

        void rebuildNode(Object node, File file) {
            if (node instanceof SceneTreeNode) {
                SceneTreeNode sct = (SceneTreeNode) node;
                if (file == null || file.equals(sct.file)) {
                    sct.textures = null;
                    sct.materials = null;
                    if (sct.file != null) {
                        sct.scene = null;
                    }
                }
                return;
            }
            if (node instanceof FolderTreeNode && ((FolderTreeNode) node).children == null) {
                return;
            }
            int numChildren = getChildCount(node);
            for (int i = 0; i < numChildren; i++) {
                rebuildNode(getChild(node, i), file);
            }
        }

        void rebuildScenes(final File file) {
            updateTree(() -> rebuildNode(root, file));
        }

        void rebuildLibrary() {
            updateTree(() -> ((FolderTreeNode) rootNodes.get(1)).children = null);
        }

        void resetFilter() {
            updateTree(null);
        }

        void addScene(final File file) {
            updateTree(() -> rootNodes.add(new SceneTreeNode(file)));
        }

        void updateTree(Runnable updater) {
            List<TreePath> expanded = Collections.list(libraryList.getComponent().getExpandedDescendants(libraryList.getRootNode()));
            if (updater != null) {
                updater.run();
            }
            Object selection = (selectedTexture == null ? selectedMaterial : selectedTexture);
            TreeModelEvent ev = new TreeModelEvent(this, new TreePath(root));
            for (TreeModelListener listener : listeners) {
                listener.treeStructureChanged(ev);
            }
            for (TreePath path : expanded) {
                libraryList.setNodeExpanded(path, true);
            }
            if (selection != null) {
                setSelection(new TreePath(root), selectedScene, selection);
            }
        }
    }

    private class DragHandler extends TransferHandler {

        @Override
        public int getSourceActions(JComponent jComponent) {
            return COPY;
        }

        @Override
        public boolean canImport(TransferSupport transferSupport) {
            SceneTreeNode sceneNode = findDropLocation(transferSupport);
            if (sceneNode == null) {
                return false;
            }
            if (sceneNode == selectedSceneNode) {
                int current = -1;
                try {
                    if (selectedTexture != null) {
                        current = selectedSceneNode.getScene().indexOf(selectedTexture);
                    } else if (selectedMaterial != null) {
                        current = selectedSceneNode.getScene().indexOf(selectedMaterial);
                    }
                } catch (IOException ex) {
                    log.atError().setCause(ex).log("Unable to get scene: {}", ex.getMessage());
                    return false;
                }
                if (insertLocation == -1 || insertLocation == current) {
                    return false;
                }
            }
            transferSupport.setShowDropLocation(true);
            return true;
        }

        @Override
        protected Transferable createTransferable(JComponent jComponent) {
            if (selectedTexture != null) {
                return new DragTransferable(selectedTexture);
            }
            if (selectedMaterial != null) {
                return new DragTransferable(selectedMaterial);
            }
            return null;
        }

        @Override
        public boolean importData(TransferSupport transferSupport) {
            SceneTreeNode sceneNode = findDropLocation(transferSupport);
            if (sceneNode == null) {
                return false;
            }
            try {
                Scene destScene = sceneNode.getScene();
                if (sceneNode == selectedSceneNode) {
                    Scene saveScene = (destScene == theScene ? theScene : new Scene(sceneNode.file, true));
                    if (selectedTexture != null) {
                        saveScene.reorderTexture(destScene.indexOf(selectedTexture), insertLocation);
                    } else if (selectedMaterial != null) {
                        saveScene.reorderMaterial(destScene.indexOf(selectedMaterial), insertLocation);
                    }
                    if (destScene != theScene) {
                        saveScene.writeToFile(sceneNode.file);
                    }
                    ((SceneTreeModel) libraryList.getModel()).rebuildScenes(null);
                } else if (destScene == theScene) {
                    doLoadFromLibrary();
                } else {
                    saveToFile(sceneNode.file);
                }
            } catch (IOException ex) {
                log.atError().setCause(ex).log("Error import data: {}", ex.getMessage());
            }
            return true;
        }

        private SceneTreeNode findDropLocation(TransferSupport transferSupport) {
            TreePath location = libraryList.findNode(transferSupport.getDropLocation().getDropPoint());
            if (location == null) {
                return null;
            }
            insertLocation = -1;
            if (selectedTexture != null && location.getLastPathComponent() instanceof TextureTreeNode) {
                insertLocation = ((TextureTreeNode) location.getLastPathComponent()).index;
            }
            if (selectedMaterial != null && location.getLastPathComponent() instanceof MaterialTreeNode) {
                insertLocation = ((MaterialTreeNode) location.getLastPathComponent()).index;
            }
            for (Object node : location.getPath()) {
                if (node instanceof SceneTreeNode) {
                    return (SceneTreeNode) node;
                }
            }
            return null;
        }
    }

    private class DragTransferable implements Transferable {

        private final Object data;
        private final DataFlavor[] flavors;

        DragTransferable(Object data) {
            this.data = data;
            flavors = new DataFlavor[]{DataFlavor.stringFlavor, data instanceof Texture ? TextureFlavor : MaterialFlavor};
        }

        @Override
        public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedFlavorException, IOException {
            if (dataFlavor == DataFlavor.stringFlavor) {
                return data.toString();
            }
            return data;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
            for (DataFlavor flavor : flavors) {
                if (flavor == dataFlavor) {
                    return true;
                }
            }
            return false;
        }
    }
}
