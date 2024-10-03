package artofillusion.theme;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("theme")
@Getter
public class UITheme {

    @Getter(AccessLevel.NONE)
    @XStreamAlias("selectable")
    private Boolean selectable = true;

    public Boolean isSelectable() { return selectable == null ? true : selectable; }

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

    @XStreamAlias("button")
    private Button button;
}
