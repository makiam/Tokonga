package artofillusion.plugin

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamImplicit
import java.util.ArrayList

@XStreamAlias("history")
class History {
    @XStreamImplicit
    private val records: MutableList<LogRecord?>? = ArrayList<LogRecord?>()

    fun getRecords(): MutableList<LogRecord?> {
        return (if (records == null) kotlin.collections.mutableListOf<artofillusion.plugin.LogRecord?>() else records)
    }
}
