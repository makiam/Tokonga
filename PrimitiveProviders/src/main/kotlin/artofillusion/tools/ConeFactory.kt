package artofillusion.tools

import artofillusion.`object`.Cylinder
import artofillusion.`object`.Object3D
import artofillusion.ui.Translate
import java.util.Optional

class ConeFactory : PrimitiveFactory {
    override fun getName() = Translate.text("menu.cone")

    override fun getCategory() = "Geometry"

    override fun create() = Optional.of<Object3D>(Cylinder(1.0, 0.5, 0.5, 0.0))
}
