/*
 *  Copyright 2004 Francois Guillet
 *  Changes copyright 2022-2024 by Maksim Khramov
 *
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package artofillusion.spmanager;

import artofillusion.*;
import artofillusion.ui.Translate;
import artofillusion.ui.UIUtilities;
import buoy.event.*;
import buoy.widget.*;
import lombok.Getter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Predicate;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Main frame of the scripts and plugins manager.
 *
 * @author françois guillet
 * @created 13 mars 2004
 */
public class SPManagerFrame extends BFrame {

    private static SPManagerFrame spmFrame;
    /**
     * -- GETTER --
     *  Gets the parameters attribute of the manager
     *
     * @return The parameters value
     */
    @Getter
    private static SPMParameters parameters;
    private final BTabbedPane tabbedPane;
    private final SPMSplitPane manageSplitPane;
    private final InstallSplitPane updateSplitPane;
    private final InstallSplitPane installSplitPane;
    private final BLabel statusLabel = new BLabel(" ");
    private String statusText;
    private javax.swing.Timer timer;
    private final ActionListener statusTextClearAction = (ActionEvent ae) -> statusLabel.setText(" ");

    public static final String[] YES_NO = {
        SPMTranslate.text("Yes"), SPMTranslate.text("No")
    };

    public static final String[] CONTINUE_IGNORE = {
        SPMTranslate.text("Continue"), SPMTranslate.text("Stop"),
        SPMTranslate.text("Ignore")
    };

    /**
     * Gets the single instance of SPManagerFrame currently running
     *
     * @return The bFrame value
     */
    public static SPManagerFrame getInstance() {
        return spmFrame;
    }

    /**
     * Constructor for the SPManagerFrame object
     */
    public SPManagerFrame() {
        super(SPMTranslate.text("SPManager"));
        setIcon(ArtOfIllusion.APP_ICON);

        spmFrame = this;

        parameters = new SPMParameters();


        manageSplitPane = new ManageSplitPane();
        updateSplitPane = new InstallSplitPane(SPMSplitPane.UPDATE, (java.net.URL) null);
        installSplitPane = new InstallSplitPane(SPMSplitPane.INSTALL, updateSplitPane.getFileSystem());
        ((HttpSPMFileSystem) updateSplitPane.getFileSystem()).setRepository(parameters.getCurrentRepository());
        RowContainer rc = new RowContainer();
        ColumnContainer cc = new ColumnContainer();
        LayoutInfo headLayout = new LayoutInfo(LayoutInfo.WEST, LayoutInfo.NONE, new Insets(3, 5, 5, 3), new Dimension(0, 0));
        rc.add(new BLabel(new ImageIcon(getClass().getResource("/artofillusion/spmanager/icons/gear.png"))), headLayout);
        //Icon gear.png taken from the KDE desktop environment !!!
        rc.add(SPMTranslate.bLabel("Version"), headLayout);
        cc.add(rc, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH, new Insets(3, 3, 5, 3), new Dimension(0, 0)));

        //Tabbed Pane setup
        tabbedPane = new BTabbedPane();
        tabbedPane.add(manageSplitPane, SPMTranslate.text("manage"));
        tabbedPane.add(updateSplitPane, SPMTranslate.text("update"));
        tabbedPane.add(installSplitPane, SPMTranslate.text("install"));
        tabbedPane.addEventLink(SelectionChangedEvent.class, this, "doTabbedPaneSelection");

