package artofillusion.plugin

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute
import com.thoughtworks.xstream.annotations.XStreamConverter
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter

@XStreamAlias("log")
@XStreamConverter(value = ToAttributedValueConverter::class, strings = ["text"])
data class LogRecord(
    @XStreamAsAttribute
    val version: String? = null,
    @XStreamAsAttribute
    private val date: String? = null,
    @XStreamAsAttribute
    val author: String? = null,
    val text: String? = null
)
