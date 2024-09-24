package artofillusion.plugin;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;

@XStreamAlias("category")
@Data
public class Category {

    @XStreamAsAttribute
    @XStreamAlias("class") private String category;
}
