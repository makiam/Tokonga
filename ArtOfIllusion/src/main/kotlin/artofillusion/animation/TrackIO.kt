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
import java.lang.reflect.Constructor

object TrackIO {
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
    fun writeTracks(output: DataOutputStream, scene: Scene, owner: ObjectInfo, version: Short) {
        log.debug("Write {} tracks: {}. Version {}", owner.name, owner.tracks.size, version)
        output.writeInt(owner.tracks.size)
        owner.tracks.forEach { track -> writeTrack(output, scene, track, version) }
        log.debug("Write tracks completed")
    }

    @Throws(IOException::class)
    fun writeTrack(output: DataOutputStream, scene: Scene, track: Track<*>, version: Short) {
        log.debug("Write track: {} of {}. Version {}", track.name, track.javaClass.name, version)
            writeClass(output, track)
            track.writeToStream(output, scene)
        log.debug("Write track completed")
    }

    @Throws(IOException::class)
    fun readTracksV6(input: DataInputStream, scene: Scene, owner: ObjectInfo, tracks: Int) {

    }

    @Throws(IOException::class, Exception::class)
    fun readTracksV5(input: DataInputStream, scene: Scene, owner: ObjectInfo, tracks: Int) {
        for(i in 0 until tracks) {
            var className = readString(input)
            try {
                val clazz = ArtOfIllusion.getClass(className)
                if(clazz == null) {
                    throw IOException("Unknown Track class $className")
                }
                val tc: Constructor<*>  = clazz.getConstructor(ObjectInfo::class.java);
                val track: Track<*> = tc.newInstance(owner) as Track<*>
                track.initFromStream(input, scene)
                owner.addTrack(track)
            } catch ( ex: Exception) {
                when(ex) {
                    is IOException, is ReflectiveOperationException -> {
                        log.atError().setCause(ex).log("Tracks reading error: {}", ex.message)
                        throw IOException(ex)
                    }
                    else -> throw ex
                }
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
