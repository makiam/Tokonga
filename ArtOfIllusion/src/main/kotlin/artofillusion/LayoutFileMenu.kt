package artofillusion

import artofillusion.ui.Translate
import buoy.widget.BMenu
import buoy.widget.BMenuItem
import org.greenrobot.eventbus.Subscribe
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.swing.AbstractAction
import javax.swing.SwingUtilities

class LayoutFileMenu(private val layout: LayoutWindow) : BMenu(Translate.text("menu.file")) {

    init {
        org.greenrobot.eventbus.EventBus.getDefault().register(this)
        this.add(Translate.menuItem("new") { layout.savePreferences(); edt { ArtOfIllusion.newWindow() } })
        this.add(Translate.menuItem("open") { layout.savePreferences(); edt { ArtOfIllusion.openScene(layout) } })
        //Insert recent files here?
        this.add(Translate.menuItem("close") { layout.savePreferences(); edt { ArtOfIllusion.closeWindow(layout) } })
        this.addSeparator()
        this.add(Translate.menuItem("quit") { layout.savePreferences(); edt { ArtOfIllusion.quit() } })
        this.addSeparator()
    }

    @Subscribe
    fun onSceneChangedEvent( event: SceneChangedEvent ): Unit {
        log.info("On Scene changed {} ", if(layout == event.window) "this" else "other")
    }

    private fun edt(action: () -> Unit) = SwingUtilities.invokeLater() { action() }

    companion object  {
        private val log: Logger = LoggerFactory.getLogger(LayoutFileMenu::class.java)
    }

    inner class ImportAction(private val translator: Translator): AbstractAction() {
        override fun actionPerformed(e: java.awt.event.ActionEvent?) {
            translator.importFile(layout)
        }
    }

    inner class ImportSceneMenuItem(private val translator: Translator): BMenuItem(translator.name) {
        init {
            component.addActionListener { translator.importFile(layout) }
        }
    }

    inner class ExportAction(private val translator: Translator): AbstractAction() {
        override fun actionPerformed(e: java.awt.event.ActionEvent?) {
            translator.exportFile(layout, layout.theScene)
        }
    }

    inner class ExportSceneMenuItem(private val translator: Translator): BMenuItem(translator.name) {
        init {
            component.addActionListener { translator.exportFile(layout, layout.theScene) }
        }
    }
}
