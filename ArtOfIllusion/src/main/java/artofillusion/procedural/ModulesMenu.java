package artofillusion.procedural;

import java.awt.*;

import artofillusion.PluginRegistry;

import javax.swing.*;

public class ModulesMenu extends JPanel {
    public ModulesMenu() {
        super();
        this.setLayout( new FlowLayout());
        PluginRegistry.getPlugins(Module.class).forEach(mp -> {
            this.add(new JLabel(mp.name));
        });
    }
}
