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
    private String name;
    @XStreamAsAttribute
    private String version;
    @XStreamAlias("author")
    private String author;
    @XStreamAlias("date")
    private String date;
    @XStreamAlias("description")
    private String description;

    public List<Category> getCategoryList() {
        return categoryList == null ? List.of() : categoryList;
    }

    @XStreamImplicit
    private final List<Category> categoryList = new ArrayList<>();

    public List<PluginDef> getPluginsList() {
        return pluginsList == null ? List.of() : pluginsList;
    }

    @XStreamImplicit
    private final List<PluginDef> pluginsList = new ArrayList<>();

    public List<ImportDef> getImports() {
        return imports == null ? List.of() : imports;
    }

    @XStreamImplicit
    private List<ImportDef> imports = new ArrayList<>();

    public String getComments() {
        return comments.strip();
    }

    @XStreamAlias("comments")
    private String comments;

    private History history;

    public List<Resource> getResources() {
        return resources == null ? List.of() : resources;
    }

    @XStreamImplicit
    private List<Resource> resources = new ArrayList<>();


}
