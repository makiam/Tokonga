package artofillusion.tools

import artofillusion.`object`.Object3D
import artofillusion.`object`.SceneCamera
import artofillusion.ui.Translate
import java.util.Optional

class CameraFactory: PrimitiveFactory {
    override fun getName() = Translate.text("menu.camera")


    override fun getCategory() = "Cameras"

    override fun create(): Optional<Object3D> {
        return Optional.of<Object3D>(SceneCamera())
    }
}
