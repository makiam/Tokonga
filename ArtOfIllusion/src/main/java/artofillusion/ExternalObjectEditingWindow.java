/* Copyright (C) 2004-2007 by Peter Eastman
   Changes copyright (C) 2017-2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.object.*;
import artofillusion.ui.*;
import buoy.widget.*;
import buoy.event.*;

import java.awt.*;
import java.io.*;
import javax.swing.JFileChooser;

/**
 * This is a window for editing ExternalObjects.
 */
public class ExternalObjectEditingWindow extends BDialog {

    private final EditingWindow parentWindow;
    private final ExternalObject theObject;
    private final ObjectInfo info;
    private Scene scene;
    private final BTextField fileField;
    private final TreeList itemTree;
    private final BButton okButton;
    private final BCheckBox includeChildrenBox;
    private String objectName;
    private int objectId;
    private final Runnable onClose;

    /**
     * Display a window for editing an ExternalObject.
     *
     * @param parent the parent window
     * @param obj the object to edit
     * @param info the ObjectInfo for the ExternalObject
     * @param onClose a callback to invoke when the user clicks OK (may be null)
     */
    public ExternalObjectEditingWindow(EditingWindow parent, ExternalObject obj, ObjectInfo info, Runnable onClose) {
        super(parent.getFrame(), info.getName(), true);
        parentWindow = parent;
        theObject = obj;
        this.info = info;
        this.onClose = onClose;
        objectName = obj.getExternalObjectName();
        objectId = obj.getExternalObjectId();
        FormContainer content = new FormContainer(new double[]{0, 1, 0, 0}, new double[]{0, 1, 0, 0});
        setContent(BOutline.createEmptyBorder(content, UIUtilities.getStandardDialogInsets()));
        content.setDefaultLayout(new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.HORIZONTAL, new Insets(2, 2, 2, 2), null));
        LayoutInfo labelLayout = new LayoutInfo(LayoutInfo.EAST, LayoutInfo.NONE, new Insets(2, 2, 2, 2), null);
        content.add(Translate.label("externalObject.sceneFile"), 0, 0, labelLayout);
        content.add(fileField = new BTextField(theObject.getExternalSceneFile().getAbsolutePath(), 30), 1, 0);
        content.add(Translate.button("browse", event -> doBrowseFile()), 2, 0);
        fileField.setEditable(false);
        itemTree = new TreeList(parentWindow);
        itemTree.setPreferredSize(new Dimension(130, 100));
        itemTree.setAllowMultiple(false);
        itemTree.addEventLink(SelectionChangedEvent.class, this, "selectionChanged");
        BScrollPane itemTreeScroller = new BScrollPane(itemTree);
        itemTreeScroller.setForceWidth(true);
        itemTreeScroller.setForceHeight(true);
        itemTreeScroller.getVerticalScrollBar().setUnitIncrement(10);
        content.add(itemTreeScroller, 0, 1, 3, 1);
        includeChildrenBox = new BCheckBox(Translate.text("externalObject.includeChildren"), obj.getIncludeChildren());
        content.add(includeChildrenBox, 0, 2, 3, 1);
        RowContainer buttons = new RowContainer();
        content.add(buttons, 0, 3, 3, 1, new LayoutInfo());
        buttons.add(okButton = Translate.button("ok", event -> doOk()));
        buttons.add(Translate.button("cancel", event -> dispose()));
        loadExternalScene();
        buildObjectTree();
        selectionChanged();
        pack();
        UIUtilities.centerDialog(this, parentWindow.getFrame());
        setVisible(true);
    }

    /**
     * Allow the user to select a file.
     */
    private void doBrowseFile() {
        var chooser = new JFileChooser();
        chooser.setName(Translate.text("externalObject.selectScene"));

        File f = theObject.getExternalSceneFile();
        if (f.isFile()) {
            chooser.setSelectedFile(f);
        }
        if (chooser.showOpenDialog(this.getComponent()) == JFileChooser.APPROVE_OPTION) {
            fileField.setText(chooser.getSelectedFile().getAbsolutePath());
            loadExternalScene();
            buildObjectTree();
            selectionChanged();
        }
    }

    /**
     * Load the external scene file.
     */
    private void loadExternalScene() {
        File f = new File(fileField.getText());
        scene = null;
        if (!f.isFile()) {
            new BStandardDialog("", UIUtilities.breakString(Translate.text("externalObject.sceneNotFound",
                    theObject.getExternalSceneFile().getAbsolutePath())), BStandardDialog.ERROR).showMessageDialog(this);
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            scene = new Scene(f, true);
        } catch (InvalidObjectException ex) {
            new BStandardDialog("", UIUtilities.breakString(Translate.text("errorLoadingWholeScene")), BStandardDialog.ERROR).showMessageDialog(this);
        } catch (IOException ex) {
            new BStandardDialog("", new String[]{Translate.text("errorLoadingFile"), ex.getMessage() == null ? "" : ex.getMessage()}, BStandardDialog.ERROR).showMessageDialog(this);
        }
        setCursor(Cursor.getDefaultCursor());

    }

    /**
     * Build the list of objects for the user to select from.
     */
    private void buildObjectTree() {
        itemTree.removeAllElements();
        if (scene == null) {
            return;
        }
        itemTree.setUpdateEnabled(false);

        for (ObjectInfo item : scene.getObjects()) {
            if (item.getParent() == null) {
                itemTree.addElement(new ObjectTreeElement(item, itemTree));
            }
        }

        itemTree.setUpdateEnabled(true);
        ObjectInfo oldSelection = scene.getObjectById(objectId);
        if (oldSelection == null || !oldSelection.getName().equals(objectName)) {
            oldSelection = scene.getObject(objectName);
        }
        if (oldSelection != null) {
            itemTree.setSelected(oldSelection, true);
            itemTree.expandToShowObject(oldSelection);
        }
    }

    /**
     * This is called when the selection in the tree is changed.
     */
    private void selectionChanged() {
        Object[] sel = itemTree.getSelectedObjects();
        if (sel.length == 0) {
            okButton.setEnabled(false);
        } else {
            okButton.setEnabled(true);
            ObjectInfo selected = (ObjectInfo) sel[0];
            objectName = selected.getName();
            objectId = selected.getId();
        }
    }

    /**
     * Save the changes and reload the object.
     */
    private void doOk() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        theObject.setExternalObjectName(objectName);
        theObject.setExternalObjectId(objectId);
        theObject.setExternalSceneFile(new File(fileField.getText()));
        theObject.setIncludeChildren(includeChildrenBox.getState());
        theObject.reloadObject();
        if (theObject.getLoadingError() != null) {
            new BStandardDialog("", UIUtilities.breakString(Translate.text("externalObject.loadingError", theObject.getLoadingError())), BStandardDialog.ERROR).showMessageDialog(this);
        }
        info.clearCachedMeshes();
        theObject.sceneChanged(info, parentWindow.getScene());
        dispose();
        if (onClose != null) {
            onClose.run();
        }
        parentWindow.updateImage();
        parentWindow.updateMenus();
    }
}
