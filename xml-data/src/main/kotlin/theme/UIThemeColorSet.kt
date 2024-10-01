package theme

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute

@XStreamAlias("colorset")
data class UIThemeColorSet(@XStreamAsAttribute @XStreamAlias("name") val name: String)