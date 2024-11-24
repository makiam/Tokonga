package artofillusion

import artofillusion.ui.Translate
import buoy.widget.BMenu
import org.greenrobot.eventbus.Subscribe
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.swing.SwingUtilities

class LayoutFileMenu(private val layout: LayoutWindow) : BMenu(Translate.text("menu.file")) {

    init {
        org.greenrobot.eventbus.EventBus.getDefault().register(this)
        this.add(Translate.menuItem("new") { layout.savePreferences(); edt { ArtOfIllusion.newWindow(); } })
        this.add(Translate.menuItem("open") { layout.savePreferences(); edt { ArtOfIllusion.openScene(layout); } })
    }

    @Subscribe
    fun onSceneChangedEvent( event: SceneChangedEvent ): Unit {
        log.info("On Scene changed {} ", if(layout == event.window) "this" else "other")
    }

    private fun edt(action: () -> Unit) = SwingUtilities.invokeLater() { action() }

    companion object  {
        private val log: Logger = LoggerFactory.getLogger(LayoutFileMenu::class.java)
    }
}
