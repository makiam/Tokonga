package artofillusion.plugin;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;

import java.util.Locale;

@XStreamAlias("resource")
@Data
public class Resource {
    @XStreamAsAttribute private String type;
    @XStreamAsAttribute private String id;
    @XStreamAsAttribute private String name;
    @XStreamAsAttribute
    private Locale locale;
}
