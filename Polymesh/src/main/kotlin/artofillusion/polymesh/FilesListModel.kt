package artofillusion.polymesh

import artofillusion.ArtOfIllusion
import artofillusion.ui.Translate
import java.io.File
import java.nio.file.Paths
import java.util.stream.Stream
import javax.swing.DefaultComboBoxModel

class FilesListModel : DefaultComboBoxModel<String?>() {
    init {
        addElement(Translate.text("polymesh:cube"))
        addElement(Translate.text("polymesh:face"))
        addElement(Translate.text("polymesh:octahedron"))
        addElement(Translate.text("polymesh:cylinder"))
        addElement(Translate.text("polymesh:flatMesh"))

        val polyMeshTemplates = Paths.get(ArtOfIllusion.PLUGIN_DIRECTORY, "PolyMeshTemplates")
        polyMeshTemplates.toFile().also { it.mkdir() }.also { it.list().asSequence().forEach { it -> addElement(it) } }

    }
}
