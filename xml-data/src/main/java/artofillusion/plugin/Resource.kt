package artofillusion.plugin

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute
import lombok.Data
import java.util.Locale

@XStreamAlias("resource")
@Data
class Resource(@XStreamAsAttribute val type: String, @XStreamAsAttribute val id: String, @XStreamAsAttribute val name: String, @XStreamAsAttribute val locale: String?) {
    constructor(type: String, id: String, name: String) : this(type, id, name, null)
}

