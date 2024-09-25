package artofillusion.plugin;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("import")
@Data
public class ImportDef {
    @XStreamAsAttribute private String name;
    @XStreamAsAttribute private String url;
}
