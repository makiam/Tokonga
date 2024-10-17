package artofillusion.plugin

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute

@XStreamAlias("external")
data class External(@XStreamAsAttribute val name: String,
                    @XStreamAsAttribute val type: String,
                    @XStreamAsAttribute val association: String,
                    @XStreamAsAttribute val action: String)