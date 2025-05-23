package artofillusion;

import artofillusion.animation.Track;
import artofillusion.image.ImageMap;

import artofillusion.image.filter.ImageFilter;
import artofillusion.object.ObjectInfo;
import lombok.extern.slf4j.Slf4j;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.greenrobot.eventbus.EventBus;

import java.io.*;
import java.lang.reflect.Constructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class SceneIOUtil {

    private static EventBus bus = org.greenrobot.eventbus.EventBus.getDefault();


    public static void loadImageMaps(DataInputStream in, Scene scene) throws IOException {
        var counter = in.readInt();
        log.debug("Read images: {}", counter);

        for (int i = 0; i < counter; i++) {
            String className = in.readUTF();
            try {
                Class<?> cls = ArtOfIllusion.getClass(className);
                if (cls == null) {
                    throw new IOException("Unknown ImageMap class: " + className);
                }
                scene.add((ImageMap) cls.getConstructor(DataInputStream.class).newInstance(in));
            } catch (IOException | ReflectiveOperationException rex) {
                log.atError().setCause(rex).log("Images reading error: {}", rex.getMessage());
                throw new IOException("Error loading image: " + rex.getMessage());
            } catch(SecurityException se) {
                log.atError().setCause(se).log("Images reading error: {}", se.getMessage());
                throw new IOException("Error loading image: " + se.getMessage());
            }
        }

    }

    public static void writeTrack(DataOutputStream out, Track writable, Scene scene, boolean buffered) throws IOException {
        var fc = writable.getClass().getName();
        out.writeUTF(fc);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writable.writeToStream(new DataOutputStream(bos), scene);
        byte[] ba = bos.toByteArray();
        var size = ba.length;
        if(buffered) out.writeInt(size);
        out.write(ba, 0, size);
    }

    /*
    Buffered track read. Introduced in Scene version 6
    */
    public static void loadTracksBuffered(DataInputStream in, Scene scene, ObjectInfo owner) throws IOException {
        var tracks = in.readInt();
        log.debug("Read tracks: {}", tracks);

        var trackClassName = "";
        var trackDataSize = 0;
        byte[] trackData;
        Track track;

        for (int i = 0; i < tracks; i++) {
            // At first read binary data from input. If IOException is thrown we cannot recover data and aborting
            try {
                trackClassName = in.readUTF();
                trackDataSize = in.readInt();
                trackData = new byte[trackDataSize];
                in.read(trackData);
            } catch(IOException ie) {
                throw  ie;
            }
            //Now try to discover Track class. On exception, we cannot recover track, but can bypass it
            try {
                Class<?> trackClass = ArtOfIllusion.getClass(trackClassName);
                if (null == trackClass) {
                    bus.post(new BypassEvent(scene, "Track: " + trackClassName + " was not found"));
                    continue;
                }
                track = (Track)trackClass.getDeclaredConstructor(ObjectInfo.class).newInstance(owner);
            } catch(ReflectiveOperationException cne) {
                bus.post(new BypassEvent(scene, "Track class: " + trackClassName + " was not found or cannot instantiate", cne));
                continue;
            }
            //On exception, we cannot recover track, but can bypass it
            try {
                track.initFromStream(new DataInputStream(new ByteArrayInputStream(trackData)), scene);
            } catch (IOException ie) {
                bus.post(new BypassEvent(scene, "Track: " + trackClassName + " initialization error", ie));
                continue;
            }
            owner.addTrack(track);
        }
    }

    /*

     */
    public static void loadTracksUnbuffered(DataInputStream in, Scene scene, ObjectInfo owner) throws IOException {
        var tracks = in.readInt();
        log.debug("Read tracks: {}", tracks);

        for (int i = 0; i < tracks; i++) {
            String trackClassName = in.readUTF();
            try {
                Class<?> cls = ArtOfIllusion.getClass(trackClassName);
                if (cls == null) {
                    throw new IOException("Unknown Track class: " + trackClassName);
                }
                var tr = (Track<?>) cls.getConstructor(ObjectInfo.class).newInstance(owner);
                tr.initFromStream(in, scene);
                owner.addTrack(tr);
            } catch (IOException | ReflectiveOperationException rex) {
                log.atError().setCause(rex).log("Tracks reading error: {}", rex.getMessage());
                throw new IOException("Error loading Track: " + rex.getMessage());
            } catch(SecurityException se) {
                log.atError().setCause(se).log("Tracks reading error: {}", se.getMessage());
                throw new IOException("Error loading Track: " + se.getMessage());
            }
        }
    }

}
