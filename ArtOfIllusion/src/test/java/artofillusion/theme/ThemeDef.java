package artofillusion.theme;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;

@XStreamAlias("theme")
@Data
public class ThemeDef {
    @XStreamAlias("name")
    private String name;
}
