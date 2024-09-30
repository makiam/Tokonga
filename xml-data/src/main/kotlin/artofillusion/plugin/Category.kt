package artofillusion.plugin

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute

@XStreamAlias("category")
data class Category(@XStreamAsAttribute @XStreamAlias("class") val category: String? = null)
