package artofillusion.theme;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;
import theme.UIThemeColorSet;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("theme")
@Data
public class UITheme {
    @XStreamAlias("name")
    private String name;

    @XStreamAlias("description")
    private String description;

    @XStreamImplicit
    private List<UIThemeColorSet> colorSets = new ArrayList<>();
}
