/*
 *  Copyright 2004 Francois Guillet
 *  Changes copyright 2022-2023 by Maksim Khramov
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package artofillusion.spmanager;

import artofillusion.ui.Translate;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import javax.swing.*;
import javax.swing.tree.*;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Description of the Class
 *
 * @author pims
 * @created 13 mars 2004
 */
@Slf4j
public class SPMSplitPane extends BSplitPane {

    /**
     * Description of the Field
     */
    protected BTree tree;
    /**
     * Description of the Field
     */
    protected BTextArea objectName;
    /**
     * Description of the Field
     */
    protected BTextArea objectDescription;
    /**
     * Description of the Field
     */
    protected RowContainer buttonRow;

    /**
     * Description of the Field
     */
    protected TreePath pluginsPath;
    /**
     * Description of the Field
     */
    protected TreePath scriptsPath;
    /**
     * Description of the Field
     */
    protected TreePath toolScriptsPath;
    /**
     * Description of the Field
     */
    protected TreePath objectScriptsPath;
    /**
     * Description of the Field
     */
    protected TreePath startupScriptsPath;

    /**
     * Description of the Field
     */
    protected final short workMode;
    /**
     * Description of the Field
     */
    protected SPMFileSystem fs;

    /**
     * Description of the Field
     */
    protected static List<SPMSplitPane> splitPaneList;

    /**
     * Description of the Field
     * -- GETTER --
     *  Gets the modified attribute of the InstallSplitPane object
     *
     * @return The modified value

     */
    @Getter
    protected boolean modified;

    /**
     * Description of the Field
     */
    protected boolean acceptsFileSelection = true;

    /**
     * map of externals being processed - for loop detection
     */
    protected Map<SPMObjectInfo, SPMObjectInfo> extMap;
    protected Map<String, TreePath> pathMap;

    private BScrollPane descriptionSP;
    private BComboBox descSelect;

    private List<String> descText;

    /**
     * Description of the Field
     */
    public final static short BROWSE = 0;
    /**
     * Description of the Field
     */
    public final static short UPDATE = 1;
    /**
     * Description of the Field
     */
    public final static short INSTALL = 2;

    protected static Icon checkedIcon;
    protected static Icon uncheckedIcon;
    protected static Icon referedIcon;
    protected static Icon disableIcon;
    protected static Icon infoIcon;
    protected static Icon confirmIcon;
    protected static Icon flagIcon;
    protected static Icon alertIcon;

    /**
     * Constructor for the SPMSplitPane object
     *
     * @param s Description of the Parameter
     */
    public SPMSplitPane(String s) {
        super(BSplitPane.HORIZONTAL);
        workMode = BROWSE;
        initialize(s);
    }

    /**
     * Constructor for the SPMSplitPane object
     *
     * @param s Description of the Parameter
     * @param wm Description of the Parameter
     */
    public SPMSplitPane(String s, short wm) {
        super(BSplitPane.HORIZONTAL);
        workMode = wm;
        initialize(s);
    }

