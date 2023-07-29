package artofillusion.procedural;

import artofillusion.PluginRegistry;
import artofillusion.ui.Translate;
import lombok.Getter;

import javax.swing.*;

public class ModulesMenu extends JPanel {
    public ModulesMenu() {
        super();
        this.setLayout( new BoxLayout(this, BoxLayout.Y_AXIS));
        PluginRegistry.getPlugins(Module.class).forEach(mp -> this.add(new ModuleLabel(mp)));
    }

    private static class ModuleLabel extends JLabel {
        @Getter
        private final Module module;

        public ModuleLabel(Module module) {
            super(Translate.text(module.getName()));
            this.module = module;
            this.setTransferHandler(new ModuleTransferHandler());
        }

        private class ModuleTransferHandler extends TransferHandler {

        }
    }
}