        LayoutInfo tabbedPaneLayout = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH, new Insets(0, 0, 0, 0), new Dimension(0, 0));
        cc.add(tabbedPane, tabbedPaneLayout);

        //buttons setup
        RowContainer buttons = new RowContainer();
        buttons.add(Translate.button("spmanager:setup", event -> doSetup()));
        buttons.add(Translate.button("spmanager:rescan", event -> doRescan()));
        buttons.add(Translate.button("spmanager:close", event -> hideSPManager()));
        cc.add(buttons, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE, new Insets(3, 0, 0, 0), null));

        cc.add(new BOutline(statusLabel, BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), SPMTranslate.text("remoteStatus"))), new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH, new Insets(1, 1, 1, 1), new Dimension(0, 0)));

        setContent(cc);
        pack();
        centerAndSizeWindow();
        setVisible(true);
        addEventLink(WindowClosingEvent.class, this, "hideSPManager");
        manageSplitPane.setDividerLocation(manageSplitPane.getChild(0).getPreferredSize().width);
        updateSplitPane.setDividerLocation(updateSplitPane.getChild(0).getPreferredSize().width);
        installSplitPane.setDividerLocation(installSplitPane.getChild(0).getPreferredSize().width);
    }

    /**
     * Hides the SPManager main window
     */
    protected void hideSPManager() {
        if (manageSplitPane.isModified() || updateSplitPane.isModified() || installSplitPane.isModified()) {
            JOptionPane.showMessageDialog(null, SPMTranslate.text("modified"), SPMTranslate.text("alert"), JOptionPane.ERROR_MESSAGE);
        }
        getComponent().setVisible(false);
    }

    /**
     * Called whenever a tab of the tabbed pane is selected
     */
    private void doTabbedPaneSelection() {
        switch (tabbedPane.getSelectedTab()) {
            default:
            case 0:
                manageSplitPane.doSetup();
                break;
            case 1:
                updateSplitPane.doSetup();
                break;
            case 2:
                installSplitPane.doSetup();
                break;
        }

    }

    /**
     * check for an update to ourselves (SPManager)
     */
    protected void checkForUpdatedMe() {
        Predicate<SPMObjectInfo> self = (SPMObjectInfo t) -> t.getName().equals("SPManager");
        SPMObjectInfo localinfo = manageSplitPane.getFileSystem().getPlugins().stream().filter(self).findFirst().orElse(null);
        SPMObjectInfo remoteinfo = updateSplitPane.getFileSystem().getPlugins().stream().filter(self).findFirst().orElse(null);

        boolean update = true;

        if (localinfo == null || remoteinfo == null) {
            update = (remoteinfo != null);
        } else {
            if (remoteinfo.getMajor() < localinfo.getMajor()) {
                update = false;
            } else if (remoteinfo.getMajor() == localinfo.getMajor()) {
                if (remoteinfo.getMinor() < localinfo.getMinor()) {
                    update = false;
                } else if (remoteinfo.getMinor() == localinfo.getMinor()) {
                    if (localinfo.isBeta()) {
                        if (remoteinfo.isBeta() && (remoteinfo.getBeta() <= localinfo.getBeta())) {
                            update = false;
                        }
                    } else {
                        update = false;
                    }
                }
            }
        }

        // we found an update for SPManager, so offer to install it now
        if (update) {
            if (new BStandardDialog("SPManager",
                    UIUtilities.breakString(SPMTranslate.text("updateSPManager")),
                    BStandardDialog.QUESTION).showOptionDialog(null, SPManagerFrame.YES_NO, SPManagerFrame.YES_NO[0]) == 0) {
                final StatusDialog status = new StatusDialog(SPManagerPlugin.getFrame());
                final SPMObjectInfo info = remoteinfo;

                (new Thread() {
                    @Override
                    public void run() {
                        updateSplitPane.installFile(info);
                        updateSplitPane.showErrors();

                        SwingUtilities.invokeLater(() -> {
                            status.dispose();
                            hideSPManager();
                        });

                    }
                }).start();

            }
        }
    }

    /**
     * Called when the manage button is clicked
     */
    private void doManage() {
        tabbedPane.setSelectedTab(0);
        doTabbedPaneSelection();
    }

    /**
     * Called when the update button is clicked
     */
    private void doUpdate() {
        tabbedPane.setSelectedTab(1);
        doTabbedPaneSelection();
    }

    /**
     * Called when the update button is clicked
     */
    private void doInstall() {
        tabbedPane.setSelectedTab(2);
        doTabbedPaneSelection();
    }

    /**
     * Called when the Setup button is clicked
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    private void doSetup() {
        new SPMSetupFrame(this);
        if (parameters.hasChanged()) {
            ((HttpSPMFileSystem) updateSplitPane.getFileSystem()).setRepository(parameters.getCurrentRepository());
            doRescan();
        }
    }

    /**
     * Called when the Rescan button is clicked
     */
    private void doRescan() {
        parameters.getRepositoriesList(true);
    }

    /**
     * Updates install and update panes
     */
    public void updatePanes() {
        manageSplitPane.doUpdate();

        //setting the repository forces the remote file system to be rescanned
        ((HttpSPMFileSystem) updateSplitPane.getFileSystem()).setRepository(parameters.getCurrentRepository());
        updateSplitPane.doUpdate();
        installSplitPane.doUpdate();
    }

    /**
     * Center and resizes the frame
     */
    private void centerAndSizeWindow() {
        try {
            WindowWidget root = (WindowWidget) getParent();
            while (root != null && (!(root instanceof LayoutWindow))) {
                root = (WindowWidget) root.getParent();
            }

            Rectangle bounds = (Rectangle) PluginRegistry.invokeExportedMethod("nik777.OneFixSizesAll.getChildBounds",new Object[]{root});

            Dimension d2 = getComponent().getSize();
            int x;
            int y;

            x = bounds.x + (bounds.width - d2.width) / 2;
            y = bounds.y + (bounds.height - d2.height) / 2;

            if (x < 0) {
                x = 0;
            }
            if (y < 0) {
                y = 0;
            }

            setBounds(new Rectangle(x, y, d2.width, d2.height + 2));
        } catch (NoSuchMethodException | InvocationTargetException e) {
            UIUtilities.centerWindow(this);
        }
    }

    /**
     * Sets the text of the status bar
     *
     * @param text The text to display
     * @param time The duration (see setRemoteStatusTextDuration)
     */
    public void setRemoteStatusText(String text, int time) {
        statusText = text;
        setRemoteStatusTextDuration(time);
        SwingUtilities.invokeLater(() -> statusLabel.setText(statusText));
    }

    /**
     * Sets the duration for displaying text in the status bar A null or
     * negative duration means that the text remains forever
     *
     * @param time The duration
     */
    public void setRemoteStatusTextDuration(int time) {
        if (time > 0) {
            if (timer == null) {
                timer = new Timer(time, statusTextClearAction);
                timer.start();
            } else {
                timer.setDelay(time);
                timer.restart();
            }
        } else if (timer != null) {
            timer.stop();
        }
    }

}
