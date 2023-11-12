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
import java.util.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import lombok.extern.slf4j.Slf4j;

/**
 * Description of the Class
 *
 * @author François Guillet
 * @created March, 13 2004
 */
@Slf4j
public class SPMSetupFrame extends BDialog {

    private final BComboBox repositoriesCB;
    private final ColumnContainer filterContainer;

    private BButton removeButton;
    private BTextField repEntry;
    private final BTextField proxyHostEntry;
    private final BTextField proxyPortEntry;
    private final BTextField usernameEntry;
    private final BPasswordField passwordEntry;
    private final BLabel proxyHostLabel;
    private final BLabel proxyPortLabel;
    private final BLabel usernameLabel;
    private final BLabel passwordLabel;
    private final BCheckBox useProxyCB;
    private final BCheckBox useCacheCB;
    private final SPMParameters parameters;
    private String[] rep;

    /**
     * Constructor for the SPMSetupFrame object
     *
     * @param fr Description of the Parameter
     */
    public SPMSetupFrame(SPManagerFrame fr) {

        super(fr, true);
        setTitle(SPMTranslate.text("SPManagerSetup"));
        parameters = SPManagerFrame.getParameters();
        addEventLink(WindowClosingEvent.class, this, "doCancel");

        ColumnContainer cc = new ColumnContainer();
        LayoutInfo topLayout = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE, new Insets(5, 3, 3, 3), new Dimension(0, 0));
        cc.add(SPMTranslate.bLabel("chooseRepository"), topLayout);
        LayoutInfo layout = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE, new Insets(3, 3, 5, 3), new Dimension(0, 0));
        cc.add(repositoriesCB = new BComboBox(rep = parameters.getRepositories()), layout);
        repositoriesCB.addEventLink(ValueChangedEvent.class, this, "doRepositoriesCBChanged");
        repositoriesCB.setSelectedIndex(parameters.getCurrentRepositoryIndex());

        // NTJ: populate filters
        filterContainer = new ColumnContainer();

        Map<String, String> filters = SPMParameters.getFilters();

        if (!filters.isEmpty()) {
            String[] keys = filters.keySet().toArray(new String[0]);
            Arrays.sort(keys);

            String filterName, filterValue;

            RowContainer line = null;
            BComboBox sel = null;
            LayoutInfo right = new LayoutInfo(LayoutInfo.EAST, LayoutInfo.NONE);
            for (String key : keys) {
                filterName = key;
                filterValue = filters.get(filterName);

                log.atInfo().log("Filter: {}={}", filterName, filterValue);

                line = new RowContainer();
                sel = new BComboBox();

                line.add(new BLabel(filterName));
                line.add(sel);

                for (int j = 0; j < SPMParameters.LAST_FILTER; j++) {
                    String filterType = SPMParameters.FILTER_NAMES[j];

                    sel.add(filterType);
                    if (filterValue.equals(filterType)) {
                        sel.setSelectedValue(filterType);
                    }
                }

                filterContainer.add(line, right);
            }

            BScrollPane sp = new BScrollPane(filterContainer);
            sp.setVerticalScrollbarPolicy(BScrollPane.SCROLLBAR_AS_NEEDED);
            Dimension dim = new Dimension();
            dim.setSize(filterContainer.getPreferredSize().getWidth(),line.getPreferredSize().getHeight() * Math.min(filters.size(), 5));

            sp.setPreferredViewSize(dim);

            BOutline bo = new BOutline(sp, BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), SPMTranslate.text("filters")));

            cc.add(bo, layout);
        }

        useCacheCB = SPMTranslate.bCheckBox("useCache", parameters.getUseCache(), this, "doUseCacheCB");
        cc.add(useCacheCB, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE, new Insets(2, 3, 2, 3), new Dimension(0, 0)));

        LayoutInfo rcLayout = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE, new Insets(4, 3, 4, 3), new Dimension(0, 0));

        FormContainer fm = new FormContainer(2, 5);
        LayoutInfo formLayout = new LayoutInfo(LayoutInfo.WEST, LayoutInfo.NONE, new Insets(2, 4, 2, 4), new Dimension(0, 0));

        useProxyCB = SPMTranslate.bCheckBox("useProxy", parameters.useProxy(), this, "doUseProxyCB");
        fm.add(useProxyCB, 0, 0, formLayout);

        fm.add(proxyHostLabel = SPMTranslate.bLabel("proxyHost"), 0, 1, formLayout);
        proxyHostEntry = new BTextField(SPMParameters.getProxyHost(), 25);
        fm.add(proxyHostEntry, 1, 1, formLayout);

        fm.add(proxyPortLabel = SPMTranslate.bLabel("proxyPort"), 0, 2, formLayout);
        proxyPortEntry = new BTextField(SPMParameters.getProxyPort(), 25);
        fm.add(proxyPortEntry, 1, 2, formLayout);

        fm.add(usernameLabel = SPMTranslate.bLabel("username"), 0, 3, formLayout);
        usernameEntry = new BTextField(SPMParameters.getUserName(), 15);
        fm.add(usernameEntry, 1, 3, formLayout);

        fm.add(passwordLabel = SPMTranslate.bLabel("password"), 0, 4, formLayout);
        passwordEntry = new BPasswordField(SPMParameters.getPassword(), 15);
        fm.add(passwordEntry, 1, 4, formLayout);

        cc.add(fm, rcLayout);

        if (!useProxyCB.getState()) {
            proxyHostEntry.setEnabled(false);
            proxyPortEntry.setEnabled(false);
            usernameEntry.setEnabled(false);
            passwordEntry.setEnabled(false);
            proxyHostLabel.setEnabled(false);
            proxyPortLabel.setEnabled(false);
            usernameLabel.setEnabled(false);
            passwordLabel.setEnabled(false);
        }

        RowContainer buttons = new RowContainer();

        buttons.add(Translate.button("ok", this, "doOK"));

        buttons.add(Translate.button("cancel", this, "doCancel"));
        cc.add(buttons, new LayoutInfo());
        setContent(cc);
        addEventLink(WindowClosingEvent.class, this, "doCancel");
        pack();
        getComponent().setLocationRelativeTo(fr.getComponent());
        parameters.setChanged(false);
        setVisible(true);
    }

    /**
     * Proxy checkbox selected
     */
    private void doUseProxyCB() {
        boolean state = useProxyCB.getState();
        proxyHostEntry.setEnabled(state);
        proxyPortEntry.setEnabled(state);
        usernameEntry.setEnabled(state);
        passwordEntry.setEnabled(state);
        proxyHostLabel.setEnabled(state);
        proxyPortLabel.setEnabled(state);
        usernameLabel.setEnabled(state);
        passwordLabel.setEnabled(state);

    }

    /**
     * Add a new repository to the list
     */
    private void doAdd() {
        int i;

        String[] newRep = new String[rep.length + 1];
        for (i = 0; i < rep.length; ++i) {
            newRep[i] = rep[i];
        }
        newRep[i] = repEntry.getText();
        rep = newRep;
        repositoriesCB.setContents(newRep);
        repositoriesCB.setSelectedIndex(i);
        if (rep.length > 1) {
            removeButton.setEnabled(true);
        }
    }

    /**
     * Remove a repository from the list
     */
    private void doRemove() {
        String[] newRep = new String[rep.length - 1];
        int j = 0;
        int removed = 0;
        String s = repEntry.getText();
        for (int i = 0; i < rep.length && j < newRep.length; ++i) {
            if (!s.equals(rep[i])) {
                newRep[j] = rep[i];
                ++j;
            } else {
                removed = i;
            }
        }
        rep = newRep;
        if (removed >= rep.length) {
            --removed;
        }
        repositoriesCB.setContents(rep);
        repositoriesCB.setSelectedIndex(removed);
        if (rep.length <= 1) {
            removeButton.setEnabled(false);
        }
    }

    /**
     * OK button selected
     */
    private void doOK() {
        parameters.setURLs(rep, repositoriesCB.getSelectedIndex());
        SPMParameters.setUseProxy(useProxyCB.getState());
        SPMParameters.setProxyHost(proxyHostEntry.getText());
        SPMParameters.setProxyPort(proxyPortEntry.getText());
        SPMParameters.setUserName(usernameEntry.getText());
        parameters.setProxyParameters(useProxyCB.getState(), proxyHostEntry.getText(), proxyPortEntry.getText(), usernameEntry.getText(), passwordEntry.getText());

        Map<String, String> filters = SPMParameters.getFilters();
        filters.clear();
        RowContainer line;
        BComboBox sel;
        String filterName, filterValue;
        for (int i = 0; i < filterContainer.getChildCount(); i++) {
            line = (RowContainer) filterContainer.getChild(i);

            filterName = ((BLabel) line.getChild(0)).getText();

            sel = (BComboBox) line.getChild(1);
            if (sel.getSelectedIndex() >= 0) {
                filterValue = sel.getSelectedValue().toString();
            } else {
                filterValue = SPMParameters.FILTER_NAMES[SPMParameters.DEFAULT];
            }

            filters.put(filterName, filterValue);
        }

        parameters.setChanged(true);
        dispose();
    }

    /**
     * Cancel button selected
     */
    private void doCancel() {
        dispose();
    }

    /**
     * Use cache checkbox selected
     */
    private void doUseCacheCB() {
        parameters.setUseCache(useCacheCB.getState());
    }

    /**
     * Description of the Method
     */
    private void doRepositoriesCBChanged() {
        parameters.setCurrentRepository(repositoriesCB.getSelectedIndex());
    }


}