    /**
     * Gets the localized fullName attribute of the SPMObjectInfo object with name, author, version and size data
     *
     * @return The fullName value
     */
    protected static String getFullName(SPMObjectInfo info) {
        String betaString = "";
        if (info.getBeta() > -1) {
            betaString = "b" + info.getBeta();
        }
        String addFiles = "";
        long kbsize;
        String[] files = info.getFiles();
        if (info.files != null) {
            addFiles = " (" + SPMTranslate.text("additionalFiles") + " ";
            for (int i = 0; i < files.length; ++i) {
                kbsize = Math.round(info.getFileSizes()[i] / 1000);
                if (kbsize < 1) {
                    kbsize = 1;
                }
                addFiles = addFiles + files[i] + " " + kbsize + "kb";
                if (i != files.length - 1) {
                    addFiles = addFiles + ",";
                } else {
                    addFiles = addFiles + ")";
                }
            }
        }
        kbsize = Math.round(info.getLength() / 1000);
        if (kbsize < 1) {
            kbsize = 1;
        }
        return Translate.text("spmanager:text.fullname", info.getName(), info.getAuthor(), info.getVersion() + betaString, info.getDate(), kbsize) + addFiles;
    }

    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     */
    private void initialize(String s) {
        uncheckedIcon = UIManager.getDefaults().getIcon("Tree.leafIcon");
        if (uncheckedIcon == null) {
            uncheckedIcon = new ImageIcon(getClass().getResource("/artofillusion/spmanager/icons/file.png"));
        }

        checkedIcon = new OverlayIcon(uncheckedIcon, new ImageIcon(getClass().getResource("/artofillusion/spmanager/icons/checkCircle.png")));

        referedIcon = new ImageIcon(getClass().getResource("/artofillusion/spmanager/icons/Import16.gif"));

        disableIcon = new OverlayIcon(uncheckedIcon, new ImageIcon(getClass().getResource("/artofillusion/spmanager/icons/disabled.png")));

        infoIcon = new OverlayIcon(uncheckedIcon, new ImageIcon(getClass().getResource("/artofillusion/spmanager/icons/info.png")));

        confirmIcon = new OverlayIcon(uncheckedIcon, new ImageIcon(getClass().getResource("/artofillusion/spmanager/icons/confirm.png")));

        flagIcon = new OverlayIcon(uncheckedIcon, new ImageIcon(getClass().getResource("/artofillusion/spmanager/icons/flag.png")));

        alertIcon = new OverlayIcon(uncheckedIcon, new ImageIcon(getClass().getResource("/artofillusion/spmanager/icons/alert.png")));

        if (splitPaneList == null) {
            splitPaneList = new Vector<>();
        }
        splitPaneList.add(this);

        ColumnContainer cc = new ColumnContainer();

        add(new BScrollPane(tree = new BTree(new DefaultMutableTreeNode(SPMTranslate.text(s)))), 0);

        add(cc, 1);

        LayoutInfo labelLayout = new LayoutInfo(LayoutInfo.WEST, LayoutInfo.NONE, new Insets(3, 3, 3, 3), new Dimension(0, 0));
        LayoutInfo textAreaLayout = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.HORIZONTAL, new Insets(3, 3, 3, 3), new Dimension(0, 0));
        cc.add(SPMTranslate.bLabel("name"), labelLayout);
        objectName = new BTextArea("", 3, 50);
        objectName.setWrapStyle(BTextArea.WRAP_WORD);
        objectName.setFont(objectName.getFont().deriveFont(12f));
        BScrollPane nameSP;
        cc.add(BOutline.createEtchedBorder(nameSP = new BScrollPane(objectName, BScrollPane.SCROLLBAR_NEVER, BScrollPane.SCROLLBAR_ALWAYS), true),
                textAreaLayout);
        nameSP.setForceWidth(true);

        // NTJ: added change-log pull-down
        descSelect = new BComboBox();
        descSelect.add(SPMTranslate.text("description"));
        descSelect.addEventLink(ValueChangedEvent.class, new Object() {
            void processEvent() {
                int index = descSelect.getSelectedIndex();
                if (descText != null && index < descText.size()) {
                    objectDescription.setText(descText.get(index));

                    SwingUtilities.invokeLater(() -> {
                        BScrollBar bar = descriptionSP.getVerticalScrollBar();
                        bar.setValue(bar.getMinimum());
                    });
                }
            }
        });

        RowContainer rc = new RowContainer();
        cc.add(rc, labelLayout);

        rc.add(descSelect, labelLayout);

        rc.add(new BLabel(SPMTranslate.text("flags") + ":"));

        rc.add(new BLabel("=" + SPMTranslate.text("alertFlag"), alertIcon, BLabel.CENTER, BLabel.EAST));
        rc.add(new BLabel("=" + SPMTranslate.text("filtMark"), flagIcon, BLabel.CENTER, BLabel.EAST));
        rc.add(new BLabel("=" + SPMTranslate.text("filtDisable"), disableIcon, BLabel.CENTER, BLabel.EAST));
        rc.add(new BLabel("=" + SPMTranslate.text("filtConfirm"), confirmIcon, BLabel.CENTER, BLabel.EAST));
        rc.add(new BLabel("=" + SPMTranslate.text("required"), referedIcon, BLabel.CENTER, BLabel.EAST));

        objectDescription = new BTextArea("", 8, 50);
        objectDescription.setWrapStyle(BTextArea.WRAP_WORD);
        objectDescription.setFont(objectDescription.getFont().deriveFont(12f));
        cc.add(BOutline.createEtchedBorder(descriptionSP = new BScrollPane(objectDescription, BScrollPane.SCROLLBAR_NEVER, BScrollPane.SCROLLBAR_ALWAYS), true), textAreaLayout);
        descriptionSP.setForceWidth(true);

