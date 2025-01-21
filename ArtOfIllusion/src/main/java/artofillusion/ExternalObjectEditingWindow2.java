/* Copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.object.ExternalObject;
import artofillusion.object.ObjectInfo;
import artofillusion.ui.EditingWindow;
import artofillusion.ui.MessageDialog;
import artofillusion.ui.Translate;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import artofillusion.ui.UIUtilities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.SubscriberExceptionEvent;

@Slf4j
public final class ExternalObjectEditingWindow2 extends JDialog {
    
    private final  ExternalObject obj;
    private final ObjectInfo info;
    private final Runnable closeCallback;

    private File externalFile;
    private int objectId;

    /**
     * A return status code - returned if Cancel button has been pressed
     */
    public static final int RET_CANCEL = 0;
    /**
     * A return status code - returned if OK button has been pressed
     */
    public static final int RET_OK = 1;

    /**
     * Creates new form ExternalObjectEditingWindow2
     */
    public ExternalObjectEditingWindow2(EditingWindow parent, ExternalObject obj, ObjectInfo info, Runnable closeCallback) {
        super(parent.getFrame().getComponent(), true);
        org.greenrobot.eventbus.EventBus.getDefault().register(this);

        this.info = info;
        this.obj = obj;
        this.closeCallback = closeCallback;

        externalFile = obj.getExternalSceneFile();
        objectId = obj.getExternalObjectId();
        log.atDebug().log("ObjectId: {}", objectId);

        initComponents();
        //this.sceneTree.setModel(null);

        // Close the dialog when Esc is pressed
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener action = e -> doClose(RET_CANCEL);
        this.getRootPane().registerKeyboardAction(action, escape, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        
        runSceneTreeLoad(externalFile);



    }

    /**
     * @return the return status of this dialog - one of RET_OK or RET_CANCEL
     */
    public int getReturnStatus() {
        return returnStatus;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        includeChild = new javax.swing.JCheckBox();
        javax.swing.JLabel pathLabel = new javax.swing.JLabel();
        sourcePathField = new javax.swing.JTextField();
        javax.swing.JButton chooserButton = new javax.swing.JButton();
        javax.swing.JScrollPane sceneTreeScroll = new javax.swing.JScrollPane();
        sceneTree = new javax.swing.JTree();

        setTitle(info.getName());
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        okButton.setText(Translate.text("button.ok"));
        okButton.setEnabled(false);
        okButton.addActionListener(this::okButtonActionPerformed);

        cancelButton.setText(Translate.text("button.cancel")
        );
        cancelButton.addActionListener(this::cancelButtonActionPerformed);

        includeChild.setSelected(obj.getIncludeChildren());
        includeChild.setText(Translate.text("externalObject.includeChildren"));

        pathLabel.setLabelFor(sourcePathField);
        pathLabel.setText(Translate.text("externalObject.sceneFile"));

        sourcePathField.setText(obj.getExternalSceneFile().getAbsolutePath());
        sourcePathField.setEnabled(false);

        chooserButton.setText(Translate.text("button.browse"));
        chooserButton.addActionListener(this::chooserButtonActionPerformed);

        sceneTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sceneTreeMouseClicked(evt);
            }
        });
        sceneTree.addTreeSelectionListener(this::sceneTreeValueChanged);
        sceneTreeScroll.setViewportView(sceneTree);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(sceneTreeScroll)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 248, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(pathLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sourcePathField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chooserButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(includeChild)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pathLabel)
                    .addComponent(sourcePathField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chooserButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(sceneTreeScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(includeChild)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );

        getRootPane().setDefaultButton(okButton);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        obj.setExternalObjectId(objectId);
        obj.setExternalSceneFile(externalFile);
        obj.setIncludeChildren(this.includeChild.isSelected());

        Optional.ofNullable(closeCallback).ifPresent(action -> action.run());
        doClose(RET_OK);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose(RET_CANCEL);
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Closes the dialog
     */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(RET_CANCEL);
    }//GEN-LAST:event_closeDialog

    private void chooserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooserButtonActionPerformed
        // TODO add your handling code here:
        var chooser = new JFileChooser();
        chooser.setName(Translate.text("externalObject.selectScene"));
        
        File f = obj.getExternalSceneFile();
        if (f.isFile()) {
            chooser.setSelectedFile(f);
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            var cf = chooser.getSelectedFile().getAbsolutePath();
            if(cf.equals(sourcePathField.getText())) return;

            this.objectId = 0;
            this.externalFile = chooser.getSelectedFile();
            runSceneTreeLoad(externalFile);
        }

    }//GEN-LAST:event_chooserButtonActionPerformed

    private void runSceneTreeLoad(File file) {
        CompletableFuture<Void> task = CompletableFuture.runAsync(new SceneTreeBuilder(this, file));
        task.exceptionally(tx -> {
            var cause = tx.getCause();
            var root = new DefaultMutableTreeNode("Invalid Scene: " + obj.getExternalSceneFile());
            var model = new DefaultTreeModel(root, false);
            this.sceneTree.setModel(model);
            if(cause instanceof InvalidObjectException) {
                MessageDialog.create().withOwner(this).error(UIUtilities.breakString(Translate.text("errorLoadingWholeScene")));
                return null;
            }
            if(cause instanceof IOException) {
                MessageDialog.create().withOwner(this).error(new String[]{Translate.text("errorLoadingFile"), cause.getMessage() == null ? "" : cause.getMessage()});
                return null;
            }

            return null;
        });
    }
    
    
    private void sceneTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_sceneTreeValueChanged
        // TODO add your handling code here:
        //log.info("Event: {} {} {} {}", evt, evt.isAddedPath(), evt.getNewLeadSelectionPath(), evt.getOldLeadSelectionPath());
        var path = evt.getNewLeadSelectionPath();
        if (path != null && path.getPathCount() == 1) {
            sceneTree.clearSelection();
        }
        okButton.setEnabled(sceneTree.getSelectionCount() != 0);
        var selected = (SceneItemNode)sceneTree.getLastSelectedPathComponent();
        if(selected == null) return;
        objectId = selected.getUserObject().getId();
        log.info("Selected: {}", selected);


    }//GEN-LAST:event_sceneTreeValueChanged

    private void sceneTreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sceneTreeMouseClicked

        var near = sceneTree.getClosestPathForLocation(evt.getX(), evt.getY());

        log.atInfo().log("Path {}", near);
    }//GEN-LAST:event_sceneTreeMouseClicked
    
    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

    @Subscribe
    public void onSceneModelTreeBuildErrorEvent(SubscriberExceptionEvent event) {
        log.atError().setCause(event.throwable).log("Error building scene tree");
    }

    @Subscribe
    public void onSceneModelTreeBuildEvent(SceneModelTreeBuildEvent event) {
        sourcePathField.setText(event.path.getAbsolutePath());
        DefaultTreeModel model = event.getModel();
        sceneTree.setModel(model);
        if(objectId == 0) return;

        var nodes = Collections.list(((DefaultMutableTreeNode)model.getRoot()).breadthFirstEnumeration());

        nodes.forEach(node -> {
            if( node instanceof SceneItemNode) {
                var sn = (SceneItemNode) node;
                if(sn.getUserObject().getId() == objectId) {
                    sceneTree.setSelectionPath(new TreePath(sn.getPath()));
                }
            }
        });

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox includeChild;
    private javax.swing.JButton okButton;
    private javax.swing.JTree sceneTree;
    private javax.swing.JTextField sourcePathField;
    // End of variables declaration//GEN-END:variables

    private int returnStatus = RET_CANCEL;

    @AllArgsConstructor
    @Data
    private class SceneModelTreeBuildEvent {
        private ExternalObjectEditingWindow2 owner;
        private File path;
        private DefaultTreeModel model;
    }

    private class SceneTreeBuilder implements Runnable {

        private final File path;
        private final ExternalObjectEditingWindow2 owner;


        public SceneTreeBuilder(ExternalObjectEditingWindow2 owner, File path) {
            this.owner = owner;
            this.path = path;
        }

        @SneakyThrows
        @Override
        public void run() {
            var root = new DefaultMutableTreeNode("Scene: " + path);
            var model = new DefaultTreeModel(root, true);

            var scene = new Scene(path, true);
            scene.getObjects().stream().filter(item -> item.getParent() == null).forEach(item -> {
                var sNode = new SceneItemNode(item);
                root.add(sNode);
            });

            EventBus.getDefault().post(new SceneModelTreeBuildEvent(owner, path, model));
        }
    }

    private class SceneItemNode extends DefaultMutableTreeNode {

        public SceneItemNode(ObjectInfo userObject) {
            super(userObject, true);
            ObjectInfo[] items = userObject.getChildren();
            if(items.length == 0) this.allowsChildren = false;
            Arrays.stream(items).forEach(item -> {
                this.add(new SceneItemNode(item));
            });

        }

        @Override
        public ObjectInfo getUserObject() {
            return (ObjectInfo)super.getUserObject();
        }

        @Override
        public String toString() {
            return this.getUserObject().getName();
        }
        
        
    }
}
