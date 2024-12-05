package artofillusion

import artofillusion.ui.Translate
import buoy.widget.BMenu
import javax.swing.SwingUtilities

class LayoutAnimationMenu (private val layout: LayoutWindow) : BMenu(Translate.text("menu.animation")) {
    init {

    }

    private fun edt(action: () -> Unit) = SwingUtilities.invokeLater() { action() }
}