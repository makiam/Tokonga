package artofillusion.theme;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("theme")
@Data
public class UITheme {
    @XStreamAlias("name")
    private String name;

    @XStreamAlias("description")
    private String description;

    public Integer getButtonMargin() {
        return buttonMargin.getValue();
    }

    public Integer getPaletteMargin() {
        return paletteMargin.getValue();
    }

    @XStreamAlias("buttonmargin") private Value buttonMargin = null;

    @XStreamAlias("palettemargin") private Value paletteMargin = null;

    @XStreamImplicit
    private List<UIThemeColorSet> colorSets = new ArrayList<>();

    @XStreamImplicit
    private List<Button> buttons = new ArrayList<>();
}
