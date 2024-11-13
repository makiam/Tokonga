package artofillusion.plugin

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute
import com.thoughtworks.xstream.annotations.XStreamImplicit

@XStreamAlias("list")
class Blacklist {
    @XStreamImplicit
    private val records: List<BlacklistItem>  = ArrayList<BlacklistItem>();
}
@XStreamAlias("plugin")
data class BlacklistItem(@XStreamAsAttribute val name: String? = null)
