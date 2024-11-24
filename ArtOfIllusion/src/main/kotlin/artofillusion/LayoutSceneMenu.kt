package artofillusion

import artofillusion.image.ImagesDialog
import artofillusion.ui.Translate
import buoy.widget.BMenu
import javax.swing.SwingUtilities

class LayoutSceneMenu(private val layout: LayoutWindow) : BMenu(Translate.text("menu.scene")) {

    init {

        this.add(Translate.menuItem("renderScene") { edt { RenderSetupDialog(layout, layout.scene) }})
        this.add(Translate.menuItem("renderImmediately") { edt { RenderSetupDialog.renderImmediately(layout, layout.scene) }})
        this.addSeparator()
        this.add(Translate.menuItem("textures") { edt { TexturesAndMaterialsDialog(layout, layout.scene) }})
        this.add(Translate.menuItem("images") { edt { ImagesDialog(layout, layout.scene, null) }})
        this.add(Translate.menuItem("environment") { edt { EnvironmentPropertiesDialog(layout) }})
    }

    private fun edt(action: () -> Unit) = SwingUtilities.invokeLater() { action() }
}
