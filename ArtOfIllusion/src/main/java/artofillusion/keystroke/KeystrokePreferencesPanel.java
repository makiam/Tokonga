/* Copyright (C) 2006-2013 by Peter Eastman
   Changes copyright (C) 2017-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.keystroke;

import artofillusion.preferences.PreferencesEditor;
import buoy.widget.*;
import buoy.event.*;

import javax.swing.table.*;
import java.awt.event.*;

import java.util.*;
import java.text.*;

import artofillusion.ui.*;
import java.awt.Dimension;

/**
 * This class presents a user interface for editing the list of KeystrokeRecords.
 */
public class KeystrokePreferencesPanel extends FormContainer implements PreferencesEditor {

    private final List<KeystrokeRecord> records = KeystrokeManager.getRecords();
    private final BTable table;
    private final BButton editButton;
    private final BButton deleteButton;
    private boolean changed;
    private int sortColumn = 1;

    public KeystrokePreferencesPanel() {
        super(new double[]{1}, new double[]{1, 0});

        table = new BTable(new KeystrokeTableModel());
        table.setColumnWidth(0, 100);
        table.setColumnWidth(1, 250);
        table.setColumnsReorderable(false);
        table.addEventLink(SelectionChangedEvent.class, this, "selectionChanged");
        table.addEventLink(MouseClickedEvent.class, this, "tableClicked");
        table.getTableHeader().addEventLink(MouseClickedEvent.class, new Object() {
            void processEvent(MouseClickedEvent ev) {
                int col = table.findColumn(ev.getPoint());
                if (col > -1) {
                    sortColumn = col;
                    sortRecords();
                }
            }
        });
        BScrollPane scroll = new BScrollPane(table, BScrollPane.SCROLLBAR_NEVER, BScrollPane.SCROLLBAR_AS_NEEDED);
        scroll.setPreferredViewSize(new Dimension(350, table.getRowCount() == 0 ? 150 : 15 * table.getRowHeight(0)));
        add(scroll, 0, 0, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH));
        RowContainer buttons = new RowContainer();
        add(buttons, 0, 1, new LayoutInfo());
        buttons.add(editButton = Translate.button("edit", "...", event -> editRecord()));

        buttons.add(Translate.button("add", "...", event -> addRecord()));
        buttons.add(deleteButton = Translate.button("delete", event -> deleteRecords()));
        selectionChanged();
    }

    /**
     * Save the changes.
     */
    public void saveChanges() {
        if (!changed) {
            return;
        }
        KeystrokeManager.setRecords(records);
        try {
            KeystrokeManager.saveRecords();
        } catch (Exception ex) {
            MessageDialog.create().withOwner(this.getComponent()).error(Translate.text("errorSavingPrefs", ex.getMessage() == null ? "" : ex.getMessage()));
        }
    }

    /**
     * This is called when the selection in the table changes.
     */
    private void selectionChanged() {
        int count = table.getSelectedRows().length;
        editButton.setEnabled(count == 1);
        deleteButton.setEnabled(count > 0);
    }

    /**
     * This is called when the user clicks on the table.
     */
    private void tableClicked(MouseClickedEvent ev) {
        if (ev.getClickCount() != 2 || ev.getModifiersEx() != 0) {
            return;
        }
        editRecord();
    }

    /**
     * Edit the selected record.
     */
    private void editRecord() {
        int row = table.getSelectedRows()[0];
        KeystrokeRecord record = records.get(row);
        KeystrokeRecord edited = KeystrokeEditor.showEditor(record, UIUtilities.findWindow(this));
        if (edited == null) {
            return;
        }
        records.set(row, edited);
        sortRecords();
        changed = true;
    }

    /**
     * Add a new record.
     */
    private void addRecord() {
        // Beanshell is the only supported language here
        KeystrokeRecord record = new KeystrokeRecord(0, 0, "", "");
        KeystrokeRecord edited = KeystrokeEditor.showEditor(record, UIUtilities.findWindow(this));
        if (edited == null) {
            return;
        }
        records.add(edited);
        sortRecords();
        changed = true;
    }

    /**
     * Delete the selected records.
     */
    private void deleteRecords() {
        int[] selected = table.getSelectedRows();
        Arrays.sort(selected);
        for (int i = 0; i < selected.length; i++) {
            records.remove(selected[i]);
        }
        sortRecords();
        changed = true;
    }

    /**
     * Resort the list of records by name.
     */
    private void sortRecords() {
        Collections.sort(records, new Comparator<>() {
            @Override
            public int compare(KeystrokeRecord r1, KeystrokeRecord r2) {
                String s1, s2;
                if (sortColumn == 0) {
                    s1 = getKeyDescription(r1.getKeyCode(), r1.getModifiers());
                    s2 = getKeyDescription(r2.getKeyCode(), r2.getModifiers());
                } else {
                    s1 = r1.getName();
                    s2 = r2.getName();
                }
                return Collator.getInstance(Translate.getLocale()).compare(s1, s2);
            }
        });
        ((KeystrokeTableModel) table.getModel()).fireTableDataChanged();
    }

    /**
     * Get a string describing a keystroke.
     */
    static String getKeyDescription(int code, int modifiers) {
        if (code == 0) {
            return "";
        }
        String keyDesc = KeyEvent.getKeyText(code);
        if (modifiers != 0) {
            keyDesc = KeyEvent.getKeyModifiersText(modifiers) + "+" + keyDesc;
        }
        return keyDesc;
    }

    @Override
    public Widget getPreferencesPanel() {
        return this;
    }

    @Override
    public void savePreferences() {
        this.saveChanges();
    }

    @Override
    public String getName() {
        return Translate.text("shortcuts");
    }    
    /**
     * This is the model for the table of keystrokes.
     */
    private class KeystrokeTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return records.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            KeystrokeRecord record = records.get(rowIndex);
            if (columnIndex == 1) {
                return record.getName();
            }
            return getKeyDescription(record.getKeyCode(), record.getModifiers());
        }

        @Override
        public String getColumnName(int column) {
            return (column == 1 ? Translate.text("Name") : Translate.text("Key"));
        }
    }
}
