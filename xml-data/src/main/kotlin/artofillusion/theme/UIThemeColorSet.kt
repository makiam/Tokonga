package artofillusion.theme

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute
import com.thoughtworks.xstream.annotations.XStreamImplicit
import java.awt.Color

@XStreamAlias("colorset")
class UIThemeColorSet {
    @XStreamAsAttribute @XStreamAlias("name") val name: String  = ""

    @XStreamAlias("applicationbackground")
    var applicationBackground: ColorSetColor? = null
    @XStreamAlias("palettebackground")
    var paletteBackground: ColorSetColor? = null
    @XStreamAlias("viewerbackground")
    var viewerBackground: ColorSetColor? = null
    @XStreamAlias("viewerline")
    var viewerLine: ColorSetColor? = null
    @XStreamAlias("viewerhandle")
    var viewerHandle: ColorSetColor? = null
    @XStreamAlias("viewerhighlight")
    var viewerHighlight: ColorSetColor? = null
    @XStreamAlias("viewerspecialhighlight")
    var viewerSpecialHighlight: ColorSetColor? = null
    @XStreamAlias("viewerdisabled")
    var viewerDisabled: ColorSetColor? = null
    @XStreamAlias("viewersurface")
    var viewerSurface: ColorSetColor? = null
    @XStreamAlias("viewertransparent")
    var viewerTransparent: ColorSetColor? = null
    @XStreamAlias("viewerlowvalue")
    var viewerLowValue: ColorSetColor? = null
    @XStreamAlias("viewerhighvalue")
    var viewerHighValue: ColorSetColor? = null

    @XStreamAlias("dockabletitlecolor")
    var dockableTitleColor: ColorSetColor? = null
    @XStreamAlias("dockablebarcolor1")
    var dockableBarColor1: ColorSetColor? = null
    @XStreamAlias("dockablebarcolor2")
    var dockableBarColor2: ColorSetColor? = null

    @XStreamAlias("textcolor")
    var textColor: ColorSetColor? = null

}

data class ColorSetColor(@XStreamAsAttribute @XStreamAlias("R") val red: Int,
                         @XStreamAsAttribute @XStreamAlias("G") val green: Int,
                         @XStreamAsAttribute @XStreamAlias("B") val blue: Int) {
        fun getColor(): Color = java.awt.Color(this.red, this.green, this.blue)
}

@XStreamAlias("button")
class Button {
    @XStreamAsAttribute private val buttonClass: String = ""

    @XStreamImplicit
    val styles: List<ButtonStyle> = ArrayList()
}
@XStreamAlias("style")
data class ButtonStyle(@XStreamAsAttribute @XStreamAlias("owner") val owner: String,
    @XStreamAsAttribute @XStreamAlias("notFound") val notFound: String,
    @XStreamAsAttribute @XStreamAlias("size") val size: String,
    @XStreamAsAttribute @XStreamAlias("raw.icon") val rawIcon: String,
    @XStreamAsAttribute @XStreamAlias("normal.icon") val normalIcon: String,
    @XStreamAsAttribute @XStreamAlias("selected.icon") val selectedIcon: String)

data class Value(@XStreamAsAttribute @XStreamAlias("value") val value: Int)
