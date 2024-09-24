package artofillusion.plugin;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;

@XStreamAlias("plugin")
@Data
public class PluginDef {
    @XStreamAsAttribute
    @XStreamAlias("class") String pluginClass;
}
