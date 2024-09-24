package artofillusion.plugin;


import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("extension")
@Data
public class Extension {
    @XStreamAsAttribute
    String name;
    @XStreamAsAttribute
    String version;
    @XStreamAlias("author")
    String author;
    @XStreamAlias("date")
    String date;
    @XStreamAlias("description")
    String description;

    @XStreamImplicit
    final List<Category> categoryList = new ArrayList<>();

    @XStreamImplicit
    final List<PluginDef> pluginsList = new ArrayList<>();
}
