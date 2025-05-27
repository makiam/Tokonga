package artofillusion.animation

import artofillusion.Scene
import artofillusion.`object`.ObjectInfo
import org.greenrobot.eventbus.EventBus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.DataInputStream

class TrackIO private constructor() {
    private val bus: EventBus = EventBus.getDefault()
    private val log: Logger = LoggerFactory.getLogger(TrackIO::class.java)

    fun readTracks(input: DataInputStream, scene: Scene, owner: ObjectInfo, version: Int): Unit {
        val tracks = input.readInt()
        log.debug("Scene version {}. Reading {} tracks for {}.", version, tracks, owner.name )

        when (version) {
            6  -> readTracksV6(input, scene, owner, tracks)
            else -> readTracksV5(input, scene, owner, tracks)
        }
        log.debug("Read tracks for {} completed", owner.name)
    }

    fun readTracksV6(input: DataInputStream, scene: Scene, owner: ObjectInfo, tracks: Int): Unit {

    }

    fun readTracksV5(input: DataInputStream, scene: Scene, owner: ObjectInfo, tracks: Int): Unit {

    }

    fun readString(input: DataInputStream): String = input.readUTF()
}
