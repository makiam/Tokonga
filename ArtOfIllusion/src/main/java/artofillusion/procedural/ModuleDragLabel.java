package artofillusion.procedural;

import buoy.widget.BLabel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class ModuleDragLabel extends BLabel {
    private ProceduralModule module;

    public ModuleDragLabel(ProceduralModule module) {
        super("Drag: " + module.name);
        this.module = module;

        this.component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JComponent comp = (JComponent) e.getSource();
                TransferHandler handler = comp.getTransferHandler();
                // Start the framework's drag operation
                handler.exportAsDrag(comp, e, TransferHandler.COPY);
            }
        });
        this.component.setTransferHandler(new TransferHandler() {

            @Override
            public int getSourceActions(JComponent c) {
                return TransferHandler.COPY;
            }


            @Override
            protected Transferable createTransferable(JComponent c) {
                return new ModuleTransferable(ModuleDragLabel.this.module);
            }
        });
    }
}
