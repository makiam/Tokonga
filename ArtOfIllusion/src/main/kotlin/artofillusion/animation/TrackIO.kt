package artofillusion.animation

import artofillusion.ArtOfIllusion
import artofillusion.BypassEvent
import artofillusion.Scene
import artofillusion.SceneIO
import artofillusion.`object`.ObjectInfo
import org.greenrobot.eventbus.EventBus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.*
import java.lang.reflect.Constructor

object TrackIO {
    private val bus: EventBus = EventBus.getDefault()
    private val log: Logger = LoggerFactory.getLogger(TrackIO::class.java)

    @Throws(IOException::class)
    fun readTracksV6(input: DataInputStream, scene: Scene, owner: ObjectInfo, tracks: Int) {

        var trackClassName: String
        var dataSize: Int
        var data: ByteArray;
        var track: Track<*>

        for(i in 0 until tracks) {
            // At first read binary data from input. If IOException is thrown we cannot recover data and aborting
            try {
                trackClassName = SceneIO.readString(input)
                dataSize = input.readInt()
                data = ByteArray(dataSize)
                input.readFully(data)
            } catch (ioe: IOException) {
                throw ioe
            }
            //Now try to discover Track class. On exception, we cannot recover track, but can bypass it
            try {
                val trackClass = ArtOfIllusion.getClass(trackClassName)
                if (null == trackClass) {
                    bus.post(BypassEvent(scene, "Track class: $trackClassName was not found"))
                    continue
                }
                val tc: Constructor<*>  = trackClass.getConstructor(ObjectInfo::class.java)
                track = tc.newInstance(owner) as Track<*>

            } catch (_: ReflectiveOperationException) {
                bus.post(BypassEvent(scene, "Track class: $trackClassName was not found"))
                continue
            }
            //On exception, we cannot recover track, but can bypass it
            try {
                track.initFromStream(DataInputStream(ByteArrayInputStream(data)), scene)
            } catch (_: IOException) {
                bus.post(BypassEvent(scene, "Track: $trackClassName  initialization error"))
                continue
            }
            owner.addTrack(track)
        }
    }

    @Throws(IOException::class, Exception::class)
    fun readTracksV5(input: DataInputStream, scene: Scene, owner: ObjectInfo, tracks: Int) {
        for(i in 0 until tracks) {
            val className = SceneIO.readString(input)
            try {
                val clazz = ArtOfIllusion.getClass(className) ?: throw IOException("Unknown Track class $className")
                val tc: Constructor<*>  = clazz.getConstructor(ObjectInfo::class.java)
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

}
