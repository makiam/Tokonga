package artofillusion

import artofillusion.material.Material
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ApplyPredicate(material: Material?) {
    private val material: Material?

    init {
        this.material = material
    }

    fun somme() {
        log.info("ApplyPredicate")
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ApplyPredicate::class.java)
    }
}
