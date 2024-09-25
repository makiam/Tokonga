package artofillusion.plugin;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("plugin")
@Data
public class PluginDef {
    @XStreamAsAttribute
    @XStreamAlias("class") private String pluginClass;

    public List<Export> getExports() {
        return exports == null ? List.of() : exports;
    }

    @XStreamImplicit
    List<Export> exports = new ArrayList<>();
}
