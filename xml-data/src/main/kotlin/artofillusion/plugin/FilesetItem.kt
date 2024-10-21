package artofillusion.plugin

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute
import com.thoughtworks.xstream.annotations.XStreamConverter
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter

@XStreamAlias("file")
@XStreamConverter(ToAttributedValueConverter::class, strings = ["source"])
data class FilesetItem(@XStreamAlias("todir") @XStreamAsAttribute val target: String, val source: String)
