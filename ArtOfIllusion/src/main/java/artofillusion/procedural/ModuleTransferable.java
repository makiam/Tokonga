package artofillusion.procedural;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ModuleTransferable implements Transferable {
    private final ProceduralModule module;

    public ModuleTransferable(ProceduralModule module) {
        this.module = module;
    }

    private final DataFlavor[] flavors = { ProceduralModule.moduleFlavor };

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return flavors.clone();
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(ProceduralModule.moduleFlavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return module.duplicate();
    }
}
