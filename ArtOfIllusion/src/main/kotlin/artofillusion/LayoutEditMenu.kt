package artofillusion

import artofillusion.ui.Translate
import buoy.widget.BMenu
import buoy.widget.BMenuItem
import org.greenrobot.eventbus.Subscribe
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.swing.SwingUtilities

class LayoutEditMenu(private val layout: LayoutWindow) : BMenu(Translate.text("menu.edit")) {

    private val undoItem: BMenuItem = Translate.menuItem("undo") { layout.undoCommand()  }.also { it.isEnabled  = false }
    private val redoItem: BMenuItem = Translate.menuItem("redo") {layout.redoCommand()  }.also { it.isEnabled  = false }
    init {
        org.greenrobot.eventbus.EventBus.getDefault().register(this)
        this.add(undoItem)
        this.add(redoItem)
        this.addSeparator()
    }

    private fun edt(action: () -> Unit) = SwingUtilities.invokeLater { action() }

    @Subscribe
    fun onUndoChangedEvent(event: UndoChangedEvent) {
        if(event.record.view != layout) return
        val stack = event.stack

        edt {
            undoItem.isEnabled = stack.canUndo()
            val undoText = Translate.text("menu.undo")
            undoItem.text = if(stack.canUndo()) undoText + " " + stack.undoName else undoText
            redoItem.isEnabled = stack.canRedo()
            val redoText = Translate.text("menu.redo")
            redoItem.text = if(stack.canRedo()) redoText + " " + stack.redoName else redoText
        }
    }

    companion object   {
        private val log: Logger = LoggerFactory.getLogger(LayoutEditMenu::class.java)
    }

}