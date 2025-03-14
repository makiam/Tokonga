package artofillusion.tools

import artofillusion.`object`.Object3D
import artofillusion.script.ScriptRunner
import artofillusion.script.ScriptedObject
import artofillusion.ui.Translate
import java.util.Optional

class ScriptedObjectProvider : PrimitiveFactory {
    override fun getCategory() = "Scripting"

    override fun getName() = Translate.text("menu.createScriptObject")

    override fun create(): Optional<Object3D> {
        val obj: ScriptedObject = ScriptedObject("", ScriptRunner.Language.GROOVY.name)
        return Optional.of<Object3D?>(obj)
    }
}
