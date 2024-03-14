package artofillusion.tools;

import artofillusion.UndoRecord;
import artofillusion.object.ObjectInfo;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

public class ConvertToActorDialog extends JDialog {

    private final JTable objectsTable;
    private final JButton okButton;
    private final JButton cancelButton;

    public ConvertToActorDialog(List<ObjectInfo> infos, UndoRecord undo) {
        objectsTable = new JTable();
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        objectsTable.setModel(new ActorSelectTableModel(infos));
        // Set layout for the dialog
        setLayout(new BorderLayout());

        // Add the table to the center of the dialog
        add(new JScrollPane(objectsTable), BorderLayout.CENTER);

        // Create a panel for the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        // Add the button panel to the bottom of the dialog
        add(buttonPanel, BorderLayout.SOUTH);

    }

    @Override
    protected void dialogInit() {
        super.dialogInit();

        this.setSize(640, 480);

    }


    public class ActorSelectTableModel extends AbstractTableModel {
        private final List<ObjectInfo> infos;
        private final boolean[] checkboxes;

        public ActorSelectTableModel(List<ObjectInfo> infos) {
            this.infos = infos;
            this.checkboxes = new boolean[infos.size()];
        }

        @Override
        public int getRowCount() {
            return infos.size();
        }

        @Override
        public int getColumnCount() {
            // Add an extra column for the checkbox
            return 2;
        }

        @Override
        public String getColumnName(int column) {
            return column == 0 ? "Convert to Actor" : "Object";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? Boolean.class : ObjectInfo.class;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return checkboxes[rowIndex];
            } else {
                // Return the ObjectInfo
                return infos.get(rowIndex).name;
            }
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if (columnIndex == 0 && value instanceof Boolean) {
                checkboxes[rowIndex] = (Boolean) value;
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0; // Only the checkbox column is editable
        }
    }
}