        buttonRow = new RowContainer();
        LayoutInfo buttonRowLayout = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE, new Insets(3, 3, 7, 3), new Dimension(0, 0));
        cc.add(buttonRow, buttonRowLayout);
        objectName.setEditable(false);
        objectDescription.setEditable(false);

        tree.getComponent().putClientProperty("JTree.lineStyle", "Angled");
        tree.setCellRenderer(new SPMTreeRenderer());
        tree.addEventLink(SelectionChangedEvent.class, this, "doTreeNodeSelection");
        tree.setMultipleSelectionEnabled(false);

        //initialise the tree nodes
        pluginsPath = tree.addNode(tree.getRootNode(), new DefaultMutableTreeNode(SPMTranslate.text("plugins")));
        scriptsPath = tree.addNode(tree.getRootNode(), new DefaultMutableTreeNode(SPMTranslate.text("scripts")));
        toolScriptsPath = tree.addNode(scriptsPath, new DefaultMutableTreeNode(SPMTranslate.text("tools")));
        objectScriptsPath = tree.addNode(scriptsPath, new DefaultMutableTreeNode(SPMTranslate.text("objects")));
        startupScriptsPath = tree.addNode(scriptsPath, new DefaultMutableTreeNode(SPMTranslate.text("startup")));
        ((DefaultTreeModel) tree.getModel()).reload();
        setOneTouchExpandable(true);
        MouseListener ml = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int selRow = tree.getComponent().getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getComponent().getPathForLocation(e.getX(), e.getY());
                if (selRow != -1) {
                    if (e.getClickCount() == 2) {
                        treeDoubleClick(selRow, selPath);
                    }
                }
            }
        };
        tree.getComponent().addMouseListener(ml);

        pathMap = new Hashtable<>(8);
        pathMap.put("plugin", pluginsPath);
        pathMap.put("library", pluginsPath);
        pathMap.put("script", scriptsPath);
    }

    /**
     * Description of the Method
     */
    protected void updateTree() {
    }

    /**
     * Description of the Method
     */
    public void doTreeNodeSelection() {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getComponent().getLastSelectedPathComponent();
        if (node == null) return;

        if (node.isLeaf() && (!node.getAllowsChildren())) {
            if (node.getUserObject() != null) {
                SPMObjectInfo nodeInfo = (SPMObjectInfo) node.getUserObject();
                displayObjectInfo(nodeInfo);

                if (nodeInfo.getFileName().endsWith(".bsh")) {
                    scriptSelection(nodeInfo.deletable);
                } else {
                    pluginSelection(nodeInfo.deletable);
                }
            }
        } else {
            voidSelection();
        }
    }

    /**
     * Gets the selectedNodeInfo attribute of the SPMSplitPane object
     *
     * @return The selectedNodeInfo value
     */
    public SPMObjectInfo getSelectedNodeInfo() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getComponent().getLastSelectedPathComponent();
        if (node == null) return null;
        if (node.isLeaf() && (!node.getAllowsChildren())) {
            if (node.getUserObject() != null) {
                return (SPMObjectInfo) node.getUserObject();
            }
        } else {
            return null;
        }
        return null;
    }

    /**
     * Description of the Method
     *
     * @param info Description of the Parameter
     */
    private void displayObjectInfo(SPMObjectInfo info) {
        objectName.setBackground(Color.WHITE);

        if (info == null) {
            objectName.setText("");
            objectDescription.setText("");

            descSelect.removeAll();
            descSelect.add(SPMTranslate.text("description"));
        } else {
            objectName.setBackground(Color.WHITE);

            descText = info.getDetails();

            String name = getFullName(info);

            if (info.refcount > 0) {
                name += "\n\nRequired by " + info.refcount + " other(s).";
            }

            String extList = "\n";
            boolean missing = false;
            Collection<String> externals = info.getExternals();
            if (externals != null) {
                for (String ext : externals) {
                    if (ext.endsWith("= required")) {
                        String extName = ext.substring(0, ext.indexOf(':'));
                        String extType = ext.substring(ext.indexOf(':') + 1, ext.indexOf('=')).trim();

                        if (getInfo(extName, pathMap.get(extType)) == null) {

                            //TODO check for externals with pathnames
                            ManageSplitPane man = null;
                            for (int j = splitPaneList.size() - 1; j >= 0; j--) {
                                if (splitPaneList.get(j) instanceof ManageSplitPane) {
                                    man = (ManageSplitPane) splitPaneList.get(j);
                                    break;
                                }
                            }

                            if (man.getInfo(extName, man.pluginsPath) == null) {
                                missing = true;
                                ext += " **Not Available**";
                            }
                        }
                    }

                    extList += "\n-External " + ext;
                }

            if (missing) {
                name += "\n" + SPMTranslate.text("missingFile", SPMTranslate.text("otherFiles"));

                objectName.setBackground(Color.PINK);
            }

            if (info.invalid) {
                name += "\n" + SPMTranslate.text("failedRequirement", SPMTranslate.text("flags"));

                objectName.setBackground(Color.PINK);
            }

            info.setLog(SPMTranslate.text("otherFiles"), extList, 2);
            }

            objectName.setText(name);

            if (descText != null && descText.size() > 0) {
                objectDescription.setText(descText.get(0));
            } else {
                objectDescription.setText("");
            }

            List<String> changeLog = info.getChangeLog();
            if (changeLog == null) {
                descSelect.removeAll();
                descSelect.add(SPMTranslate.text("description"));
                descSelect.add(SPMTranslate.text("history"));
            } else {
                descSelect.setContents(changeLog);
            }
        }

        descriptionSP.layoutChildren();

        SwingUtilities.invokeLater(() -> {
            BScrollBar bar = descriptionSP.getVerticalScrollBar();
            bar.setValue(bar.getMinimum());
        });
    }

    protected final Predicate<String> required = (String name) -> name.endsWith("= requred");

    /**
     * get the info for the named item of the named type
     */
    public SPMObjectInfo getInfo(String name, String type) {
        return getInfo(name, pathMap.get(type));
    }

    /**
     * get the info for named item
     */
    public SPMObjectInfo getInfo(String name, TreePath path) {
        if (path == null) {
            log.atInfo().log("SPManager: poor XML content: invalid external type ({})", name);
            return null;
        }

        int max = tree.getChildNodeCount(path);
        for (int j = 0; j < max; j++) {
            Object info = ((DefaultMutableTreeNode) tree.getChildNode(path, j).getLastPathComponent()).getUserObject();

            if (name.equals(info.toString())) {
                return (SPMObjectInfo) info;
            }
        }

        return null;
    }

    /**
     * Description of the Method
     *
     * @param deletable Description of the Parameter
     */
    public void scriptSelection(boolean deletable) {
        layoutChildren();
    }

    /**
     * Description of the Method
     *
     * @param deletable Description of the Parameter
     */
    public void pluginSelection(boolean deletable) {
        layoutChildren();
    }

    /**
     * Description of the Method
     */
    public void voidSelection() {
        displayObjectInfo(null);
        layoutChildren();
    }

    /**
     * Description of the Method
     */
    public void doSetup() {
    }

    /**
     * Description of the Method
     */
    public void doUpdate() {
        clearTree();
        updateTree();
        layoutChildren();
        voidSelection();
    }

    /**
     * Gets the manager attribute of the SPMSplitPane object
     *
     * @return The manager value
     */
    public SPMSplitPane getManager() {
        return splitPaneList.stream().filter(pane -> pane.workMode == BROWSE).findFirst().orElse(null);
    }

    /**
     * Gets the fileSystem attribute of the SPMSplitPane object
     *
     * @return The fileSystem value
     */
    public SPMFileSystem getFileSystem() {
        return fs;
    }

    /**
     * Description of the Method
     */
    protected void clearTree() {
        clearPath(pluginsPath);
        clearPath(toolScriptsPath);
        clearPath(objectScriptsPath);
        clearPath(startupScriptsPath);
    }

    /**
     * Description of the Method
     *
     * @param path Description of the Parameter
     */
    private void clearPath(TreePath path) {
        int count = tree.getChildNodeCount(path);
        if (count > 0) {
            for (int j = count - 1; j >= 0; --j) {
                tree.removeNode(tree.getChildNode(path, j));
            }
        }
        ((DefaultTreeModel) tree.getModel()).reload();

    }

    /**
     * Description of the Method
     *
     * @param selRow Description of the Parameter
     * @param selPath Description of the Parameter
     */
    public void treeDoubleClick(int selRow, TreePath selPath) {
        if (!acceptsFileSelection) {
            return;
        }
        SPMObjectInfo nodeInfo = getSelectedNodeInfo();
        if (nodeInfo != null) {
            nodeInfo.setSelected(!nodeInfo.isSelected());
            notifyObjectInfoSelection(nodeInfo);
        }
        repaint();
    }

    /**
     * Description of the Method
     *
     * @param info Description of the Parameter
     */
    protected void notifyObjectInfoSelection(SPMObjectInfo info) {
        if (extMap != null) {
            extMap.clear();
        }
        selectExternals(info);
    }

    /**
     * set externals to be consistent with this info
     */
    protected void selectExternals(SPMObjectInfo info) {
        if (extMap == null) {
            extMap = new Hashtable<>(32);
        }

        if (extMap.containsKey(info)) {
            log.atInfo().log("SPMSplitPane: dependency loop detected: {}", info.getName());
            return;
        }

        extMap.put(info, info);

        info.getExternals().stream().peek(ex -> System.out.println("Peeked external: " + ex)).filter(required).forEach(extName -> {
            String extType = extName.substring(extName.indexOf(':') + 1, extName.indexOf('=')).trim();
            extName = extName.substring(0, extName.indexOf(':'));
            SPMObjectInfo ext = getInfo(extName, pathMap.get(extType));
            if (ext == null) return;
            if (info.isSelected()) {
                ext.refcount++;
            } else {
                ext.refcount--;
            }

            selectExternals(ext);
        });
    }

    /**
     * The tree renderer for the update & install tree
     *
     * @author Francois Guillet
     * @created 01 july 2004
     */
    private class SPMTreeRenderer extends DefaultTreeCellRenderer {

        /**
         * Gets a treeCellRendererComponent
         *
         * @param tree Description of the Parameter
         * @param value Description of the Parameter
         * @param sel Description of the Parameter
         * @param expanded Description of the Parameter
         * @param leaf Description of the Parameter
         * @param row Description of the Parameter
         * @param hasFocus Description of the Parameter
         * @return The treeCellRendererComponent value
         */
        @Override
        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

            super.getTreeCellRendererComponent(
                    tree, value, sel,
                    expanded, leaf, row,
                    hasFocus);

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

            if (node.getUserObject() instanceof SPMObjectInfo) {
                SPMObjectInfo nodeInfo = (SPMObjectInfo) node.getUserObject();

                // if other items refer to this, disable it
                if (nodeInfo.refcount > 0) {
                    setIcon(referedIcon);
                } else if (workMode != BROWSE && nodeInfo.restriction == SPMParameters.DISABLE) {
                    setIcon(disableIcon);
                } else if (nodeInfo.invalid) {
                    setIcon(alertIcon);
                } else if (nodeInfo.isSelected()) {
                    setIcon(checkedIcon);
                } else if (nodeInfo.restriction == SPMParameters.MARK) {
                    setIcon(flagIcon);
                } else if (nodeInfo.restriction == SPMParameters.CONFIRM) {
                    setIcon(confirmIcon);
                } else {
                    if (getIcon() == null) {
                        setIcon(uncheckedIcon);
                    }
                }

            }
            return this;
        }

    }

    /**
     * This class superimposes an icon over an original icon. It is used to
     * display a checkmark over file icons in the scripts & plugins tree
     *
     * @author francois
     * @created 1 juillet 2004
     */
    public class OverlayIcon implements Icon {

        private final Icon originalIcon;
        private final Icon overlayIcon;

        /**
         * Constructor for the OverlayIcon object
         *
         * @param originalIcon Description of the Parameter
         */
        public OverlayIcon(Icon originalIcon, Icon overlayIcon) {
            this.originalIcon = originalIcon;
            this.overlayIcon = overlayIcon;
        }

        /**
         * Gets the iconHeight attribute of the CheckedIcon object
         *
         * @return The iconHeight value
         */
        @Override
        public int getIconHeight() {
            return originalIcon.getIconHeight();
        }

        /**
         * Gets the iconWidth attribute of the CheckedIcon object
         *
         * @return The iconWidth value
         */
        @Override
        public int getIconWidth() {
            return originalIcon.getIconWidth();
        }

        /**
         * Description of the Method
         *
         * @param c Description of the Parameter
         * @param g Description of the Parameter
         * @param x Description of the Parameter
         * @param y Description of the Parameter
         */
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            originalIcon.paintIcon(c, g, x, y);
            overlayIcon.paintIcon(c, g, x, y);
        }
    }

}
