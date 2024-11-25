package artofillusion

import artofillusion.ui.Translate
import buoy.widget.BMenu
import buoy.widget.BMenuItem

class LayoutToolsMenu(private val layout: LayoutWindow) : BMenu(Translate.text("menu.tools")) {

    init {
        PluginRegistry.getPlugins(ModellingTool::class.java).sortedBy { it.name }.map { ToolActionMenu(layout, it) }.onEach { this.add(it) }
        this.addSeparator()
    }

    private class ToolActionMenu(layout: LayoutWindow?, tool: ModellingTool) : BMenuItem(tool.name) {
        init {
            component.addActionListener { _ -> tool.commandSelected(layout) }
        }
    }
}