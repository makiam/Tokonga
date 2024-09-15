package artofillusion

import groovy.lang.GroovyShell
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

object AppShell {

    val shell: GroovyShell by lazy {

        val config: CompilerConfiguration = CompilerConfiguration()
        val ic = ImportCustomizer()
        ic.addStarImports(ArtOfIllusion::class.java.getPackage().name)
        config.addCompilationCustomizers(ic)
        GroovyShell(config)
    }


}
