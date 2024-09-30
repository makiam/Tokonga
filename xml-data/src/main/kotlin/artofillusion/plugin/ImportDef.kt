package artofillusion.plugin

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute

@XStreamAlias("import")
data class ImportDef(@XStreamAsAttribute val name: String? = null, @XStreamAsAttribute val url: String? = null)
