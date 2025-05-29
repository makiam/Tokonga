package artofillusion.animation

import artofillusion.ArtOfIllusion
import artofillusion.Scene
import artofillusion.`object`.ObjectInfo
import org.greenrobot.eventbus.EventBus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.DataInputStream
import java.io.DataOutput
import java.io.DataOutputStream
import java.io.IOException

class TrackIO private constructor() {
    private val bus: EventBus = EventBus.getDefault()
    private val log: Logger = LoggerFactory.getLogger(TrackIO::class.java)

    @Throws(IOException::class)
    fun readTracks(input: DataInputStream, scene: Scene, owner: ObjectInfo, version: Int) {
        val tracks = input.readInt()
        log.debug("Scene version {}. Reading {} tracks for {}.", version, tracks, owner.name )

        when (version) {
            6  -> readTracksV6(input, scene, owner, tracks)
            else -> readTracksV5(input, scene, owner, tracks)
        }
        log.debug("Read tracks for {} completed", owner.name)
    }

    @Throws(IOException::class)
    fun writeTracks(output: DataOutputStream, scene: Scene, owner: ObjectInfo, version: Int) {

    }

    @Throws(IOException::class)
    fun writeTrack(output: DataOutputStream, scene: Scene, owner: ObjectInfo, track: Track<*>, version: Int) {
        log.debug("Write track: {} of {} for {}. Version {}", track.name, track.javaClass.name, owner.name, version)
            writeClass(output, track)
            track.writeToStream(output, scene)
        log.debug("Write track completed")
    }

    @Throws(IOException::class)
    fun readTracksV6(input: DataInputStream, scene: Scene, owner: ObjectInfo, tracks: Int) {

    }

    @Throws(IOException::class)
    fun readTracksV5(input: DataInputStream, scene: Scene, owner: ObjectInfo, tracks: Int) {
        for(i in 0 until tracks) {
            var className = readString(input)
            try {
                var clazz = ArtOfIllusion.getClass(className)
                if(clazz == null) {
                    throw IOException("Unknown Track class $className")
                }
                clazz.getConstructor(ObjectInfo::class.java).newInstance(owner)
            } catch ( ioe: IOException) {
            } catch ( roe: ReflectiveOperationException) {

            }
        }
    }

    @Throws(IOException::class)
    fun readString(input: DataInputStream): String = input.readUTF()

    @Throws(IOException::class)
    fun writeClass(output: DataOutputStream, item: Any) {
        output.writeUTF(item.javaClass.name)
    }
}
